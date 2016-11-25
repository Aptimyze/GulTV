var Groupie = {
    connection: null,
    room: null,
    roomjid: null,
    nickname: null,
    jid: null,
    NS_MUC: "http://jabber.org/protocol/muc",

    joined: null,
    participants: null,
    initialize:function(user_id, room_id) {
    	
    }
	,
	create:function(newroomjid, roomName, success, failed) {
		var iq, stanza;
		//send presence first for creating room
		var d = $pres({'from': Groupie.jid, 'to': newroomjid + '/' + Groupie.nickname})
		Groupie.connection.send(d.tree());
		//Groupie.connection.muc.createInstantRoom(newroomjid, success, failed);
		iq = $iq({
            to: newroomjid,
            type: "set"
        }).c("query", {
            xmlns: Strophe.NS.MUC_OWNER
        });
        iq.c("x", {
            xmlns: "jabber:x:data",
            type: "submit"
        });

        iq.c('field', { 'var': 'FORM_TYPE' }).c('value').t('http://jabber.org/protocol/muc#roomconfig').up().up();
        iq.c('field', { 'var': 'muc#roomconfig_roomname' }).c('value').t(roomName).up().up();
        iq.c('field', { 'var': 'muc#roomconfig_persistentroom' }).c('value').t(1).up().up();
        iq.c('field', { 'var': 'muc#roomconfig_enablelogging' }).c('value').t(0).up().up();
        stanza = iq.tree();
        
        Groupie.connection.sendIQ(iq, success, failed);
	},
    on_presence: function (presence) {
        var from = $(presence).attr('from');
        var room = Strophe.getBareJidFromJid(from);

        // make sure this presence is for the right room
        if (room === Groupie.room) {
            var nick = Strophe.getResourceFromJid(from);
          
            if ($(presence).attr('type') === 'error' &&
                !Groupie.joined) {
                // error joining room; reset app
                Groupie.connection.disconnect();
            } else if (!Groupie.participants[nick] &&
                $(presence).attr('type') !== 'unavailable') {
                // add to participant list
                var user_jid = $(presence).find('item').attr('jid');
                Groupie.participants[nick] = user_jid || true;
                $('#participant-list').append('<li>' + nick + '</li>');

                if (Groupie.joined) {
                    $(document).trigger('user_joined', nick);
                }
            } else if (Groupie.participants[nick] &&
                       $(presence).attr('type') === 'unavailable') {
                // remove from participants list
                $('#participant-list li').each(function () {
                    if (nick === $(this).text()) {
                        $(this).remove();
                        return false;
                    }
                });

                $(document).trigger('user_left', nick);
            }

            if ($(presence).attr('type') !== 'error' && 
                !Groupie.joined) {
                // check for status 110 to see if it's our own presence
                if ($(presence).find("status[code='110']").length > 0) {
                    // check if server changed our nick
                    if ($(presence).find("status[code='210']").length > 0) {
                        Groupie.nickname = Strophe.getResourceFromJid(from);
                    }

                    // room join complete
                    $(document).trigger("room_joined");
                }
            }
        }

        return true;
    },

    on_public_message: function (message) {
        var from = $(message).attr('from');
        var room = Strophe.getBareJidFromJid(from);
        var nick = Strophe.getResourceFromJid(from);

        // make sure message is from the right place
        if (room === Groupie.room) {
            // is message from a user or the room itself?
            var notice = !nick;

            // messages from ourself will be styled differently
            var nick_class = "nick";
            if (nick === Groupie.nickname) {
                nick_class += " self";
            }
            
            var body = $(message).children('body').text();
            var jsonBody = null;
            var content = body;
            try
            {
            	 var jsonBody = JSON.parse(body);
                 var content = jsonBody["content"];
                 nick = jsonBody["from_user"]["nickname"];
            }
            catch(e)
            {
               
            }
           
            
            var delayed = $(message).children("delay").length > 0  ||
                $(message).children("x[xmlns='jabber:x:delay']").length > 0;

            // look for room topic change
            var subject = $(message).children('subject').text();
            if (subject) {
                $('#room-topic').text(subject);
            }

            if (!notice) {
                var delay_css = delayed ? " delayed" : "";

                var action = body.match(/\/me (.*)$/);
                if (!action) {
                    Groupie.add_message(
                        "<div class='message" + delay_css + "'>" +
                            "&lt;<span class='" + nick_class + "'>" +
                            nick + "</span>&gt; <span class='body'>" +
                            content + "</span></div>");
                } else {
                    Groupie.add_message(
                        "<div class='message action " + delay_css + "'>" +
                            "* " + nick + " " + action[1] + "</div>");
                }
            } else {
                Groupie.add_message("<div class='notice'>*** " + content +
                                    "</div>");
            }
        }

        return true;
    },

    add_message: function (msg) {
        // detect if we are scrolled all the way down
        var chat = $('#chat').get(0);
        var at_bottom = chat.scrollTop >= chat.scrollHeight - 
            chat.clientHeight;
        
        $('#chat').append(msg);

        // if we were at the bottom, keep us at the bottom
        if (at_bottom) {
            chat.scrollTop = chat.scrollHeight;
        }
    },

    on_private_message: function (message) {
        var from = $(message).attr('from');
        var room = Strophe.getBareJidFromJid(from);
        var nick = Strophe.getResourceFromJid(from);

        // make sure this message is from the correct room
        if (room === Groupie.room) {
            var body = $(message).children('body').text();
            Groupie.add_message("<div class='message private'>" +
                                "@@ &lt;<span class='nick'>" +
                                nick + "</span>&gt; <span class='body'>" +
                                body + "</span> @@</div>");
            
        }

        return true;
    }
};

$(document).bind('connect', function (ev, data) {
    Groupie.connection = new Strophe.Connection(
        'http://rentin.cafe24.com:7070/http-bind/');

    Groupie.connection.connect(
        data.jid, data.password,
        function (status) {
            if (status === Strophe.Status.CONNECTED) {
            	Groupie.jid = data.jid;
                $(document).trigger('connected');
            } else if (status === Strophe.Status.DISCONNECTED) {
                $(document).trigger('disconnected');
            }
        });
});

$(document).bind('connected', function () {
    Groupie.joined = false;
    Groupie.participants = {};
    
	Groupie.connection.send($pres().c('priority').t('-1'));
    
    Groupie.connection.addHandler(Groupie.on_presence,
                                  null, "presence");
    Groupie.connection.addHandler(Groupie.on_public_message,
                                  null, "message", "groupchat");
    Groupie.connection.addHandler(Groupie.on_private_message,
                                  null, "message", "chat");
    
    Groupie.create(Groupie.room, Groupie.roomjid, 
      function(event){
    	// success
    	alert("채팅방이 생성되었습니다.!");
    },
    function(event) {
    	   /* Groupie.connection.send(
    	        $pres({
    	            to: Groupie.room + "/" + Groupie.nickname
    	        }).c('x', {xmlns: Groupie.NS_MUC}));*/
    	alert("채팅방이 생성이 실패되었습니다.다시 시도해주십오!");
    });
});

$(document).bind('disconnected', function () {
    Groupie.connection = null;
    // $('#room-name').empty();
    $('#room-topic').empty();
    $('#participant-list').empty();
    $('#chat').empty();
    $('#login_dialog').dialog('open');
});

$(document).bind('room_joined', function () {
    Groupie.joined = true;

    $('#leave').removeAttr('disabled');
    // $('#room-name').text(Groupie.room);

    Groupie.add_message("<div class='notice'>*** Room joined.</div>")
});

$(document).bind('user_joined', function (ev, nick) {
    Groupie.add_message("<div class='notice'>*** " + nick +
                         " joined.</div>");
});

$(document).bind('user_left', function (ev, nick) {
    Groupie.add_message("<div class='notice'>*** " + nick +
                        " left.</div>");
});


$(document).ready(function () {
  
    Groupie.room = $('#room').val().toLowerCase();
    Groupie.roomjid = $('#roomjid').val().toLowerCase();
    Groupie.nickname = $('#nickname').val();
    
    $(document).trigger('connect', {
        jid: $('#jid').val().toLowerCase(),
        password: $('#password').val()
    });

    $('#leave').click(function () {
        $('#leave').attr('disabled', 'disabled');
        Groupie.connection.send(
            $pres({to: Groupie.room + "/" + Groupie.nickname,
                   type: "unavailable"}));
        Groupie.connection.disconnect();
    });

    $('#input').keypress(function (ev) {
        if (ev.which === 13) {
            ev.preventDefault();

            var body = $(this).val();
            var message = $(this).val();
            var match = body.match(/^\/(.*?)(?: (.*))?$/);
            var args = null;
            
            var user = {};
            
            var idx = Groupie.jid.indexOf('@');
            var prefix = "wangjatv_talk";
            id = Groupie.jid.substring(prefix.length, idx);
            user["no"] = id;
            user["nickname"] = Groupie.nickname;
            user["user_grade"] = 0;
            
            var msgDictionary = {};
            msgDictionary["type"] = 0;
            msgDictionary["subtype"] = 0;
            msgDictionary["content"] = message;
            msgDictionary["from_user"] = user;
            msgDictionary["is_forced_exit"] = 0;
            msgDictionary["exit_user_no"] = 0;
            msgDictionary["is_bj"] = 1;
            
            body = JSON.stringify(msgDictionary);
            
            if (match) {
                if (match[1] === "msg") {
                    args = match[2].match(/^(.*?) (.*)$/);
                    if (Groupie.participants[args[1]]) {
                        Groupie.connection.send(
                            $msg({
                                to: Groupie.room + "/" + args[1],
                                type: "chat"}).c('body').t(body));
                        Groupie.add_message(
                            "<div class='message private'>" +
                                "@@ &lt;<span class='nick self'>" +
                                Groupie.nickname + 
                                "</span>&gt; <span class='body'>" +
                                args[2] + "</span> @@</div>");
                    } else {
                        Groupie.add_message(
                            "<div class='notice error'>" +
                                "Error: User not in room." +
                                "</div>");
                    }
                } else if (match[1] === "me" || match[1] === "action") {
                    Groupie.connection.send(
                        $msg({
                            to: Groupie.room,
                            type: "groupchat"}).c('body')
                            .t('/me ' + match[2]));
                } else if (match[1] === "topic") {
                    Groupie.connection.send(
                        $msg({to: Groupie.room,
                              type: "groupchat"}).c('subject')
                            .text(match[2]));
                } else if (match[1] === "kick") {
                    Groupie.connection.sendIQ(
                        $iq({to: Groupie.room,
                             type: "set"})
                            .c('query', {xmlns: Groupie.NS_MUC + "#admin"})
                            .c('item', {nick: match[2],
                                        role: "none"}));
                } else if (match[1] === "ban") {
                    Groupie.connection.sendIQ(
                        $iq({to: Groupie.room,
                             type: "set"})
                            .c('query', {xmlns: Groupie.NS_MUC + "#admin"})
                            .c('item', {jid: Groupie.participants[match[2]],
                                        affiliation: "outcast"}));
                } else if (match[1] === "op") {
                    Groupie.connection.sendIQ(
                        $iq({to: Groupie.room,
                             type: "set"})
                            .c('query', {xmlns: Groupie.NS_MUC + "#admin"})
                            .c('item', {jid: Groupie.participants[match[2]],
                                        affiliation: "admin"}));
                } else if (match[1] === "deop") {
                    Groupie.connection.sendIQ(
                        $iq({to: Groupie.room,
                             type: "set"})
                            .c('query', {xmlns: Groupie.NS_MUC + "#admin"})
                            .c('item', {jid: Groupie.participants[match[2]],
                                        affiliation: "none"}));
                } else {
                    Groupie.add_message(
                        "<div class='notice error'>" +
                            "Error: Command not recognized." +
                            "</div>");
                }
            } else {
                Groupie.connection.send(
                    $msg({
                        to: Groupie.room,
                        type: "groupchat"}).c('body').t(body));
            }

            $(this).val('');
        }
    });
});


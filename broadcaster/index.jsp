<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.net.Inet4Address"%>
<%@ page import="com.red5pro.server.secondscreen.net.NetworkUtil"%>
<%@ page import="java.io.*,java.util.regex.*,java.net.URL,java.nio.charset.Charset"%>
<%@ page import="com.red5pro.live.*"%>
<%
  String cookieStr = "";
  String cookieName = "storedIpAddress";
  Pattern addressPattern = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

  String ip = null;
  String localIp = NetworkUtil.getLocalIpAddress();
  boolean ipExists = false;

  // Flip localIp to null if unknown.
  localIp = addressPattern.matcher(localIp).find() ? localIp : null;

  // First check if provided as a query parameter...
  if(request.getParameter("host") != null) {
    ip = request.getParameter("host");
    ip = addressPattern.matcher(ip).find() ? ip : null;
  }

  Cookie cookie;
  Cookie[] cookies = request.getCookies();

  // If we have stored cookies check if already stored ip address by User.
  if(ip == null && cookies != null) {
    for(int i = 0; i < cookies.length; i++) {
      cookie = cookies[i];
      cookieStr += cookie.getName() + "=" + cookie.getValue() + "; ";
      if(cookie.getName().equals(cookieName)) {
        ip = cookie.getValue();
        ip = addressPattern.matcher(ip).find() ? ip : null;
        break;
      }
    }
  }

  // Is a valid IP address from stored IP by User:
  if(ip == null) {

    ip = localIp;

    if(ip != null && addressPattern.matcher(ip).find()) {
      // The IP returned from NetworkUtils is valid...
    }
    else {

      // Invoke AWS service
      URL whatismyip = new URL("http://checkip.amazonaws.com");
      BufferedReader in = null;
      try {
        in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        ip = in.readLine();
      }
      catch(Exception e) {
        ip = null;
      }
      finally {
        if (in != null) {
          try {
            in.close();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      // If failure in AWS service and/or IP still null => flag to show alert.
    }

  }

  ipExists = ip != null && !ip.isEmpty();
%>
<%
  String host = ip;
  String stream_name = request.getParameter("stream_name"); 
  String no = request.getParameter("no");
  String user_no = request.getParameter("user_no");
  String user_name = request.getParameter("user_name");
  user_name = new String(user_name.getBytes("ISO-8859-1"), "UTF-8");
  String broadcast_title = request.getParameter("broadcast_title");
  broadcast_title = new String(broadcast_title.getBytes("ISO-8859-1"), "UTF-8");
%>
<!doctype html>
<html lang="eng">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Welcome to the Red5 Pro Server Pages!">
    <link rel="stylesheet" href="assets/css/main.css">
    <link href="http://fonts.googleapis.com/css?family=Lato:400,700" rel="stylesheet" type="text/css">
    <link rel="shortcut icon" type="image/ico" href="assets/images/favicon.ico"/>
    <link type="text/css" rel="stylesheet" media="screen" href="assets/css/groupie.css" />
    <script src='assets/js/jquery.min.js'></script>
    <script src='assets/js/jquery.ui.core.min.js'></script>
    <script type="text/javascript" src="assets/js/groupie.js"></script>
    <script src='assets/js/strophe.js'></script>
    <script src='assets/js/strophe.muc.js'></script>
    
    <title>방송하기</title>
    <style>
      object:focus {
        outline:none;
      }

      #flashContent {
        border-radius: 5px;
        background-color: #e3e3e3;
        padding: 10px;
      }

      #live-page-subcontent {
        text-align: center;
        position: relative;
        width: 100%;
        height: 230px;
        overflow: hidden;
      }

      #live-container {
        position: absolute;
      }

      #live-image-container {
        width: 540px;
      }

      #live-page-img {
        width: 100%;
      }
    </style>
    <script type="text/javascript" src="swf/swfobject.js"></script>
    <script type="text/javascript">
      // For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection.
      var swfVersionStr = "11.1.0";
      // To use express install, set to playerProductInstall.swf, otherwise the empty string.
      var xiSwfUrlStr = "swf/playerProductInstall.swf";
      var flashTitle = encodeURI('<%= broadcast_title %>');
      var flashvars = {
        host: "<%= host %>", 
        streamName: "<%= stream_name %>",
        broadcastNo: "<%= no %>",
        title: flashTitle
      };
      var params = {};
      params.quality = "high";
      params.bgcolor = "#ffffff";
      params.allowscriptaccess = "always";
      params.allowfullscreen = "true";
      var attributes = {};
      attributes.id = "Broadcaster";
      attributes.name = "Broadcaster";
      attributes.align = "middle";
      if(swfobject.hasFlashPlayerVersion("11.1.0")) {
        swfobject.embedSWF(
            "Broadcaster.swf", "flashContent",
            "100%", "782",
            swfVersionStr, xiSwfUrlStr,
            flashvars, params, attributes);
        // JavaScript enabled so display the flashContent div in case it is not replaced with a swf object.
        swfobject.createCSS("#flashContent", "display:block; text-align:left; padding: 0; background-color: #ffffff");
      }
      else {
        // nada.

      }
  </script>
  </head>
  <body onunload="close(<%= no %>)">
    <div class="container main-container clear-fix">
      <div id="content-section">
        <div class="content-section-story">
        <div id="flashContent">
            <p>
                To view this page ensure that Adobe Flash Player version 11.1.0 or greater is installed.
            </p>
            <script type="text/javascript">
                var pageHost = ((document.location.protocol == "https:") ? "https://" : "http://");
                document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='"
                                + pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" );
            </script>
        </div>
        <div id="flashContent1" style="width: 50%;">
	        <noscript>
	            <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="100%" id="Broadcaster">
	                <param name="movie" value="Broadcaster.swf?quality=high" />
	                <param name="bgcolor" value="#ffffff" />
	                <param name="allowScriptAccess" value="sameDomain" />
	                <param name="allowFullScreen" value="true" />
	                <!--[if !IE]>-->
	                <object type="application/x-shockwave-flash" data="Broadcaster.swf?quality=high" width="100%" height="100%">
	                    <param name="bgcolor" value="#ffffff" />
	                    <param name="quality" value="high" />
	                    <param name="allowScriptAccess" value="sameDomain" />
	                    <param name="allowFullScreen" value="true" />
	                <!--<![endif]-->
	                <!--[if gte IE 6]>-->
	                    <p>
	                        Either scripts and active content are not permitted to run or Adobe Flash Player version
	                        11.1.0 or greater is not installed.
	                    </p>
	                <!--<![endif]-->
	                    <a href="http://www.adobe.com/go/getflashplayer">
	                        <img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash Player" />
	                    </a>
	                <!--[if !IE]>-->
	                </object>
	                <!--<![endif]-->
	            </object>
	          </noscript>
	          <br><br>
          </div>
        </div>
        
       <div id="chat-section">
		   <div id='toolbar' style="display: none;">
		      회원수<input id='leave' type='text' value='0' disabled='disabled' >명
		    </div>
		
		    <div>
		      <div id='chat-area'>
		        <div>
		          <div id='room-name'></div>
		          <div id='room-topic'></div>
		        </div>
		        <div id='chat'>
		        </div>
		      </div>
		    
		      <div id='participants'>
		      	 <div id='participants-name'>참여자목록:</div>
		        <ul id='participant-list'>
		        </ul>
		      </div>
		      
		      <div id="input-container">
		        <textarea id='input'></textarea>
		        <button onclick="onSend()" id="btn-send">보내기</button>
		      </div>
		    </div>
	      </div>
      </div>
    </div>
    <script>
      (function(window, document) {

       function accessSWF() {
          return document.getElementById("Broadcaster");
        }

        function handleBroadcastIpChange(value) {
          accessSWF().resetHost(value);
        }
        
       }(this, document));
       
      function onSend() {
    	  var e = jQuery.Event("keypress");
    	  e.which = 13; //choose the one you want
    	  e.keyCode = 13;
    	  $("#input").trigger(e);
      }
    </script>
    <div class="footer">
      <div>
        <a id="footer-email" href="https://red5pro.zendesk.com?origin=webapps" target="_blank">
          <span id="footer-email-lead" class="black-text">We're here to help!</span>      <span class="red-text">Get in touch</span>
        </a>
      </div>
  
    </div>
    
     <!-- login dialog -->
    <div id='login_dialog' class='hidden'>
      <label>JID:</label><input type='text' id='jid'>
      <label>Password:</label><input type='password' id='password'>
      <label>Chat Room:</label><input type='text' id='room'>
      <label>Chat Room:</label><input type='text' id='roomjid'>
      <label>Nickname:</label><input type='text' id='nickname'>
    </div>
   </body>
 <script>
	 var broadcast_no = <%= no %>;
	 var user_no = <%= user_no %>;
	 var user_name = '<%= user_name %>';
	 var room_name = '<%= broadcast_title %>';
	 room_name = '채팅';
	 var xmppJid = "wangjatv_talk"+user_no;
	 var xmppRoomJid = "wangjatv_talk_room"+broadcast_no;
	 var serverName = "rentin.cafe24.com";
	 var conferenceServiceName = "conference";
	 $('#roomjid').val(xmppRoomJid);
	 $('#room').val(xmppRoomJid+'@'+conferenceServiceName+'.'+serverName);
	 $('#jid').val(xmppJid+'@'+serverName);
	 $('#nickname').val(user_name);
	 $('#password').val(xmppJid);
	 $('#room-name').text(room_name);
	 
	 function close(no) {
		 alert("방송중지 버튼을 누르셨는지 확인해주십시오.");
	 }
</script>
</html>
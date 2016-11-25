(function ($) {
    // 영문과 숫자만
    $.fn.alphanumeric = function (p) {

        p = $.extend({
            ichars: "!@#$%^&*()+=[]\\\';,/{}|\":<>?~`.- ",
            nchars: "",
            allow: ""
        }, p);

        return this.each
				(
					function () {

					    if (p.nocaps) p.nchars += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
					    if (p.allcaps) p.nchars += "abcdefghijklmnopqrstuvwxyz";

					    s = p.allow.split('');
					    for (var i = 0; i < s.length; i++) if (p.ichars.indexOf(s[i]) != -1) s[i] = "\\" + s[i];
					    p.allow = s.join('|');

					    var reg = new RegExp(p.allow, 'gi');
					    var ch = p.ichars + p.nchars;
					    ch = ch.replace(reg, '');

					    $(this).keypress
							(
								function (e) {

								    if (!e.charCode) k = String.fromCharCode(e.which);
								    else k = String.fromCharCode(e.charCode);

								    if (ch.indexOf(k) != -1) e.preventDefault();
								    if (e.ctrlKey && k == 'v') e.preventDefault();

								}

							);

					    $(this).bind('contextmenu', function () { return false; });

					}
				);

    };

    // 점수입력 시 사용 숫자, 소수점만 허용 //
    $.fn.inputScore = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        p = $.extend({ nchars: nm, allow: "." }, p);
        this.attr('maxlength', '6');

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 시간입력 시 사용 숫자, ':' 만 허용 //
    $.fn.inputTime = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        p = $.extend({ nchars: nm, allow: ":" }, p);
        this.attr('maxlength', '5');

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 숫자만 허용 //
    $.fn.inputNumeric = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        p = $.extend({ nchars: nm, allow: "" }, p);

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 이름입력상자에 사용하면 좋음 : 국문, 영문, 숫자만, 대괄호, 중괄호, 언더바(_)만 허용 : 공백 사용불가, Maxlength:30 //
    $.fn.inputName = function (p) {
        var nm = "!@#$%^&*+=\\\';,/|\":<>?~`.-";
        p = $.extend({ nchars: nm, allow: "()[]{}_" }, p);
        this.attr('maxlength', '30');

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 영문이름입력상자에 사용하면 좋음 : 영문, 숫자, 공백 사용가능 //
    $.fn.inputEngName = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "!@#$%^&*+=\\\';,/|\":<>?~`.-";
        p = $.extend({ nchars: nm, allow: " " }, p);

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 아이디입력상자에 사용하면 좋음 : 영문, 숫자만 허용 : 특수문자, 공백 사용불가 //
    $.fn.inputUserId = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "_";
        p = $.extend({ nchars: nm }, p);

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 비밀번호입력상자에 사용하면 좋음 : 영문, 숫자, 특수문자 허용 : 공백 사용불가 //
    $.fn.inputPassword = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "";
        p = $.extend({ nchars: nm, allow: "!@#$%^&*()+=[]\\\';,/{}|\":<>?~`.-" }, p);
        this.attr('maxlength', '20');

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 메일입력 단어 제어 //
    $.fn.inputEmail = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "";
        p = $.extend({ nchars: nm, allow: "@." }, p);

        this.bind("blur", function () {
            if ($(this).val() != "") {
                if (!$(this).val().match(/[\w\-\~]+\@[\w\-\~]+(\.[\w\-\~]+)+/g)) {
                    alert('메일 주소 형식이 잘못 되었습니다.');
                    $(this).focus();
                }
            }
        });

        return this.each(function () { $(this).alphanumeric(p); });
    };

    // 파일선택 상자에 등록된 파일의 확장자명 반환 : '.'를 제외환 확장자만 반환 //
    $.fn.getFileExtension = function () {
        var vals = $(this).val().split("\\");
        var fileName = vals[vals.length - 1];
        var extensionName = fileName.split('.')[1];

        return extensionName.toLowerCase();
    };

    //숫자만
    $.fn.numeric = function (p) {
        this.css("ime-mode", "disabled");
        var az = "abcdefghijklmnopqrstuvwxyz";
        az += az.toUpperCase();

        p = $.extend({
            nchars: az
        }, p);

        return this.each(function () {
            $(this).alphanumeric(p);
        });

    };

    //영문만
    $.fn.alpha = function (p) {
        this.css("ime-mode", "disabled");
        var nm = "1234567890";

        p = $.extend({
            nchars: nm
        }, p);

        return this.each(function () {
            $(this).alphanumeric(p);
        });
    };

    /*--------------------------------------------------------------------
    ' 이름   : inputCheck(txt) : 전체 alert 메세지를 받아서 내용을 반환한다.
    ' 설명   : 필수항목체크 
    ' 사용예 : return $("#txtAliasName").inputCheck('학원별칭을 입력하여 주십시오.');
    --------------------------------------------------------------------*/
    $.fn.inputCheck = function (txt) {
        if (jQuery.trim(this.val()) == '') {
            alert(txt);
            this.focus();

            return false;
        }

        return true;
    };

    /*--------------------------------------------------------------------
    ' 이름   : inputLengthCheck(message, length) : 입력길이를 체크하여 요청한 길이보다 작을 경우 message를 출력한다.
    ' 설명   : 필수길이 항목 체크 
    ' 사용예 : return $("#txtAliasName").inputLengthCheck('학원별칭을', 1);
    --------------------------------------------------------------------*/
    $.fn.inputLengthCheck = function (message, length) {
        if (jQuery.trim(this.val()).length < parseInt(length)) {
            if (message.length > 0) {
                alert(message + ' ' + length + '자 이상 입력하여 주십시오.');
            }
            this.focus();

            return false;
        }
        else {
            return true;
        }
    };

    /*--------------------------------------------------------------------
    ' 이름   : inputMinMaxLengthCheck(message, minLength, maxLength) : 입력길이를 체크하여 요청한 길이보다 작거나 클 경우 message를 출력한다.
    ' 설명   : 필수길이 항목 체크 
    ' 사용예 : return $("#txtAliasName").inputMinMaxLengthCheck('학원별칭을', 6,12);
    --------------------------------------------------------------------*/
    $.fn.inputMinMaxLengthCheck = function (message, minLength, maxLength) {
        if (parseInt(minLength) > jQuery.trim(this.val()).length || parseInt(maxLength) < jQuery.trim(this.val()).length) {
            if (message.length > 0) {
                alert(message + ' ' + minLength + '자 이상 ' + maxLength + '자 이하로 입력해 주세요.');
            }
            this.focus();

            return false;
        }
        else {
            return true;
        }
    };

    /*--------------------------------------------------------------------
    ' 이름   : inputEmailCheck(isReq) : 전체 alert 메세지를 받아서 내용을 반환한다.
    ' 설명   : 메일주소 입력 체크 
    ' 사용예 : return $("#txtAliasName").inputEmailCheck();
    --------------------------------------------------------------------*/
    $.fn.inputEmailCheck = function (isReq) {
        if (isReq) {
            if ($(this).val().match(/[\w\-\~]+\@[\w\-\~]+(\.[\w\-\~]+)+/g)) {
                return true;
            }
            else {
                alert('메일주소 형식에 맞게 정확히 입력하여 주십시오.');
                this.focus();
            }
        }
        else {
            if (jQuery.trim(this.val()) == '') {
                return true;
            }
            else {
                if ($(this).val().match(/[\w\-\~]+\@[\w\-\~]+(\.[\w\-\~]+)+/g)) {
                    return true;
                }
                else {
                    alert('메일 주소 형식이 잘못 되었습니다.');
                    this.focus();
                    return false;
                }
            }
        }
    };

    /*--------------------------------------------------------------------
    ' 이름   : toDay()
    ' 설명   : text에 오늘날짜를 value값에 리턴한다.
    ' 사용예 : $("#txtMembNm").toDay();
    --------------------------------------------------------------------*/
    $.fn.toDay = function () {
        var rtn;
        var date = new Date();
        var month;
        var day;
        if ((date.getMonth() + 1) < 10) {
            month = "0" + (date.getMonth() + 1);
        }
        else {
            month = date.getMonth() + 1;
        }
        if (date.getDate() < 10) {
            day = "0" + date.getDate();
        }
        else {
            day = date.getDate();
        }
        rtn = $(this).val(date.getFullYear() + "-" + month + "-" + day);
        return rtn;
    };

    /*--------------------------------------------------------------------
    ' 이름   : moneyCipher 
    ' 설명   : 금액 자릿수 표시
    ' 사용예 : $(this).moneyCipher(3);
    --------------------------------------------------------------------*/
    $.fn.moneyCipher = function () {
        $(this).attr('maxlength', '9');
        $(this).bind("keyup", function () {
            $(this).toCipher(3);
            if ($(this).val() == "")
                $(this).val(0);
        });
    };

    /*--------------------------------------------------------------------
    ' 이름   : toCipher 
    ' 설명   : 숫자 자릿수 표시
    ' 사용예 : $(this).toPrice(3);
    --------------------------------------------------------------------*/
    $.fn.toCipher = function (cipher) {
        var strb, len, revslice;

        strb = $(this).val().toString();
        strb = strb.replace(/,/g, '');
        strb = parseInt(strb, 10);
        if (isNaN(strb))
            return $(this).val('');

        strb = strb.toString();
        len = strb.length;

        if (len < 4)
            return $(this).val(strb);

        count = len / cipher;
        slice = new Array();

        for (var i = 0; i < count; ++i) {
            if (i * cipher >= len)
                break;
            slice[i] = strb.slice((i + 1) * -cipher, len - (i * cipher));
        }

        revslice = slice.reverse();
        return $(this).val(revslice.join(','));
    };

    /*--------------------------------------------------------------------
    ' 이름   : datePicker();
    ' 설명   : 달력 레이어
    ' 사용예 : $('#txtStartDate').datePicker();
    --------------------------------------------------------------------*/
    $.fn.datePicker = function () {
        $(this).datepicker().datepickerCheck();
    };

    /*--------------------------------------------------------------------
    ' 이름   : datepickerCheck 
    ' 설명   : 달력의 입력 유효성 검사
    --------------------------------------------------------------------*/
    $.fn.datepickerCheck = function () {
        $(this).attr('maxlength', '10');
        var oldDate = $(this).val();
        $(this).blur(function () {
            if ($(this).val() != '' && !fnCheckDate($(this).val())) {
                alert("날짜 형식과 일치하지 않습니다.");
                $(this).val(oldDate);
                this.focus();
                return false;
            }
        });
    };

    /*--------------------------------------------------------------------
    ' 이름   : fncSmsLengthCount 
    ' 설명   : 입력문자열 길이 제한
    --------------------------------------------------------------------*/
    $.fn.inputStringLimit = function (maxlength, msg, disp) {
        $(this).bind("keyup", function () {
            var message = $(this).val();
            var resultMessageLength = 0;
            var nowLenght = 0;
            var loofCnt = 0;

            for (var i = 0; i < message.length; i++) {
                if (escape(message.charAt(i)).length > 4) {
                    nowLenght = 2;
                    resultMessageLength += 2;
                    loofCnt++;
                }
                else {
                    nowLenght = 1;
                    resultMessageLength++;
                    loofCnt++;
                }

                $(disp).text(resultMessageLength);
                if (maxlength < resultMessageLength) {
                    alert(msg);
                    $(this).val(message.substring(0, loofCnt - 1));
                    $(disp).text(resultMessageLength - nowLenght);
                    return false;
                }
            }
        });
    };

    /*--------------------------------------------------------------------
    ' 이름   : checkboxAllCheck()
    ' 설명   : 목록의 체크 박스를 전체선택 또는 전체선택 해제 한다.
    ' 사용예 : $("#all_checkbox_id").checkboxAllCheck('listCheckboxId');
    --------------------------------------------------------------------*/
    $.fn.checkboxAllCheck = function (listCheckboxId) {
        $(this).click(function () {
            var isCheck = $(this).is(":checked");
            $("input[id=" + listCheckboxId + "]").each(function () {
                if (!$(this).is(":disabled")) {
                    $(this).attr("checked", isCheck);
                }
            });
        });
    };

    /*--------------------------------------------------------------------
    ' 이름   : inputEnterSubmit()
    ' 설명   : 엔터 입력시 form을 submit한다.
    ' 사용예 : $("#form").formSubmit("url", "method");
    --------------------------------------------------------------------*/
    $.fn.inputEnterSubmit = function (form) {
        $(this).bind("keydown", function (evt) {
            if ((evt.keyCode) && (evt.keyCode == 13)) {
                $(form).submit();
            }
        });
    };

    /*--------------------------------------------------------------------
    ' 이름   : fnCheckDate()
    ' 설명   : 날짜형식에 맞는지 체크한다 true, false 반환
    --------------------------------------------------------------------*/
    fnCheckDate = function (Val) {
        var rtn = Val.search(/[0-9][0-9][0-9][0-9]-([0][0-9]|[1][0-2])-([0-2][0-9]|[3][0-1])/);
        if (rtn == 0) {
            var objDate = new Date(parseFloat(Val.substring(0, 4)), parseFloat(Val.substring(5, 7)) - 1, parseFloat(Val.substring(8, 10)));
            if (objDate.getDate() != parseFloat(Val.substring(8, 10))) {
                rtn = -1;
            }
        }

        return (rtn >= 0);
    };

    /*--------------------------------------------------------------------
    ' 이름   : fncCheckSEDate(objId1, objId2)
    ' 설명   : 날짜형식과 시작일과 종료일이 유효한지 체크 후 true, false 반환
    ' 사용예 : fncCheckSEDate('sdate_id', 'edate_id')
    --------------------------------------------------------------------*/
    fncCheckSEDate = function (objId1, objId2) {
        var obj1Value = $("#" + objId1).val();
        var obj2Value = $("#" + objId2).val();

        return fnCheckDate(obj1Value) && fnCheckDate(obj2Value) && obj1Value <= obj2Value;
    };

    /*--------------------------------------------------------------------
    ' 이름   : fnGetDate
    ' 설명   : 날짜 더하기, 빼기
    --------------------------------------------------------------------*/
    fnGetDate = function (iDiff, sGubun, sDate) {
        var setDat;
        if (!sDate) {
            setDat = new Date();
        } else {
            setDat = new Date(sDate.substr(0, 4), parseFloat(sDate.substr(5, 2)) - 1, sDate.substr(8, 2));
        }
        var strDate = "";
        switch (sGubun) {
            case 'd':
                setDat.setDate(setDat.getDate() + parseInt(iDiff));

                strDate = setDat.getFullYear() + "-" + fnSetFormatLen(setDat.getMonth() + 1) + "-" + fnSetFormatLen(setDat.getDate());
                break;
            case 'w':
                setDat.setDate(parseInt(setDat.getDate()) + (parseInt(iDiff) * 7));

                if (parseInt(iDiff) > 0)
                    setDat.setDate(setDat.getDate() - 1);
                else
                    setDat.setDate(setDat.getDate() + 1);

                strDate = setDat.getFullYear() + "-" + fnSetFormatLen(setDat.getMonth() + 1) + "-" + fnSetFormatLen(setDat.getDate());
                break;
            case 'm':
                setDat.setMonth(parseInt(setDat.getMonth()) + parseInt(iDiff));

                if (parseInt(iDiff) > 0)
                    setDat.setDate(setDat.getDate() - 1);
                else
                    setDat.setDate(setDat.getDate() + 1);

                strDate = setDat.getFullYear() + "-" + fnSetFormatLen(setDat.getMonth() + 1) + "-" + fnSetFormatLen(setDat.getDate());
                break;
        }

        return strDate;
    };

    /*--------------------------------------------------------------------
    ' 이름   : fnSetSearchTerm()
    ' 설명   : 검색기간 설정
    ' 사용예 :  fnSetDate()
    --------------------------------------------------------------------*/
    fnSetSearchTerm = function (sDiffType, sDiff, eDiffType, eDiff, sId1, sId2) {
        $('#' + sId1).val(fnGetDate(sDiff, sDiffType));
        $('#' + sId2).val(fnGetDate(eDiff, eDiffType));
    }

    function fnSetFormatLen(str) {
        return str = ("" + str).length < 2 ? "0" + str : str;
    }

    /*--------------------------------------------------------------------
    ' 이름   : fncOnlyNumber
    ' 설명   : 숫자값만 반환
    ' 사용예 :  fncOnlyNumber("111,000")
    --------------------------------------------------------------------*/
    fncOnlyNumber = function (val) {
        var s, c;
        var ret = "";

        if (val.length == 0) return 0;

        s = val.toUpperCase();
        for (var n = 0; n < s.length; n++) {
            c = s.charAt(n);

            if ((c >= '0' && c <= '9'))
                ret += c;

        }

        return parseInt(ret);
    };
})(jQuery);

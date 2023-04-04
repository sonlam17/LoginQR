<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        ${msg("loginTitleHtml",realm.name)}
    <#elseif section = "form">
    	<link rel="stylesheet" href="${url.resourcesPath}/SecSignIDUi.css" />
    	<script src="https://code.jquery.com/jquery-3.6.0.js"  integrity="sha256-H+K7U5CnXl1h5ywQfKtSj8PCmoN9aaq30gDh27Xc0jk="  crossorigin="anonymous"></script>
	    
	    <div id="secsignid-secUi" style="margin:40px auto;width:100%;">
            <div id="secUi-main__container" class="secUi-beforeAnnim">
			        <div id="secUi-pageQr" class="secUi-page">
	                    <p class="secUi-main__text">Open the SecSignID app and click/tap on <strong>+</strong>, then on <strong>start QR-Code pairing</strong> and scan the following code</p>
	                    <img class="secUi-pageQr__code" src="data:image/png;base64,${createQRCode}">
	                    <p class="secUi-main__text">This screen will proceed automatically after creating your SecSignID: <strong class="secUi-main__displayid">${secsignid}</strong></p>
	                    <button class="secUi-main__button secUi-custbutton" id="secUi-pageQr__desktopbtn">No camera / desktop apps</button>
	                    <button class="secUi-main__button secUi-custbutton" id="secUi-pageQr__cancelbtn">Cancel</button>
	                </div>
			 	</div>
		 </div>  
		 
		 <form id="checkCreationForm"  action="${url.loginAction}" method="post">
		 	<input id="secsign_createAction_check" name="secsign_createAction" type="hidden" value="checkCreation"> </input>
		 	<input id="secsign_secsignid" name="secsign_secsignid" type="hidden" value="${secsignid}"> </input>
		 	<input id="secsign_createQRCode" name="secsign_createQRCode" type="hidden" value="${createQRCode}"> </input>
		 	<input id="secsign_createURL" name="secsign_createURL" type="hidden" value="${createURL}"> </input>
		 </form>
		 
		 <form id="cancelCreationForm"  action="${url.loginAction}" method="post">
		 	<input id="secsign_createAction_cancel" name="secsign_createAction" type="hidden" value="cancelCreation"> </input>
		 	
		 </form>
		 <script>
		 

		 	jQuery(document).ready(function ($) {
		 		$("#secUi-pageQr__desktopbtn").click(function(e) {
		 			window.open(decodeURIComponent("${createURL}"),"_blank").focus();
		 		});
		 		
		 		$("#secUi-pageQr__cancelbtn").click(function(e) {
		 			$("#cancelCreationForm").submit();
		 		});
		 		
		 		var checkCreateInterval = 3000;
		 		var checkCreateFunc = function () {

		 			$("#checkCreationForm").submit();
		 		}
		 		checkCreateTimerId = window.setTimeout(checkCreateFunc, checkCreateInterval);
		 		
		 	});
		 </script>
    </#if>
    <head>
    	<meta http-equiv="Cache-control" content="no-cache">
    </head>
</@layout.registrationLayout>



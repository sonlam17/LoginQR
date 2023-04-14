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
			        <div id="secUi-pageAccesspass" class="secUi-page">
				        <div id="secUi-pageAccesspass__accesspassicon">
				            <div class="secUi-pageAccesspass__apcontainer">
				                <img class="secUi-pageAccesspass__accesspass" id="secUi-pageAccesspass__accesspass" src="data:image/png;base64,${accessPassIconData}">
				            </div>
				            <p class="secUi-main__textsmall">Please use Scan Qr Code<br>in your SCAMobile app</p>
				        </div>
				
				        <button class="secUi-main__button secUi-custbutton" id="secUi-pageAccesspass__cancelbtn">Cancel</button>
				    </div>
			 	</div>
		 </div>   
		 <form id="checkAuthForm"  action="${url.loginAction}" method="post">
		 	<input id="secsign_accessPassAction" name="accessPassAction" type="hidden" value="checkAuth"> </input>
		 	<input id="secsign_accessPassIconData" name="accessPassIconData" type="hidden" value="${accessPassIconData}"> </input>
		 </form>
		 
		 <form id="cancelAuthForm"  action="${url.loginAction}" method="post">
		 	<input id="secsign_accessPassAction" name="accessPassAction" type="hidden" value="cancelAuth"> </input>
		 	<input id="secsign_accessPassIconData" name="accessPassIconData" type="hidden" value="${accessPassIconData}"> </input>
		 </form>
		 <script>
		 

		 	jQuery(document).ready(function ($) {
		 		
		 		$("#secUi-pageAccesspass__cancelbtn").click(function(e) {
		 			$("#cancelAuthForm").submit();
		 		});
		 		
				var checkAuthSessionStateInterval = 3000;
		 		var checkAuthSessionStateFunc = function () {

		 			$("#checkAuthForm").submit();
		 		}
				checkSessionTimerId = window.setTimeout(checkAuthSessionStateFunc, checkAuthSessionStateInterval);
		 	});
		 </script>
    </#if>
    <head>
    	<meta http-equiv="Cache-control" content="no-cache">
    </head>
</@layout.registrationLayout>
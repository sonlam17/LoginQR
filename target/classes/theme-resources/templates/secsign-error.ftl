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
			        <div id="secUi-pageError" class="secUi-page">
                        <p id="secUi-pageError__errorMsg">${errorMsg}</p>
                        <button class="secUi-main__button secUi-custbutton" id=tryagain>Try again</button>
                    </div>
			 	</div>
		 </div>   
		 
		 <script>
		 

		 	jQuery(document).ready(function ($) {
		 		
		 		
		 		if($("#reset-login").length)
	 			{
		 			$("#tryagain").click(function(e) {
		 				window.location.replace($("#reset-login").attr("href"));
			 		});
	 			}else{
	 				$("#tryagain").click(function(e) {
		 				window.location.href=window.location.href;
			 		});
	 			}
		 		
		 			
	 			
		 		
		 		
		 	});
		 </script>
    </#if>
    <head>
    	<meta http-equiv="Cache-control" content="no-cache">
    </head>
</@layout.registrationLayout>
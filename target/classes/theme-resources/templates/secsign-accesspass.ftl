<#import "template.ftl" as layout>
<#import "components/provider.ftl" as provider>
<#import "components/button/primary.ftl" as buttonPrimary>
<#import "components/checkbox/primary.ftl" as checkboxPrimary>
<#import "components/input/primary.ftl" as inputPrimary>
<#import "components/label/username.ftl" as labelUsername>
<#import "components/link/primary.ftl" as linkPrimary>
<@layout.registrationLayout
displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??
displayMessage=!messagesPerField.existsError("username", "password" )
; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        ${msg("loginTitleHtml",realm.name)}
    <#elseif section = "form">
		<div class="view-mode float-left">
			<a href="javascript:void(0)" id="grid-view" onclick="gridView()" data-toggle="tooltip" title="Lưới">
				<label class="text-sm-left">
					<b class="btn button-view-mode active" id = "view-mode-grid">
						<span>Password</span>
					</b>
				</label>
			</a>
			<a href="javascript:void(0)" id="list-view" onclick="listView()" data-toggle="tooltip" title="Danh sách">
				<label class="text-sm-left">
					<b class="btn button-view-mode" id = "view-mode-list">
						<span>Scan Qr-Code</span>
					</b>
				</label>
			</a>
		</div>
		<div class="row list-products" id = "product-grid">
			<#if realm.password>
				<form
						action="${url.loginAction}"
						class="m-0 space-y-4"
						method="post"
						onsubmit="login.disabled = true; return true;">
					<input
							name="credentialId"
							type="hidden"
							value="<#if auth.selectedCredential?has_content>
                                ${auth.selectedCredential}
                                </#if>">
					<div>
						<@inputPrimary.kw
						autocomplete=realm.loginWithEmailAllowed?string("email", "username" )
						autofocus=true
						disabled=usernameEditDisabled??
						invalid=["username", "password" ]
						name="username"
						type="text"
						value=(login.username)!''>
							<@labelUsername.kw />
						</@inputPrimary.kw>
					</div>
					<div>
						<@inputPrimary.kw
						invalid=["username", "password" ]
						message=false
						name="password"
						type="password">
							${msg("password")}
						</@inputPrimary.kw>
					</div>
					<div class="flex items-center justify-between">
						<#if realm.rememberMe && !usernameEditDisabled??>
							<div class="flex items-center">
								<input
										class="border-secondary-200 h-4 rounded text-primary-600 w-4 focus:ring-primary-200 focus:ring-opacity-50"
										id="rememberMe"
										name="rememberMe"
										type="checkbox"
								>
								<label class="block ml-2 text-secondary-900 text-sm" for="rememberMe">
									${msg("rememberMe")}
								</label>
							</div>
						</#if>
						<#if realm.resetPasswordAllowed>
							<@linkPrimary.kw href=url.loginResetCredentialsUrl>
								<span class="text-sm">
                                    ${msg("doForgotPassword")}
                                  </span>
							</@linkPrimary.kw>
						</#if>
					</div>
					<div class="pt-4">
						<@buttonPrimary.kw name="login" type="submit">
							${msg("doLogIn")}
						</@buttonPrimary.kw>
					</div>
				</form>
			</#if>
			<#if realm.password && social.providers??>
				<@provider.kw />
			</#if>
		</div>
		<div class="row list-products " id = "product-list" style="display: none;">

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
		</div>
		<script type="text/javascript" >
			function gridView(){
				var viewModeGrid = document.getElementById("view-mode-grid");
				viewModeGrid.classList.add("active")
				var viewModeList = document.getElementById("view-mode-list");
				viewModeList.classList.remove("active");


				var productGrid = document.getElementById("product-grid").removeAttribute("style");
				var productList = document.getElementById("product-list").setAttribute("style", "display: none;");
			}

			function listView(){
				var viewModeList = document.getElementById("view-mode-list");
				viewModeList.classList.add("active")
				var viewModeGrid = document.getElementById("view-mode-grid");
				viewModeGrid.classList.remove("active");


				var productList = document.getElementById("product-list").removeAttribute("style");
				var productGrid = document.getElementById("product-grid").setAttribute("style", "display: none;");
			}
		</script>
    </#if>
    <head>
    	<meta http-equiv="Cache-control" content="no-cache">
    </head>
</@layout.registrationLayout>
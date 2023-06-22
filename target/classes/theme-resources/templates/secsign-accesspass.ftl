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
		<div class="card" xmlns="http://www.w3.org/1999/html">
			<div class="card-body">
				<!-- Logo -->
				<div class="app-brand justify-content-center mb-4 mt-2">
					<a href="http://cmcati.vn/" class="app-brand-link gap-2">
                  <span class="app-brand-logo demo">
                  <img src="${url.resourcesPath}/favicon.ico" alt="Logo" class="app-brand-logo demo">
                  </span>
						<span class="app-brand-text demo text-body fw-bold ms-1">CMC ATI</span>
					</a>
				</div>
				<!-- /Logo -->
				<div class="">
					<div class="nav-align-top mb-4">
						<ul class="nav nav-pills mb-3 nav-fill" role="tablist">
							<li class="nav-item disabled-li" id="password-btn" onclick="passwordClick()">
								<style>
									.disabled-li {
										pointer-events: none;
										cursor: default;
									}
								</style>
								<button
										type="button"
										class="nav-link active"
										role="tab"
										data-bs-toggle="tab"
										data-bs-target="#navs-pills-justified-home"
										aria-controls="navs-pills-justified-home"
										aria-selected="true"
								>
									<i class="tf-icons ti ti-home ti-xs me-1"></i> Password
								</button>
							</li>
							<li class="nav-item" id="qr-btn" onclick="qrClick()">
								<style>
									.disabled-li {
										pointer-events: none;
										cursor: default;
									}
								</style>
								<button
										type="button"
										class="nav-link"
										role="tab"
										data-bs-toggle="tab"
										data-bs-target="#navs-pills-justified-profile"
										aria-controls="navs-pills-justified-profile"
										aria-selected="false"
								>
									<i class="tf-icons ti ti-user ti-xs me-1"></i> QrCode
								</button>
							</li>
						</ul>
						<div class="tab-content">
							<div class="tab-pane fade show active" id="navs-pills-justified-home" role="tabpanel">
									<#if section="header">
										${msg("loginAccountTitle")}
									<#elseif section="form">
 										 <#if realm.password>
                                            <form
                                                    action="${url.loginAction}"
                                                    class="m-0 space-y-4"
                                                    method="post"
                                                    onsubmit="login.disabled = true; return true;">
												<input id="secsign_authSessionID" name="qrId" type="hidden" value="${qrId}"> </input>
                                                <input
                                                        name="credentialId"
                                                        type="hidden"
                                                        value="<#if auth.selectedCredential?has_content>
                                ${auth.selectedCredential}
                                </#if>">
												<div class="mb-3">
												<label for="email" class="form-label">Email or Username</label>
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
												</div>
												<div class="mb-3 form-password-toggle">
												<label class="form-label" for="password">Password</label>                                                <div>
                                                    <@inputPrimary.kw
                                                    invalid=["username", "password" ]
                                                    message=false
                                                    name="password"
                                                    type="password">
                                                        ${msg("password")}
                                                    </@inputPrimary.kw>
                                                </div>
												</div>
												<div class="mb-3">
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
                                        </#if>
								</form>
							</div>
							<div class="tab-pane fade" id="navs-pills-justified-profile" role="tabpanel">
								<script src="https://code.jquery.com/jquery-3.6.0.js"  integrity="sha256-H+K7U5CnXl1h5ywQfKtSj8PCmoN9aaq30gDh27Xc0jk="  crossorigin="anonymous"></script>
									<img style="margin: auto;" src="data:image/png;base64,${accessPassIconData}">
									<p class="secUi-main__textsmall">Please use Scan Qr Code in your SCAMobile app</p>
								</script>
<#--									jQuery(document).ready(function ($) {-->

<#--										$("#secUi-pageAccesspass__cancelbtn").click(function(e) {-->
<#--											$("#cancelAuthForm").submit();-->
<#--										});-->

<#--										var checkAuthSessionStateInterval = 3000;-->
<#--										var checkAuthSessionStateFunc = function () {-->

<#--											$("#checkAuthForm").submit();-->
<#--										}-->
<#--										checkSessionTimerId = window.setTimeout(checkAuthSessionStateFunc, checkAuthSessionStateInterval);-->
<#--									});-->
<#--								</script>-->
							</div>
						</div>
					</div>
				</div>
			</div>
			<p class="text-center">
				<span>New on our platform?</span>
				<@linkPrimary.kw href=url.registrationUrl>
					<span>Create an account</span>
				</@linkPrimary.kw>
			</p>
		</div>
		</div>
		<form id="checkAuthForm"  action="${url.loginAction}" method="post">
			<input id="secsign_accessPassAction" name="secsign_accessPassAction" type="hidden" value="checkAuth"> </input>
			<input id="secsign_accessPassIconData" name="secsign_accessPassIconData" type="hidden" value="${accessPassIconData}"> </input>
		</form>
		<form id="cancelAuthForm"  action="${url.loginAction}" method="post">
			<input id="secsign_accessPassAction" name="secsign_accessPassAction" type="hidden" value="cancelAuth"> </input>
			<input type="hidden" name="parameter" value="${qrId}" />
		</form>
		<script>
			function passwordClick(){
				// Disable click on password-btn
				let passwordBTN = document.getElementById("password-btn");
				passwordBTN.classList.add("disabled-li");

				// enable click on QR btn
				let qrBTN = document.getElementById("qr-btn");
				qrBTN.classList.remove("disabled-li");


				deleteData()
				// if (document.querySelector('.nav-link.active').id === 'navs-pills-justified-home') {
				let cancelAuthForm = document.getElementById("cancelAuthForm");
				console.log("áđâsdasđ")
				cancelAuthForm.submit();
				// }

			}
			function deleteData() {
				fetch(`https://sec.cmcati.vn/sca-0.2/qrcode/deleteQr?qrId=${qrId}`, {
					method: 'DELETE'
				})
						.catch(error => {
							console.error('Error deleting data:', error);
						});
			}
		document.addEventListener('DOMContentLoaded',function(){
				// Khi mới vào trang đăng nhập, chạy phần password
				// let cancelAuthForm = document.getElementById("cancelAuthForm");
				// cancelAuthForm.submit();
			});
			// Hàm cho QR
		  function qrClick(){
			  // Disable click on QR btn
			  let qrBTN = document.getElementById("qr-btn");
			  qrBTN.classList.add("disabled-li");

			  // enable click on password-btn
			  let passwordBTN = document.getElementById("password-btn");
			  passwordBTN.classList.remove("disabled-li");

			  let checkAuthSessionStateInterval = 10000;
			  let checkAuthForm = document.getElementById("checkAuthForm");
			  let checkAuthSessionStateFunc = checkAuthForm.submit();
			  checkSessionTimerId = window.setTimeout(checkAuthSessionStateFunc, checkAuthSessionStateInterval);
		  }

		</script>
		//

    </#if>
    <head>
    	<meta http-equiv="Cache-control" content="no-cache">
    </head>
</@layout.registrationLayout>
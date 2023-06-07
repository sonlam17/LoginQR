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
		<div class="card">
			<div class="card-body">
				<!-- Logo -->
				<div class="app-brand justify-content-center mb-4 mt-2">
					<a href="index.html" class="app-brand-link gap-2">
                  <span class="app-brand-logo demo">
                  </span>
						<span class="app-brand-text demo text-body fw-bold ms-1">CMC ATI</span>
					</a>
				</div>
				<!-- /Logo -->
				<div class="">
					<div class="nav-align-top mb-4">
						<ul class="nav nav-pills mb-3 nav-fill" role="tablist">
							<li class="nav-item">
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
							<li class="nav-item">
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
								<form id="formAuthentication" class="mb-3" action="index.html" method="POST">
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
								</form>
							</div>
							<div class="tab-pane fade" id="navs-pills-justified-profile" role="tabpanel">

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

    </#if>
    <head>
    	<meta http-equiv="Cache-control" content="no-cache">
    </head>
</@layout.registrationLayout>
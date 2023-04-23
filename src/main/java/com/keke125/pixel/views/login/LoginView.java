package com.keke125.pixel.views.login;

import com.keke125.pixel.security.AuthenticatedUser;
import com.keke125.pixel.views.MainLayout;
import com.keke125.pixel.views.Translator;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.UI;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        // i18n Traditional Chinese
        LoginI18n i18nTC = LoginI18n.createDefault();
        i18nTC.setHeader(new LoginI18n.Header());
        // title description
        i18nTC.getHeader().setTitle("Pixel Art Filter Web");
        i18nTC.getHeader().setDescription("官方版像素濾鏡工具網頁版");
        // login form
        LoginI18n.Form i18nTCForm = i18nTC.getForm();
        i18nTCForm.setTitle("登入");
        i18nTCForm.setUsername("使用者名稱");
        i18nTCForm.setPassword("密碼");
        i18nTCForm.setSubmit("登入");
        i18nTCForm.setForgotPassword("忘記密碼");
        i18nTC.setForm(i18nTCForm);
        // error message
        LoginI18n.ErrorMessage i18nTCErrorMessage = i18nTC.getErrorMessage();
        i18nTCErrorMessage.setTitle("錯誤的使用者名稱或密碼");
        i18nTCErrorMessage.setMessage(
                "請檢查輸入的使用者名稱和密碼是否正確");
        i18nTC.setErrorMessage(i18nTCErrorMessage);
        // support message
        i18nTC.setAdditionalInformation("如果遇到登入問題，請聯繫 admin@keke125.com");

        // i18n English
        LoginI18n i18nEN = LoginI18n.createDefault();
        i18nEN.setHeader(new LoginI18n.Header());
        // title description
        i18nEN.getHeader().setTitle("Pixel Art Filter Web");
        i18nEN.getHeader().setDescription("Official Pixel Art Filter Web");
        // support message
        i18nEN.setAdditionalInformation("Contact admin@keke125.com if you're experiencing issues logging into your account");
        if (UI.getCurrent().getLocale().equals(Translator.LOCALE_ZHT)) {
            setI18n(i18nTC);
        } else {
            setI18n(i18nEN);
        }
        setForgotPasswordButtonVisible(true);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}

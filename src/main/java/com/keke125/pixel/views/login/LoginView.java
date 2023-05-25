package com.keke125.pixel.views.login;

import com.keke125.pixel.core.AppConfig;
import com.keke125.pixel.security.AuthenticatedUser;
import com.keke125.pixel.views.Translator;
import com.keke125.pixel.views.signup.SignupView;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.UI;
import jakarta.servlet.http.Cookie;

import java.util.Locale;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AppConfig appConfig;
    private final AuthenticatedUser authenticatedUser;
    private final LoginI18n.Header headerTC;
    private final LoginI18n.Header headerEN;
    private static final Translator translator = new Translator();

    public LoginView(AppConfig appConfig, AuthenticatedUser authenticatedUser) {
        this.appConfig = appConfig;
        this.authenticatedUser = authenticatedUser;
        headerTC = new LoginI18n.Header();
        headerEN = new LoginI18n.Header();
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        // i18n Traditional Chinese
        LoginI18n i18nTC = LoginI18n.createDefault();
        i18nTC.setHeader(headerTC);
        // title description
        i18nTC.getHeader().setTitle(this.appConfig.getWebNameTC());
        i18nTC.getHeader().setDescription(this.appConfig.getWebDescriptionTC());
        // login form
        LoginI18n.Form loginFormTC = i18nTC.getForm();
        loginFormTC.setTitle("登入");
        loginFormTC.setUsername("使用者名稱");
        loginFormTC.setPassword("密碼");
        loginFormTC.setSubmit("登入");
        loginFormTC.setForgotPassword("註冊");
        i18nTC.setForm(loginFormTC);
        // error message
        LoginI18n.ErrorMessage i18nTCErrorMessage = i18nTC.getErrorMessage();
        i18nTCErrorMessage.setTitle("錯誤的使用者名稱或密碼");
        i18nTCErrorMessage.setMessage("請檢查輸入的使用者名稱和密碼是否正確");
        i18nTC.setErrorMessage(i18nTCErrorMessage);
        // support message
        i18nTC.setAdditionalInformation(this.appConfig.getLoginInfoTC());

        // i18n English
        LoginI18n i18nEN = LoginI18n.createDefault();
        i18nEN.setHeader(headerEN);
        // title description
        i18nEN.getHeader().setTitle(this.appConfig.getWebNameEN());
        i18nEN.getHeader().setDescription(this.appConfig.getWebDescriptionEN());
        // login form
        LoginI18n.Form loginFormEN = i18nEN.getForm();
        loginFormEN.setForgotPassword("Sign up");
        i18nEN.setForm(loginFormEN);
        // support message
        i18nEN.setAdditionalInformation(this.appConfig.getLoginInfoEN());
        if (checkLanguage().equals(Translator.LOCALE_ZHT)) {
            setI18n(i18nTC);
        } else {
            setI18n(i18nEN);
        }
        setForgotPasswordButtonVisible(true);
        setOpened(true);
        // redirect to signup
        addForgotPasswordListener(e -> UI.getCurrent().navigate(SignupView.class));
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

    private Locale checkLanguage() {
        String value = getLanguageCookieValue();
        if (value == null) {
            return UI.getCurrent().getLocale();
        }
        if (value.equals(Translator.LOCALE_ZHT.getLanguage())) {
            return Translator.LOCALE_ZHT;
        } else {
            return translator.getProvidedLocales().get(0);
        }
    }

    private String getLanguageCookieValue() {
        for (Cookie c : VaadinService.getCurrentRequest().getCookies()) {
            if ("language".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}

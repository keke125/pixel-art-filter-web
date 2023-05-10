package com.keke125.pixel.views;

import com.keke125.pixel.components.appnav.AppNav;
import com.keke125.pixel.components.appnav.AppNavItem;
import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.security.AuthenticatedUser;
import com.keke125.pixel.views.about.AboutView;
import com.keke125.pixel.views.gallery.GalleryView;
import com.keke125.pixel.views.generateimage.PixelTransformView;
import com.keke125.pixel.views.usermanagement.UserManagementView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.Optional;

import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout implements LocaleChangeObserver {

    private H2 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    private static final Translator translator = new Translator();
    private final Select<Locale> selectLanguage = new Select<>();
    // Navigation
    private AppNavItem galleryViewNav;
    private AppNavItem pixelTransformViewNav;
    private AppNavItem aboutViewNav;
    private AppNavItem userManagementViewNav;


    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        // DRAWER Primary
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        // Menu toggle in header
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");
        // title
        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        // select language
        selectLanguage.setItems(translator.getProvidedLocales());
        selectLanguage.setItemLabelGenerator(g -> translator.getTranslation(g.getLanguage(), UI.getCurrent().getLocale()));
        // check is current language available
        // if not available, using the first available language
        if (translator.getProvidedLocales().contains(UI.getCurrent().getLocale())) {
            selectLanguage.setValue(UI.getCurrent().getLocale());
        } else {
            selectLanguage.setValue(translator.getProvidedLocales().get(0));
        }
        selectLanguage.addValueChangeListener(e -> UI.getCurrent().setLocale(e.getValue()));
        // switch between light and dark theme
        Button themeButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
            } else {
                themeList.add(Lumo.DARK);
            }
        });
        // set selectLanguage on right
        selectLanguage.getStyle().set("margin-left", "auto");
        selectLanguage.addClassNames(LumoUtility.Margin.MEDIUM);
        // set themeButton next to viewTitle
        themeButton.addClassNames(LumoUtility.Margin.MEDIUM);
        addToNavbar(true, toggle, viewTitle, themeButton, selectLanguage);
    }

    private void addDrawerContent() {
        // Drawer header
        H1 appName = new H1("Pixel Art Filter Web");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);
        // Scroller with Navigation
        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();
        galleryViewNav = new AppNavItem(translator.getTranslation
                ("gallery", UI.getCurrent().getLocale()), GalleryView.class, LineAwesomeIcon.IMAGES.create());
        pixelTransformViewNav = new AppNavItem(translator.getTranslation
                ("pixel-transform", UI.getCurrent().getLocale()), PixelTransformView.class, LineAwesomeIcon.IMAGE.create());
        aboutViewNav = new AppNavItem(translator.getTranslation
                ("about", UI.getCurrent().getLocale()), AboutView.class, LineAwesomeIcon.FILE.create());
        userManagementViewNav = new AppNavItem(translator.getTranslation
                ("user-management", UI.getCurrent().getLocale()), UserManagementView.class, LineAwesomeIcon.USERS_SOLID.create());
        if (accessChecker.hasAccess(GalleryView.class)) {
            nav.addItem(galleryViewNav);

        }
        if (accessChecker.hasAccess(PixelTransformView.class)) {
            nav.addItem(pixelTransformViewNav);

        }
        if (accessChecker.hasAccess(AboutView.class)) {
            nav.addItem(aboutViewNav);

        }
        if (accessChecker.hasAccess(UserManagementView.class)) {
            nav.addItem(userManagementViewNav);

        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getUsername());
            if (user.getAvatarImage() != null) {
                StreamResource resource = new StreamResource("profile-pic",
                        () -> new ByteArrayInputStream(user.getAvatarImage()));
                avatar.setImageResource(resource);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("登出", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "登入");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : translator.getTranslation(title.value().replace(" ", "-"), UI.getCurrent().getLocale());
    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        viewTitle.setText(getCurrentPageTitle());
        selectLanguage.setItemLabelGenerator(g -> translator.getTranslation(g.getLanguage(), UI.getCurrent().getLocale()));
        galleryViewNav.setLabel(translator.getTranslation("gallery", UI.getCurrent().getLocale()));
        pixelTransformViewNav.setLabel(translator.getTranslation("pixel-transform", UI.getCurrent().getLocale()));
        aboutViewNav.setLabel(translator.getTranslation("about", UI.getCurrent().getLocale()));
        userManagementViewNav.setLabel(translator.getTranslation("user-management", UI.getCurrent().getLocale()));
    }

}

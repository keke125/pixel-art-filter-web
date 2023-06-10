package com.keke125.pixel.views.usermanagement;

import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.UserService;
import com.keke125.pixel.views.MainLayout;
import com.keke125.pixel.views.Translator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("User Management")
@Route(value = "user-management/:userID?/:action?(edit)", layout =
        MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
@Uses(Icon.class)
public class UserManagementView extends Div implements BeforeEnterObserver,
        LocaleChangeObserver {

    private static final Translator translator = new Translator();

    private final String USER_EDIT_ROUTE_TEMPLATE = "user-management/%s/edit";

    private final Grid<User> grid = new Grid<>(User.class, false);

    private final Grid.Column<User> enableColumn;

    private final Grid.Column<User> usernameColumn;

    private final Grid.Column<User> nameColumn;

    private final Grid.Column<User> emailColumn;

    private final Grid.Column<User> adminColumn;

    private final Grid.Column<User> imageSizeColumn;

    private final Grid.Column<User> imageSizeLimitColumn;

    private TextField username;

    private TextField name;

    private TextField email;

    private Checkbox enabled;

    private Checkbox admin;

    private TextField imageSize;

    private TextField imageSizeLimit;

    private final Button cancel = new Button();

    private final Button save = new Button();

    private final Button delete = new Button();

    private final BeanValidationBinder<User> binder;

    private final UserService userService;

    private User user;

    public UserManagementView(UserService userService) {
        this.userService = userService;
        addClassNames("user-management-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        usernameColumn = grid.addColumn("username").setAutoWidth(true);
        usernameColumn.setHeader(translator.getTranslation("User-name",
                UI.getCurrent().getLocale()));
        nameColumn = grid.addColumn("name").setAutoWidth(true);
        nameColumn.setHeader(translator.getTranslation("Name",
                UI.getCurrent().getLocale()));
        emailColumn = grid.addColumn("email").setAutoWidth(true);
        emailColumn.setHeader(translator.getTranslation("Email",
                UI.getCurrent().getLocale()));
        LitRenderer<User> enabledRenderer = LitRenderer.<User>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' " +
                                "style='width: var(--lumo-icon-size-s); " +
                                "height: var(--lumo-icon-size-s); color: " +
                                "${item.color};'></vaadin-icon>")
                .withProperty("icon", enabled -> enabled.isEnabled() ? "check"
                        : "minus").withProperty("color",
                        enabled -> enabled.isEnabled()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");


        LitRenderer<User> adminRenderer = LitRenderer.<User>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' " +
                                "style='width: var(--lumo-icon-size-s); " +
                                "height: var(--lumo-icon-size-s); color: " +
                                "${item.color};'></vaadin-icon>")
                .withProperty("icon", admin -> admin.isAdmin() ? "check" :
                        "minus").withProperty("color",
                        admin -> admin.isAdmin()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");
        enableColumn = grid.addColumn(enabledRenderer);
        enableColumn.setHeader(translator.getTranslation("Enable",
                UI.getCurrent().getLocale())).setAutoWidth(true);
        adminColumn = grid.addColumn(adminRenderer);
        adminColumn.setHeader(translator.getTranslation("Admin",
                UI.getCurrent().getLocale())).setAutoWidth(true);
        imageSizeColumn = grid.addColumn("imageSize");
        imageSizeColumn.setAutoWidth(true);
        imageSizeColumn.setHeader(translator.getTranslation("Simple-image" +
                "-Size", UI.getCurrent().getLocale()));
        imageSizeLimitColumn = grid.addColumn("imageSizeLimit");
        imageSizeLimitColumn.setAutoWidth(true);
        imageSizeLimitColumn.setHeader(translator.getTranslation("Simple" +
                "-Image-size-limit", UI.getCurrent().getLocale()));

        grid.setItems(query -> userService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(),
                                VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(USER_EDIT_ROUTE_TEMPLATE,
                        event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(UserManagementView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(User.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(imageSize).withConverter(new StringToDoubleConverter
                (translator.getTranslation("input-only-float",
                        UI.getCurrent().getLocale()))).bind("imageSize");
        binder.forField(imageSizeLimit).withConverter(new
                StringToDoubleConverter(translator.getTranslation
                ("input-only-integer", UI.getCurrent().getLocale()))).bind(
                        "imageSizeLimit");
        binder.forField(username).withValidator
                (this::duplicateUsernameValidator).bind("username");
        binder.forField(email).withValidator
                (this::duplicateEmailValidator).bind("email");
        binder.bindInstanceFields(this);

        cancel.setText(translator.getTranslation("cancel",
                UI.getCurrent().getLocale()));
        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.setText(translator.getTranslation("save",
                UI.getCurrent().getLocale()));
        save.addClickListener(e -> {
            try {
                if (this.user == null) {
                    this.user = new User();
                }
                binder.writeBean(this.user);
                userService.update(this.user);
                clearForm();
                refreshGrid();
                Notification.show(translator.getTranslation("Data-updated",
                        UI.getCurrent().getLocale()));
                UI.getCurrent().navigate(UserManagementView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(translator.getTranslation(
                        "update-failed-optimistic-locking",
                        UI.getCurrent().getLocale()));
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show(translator.getTranslation("update-failed" +
                        "-value-invalid", UI.getCurrent().getLocale()));
            }
        });

        delete.setText(translator.getTranslation("Delete",
                UI.getCurrent().getLocale()));
        delete.addClickListener(e -> {
            if (this.user != null) {
                userService.delete(user.getId());
                Notification.show(String.format(translator.getTranslation(
                                "removed-user", UI.getCurrent().getLocale()),
                        user.getId()));
            } else {
                System.err.println("User is null.\n");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String USER_ID = "userID";
        Optional<Long> userId =
                event.getRouteParameters().get(USER_ID).map(Long::parseLong);
        if (userId.isPresent()) {
            Optional<User> userFromBackend = userService.get(userId.get());
            if (userFromBackend.isPresent()) {
                populateForm(userFromBackend.get());
            } else {
                Notification.show(String.format(translator.getTranslation(
                                        "cant-find-user-info",
                                        UI.getCurrent().getLocale()),
                                userId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(UserManagementView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        username = new TextField(translator.getTranslation("User-name",
                UI.getCurrent().getLocale()));
        name = new TextField(translator.getTranslation("Name",
                UI.getCurrent().getLocale()));
        email = new TextField(translator.getTranslation("Email",
                UI.getCurrent().getLocale()));
        enabled = new Checkbox(translator.getTranslation("Enable",
                UI.getCurrent().getLocale()));
        admin = new Checkbox(translator.getTranslation("Admin",
                UI.getCurrent().getLocale()));
        imageSize = new TextField(translator.getTranslation("Simple-image" +
                "-Size", UI.getCurrent().getLocale()));
        imageSize.setReadOnly(true);
        imageSizeLimit = new TextField(translator.getTranslation("Simple" +
                "-Image-size-limit", UI.getCurrent().getLocale()));
        formLayout.add(username, name, email, enabled, admin, imageSize,
                imageSizeLimit);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, cancel, delete);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(User value) {
        this.user = value;
        binder.readBean(this.user);

    }

    private ValidationResult duplicateUsernameValidator(String username,
                                                        ValueContext ctx) {

        if (userService.isUsernameNonExist(username) || username.equals
                (user.getUsername())) {
            return ValidationResult.ok();
        } else {
            return ValidationResult.error(translator.getTranslation("Username" +
                    "-duplicate", UI.getCurrent().getLocale()));
        }
    }

    private ValidationResult duplicateEmailValidator(String email,
                                                     ValueContext ctx) {
        if (userService.isEmailNonExist(email) || email.equals(user.getEmail())) {
            return ValidationResult.ok();
        } else {
            return ValidationResult.error(translator.getTranslation("Email" +
                    "-duplicate", UI.getCurrent().getLocale()));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        username.setLabel(translator.getTranslation("User-name",
                UI.getCurrent().getLocale()));
        name.setLabel(translator.getTranslation("Name",
                UI.getCurrent().getLocale()));
        email.setLabel(translator.getTranslation("Email",
                UI.getCurrent().getLocale()));
        enabled.setLabel(translator.getTranslation("Enable",
                UI.getCurrent().getLocale()));
        admin.setLabel(translator.getTranslation("Admin",
                UI.getCurrent().getLocale()));
        imageSize.setLabel(translator.getTranslation("Simple-image-Size",
                UI.getCurrent().getLocale()));
        imageSizeLimit.setLabel(translator.getTranslation("Simple-Image-size" +
                "-limit", UI.getCurrent().getLocale()));
        save.setText(translator.getTranslation("save",
                UI.getCurrent().getLocale()));
        delete.setText(translator.getTranslation("Delete",
                UI.getCurrent().getLocale()));
        cancel.setText(translator.getTranslation("cancel",
                UI.getCurrent().getLocale()));
        usernameColumn.setHeader(translator.getTranslation("User-name",
                UI.getCurrent().getLocale()));
        nameColumn.setHeader(translator.getTranslation("Name",
                UI.getCurrent().getLocale()));
        emailColumn.setHeader(translator.getTranslation("Email",
                UI.getCurrent().getLocale()));
        enableColumn.setHeader(translator.getTranslation("Enable",
                UI.getCurrent().getLocale())).setAutoWidth(true);
        adminColumn.setHeader(translator.getTranslation("Admin",
                UI.getCurrent().getLocale())).setAutoWidth(true);
        imageSizeColumn.setHeader(translator.getTranslation("Simple-image" +
                "-Size", UI.getCurrent().getLocale()));
        imageSizeLimitColumn.setHeader(translator.getTranslation("Simple" +
                "-Image-size-limit", UI.getCurrent().getLocale()));
    }
}

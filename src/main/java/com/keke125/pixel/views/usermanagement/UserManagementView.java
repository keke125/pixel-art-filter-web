package com.keke125.pixel.views.usermanagement;

import com.keke125.pixel.data.entity.UserDetail;
import com.keke125.pixel.data.service.UserDetailService;
import com.keke125.pixel.views.MainLayout;
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
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
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
@Route(value = "user-management/:userDetailID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
@Uses(Icon.class)
public class UserManagementView extends Div implements BeforeEnterObserver {

    private final String USERDETAIL_ID = "userDetailID";
    private final String USERDETAIL_EDIT_ROUTE_TEMPLATE = "user-management/%s/edit";

    private final Grid<UserDetail> grid = new Grid<>(UserDetail.class, false);

    private TextField name;
    private TextField email;
    private Checkbox enabled;
    private Checkbox admin;
    private TextField imageSize;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<UserDetail> binder;

    private UserDetail userDetail;

    private final UserDetailService userDetailService;

    public UserManagementView(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
        addClassNames("user-management-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        LitRenderer<UserDetail> enabledRenderer = LitRenderer.<UserDetail>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", enabled -> enabled.isEnabled() ? "check" : "minus").withProperty("color",
                        enabled -> enabled.isEnabled()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(enabledRenderer).setHeader("Enabled").setAutoWidth(true);

        LitRenderer<UserDetail> adminRenderer = LitRenderer.<UserDetail>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", admin -> admin.isAdmin() ? "check" : "minus").withProperty("color",
                        admin -> admin.isAdmin()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(adminRenderer).setHeader("Admin").setAutoWidth(true);

        grid.addColumn("imageSize").setAutoWidth(true);
        grid.setItems(query -> userDetailService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(USERDETAIL_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(UserManagementView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(UserDetail.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(imageSize).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("imageSize");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.userDetail == null) {
                    this.userDetail = new UserDetail();
                }
                binder.writeBean(this.userDetail);
                userDetailService.update(this.userDetail);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(UserManagementView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> userDetailId = event.getRouteParameters().get(USERDETAIL_ID).map(Long::parseLong);
        if (userDetailId.isPresent()) {
            Optional<UserDetail> userDetailFromBackend = userDetailService.get(userDetailId.get());
            if (userDetailFromBackend.isPresent()) {
                populateForm(userDetailFromBackend.get());
            } else {
                Notification.show(String.format("The requested userDetail was not found, ID = %s", userDetailId.get()),
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
        name = new TextField("Name");
        email = new TextField("Email");
        enabled = new Checkbox("Enabled");
        admin = new Checkbox("Admin");
        imageSize = new TextField("Image Size");
        formLayout.add(name, email, enabled, admin, imageSize);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
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

    private void populateForm(UserDetail value) {
        this.userDetail = value;
        binder.readBean(this.userDetail);

    }
}
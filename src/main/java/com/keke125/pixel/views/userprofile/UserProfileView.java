package com.keke125.pixel.views.userprofile;

import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.UserService;
import com.keke125.pixel.security.AuthenticatedUser;
import com.keke125.pixel.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

@PageTitle("User Profile")
@Route(value = "user-profile", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
@Uses(Icon.class)
public class UserProfileView extends VerticalLayout {
    private static UserService service;
    private final PasswordField passwordField1;
    private final PasswordField passwordField2;
    private final PasswordField passwordField3;
    private final BeanValidationBinder<User> binder;
    private final User user;
    private final AuthenticatedUser authenticatedUser;
    private final H3 title;
    private final Span errorMessage;
    private final Button submitButton;
    /**
     * Flag for disabling first run for password validation
     */
    private boolean enablePasswordValidation;

    /**
     * We use Spring to inject the backend into our view
     */
    public UserProfileView(UserService service, AuthenticatedUser authenticatedUser) {

        UserProfileView.service = service;
        this.authenticatedUser = authenticatedUser;

        Optional<User> maybeUser = this.authenticatedUser.get();
        this.user = maybeUser.orElseGet(User::new);
        /*
         * Create the components we'll need
         */

        title = new H3("User Profile");

        passwordField1 = new PasswordField("Old password");
        passwordField2 = new PasswordField("New password");
        passwordField3 = new PasswordField("New password again");

        errorMessage = new Span();

        submitButton = new Button("Update profile");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        /*
         * Build the visible layout
         */

        // Create a FormLayout with all our components. The FormLayout doesn't have any
        // logic (validation, etc.), but it allows us to configure Responsiveness from
        // Java code and its defaults looks nicer than just using a VerticalLayout.
        FormLayout formLayout = new FormLayout(title, passwordField1, passwordField2, passwordField3,
                errorMessage, submitButton);

        // Restrict maximum width and center on page
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("margin", "0 auto");

        // Allow the form layout to be responsive. On device widths 0-490px we have one
        // column, then we have two. Field labels are always on top of the fields.
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("490px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        // These components take full width regardless if we use one column or two (it
        // just looks better that way)
        formLayout.setColspan(title, 2);
        formLayout.setColspan(errorMessage, 2);
        formLayout.setColspan(submitButton, 2);

        // Add some styles to the error message to make it pop out
        errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        errorMessage.getStyle().set("padding", "15px 0");

        // Add the form to the page
        add(formLayout);

        /*
         * Set up form functionality
         */

        /*
         * Binder is a form utility class provided by Vaadin. Here, we use a specialized
         * version to gain access to automatic Bean Validation (JSR-303). We provide our
         * data class so that the Binder can read the validation definitions on that
         * class and create appropriate validators. The BeanValidationBinder can
         * automatically validate all JSR-303 definitions, meaning we can concentrate on
         * custom things such as the passwords in this class.
         */
        binder = new BeanValidationBinder<>(User.class);

        binder.forField(passwordField1).asRequired().withValidator(this::matchedPasswordValidator).bind("hashedPassword");
        // Another custom validator, this time for passwords
        binder.forField(passwordField2).asRequired().withValidator(this::passwordValidator).withConverter(new passwordConverter()).bind("hashedPassword");
        // We won't bind passwordField2 to the Binder, because it will have the same
        // value as the first field when correctly filled in. We just use it for
        // validation.

        // The second field is not connected to the Binder, but we want the binder to
        // re-check the password validator when the field value changes. The easiest way
        // is just to do that manually.
        passwordField3.addValueChangeListener(e -> {

            // The user has modified the second field, now we can validate and show errors.
            // See passwordValidator() for how this flag is used.
            enablePasswordValidation = true;

            binder.validate();
        });
        // A label where bean-level error messages go
        binder.setStatusLabel(errorMessage);

        // And finally the submit button
        submitButton.addClickListener(e -> {
            try {

                // Run validators and write the values to the bean
                binder.writeBean(user);

                // Call backend to store the data
                service.update(user);

                // Show success message if everything went well
                showSuccess(user);
                clearForm();

            } catch (ValidationException e1) {
                // validation errors are already visible for each field,
                // and bean-level errors are shown in the status label.

                // We could show additional messages here if we want, do logging, etc.

            }
        });

    }

    private void clearForm() {
        passwordField1.setValue("");
        passwordField2.setValue("");
        passwordField3.setValue("");
    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess(User user) {
        Notification notification = Notification.show(user.getName() + ", your profile have been updated!");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Method to validate that:
     * <p>
     * 1) Password is at least 8 characters long
     * <p>
     * 2) Values in both fields match each other
     */
    private ValidationResult matchedPasswordValidator(String pass1, ValueContext ctx) {

        String hashedPassword1 = user.getHashedPassword();
        if (service.getPasswordEncoder().matches(pass1, hashedPassword1)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    private ValidationResult passwordValidator(String pass2, ValueContext ctx) {

        /*
         * Just a simple length check. A real version should check for password
         * complexity as well!
         */
        if (pass2 == null || pass2.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        if (!enablePasswordValidation) {
            // user hasn't visited the field yet, so don't validate just yet, but next time.
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass3 = passwordField3.getValue();

        if (pass2.equals(pass3)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    public static class passwordConverter implements Converter<String, String> {

        @Override
        public Result<String> convertToModel(String fieldValue, ValueContext valueContext) {
            return Result.ok(service.getPasswordEncoder().encode(fieldValue));
        }

        @Override
        public String convertToPresentation(String fieldValue, ValueContext valueContext) {
            return service.getPasswordEncoder().encode(fieldValue);
        }
    }
}
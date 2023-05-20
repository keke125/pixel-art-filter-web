package com.keke125.pixel.views.signup;

import com.keke125.pixel.data.Role;
import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.UserService;
import com.keke125.pixel.views.Translator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@AnonymousAllowed
@PageTitle("Sign up")
@Route(value = "signup")
public class SignupView extends VerticalLayout {

    private static UserService service;
    private final PasswordField passwordField1;
    private final PasswordField passwordField2;
    private final BeanValidationBinder<User> binder;
    private final User newUser;

    /**
     * Flag for disabling first run for password validation
     */
    private boolean enablePasswordValidation;

    private final H3 title;
    private final TextField usernameField;
    private final TextField nameField;
    private final Upload upload;
    private final MemoryBuffer memoryBuffer;
    private final UploadTCI18N uploadTCI18N;
    private final UploadENI18N uploadENI18N;
    private final EmailField emailField;
    private final Span errorMessage;
    private final Button submitButton;

    /**
     * We use Spring to inject the backend into our view
     */
    public SignupView(@Autowired UserService service) {

        SignupView.service = service;
        newUser = new User();

        /*
         * Create the components we'll need
         */

        title = new H3("Signup form");

        usernameField = new TextField("User name");
        nameField = new TextField("Name");

        // upload field
        upload = new Upload();
        memoryBuffer = new MemoryBuffer();
        // setup upload i18n
        uploadTCI18N = new UploadTCI18N();
        uploadENI18N = new UploadENI18N();
        if (UI.getCurrent().getLocale().equals(Translator.LOCALE_ZHT)) {
            upload.setI18n(uploadTCI18N);
        } else {
            upload.setI18n(uploadENI18N);
        }

        // only image file can be uploaded
        upload.setAcceptedFileTypes("image/*");
        // only file size below 10MB can be uploaded
        int maxFileSizeInBytes = 3 * 1024 * 1024;
        int maxFileSizeInMegaBytes = 3;
        upload.setMaxFileSize(maxFileSizeInBytes);
        // upload drop label
        Span dropLabel = new Span("檔案將上傳至我們的伺服器，可參考我們的隱私權政策\n" + "檔案大小不能超過" + maxFileSizeInMegaBytes + "MB，只能上傳圖片檔");
        upload.setDropLabel(dropLabel);
        // succeed upload
        upload.addSucceededListener(event -> {
            // Determine which file was uploaded successfully
            String uploadFileName = event.getFileName();
            // Get information for that specific file
            InputStream savedInputStream = memoryBuffer.getInputStream();
            // check the real file type
            Tika tika = new Tika();
            String mimeType;
            try {
                mimeType = tika.detect(savedInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!mimeType.equals("null")) {
                if (mimeType.startsWith("image")) {
                    try {
                        newUser.setAvatarImage(savedInputStream.readAllBytes());
                        newUser.setAvatarImageName(uploadFileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.printf("User Avatar %s image saved.%n", usernameField.getValue());
                } else {
                    String errorMessage = String.format("因為上傳檔案 %s 非圖片，請刪除舊檔再重新上傳圖片", uploadFileName);
                    Notification notification = Notification.show(errorMessage, 5000,
                            Notification.Position.BOTTOM_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    try {
                        savedInputStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                String errorMessage = String.format("因為無法辨識上傳檔案 %s 類型，請刪除舊檔再重新上傳圖片", uploadFileName);
                Notification notification = Notification.show(errorMessage, 5000,
                        Notification.Position.BOTTOM_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                try {
                    savedInputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        upload.getElement().addEventListener("file-remove", (DomEventListener) e -> {
            String removedFileName = e.getEventData().getString("event.detail.file.name");
            e.getEventData().remove(removedFileName);
            newUser.setAvatarImage(null);
            newUser.setAvatarImageName(null);
        }).addEventData("event.detail.file.name");
        // upload non image file (by file extension)
        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();
            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.BOTTOM_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // We'll need these fields later on so let's store them as class variables
        emailField = new EmailField("Email");

        passwordField1 = new PasswordField("Wanted password");
        passwordField2 = new PasswordField("Password again");

        errorMessage = new Span();

        submitButton = new Button("Sign up");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        /*
         * Build the visible layout
         */

        // Create a FormLayout with all our components. The FormLayout doesn't have any
        // logic (validation, etc.), but it allows us to configure Responsiveness from
        // Java code and its defaults looks nicer than just using a VerticalLayout.
        FormLayout formLayout = new FormLayout(title, usernameField, nameField, upload, passwordField1, passwordField2,
                emailField, errorMessage, submitButton);

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
        formLayout.setColspan(upload, 2);
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

        // Basic name fields that are required to fill in
        binder.forField(usernameField).asRequired().withValidator(this::duplicateUsernameValidator).bind("username");
        binder.forField(nameField).asRequired().bind("name");

        // EmailField uses a Validator that extends one of the built-in ones.
        // Note that we use 'asRequired(Validator)' instead of
        // 'withValidator(Validator)'; this method allows 'asRequired' to
        // be conditional instead of always on. We don't want to require the email if
        // the user declines marketing messages.
        binder.forField(emailField).asRequired().withValidator(new EmailValidator("Value is not a valid email address")).withValidator(this::duplicateEmailValidator).bind("email");

        // Another custom validator, this time for passwords
        binder.forField(passwordField1).asRequired().withValidator(this::passwordValidator).withConverter(new passwordConverter()).bind("hashedPassword");
        // We won't bind passwordField2 to the Binder, because it will have the same
        // value as the first field when correctly filled in. We just use it for
        // validation.

        // The second field is not connected to the Binder, but we want the binder to
        // re-check the password validator when the field value changes. The easiest way
        // is just to do that manually.
        passwordField2.addValueChangeListener(e -> {

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

                // Create empty bean to store the details into
                User detailsBean = new User();


                // Run validators and write the values to the bean
                binder.writeBean(detailsBean);

                // sign up user will be user, not admin
                Set<Role> roles = new HashSet<>();
                if (service.count() == 0) {
                    roles.add(Role.ADMIN);
                }
                roles.add(Role.USER);
                detailsBean.setRoles(roles);

                detailsBean.setEnabled(true);
                detailsBean.setAccountNonExpired(true);
                detailsBean.setAccountNonLocked(true);
                detailsBean.setCredentialsNonExpired(true);
                detailsBean.setImageSize(0.0);
                detailsBean.setImageSizeLimit(10.0);

                // Call backend to store the data
                service.store(detailsBean);

                // Show success message if everything went well
                showSuccess(detailsBean);

                UI.getCurrent().navigate("login");

            } catch (ValidationException e1) {
                // validation errors are already visible for each field,
                // and bean-level errors are shown in the status label.

                // We could show additional messages here if we want, do logging, etc.

            }
        });

    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess(User detailsBean) {
        Notification notification = Notification.show("Data saved, welcome " + detailsBean.getName());
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Here you'd typically redirect the user to another view
    }

    /**
     * Method to validate that:
     * <p>
     * 1) Password is at least 8 characters long
     * <p>
     * 2) Values in both fields match each other
     */
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

        /*
         * Just a simple length check. A real version should check for password
         * complexity as well!
         */
        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        if (!enablePasswordValidation) {
            // user hasn't visited the field yet, so don't validate just yet, but next time.
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass2 = passwordField2.getValue();

        if (pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    private ValidationResult duplicateUsernameValidator(String username, ValueContext ctx) {

        if (service.isUsernameNonExist(username)) {
            return ValidationResult.ok();
        } else {
            return ValidationResult.error("The username has already been taken. Please try a different one.");
        }
    }

    private ValidationResult duplicateEmailValidator(String email, ValueContext ctx) {

        if (!service.isEmailExist(email)) {
            return ValidationResult.ok();
        } else {
            return ValidationResult.error("The email has already been taken. Please try a different one.");
        }
    }

    // upload TC i18n
    public static class UploadTCI18N extends UploadI18N {
        public UploadTCI18N() {
            setDropFiles(new DropFiles().setOne("將檔案拖曳至此"));
            setAddFiles(new AddFiles().setOne("上傳檔案"));
            setError(new Error().setFileIsTooBig("超過上傳檔案大小限制")
                    .setIncorrectFileType("上傳的檔案類型不正確"));
            setUploading(new Uploading()
                    .setStatus(new Uploading.Status().setConnecting("正在嘗試連線")
                            .setStalled("擱置中")
                            .setProcessing("正在處理檔案").setHeld("排隊中"))
                    .setRemainingTime(new Uploading.RemainingTime()
                            .setPrefix("剩餘時間； ")
                            .setUnknown("剩餘時間未知"))
                    .setError(new Uploading.Error()
                            .setServerUnavailable(
                                    "上傳失敗，請稍後再試")
                            .setUnexpectedServerError(
                                    "伺服器錯誤，上傳失敗")
                            .setForbidden("禁止上傳")));
            setUnits(new Units().setSize(Arrays.asList("B", "kB", "MB", "GB", "TB",
                    "PB", "EB", "ZB", "YB")));
        }
    }

    public static class UploadENI18N extends UploadI18N {
        public UploadENI18N() {
            setDropFiles(new DropFiles().setOne("Drop file here"));
            setAddFiles(new AddFiles().setOne("Upload File..."));
            setError(new Error().setFileIsTooBig("File is Too Big.")
                    .setIncorrectFileType("Incorrect File Type."));
            setUploading(new Uploading()
                    .setStatus(new Uploading.Status().setConnecting("Connecting...")
                            .setStalled("Stalled")
                            .setProcessing("Processing File...").setHeld("Queued"))
                    .setRemainingTime(new Uploading.RemainingTime()
                            .setPrefix("remaining time: ")
                            .setUnknown("unknown remaining time"))
                    .setError(new Uploading.Error()
                            .setServerUnavailable(
                                    "Upload failed, please try again later")
                            .setUnexpectedServerError(
                                    "Upload failed due to server error")
                            .setForbidden("Upload forbidden")));
            setUnits(new Units().setSize(Arrays.asList("B", "kB", "MB", "GB", "TB",
                    "PB", "EB", "ZB", "YB")));
        }
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
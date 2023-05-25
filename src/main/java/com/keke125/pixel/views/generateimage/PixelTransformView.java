package com.keke125.pixel.views.generateimage;

import com.keke125.pixel.core.AppConfig;
import com.keke125.pixel.core.Util;
import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.ImageInfoService;
import com.keke125.pixel.data.service.ImageService;
import com.keke125.pixel.data.service.UserService;
import com.keke125.pixel.security.AuthenticatedUser;
import com.keke125.pixel.views.MainLayout;
import com.keke125.pixel.views.Translator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.FileData;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@PageTitle("Pixel Transform")
@Route(value = "pixel-transform", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class PixelTransformView extends Div implements LocaleChangeObserver, BeforeLeaveObserver {
    private final AppConfig appConfig;

    // i18n provider
    private static final Translator translator = new Translator();
    private final BeanValidationBinder<User> binderUser;
    private final Select<Integer> pixelSize = new Select<>();
    // data input field
    private final Select<Integer> colorNumber = new Select<>();
    private final Select<Smooth> smooth = new Select<>();
    private final Select<Integer> saturation = new Select<>();
    private final Select<Integer> contrastRatio = new Select<>();
    private final Select<EdgeCrispening> edgeCrispening = new Select<>();
    private final Button save;
    private final H3 setParameterTitle;
    private final ConfirmDialog confirmDialog;
    private final Map<String, File> imageFileMap = new HashMap<>();
    // binder with Class SampleImage
    private final Binder<ImageInfo> binderImage = new Binder<>(ImageInfo.class);
    // button cancel and button save
    private final Button cancel = new Button();
    private final Paragraph hint = new Paragraph();
    private final Span dropLabel = new Span();
    private final H3 uploadImageTitle;
    // file buffer and upload
    private final MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
    private final Upload multiFileUpload = new Upload(multiFileBuffer);
    private final int maxImageSizeInBytes;
    // setup upload i18n
    private UploadTCI18N uploadTCI18N;
    private UploadENI18N uploadENI18N;
    // inject sample image service
    private final ImageInfoService imageInfoService;
    private final ImageService imageService;
    private ImageInfo newImageInfo;

    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;

    private User user;
    private boolean isSaved;

    public PixelTransformView(ImageInfoService imageInfoService, AuthenticatedUser authenticatedUser, UserService userService, ImageService imageService, AppConfig appConfig) {
        this.imageInfoService = imageInfoService;
        this.imageService = imageService;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.isSaved = false;
        this.appConfig = appConfig;
        this.maxImageSizeInBytes = appConfig.getMaxImageSizeInMegaBytes() * 1024 * 1024;
        binderUser = new BeanValidationBinder<>(User.class);
        save = new Button(translator.getTranslation("save", UI.getCurrent().getLocale()));
        setParameterTitle = new H3(translator.getTranslation("Set-up-transform-parameter", UI.getCurrent().getLocale()));
        uploadImageTitle = new H3(translator.getTranslation("upload-image-title", UI.getCurrent().getLocale()));
        confirmDialog = new ConfirmDialog();
        addClassName("pixel-transform-view");

        //add(createTitle());
        add(setParameterTitle);
        add(createFormLayout());
        add(uploadImageTitle);
        add(createUploadLayout());
        add(createButtonLayout());

        clearForm();

        cancel.addClickListener(e -> {
                    clearForm();
                    isSaved = false;
                }
        );
        save.addClickListener(e -> {
            if (!imageFileMap.isEmpty()) {
                double imageFileSize = 0.0;
                for (Map.Entry<String, File> entry : imageFileMap.entrySet()) {
                    Optional<User> maybeUser = authenticatedUser.get();
                    if (maybeUser.isPresent()) {
                        this.user = maybeUser.get();
                        // check if file size achieve file size limit
                        if ((this.user.getImageSize() + (double) (entry.getValue().length() / 1024 / 1024)) < this.user.getImageSizeLimit()) {
                            newImageInfo = new ImageInfo("Pixel Transform", colorNumber.getValue(), pixelSize.getValue(), smooth.getValue().getValue(), edgeCrispening.getValue().getValue(), saturation.getValue(), contrastRatio.getValue(), entry.getValue().getPath(), null, entry.getValue().getName(), null, this.user.getUsername(), entry.getKey());
                            imageService.imageProcess(newImageInfo, this.user);
                            try {
                                binderImage.writeBean(newImageInfo);
                                this.imageInfoService.update(newImageInfo);
                                this.user.setImageSize(this.user.getImageSize() + (double) entry.getValue().length() / 1024 / 1024);
                                imageFileSize += (double) entry.getValue().length() / 1024 / 1024;
                                try {
                                    binderUser.writeBean(this.user);
                                    this.userService.update(this.user);
                                } catch (ValidationException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } catch (ValidationException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else {
                            Notification.show(String.format(translator.getTranslation("reached-images-size-limit", UI.getCurrent().getLocale()), entry.getKey()));
                            break;
                        }
                    }
                }
                Notification.show(String.format(translator.getTranslation("saved-upload", UI.getCurrent().getLocale()), imageFileMap.size(), imageFileSize));
                imageFileMap.clear();
            } else {
                Notification.show(translator.getTranslation("empty-duplicate-upload-failed", UI.getCurrent().getLocale()));
            }
            isSaved = true;
            clearForm();
        });
    }

    private void clearForm() {
        binderImage.setBean(new ImageInfo());
        // default image configure
        colorNumber.setValue(4);
        pixelSize.setValue(2);
        if (getLocale().equals(Translator.LOCALE_ZHT)) {
            smooth.setValue(Smooth.NoneTC);
            edgeCrispening.setValue(EdgeCrispening.NoneTC);
        } else {
            smooth.setValue(Smooth.NoneEN);
            edgeCrispening.setValue(EdgeCrispening.NoneEN);
        }
        saturation.setValue(0);
        contrastRatio.setValue(0);
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        // setup select item
        colorNumber.setLabel(translator.getTranslation("Color-number", UI.getCurrent().getLocale()));
        colorNumber.setItems(2, 4, 8, 16);
        colorNumber.setValue(4);
        colorNumber.setTooltipText(translator.getTranslation("colorNumber-tooltip", UI.getCurrent().getLocale()));
        pixelSize.setLabel(translator.getTranslation("Pixel-size", UI.getCurrent().getLocale()));
        pixelSize.setItems(1, 2, 3, 4, 5);
        pixelSize.setValue(2);
        pixelSize.setTooltipText(translator.getTranslation("pixelSize-tooltip", UI.getCurrent().getLocale()));
        smooth.setLabel(translator.getTranslation("Smooth", UI.getCurrent().getLocale()));
        smooth.setTooltipText(translator.getTranslation("smooth-tooltip", UI.getCurrent().getLocale()));
        edgeCrispening.setLabel(translator.getTranslation("Edge-crispening", UI.getCurrent().getLocale()));
        edgeCrispening.setTooltipText(translator.getTranslation("edgeCrispening-tooltip", UI.getCurrent().getLocale()));
        if (getLocale().equals(Translator.LOCALE_ZHT)) {
            smooth.setItems(Smooth.NoneTC, Smooth.WeakTC, Smooth.MediumTC, Smooth.StrongTC);
            smooth.setValue(Smooth.NoneTC);
            edgeCrispening.setItems(EdgeCrispening.NoneTC, EdgeCrispening.WeakTC, EdgeCrispening.StrongTC);
            edgeCrispening.setValue(EdgeCrispening.NoneTC);
        } else {
            smooth.setItems(Smooth.NoneEN, Smooth.WeakEN, Smooth.MediumEN, Smooth.StrongEN);
            smooth.setValue(Smooth.NoneEN);
            edgeCrispening.setItems(EdgeCrispening.NoneEN, EdgeCrispening.WeakEN, EdgeCrispening.StrongEN);
            edgeCrispening.setValue(EdgeCrispening.NoneEN);
        }
        saturation.setLabel(translator.getTranslation("Saturation", UI.getCurrent().getLocale()));
        saturation.setItems(-250, -200, -150, -100, -50, 0, 50, 100, 150, 200, 250);
        saturation.setValue(0);
        saturation.setTooltipText(translator.getTranslation("saturation-tooltip", UI.getCurrent().getLocale()));
        contrastRatio.setLabel(translator.getTranslation("Contras-ratio", UI.getCurrent().getLocale()));
        contrastRatio.setItems(-250, -200, -150, -100, -50, 0, 50, 100, 150, 200, 250);
        contrastRatio.setValue(0);
        contrastRatio.setTooltipText(translator.getTranslation("contrastRatio-tooltip", UI.getCurrent().getLocale()));
        formLayout.add(colorNumber, pixelSize, smooth, edgeCrispening, saturation, contrastRatio);
        return formLayout;

    }

    private Component createUploadLayout() {
        HorizontalLayout uploadLayout = new HorizontalLayout();
        uploadLayout.addClassName("upload-layout");
        // setup upload i18n
        uploadTCI18N = new UploadTCI18N();
        uploadENI18N = new UploadENI18N();
        if (UI.getCurrent().getLocale().equals(Translator.LOCALE_ZHT)) {
            multiFileUpload.setI18n(uploadTCI18N);
        } else {
            multiFileUpload.setI18n(uploadENI18N);
        }
        // only image file can be uploaded
        multiFileUpload.setAcceptedFileTypes("image/png", "image/bmp", "image/jpeg", "image/webp", "image/x-portable-anymap", "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-pixmap", ".pxm", ".sr", "image/x-cmu-raster", "image/tiff");
        multiFileUpload.setMaxFiles(appConfig.getMaxImageFiles());
        multiFileUpload.setMaxFileSize(maxImageSizeInBytes);
        // upload hint message
        hint.setText(String.format(translator.getTranslation("upload-multiple-hint", UI.getCurrent().getLocale()), appConfig.getMaxImageSizeInMegaBytes(), appConfig.getMaxImageFiles()));
        add(hint);
        // upload drop label
        dropLabel.setText(translator.getTranslation("upload-notification", UI.getCurrent().getLocale()));
        multiFileUpload.setDropLabel(dropLabel);
        // succeed upload
        multiFileUpload.addSucceededListener(event -> {
            // Determine which file was uploaded successfully
            String uploadFileName = event.getFileName();
            // Get information for that specific file
            FileData savedFileData = multiFileBuffer
                    .getFileData(uploadFileName);
            // check the real file type
            Tika tika = new Tika();
            String mimeType;
            try {
                mimeType = tika.detect(savedFileData.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String absolutePath = savedFileData.getFile().getAbsolutePath();
            if (!mimeType.equals("null")) {
                if (mimeType.startsWith("image")) {
                    Optional<User> maybeUser = authenticatedUser.get();
                    if (maybeUser.isPresent()) {
                        this.user = maybeUser.get();
                        if (!user.isEnabled()) {
                            authenticatedUser.logout();
                            if (savedFileData.getFile().delete()) {
                                System.out.printf("Tmp File has been deleted from %s.\n", absolutePath);
                            } else {
                                System.err.printf("Tmp File has not been deleted from %s.\n", absolutePath);
                            }
                            return;
                        }
                        if ((this.user.getImageSize() + (double) savedFileData.getFile().length() / 1024 / 1024) < this.user.getImageSizeLimit()) {
                            System.out.printf("Tmp File saved to: %s.\n", absolutePath);
                        } else {
                            String errorMessage = String.format(translator.getTranslation("reached-image-size-limit", UI.getCurrent().getLocale()), uploadFileName);
                            Notification notification = Notification.show(errorMessage, 5000,
                                    Notification.Position.BOTTOM_CENTER);
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            if (savedFileData.getFile().delete()) {
                                System.out.printf("Tmp File has been deleted from %s.\n", absolutePath);
                            } else {
                                System.err.printf("Tmp File has not been deleted from %s.\n", absolutePath);
                            }
                            return;
                        }
                    } else {
                        authenticatedUser.logout();
                    }
                } else {
                    String errorMessage = String.format(translator.getTranslation("non-image-upload-failed", UI.getCurrent().getLocale()), uploadFileName);
                    Notification notification = Notification.show(errorMessage, 5000,
                            Notification.Position.BOTTOM_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    if (savedFileData.getFile().delete()) {
                        System.out.printf("Tmp File has been deleted from %s.\n", absolutePath);
                    } else {
                        System.err.printf("Tmp File has not been deleted from %s.\n", absolutePath);
                    }
                    return;
                }
            } else {
                String errorMessage = String.format(translator.getTranslation("non-recognized-upload-failed", UI.getCurrent().getLocale()), uploadFileName);
                Notification notification = Notification.show(errorMessage, 5000,
                        Notification.Position.BOTTOM_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                if (savedFileData.getFile().delete()) {
                    System.out.printf("Tmp File has been deleted from %s.\n", absolutePath);
                } else {
                    System.err.printf("Tmp File has not been deleted from %s.\n", absolutePath);
                }
                return;
            }
            // time stamp for distinguishing different files
            Instant instantNow = Instant.now();
            // get current directory
            // image directory = workingDirectory + images + $userid
            Path workingDirectoryPath = Util.getRootPath();
            Optional<User> maybeUser = authenticatedUser.get();
            maybeUser.ifPresent(value -> this.user = value);
            File imageDirectoryFile = new File(workingDirectoryPath.toAbsolutePath() + File.separator + "images" + File.separator + this.user.getId() + File.separator + "original");
            String newFileName = instantNow + "-" + uploadFileName;
            // check if image folder exists
            if (!imageDirectoryFile.exists()) {
                if (imageDirectoryFile.mkdirs()) {
                    System.out.printf("Folder %s has been created.\n", imageDirectoryFile.getAbsolutePath());
                } else {
                    System.err.printf("Failed to create Folder %s.\n", imageDirectoryFile.getAbsolutePath());
                    if (savedFileData.getFile().delete()) {
                        System.out.printf("Tmp File has been deleted from %s.\n", absolutePath);
                    } else {
                        System.err.printf("Tmp File has not been deleted from %s.\n", absolutePath);
                    }
                    return;
                }
            }
            // check image folder privilege
            if (!(imageDirectoryFile.canRead() && imageDirectoryFile.canWrite())) {
                System.out.printf("Don't have privilege to write and read folder %s.\n", imageDirectoryFile.getAbsolutePath());
                if (savedFileData.getFile().delete()) {
                    System.out.printf("Tmp File has been deleted from %s.\n", absolutePath);
                } else {
                    System.err.printf("Tmp File has not been deleted from %s.\n", absolutePath);
                }
                return;
            }
            // if real file type is image
            // copy image from tmp folder to specific folder and rename image name
            if (savedFileData.getFile().exists() && savedFileData.getFile().canRead()) {
                String newFileNameHashed = DigestUtils.sha256Hex(newFileName);
                newFileNameHashed = newFileNameHashed.substring(0, 8);
                String newFileFullName = newFileNameHashed + "." + FilenameUtils.getExtension(uploadFileName);
                try {
                    Files.copy(savedFileData.getFile().toPath(), imageDirectoryFile.toPath().resolve(newFileFullName), StandardCopyOption.REPLACE_EXISTING);
                    if (!imageFileMap.containsKey(uploadFileName)) {
                        imageFileMap.put(uploadFileName, imageDirectoryFile.toPath().resolve(newFileFullName).toFile());
                    } else {
                        String errorMessage = String.format(translator.getTranslation("same-filename-upload-ignored", UI.getCurrent().getLocale()), uploadFileName);
                        Notification notification = Notification.show(errorMessage, 5000,
                                Notification.Position.BOTTOM_CENTER);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        if (imageDirectoryFile.toPath().resolve(newFileFullName).toFile().delete()) {
                            System.out.printf("Removed duplicate file from %s.\n", imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath());
                        } else {
                            System.err.printf("Failed to remove duplicate file from %s.\n", imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath());
                        }
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("Saved New file %s from %s.\n", imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath(), savedFileData.getFile().toPath().toAbsolutePath());
                if (savedFileData.getFile().delete()) {
                    System.out.printf("Removed tmp file from %s.\n", absolutePath);
                } else {
                    System.err.printf("Failed to remove tmp file from %s.\n", absolutePath);
                }
            }
        });
        multiFileUpload.getElement().addEventListener("file-remove", (DomEventListener) e -> {
            String removedFileName = e.getEventData().getString("event.detail.file.name");
            File removedFile = imageFileMap.get(removedFileName);
            if (removedFile != null) {
                if (removedFile.delete()) {
                    System.out.println("Removed file " + removedFileName + " by user");
                } else {
                    System.err.println("Failed to remove file " + removedFileName + " by user");
                }
                imageFileMap.remove(removedFileName);
            }
        }).addEventData("event.detail.file.name");
        // upload non image file (by file extension)
        multiFileUpload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();
            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.BOTTOM_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        uploadLayout.add(multiFileUpload);
        return uploadLayout;
    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        save.setText(translator.getTranslation("save", UI.getCurrent().getLocale()));
        setParameterTitle.setText(translator.getTranslation("Set-up-transform-parameter", UI.getCurrent().getLocale()));
        uploadImageTitle.setText(translator.getTranslation("upload-image-title", UI.getCurrent().getLocale()));
        confirmDialog.setText(translator.getTranslation("Unsaved-changes", UI.getCurrent().getLocale()));
        confirmDialog.setCancelText(translator.getTranslation("cancel", UI.getCurrent().getLocale()));
        confirmDialog.setConfirmText(translator.getTranslation("save", UI.getCurrent().getLocale()));
        confirmDialog.setRejectText(translator.getTranslation("discard", UI.getCurrent().getLocale()));
        colorNumber.setLabel(translator.getTranslation("Color-number", UI.getCurrent().getLocale()));
        colorNumber.setTooltipText(translator.getTranslation("colorNumber-tooltip", UI.getCurrent().getLocale()));
        pixelSize.setLabel(translator.getTranslation("Pixel-size", UI.getCurrent().getLocale()));
        pixelSize.setTooltipText(translator.getTranslation("pixelSize-tooltip", UI.getCurrent().getLocale()));
        smooth.setLabel(translator.getTranslation("Smooth", UI.getCurrent().getLocale()));
        smooth.setTooltipText(translator.getTranslation("smooth-tooltip", UI.getCurrent().getLocale()));
        edgeCrispening.setLabel(translator.getTranslation("Edge-crispening", UI.getCurrent().getLocale()));
        edgeCrispening.setTooltipText(translator.getTranslation("edgeCrispening-tooltip", UI.getCurrent().getLocale()));
        saturation.setLabel(translator.getTranslation("Saturation", UI.getCurrent().getLocale()));
        saturation.setTooltipText(translator.getTranslation("saturation-tooltip", UI.getCurrent().getLocale()));
        contrastRatio.setLabel(translator.getTranslation("Contras-ratio", UI.getCurrent().getLocale()));
        contrastRatio.setTooltipText(translator.getTranslation("contrastRatio-tooltip", UI.getCurrent().getLocale()));
        hint.setText(String.format(translator.getTranslation("upload-multiple-hint", UI.getCurrent().getLocale()), appConfig.getMaxImageSizeInMegaBytes(), appConfig.getMaxImageFiles()));
        dropLabel.setText(translator.getTranslation("upload-notification", UI.getCurrent().getLocale()));
        save.setText(translator.getTranslation("save", UI.getCurrent().getLocale()));
        cancel.setText(translator.getTranslation("cancel", UI.getCurrent().getLocale(), cancel));
        if (localeChangeEvent.getLocale().equals(Translator.LOCALE_ZHT)) {
            multiFileUpload.setI18n(uploadTCI18N);
            smooth.setItems(Smooth.NoneTC, Smooth.WeakTC, Smooth.MediumTC, Smooth.StrongTC);
            smooth.setValue(Smooth.NoneTC);
            edgeCrispening.setItems(EdgeCrispening.NoneTC, EdgeCrispening.WeakTC, EdgeCrispening.StrongTC);
            edgeCrispening.setValue(EdgeCrispening.NoneTC);
        } else {
            multiFileUpload.setI18n(uploadENI18N);
            smooth.setItems(Smooth.NoneEN, Smooth.WeakEN, Smooth.MediumEN, Smooth.StrongEN);
            smooth.setValue(Smooth.NoneEN);
            edgeCrispening.setItems(EdgeCrispening.NoneEN, EdgeCrispening.WeakEN, EdgeCrispening.StrongEN);
            edgeCrispening.setValue(EdgeCrispening.NoneEN);
        }
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        cancel.setText(translator.getTranslation("cancel", UI.getCurrent().getLocale(), cancel));
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        if (!isSaved && !imageFileMap.isEmpty()) {
            BeforeLeaveEvent.ContinueNavigationAction action = beforeLeaveEvent.postpone();
            confirmDialog.setText(translator.getTranslation("Unsaved-changes", UI.getCurrent().getLocale()));
            confirmDialog.setCancelable(true);
            confirmDialog.setCancelText(translator.getTranslation("cancel", UI.getCurrent().getLocale()));
            confirmDialog.setConfirmText(translator.getTranslation("save", UI.getCurrent().getLocale()));
            confirmDialog.setRejectable(true);
            confirmDialog.setRejectText(translator.getTranslation("discard", UI.getCurrent().getLocale()));
            confirmDialog.addRejectListener(e -> {
                if (!imageFileMap.isEmpty()) {
                    for (String s : imageFileMap.keySet()) {
                        if (imageFileMap.get(s).delete()) {
                            System.out.printf("Unsaved image %s have been deleted.\n", s);
                        } else {
                            System.err.printf("Failed to delete image %s.\n", s);
                        }
                    }
                    imageFileMap.clear();
                }
                UI.getCurrent().getPage().reload();
            });
            confirmDialog.addConfirmListener(e -> {
                if (!imageFileMap.isEmpty()) {
                    double imageFileSize = 0.0;
                    for (Map.Entry<String, File> entry : imageFileMap.entrySet()) {
                        Optional<User> maybeUser = authenticatedUser.get();
                        if (maybeUser.isPresent()) {
                            this.user = maybeUser.get();
                            // check if file size achieve file size limit
                            if ((this.user.getImageSize() + (double) (entry.getValue().length() / 1024 / 1024)) < this.user.getImageSizeLimit()) {
                                newImageInfo = new ImageInfo("Pixel Transform", colorNumber.getValue(), pixelSize.getValue(), smooth.getValue().getValue(), edgeCrispening.getValue().getValue(), saturation.getValue(), contrastRatio.getValue(), entry.getValue().getPath(), null, entry.getValue().getName(), null, this.user.getUsername(), entry.getKey());
                                imageService.imageProcess(newImageInfo, this.user);
                                try {
                                    binderImage.writeBean(newImageInfo);
                                    this.imageInfoService.update(newImageInfo);
                                    this.user.setImageSize(this.user.getImageSize() + (double) entry.getValue().length() / 1024 / 1024);
                                    imageFileSize += (double) entry.getValue().length() / 1024 / 1024;
                                    try {
                                        binderUser.writeBean(this.user);
                                        this.userService.update(this.user);
                                    } catch (ValidationException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                } catch (ValidationException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                Notification.show(String.format(translator.getTranslation("reached-images-size-limit", UI.getCurrent().getLocale()), entry.getKey()));
                                break;
                            }
                        }
                    }
                    Notification.show(String.format(translator.getTranslation("saved-upload", UI.getCurrent().getLocale()), imageFileMap.size(), imageFileSize));
                    imageFileMap.clear();
                } else {
                    Notification.show(translator.getTranslation("empty-duplicate-upload-failed", UI.getCurrent().getLocale()));
                }
                isSaved = true;
                clearForm();
            });
            confirmDialog.open();
        }
    }

    public enum Smooth {
        NoneEN(0, "None"), WeakEN(50, "Weak"), MediumEN(100, "Medium"), StrongEN(200, "Strong"), NoneTC(0, "無"), WeakTC(50, "弱"), MediumTC(100, "中"), StrongTC(200, "強");
        final Integer value;
        final String name;

        Smooth(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public static String valueToName(Integer value, String language) {
            String name = null;
            if (value == 0 && language.equals("EN")) {
                name = "None";
            } else if (value == 50) {
                name = "Weak";
            } else if (value == 100) {
                name = "Medium";
            } else if (value == 200) {
                name = "Strong";
            }

            if (value == 0 && language.equals("TC")) {
                name = "無";
            } else if (value == 50) {
                name = "弱";
            } else if (value == 100) {
                name = "中";
            } else if (value == 200) {
                name = "強";
            }
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum EdgeCrispening {
        NoneEN(0, "None"), WeakEN(1, "Weak"), StrongEN(2, "Strong"), NoneTC(0, "無"), WeakTC(1, "弱"), StrongTC(2, "強");
        final Integer value;
        final String name;

        EdgeCrispening(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public static String valueToName(Integer value, String language) {
            String name = null;
            if (value == 0 && language.equals("EN")) {
                name = "None";
            } else if (value == 1) {
                name = "Weak";
            } else if (value == 2) {
                name = "Strong";
            }

            if (value == 0 && language.equals("TC")) {
                name = "無";
            } else if (value == 1) {
                name = "弱";
            } else if (value == 2) {
                name = "強";
            }
            return name;
        }

        public Integer getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // upload TC i18n
    public static class UploadTCI18N extends UploadI18N {
        public UploadTCI18N() {
            setDropFiles(new DropFiles().setOne("將檔案拖曳至此")
                    .setMany("將多個檔案拖曳至此"));
            setAddFiles(new AddFiles().setOne("上傳檔案")
                    .setMany("上傳多個檔案"));
            setError(new Error().setTooManyFiles("超過上傳檔案數量限制")
                    .setFileIsTooBig("超過上傳檔案大小限制")
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

    /*
    // upload image title
    private Component createUploadTitle() {
        return new H3("上傳圖片");
    }
    */
    // upload EN i18n
    public static class UploadENI18N extends UploadI18N {
        public UploadENI18N() {
            setDropFiles(new DropFiles().setOne("Drop file here")
                    .setMany("Drop files here"));
            setAddFiles(new AddFiles().setOne("Upload File...")
                    .setMany("Upload Files..."));
            setError(new Error().setTooManyFiles("Too Many Files.")
                    .setFileIsTooBig("File is Too Big.")
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


}

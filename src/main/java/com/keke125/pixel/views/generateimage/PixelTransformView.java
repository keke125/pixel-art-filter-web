package com.keke125.pixel.views.generateimage;

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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
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

import java.io.*;
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

    // i18n provider
    private static final Translator translator = new Translator();
    private final BeanValidationBinder<User> binderUser;
    private Select<Integer> pixelSize = new Select<>();
    // data input field
    private Select<Integer> colorNumber = new Select<>();
    private Select<Smooth> smooth = new Select<>();
    private Select<Integer> saturation = new Select<>();
    private Select<Integer> contrastRatio = new Select<>();
    private Checkbox isPublic = new Checkbox();
    private Select<EdgeCrispening> edgeCrispening = new Select<>();
    private Button save = new Button("儲存");
    private Map<String, File> imageFileMap = new HashMap<>();
    // binder with Class SampleImage
    private Binder<ImageInfo> binderImage = new Binder<>(ImageInfo.class);
    // button cancel and button save
    private Button cancel = new Button();
    // file buffer and upload
    private MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
    private Upload multiFileUpload = new Upload(multiFileBuffer);
    // setup upload i18n
    private UploadTCI18N uploadTCI18N;
    private UploadENI18N uploadENI18N;
    // inject sample image service
    private ImageInfoService imageInfoService;
    private ImageInfo newImageInfo;

    private AuthenticatedUser authenticatedUser;
    private UserService userService;

    private User user;
    private boolean isSaved;

    public PixelTransformView(ImageInfoService imageInfoService, AuthenticatedUser authenticatedUser, UserService userService, ImageService imageService) {
        this.imageInfoService = imageInfoService;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.isSaved = false;
        binderUser = new BeanValidationBinder<>(User.class);
        addClassName("pixel-transform-view");

        add(createTitle());
        add(createFormLayout());
        add(createUploadTitle());
        add(createUploadLayout());
        add(createButtonLayout());

        //binder.bindInstanceFields(this);
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
                            newImageInfo = new ImageInfo("Pixel Transform", colorNumber.getValue(), pixelSize.getValue(), smooth.getValue().getValue(), edgeCrispening.getValue().getValue(), saturation.getValue(), contrastRatio.getValue(), isPublic.getValue(), entry.getValue().getPath(), null, entry.getValue().getName(), null, this.user.getUsername(), entry.getKey());
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
                            Notification.show(String.format("因為圖片儲存空間已滿，從圖片 %s 開始上傳失敗", entry.getKey()));
                            break;
                        }
                    }
                }
                Notification.show(String.format("已保存圖片及生成參數資訊，共成功上傳%d張圖片，圖片總大小為 %f MB", imageFileMap.size(), imageFileSize));
                imageFileMap.clear();
            } else {
                Notification.show("沒有上傳圖片或重複上傳圖片");
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
            //smooth.setItems(Smooth.NoneTC, Smooth.WeakTC, Smooth.MediumTC, Smooth.StrongTC);
            smooth.setValue(Smooth.NoneTC);
            //edgeCrispening.setItems(EdgeCrispening.NoneTC, EdgeCrispening.WeakTC, EdgeCrispening.StrongTC);
            edgeCrispening.setValue(EdgeCrispening.NoneTC);
        } else {
            //smooth.setItems(Smooth.NoneEN, Smooth.WeakEN, Smooth.MediumEN, Smooth.StrongEN);
            smooth.setValue(Smooth.NoneEN);
            //edgeCrispening.setItems(EdgeCrispening.NoneEN, EdgeCrispening.WeakEN, EdgeCrispening.StrongEN);
            edgeCrispening.setValue(EdgeCrispening.NoneEN);
        }
        saturation.setValue(0);
        contrastRatio.setValue(0);
        isPublic.setValue(false);
    }

    private Component createTitle() {
        return new H3("設定轉換參數");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        // setup select item
        colorNumber.setLabel(translator.getTranslation("colorNumber", UI.getCurrent().getLocale(), colorNumber));
        colorNumber.setItems(2, 4, 8, 16);
        colorNumber.setValue(4);
        colorNumber.setTooltipText("Color Number refers to the number of colors to be reserved after the pixel transform.");
        pixelSize.setLabel("像素大小");
        pixelSize.setItems(1, 2, 3, 4, 5);
        pixelSize.setValue(2);
        pixelSize.setTooltipText("Pixel Size refers to the size of each pixel in an image. If the pixel size is increased, the image will become more blurred.");
        smooth.setLabel("平滑程度");
        smooth.setTooltipText("Smooth will blur certain parts of your image, resulting in a reduction of noise points in the final output.");
        edgeCrispening.setLabel("邊緣銳化");
        edgeCrispening.setTooltipText("Outlines Enhance can help to emphasize objects within an image.");
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
        saturation.setLabel("色度(飽和度)");
        saturation.setItems(-250, -200, -150, -100, -50, 0, 50, 100, 150, 200, 250);
        saturation.setValue(0);
        saturation.setTooltipText("Saturation can help to adjust the light intensity of an image.");
        contrastRatio.setLabel("對比度");
        contrastRatio.setItems(-250, -200, -150, -100, -50, 0, 50, 100, 150, 200, 250);
        contrastRatio.setValue(0);
        contrastRatio.setTooltipText("Contrast Ratio can help to make an image appear more intense or have greater contrast.");
        isPublic.setLabel("公開圖片");
        isPublic.setValue(false);
        isPublic.setTooltipText("By checking this box, the image after the pixel transform can be shared via a link.");
        formLayout.add(colorNumber, pixelSize, smooth, edgeCrispening, saturation, contrastRatio, isPublic);
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
        multiFileUpload.setAcceptedFileTypes("image/bmp", "image/jpeg", "image/webp", "image/x-portable-anymap", "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-pixmap", ".pxm", ".sr", "image/x-cmu-raster", "image/tiff");
        // max 3 files can be uploaded
        int maxFiles = 3;
        multiFileUpload.setMaxFiles(maxFiles);
        // only file size below 10MB can be uploaded
        int maxFileSizeInBytes = 10 * 1024 * 1024;
        int maxFileSizeInMegaBytes = 10;
        multiFileUpload.setMaxFileSize(maxFileSizeInBytes);
        // upload hint message
        Paragraph hint = new Paragraph(
                "單一檔案大小不能超過" + maxFileSizeInMegaBytes + "MB，每次最多上傳" + maxFiles + "張，只能上傳圖片檔");
        add(hint);
        // upload drop label
        Span dropLabel = new Span("檔案將上傳至我們的伺服器，可參考我們的隱私權政策");
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
                        if ((this.user.getImageSize() + (double) savedFileData.getFile().length() / 1024 / 1024) < this.user.getImageSizeLimit()) {
                            System.out.printf("Tmp File saved to: %s.%n", absolutePath);
                        } else {
                            String errorMessage = String.format("因為儲存空間不足，上傳檔案 %s 失敗", uploadFileName);
                            Notification notification = Notification.show(errorMessage, 5000,
                                    Notification.Position.BOTTOM_CENTER);
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            if (savedFileData.getFile().delete()) {
                                System.out.printf("Tmp File has been deleted from %s.%n", absolutePath);
                            } else {
                                System.out.printf("Tmp File has not been deleted from %s.%n", absolutePath);
                            }
                            UI.getCurrent().getPage().reload();
                            return;
                        }
                    }
                } else {
                    String errorMessage = String.format("因為上傳檔案 %s 非圖片，將忽略該檔案", uploadFileName);
                    Notification notification = Notification.show(errorMessage, 5000,
                            Notification.Position.BOTTOM_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    if (savedFileData.getFile().delete()) {
                        System.out.printf("Tmp File has been deleted from %s.%n", absolutePath);
                    } else {
                        System.out.printf("Tmp File has not been deleted from %s.%n", absolutePath);
                    }
                    return;
                }
            } else {
                String errorMessage = String.format("因為無法辨識上傳檔案 %s 類型，將忽略該檔案", uploadFileName);
                Notification notification = Notification.show(errorMessage, 5000,
                        Notification.Position.BOTTOM_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                if (savedFileData.getFile().delete()) {
                    System.out.printf("Tmp File has been deleted from %s.%n", absolutePath);
                } else {
                    System.out.printf("Tmp File has not been deleted from %s.%n", absolutePath);
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
                    System.out.printf("Folder %s has been created.%n", imageDirectoryFile.getAbsolutePath());
                } else {
                    System.out.printf("Failed to create Folder %s.%n", imageDirectoryFile.getAbsolutePath());
                    if (savedFileData.getFile().delete()) {
                        System.out.printf("Tmp File has been deleted from %s.%n", absolutePath);
                    } else {
                        System.out.printf("Tmp File has not been deleted from %s.%n", absolutePath);
                    }
                    return;
                }
            }
            // check image folder privilege
            if (!(imageDirectoryFile.canRead() && imageDirectoryFile.canWrite())) {
                System.out.printf("Don't have privilege to write and read folder %s%n", imageDirectoryFile.getAbsolutePath());
                if (savedFileData.getFile().delete()) {
                    System.out.printf("Tmp File has been deleted from %s.%n", absolutePath);
                } else {
                    System.out.printf("Tmp File has not been deleted from %s.%n", absolutePath);
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
                        String errorMessage = String.format("同時上傳相同檔名 %s 的圖片，將只處理第一次上傳的圖片", uploadFileName);
                        Notification notification = Notification.show(errorMessage, 5000,
                                Notification.Position.BOTTOM_CENTER);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        if (imageDirectoryFile.toPath().resolve(newFileFullName).toFile().delete()) {
                            System.out.printf("Removed duplicate file from %s.%n", imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath());
                        } else {
                            System.out.printf("Failed to remove duplicate file from %s.%n", imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath());
                        }
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("Saved New file %s from %s.%n", imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath(), savedFileData.getFile().toPath().toAbsolutePath());
                if (savedFileData.getFile().delete()) {
                    System.out.printf("Removed tmp file from %s.%n", absolutePath);
                } else {
                    System.out.printf("Failed to remove tmp file from %s.%n", absolutePath);
                }
            }
        });
        multiFileUpload.getElement().addEventListener("file-remove", (DomEventListener) e -> {
            String removedFileName = e.getEventData().getString("event.detail.file.name");
            File removedFile = imageFileMap.get(removedFileName);
            if (removedFile.delete()) {
                System.out.println("Removed file " + removedFileName + " by user");
            } else {
                System.out.println("Failed to remove file " + removedFileName + " by user");
            }
            imageFileMap.remove(removedFileName);
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
        colorNumber.setLabel(translator.getTranslation("colorNumber", UI.getCurrent().getLocale(), colorNumber));
        cancel.setText(translator.getTranslation("cancelButton", UI.getCurrent().getLocale(), cancel));
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
        cancel.setText(translator.getTranslation("cancelButton", UI.getCurrent().getLocale(), cancel));
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        BeforeLeaveEvent.ContinueNavigationAction action =
                beforeLeaveEvent.postpone();
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setText("尚未儲存圖片及參數，是否離開?");
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("留在此頁");
        confirmDialog.setConfirmText("確認離開");
        confirmDialog.addConfirmListener(e -> {
                    for (String s : imageFileMap.keySet()) {
                        if (imageFileMap.get(s).delete()) {
                            System.out.printf("Unsaved image %s have been deleted.%n", s);
                        } else {
                            System.out.printf("Failed to delete image %s.%n", s);
                        }

                        imageFileMap.clear();
                    }
                    action.proceed();
                }
        );
        if (!isSaved && !imageFileMap.isEmpty()) {
            confirmDialog.open();
        } else {
            confirmDialog.close();
            action.proceed();
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

        public static String valueToName(Integer value) {
            String name = null;
            if (value == 0) {
                name = "None";
            } else if (value == 50) {
                name = "Weak";
            } else if (value == 100) {
                name = "Medium";
            } else if (value == 200) {
                name = "Strong";
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

        public static String valueToName(Integer value) {
            String name = null;
            if (value == 0) {
                name = "None";
            } else if (value == 1) {
                name = "Weak";
            } else if (value == 2) {
                name = "Strong";
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

    // upload image title
    private Component createUploadTitle() {
        return new H3("上傳圖片");
    }

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

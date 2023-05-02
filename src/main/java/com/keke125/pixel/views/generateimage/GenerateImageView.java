package com.keke125.pixel.views.generateimage;

import com.keke125.pixel.data.entity.ImageFile;
import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.SampleImage;
import com.keke125.pixel.data.service.SampleImageService;
import com.keke125.pixel.views.MainLayout;
import com.keke125.pixel.views.Translator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;

@PageTitle("Generate Image")
@Route(value = "generate-image", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class GenerateImageView extends Div implements LocaleChangeObserver {

    // i18n provider
    private static final Translator translator = new Translator();
    // private String title;
    private Select<Integer> pixelSize = new Select<>();
    private Select<String> smooth = new Select<>();
    private Select<String> edgeCrispening = new Select<>();
    private Select<Integer> saturation = new Select<>();
    private Select<Integer> contrastRatio = new Select<>();
    private Checkbox isPublic = new Checkbox();
    private ImageFile newImageFile;
    // data input field
    private Select<Integer> colorNumber = new Select<>();
    private Button save = new Button("儲存");

    // binder with Class SampleImage
    private Binder<SampleImage> binder = new Binder<>(SampleImage.class);
    // button cancel and button save
    private Button cancel = new Button();
    // file buffer and upload
    private MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
    private Upload multiFileUpload = new Upload(multiFileBuffer);
    // setup upload i18n
    private UploadTCI18N uploadTCI18N;
    private UploadENI18N uploadENI18N;
    // inject sample image service
    private SampleImageService sampleImageService;

    public GenerateImageView(SampleImageService sampleImageService) {
        addClassName("generate-image-view");

        add(createTitle());
        add(createFormLayout());
        add(createUploadTitle());
        add(createUploadLayout());
        add(createButtonLayout());

        binder.bindInstanceFields(this);
        clearForm();

        cancel.addClickListener(e -> clearForm());
        save.addClickListener(e -> {
            sampleImageService.update(binder.getBean());
            Notification.show("已保存圖片生成參數資訊");
            ImageInfo newImageInfo = new ImageInfo(colorNumber.getValue(), pixelSize.getValue(), smooth.getValue(), edgeCrispening.getValue(), saturation.getValue(), contrastRatio.getValue(), isPublic.getValue(), newImageFile);
            clearForm();
        });
    }

    private void clearForm() {
        binder.setBean(new SampleImage());
        // default image configure
        colorNumber.setValue(4);
        pixelSize.setValue(2);
        smooth.setValue("無");
        edgeCrispening.setValue("無");
        saturation.setValue(0);
        contrastRatio.setValue(0);
        isPublic.setValue(false);
    }

    private Component createTitle() {
        return new H3("生成圖片");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        // setup select item
        colorNumber.setLabel(translator.getTranslation("colorNumber", UI.getCurrent().getLocale(), colorNumber));
        colorNumber.setItems(2, 4, 8, 16);
        colorNumber.setValue(4);
        pixelSize.setLabel("像素大小");
        pixelSize.setItems(1, 2, 3, 4, 5);
        pixelSize.setValue(2);
        smooth.setLabel("平滑程度");
        smooth.setItems("無", "弱", "中", "強");
        smooth.setValue("無");
        edgeCrispening.setLabel("邊緣銳化");
        edgeCrispening.setItems("無", "弱", "強");
        edgeCrispening.setValue("無");
        saturation.setLabel("色度(飽和度)");
        saturation.setItems(-250, -200, -150, -100, -50, 0, 50, 100, 150, 200, 250);
        saturation.setValue(0);
        contrastRatio.setLabel("對比度");
        contrastRatio.setItems(-250, -200, -150, -100, -50, 0, 50, 100, 150, 200, 250);
        contrastRatio.setValue(0);
        isPublic.setLabel("公開圖片");
        isPublic.setValue(false);
        formLayout.add(colorNumber, pixelSize, smooth, edgeCrispening, saturation, contrastRatio, isPublic);
        return formLayout;

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
        multiFileUpload.setAcceptedFileTypes("image/*");
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
                    System.out.printf("Tmp File saved to: %s.%n", absolutePath);
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
            }
            // time stamp for distinguishing different files
            Instant instantNow = Instant.now();
            // get current directory
            Path workingDirectoryPath = Paths.get("");
            // image directory = workingDirectory + image
            // ex C:\path\to\your\app\image
            File imageDirectoryFile = new File(workingDirectoryPath.toAbsolutePath() + File.separator + "image");
            String newFileName = instantNow + "-" + uploadFileName;
            // check if image folder exists
            if (!imageDirectoryFile.exists()) {
                if (imageDirectoryFile.mkdir()) {
                    System.out.printf("Folder %s has been created.%n", imageDirectoryFile.getAbsolutePath());
                } else {
                    System.out.printf("Failed to create Folder %s.%n", imageDirectoryFile.getAbsolutePath());
                    return;
                }
            }
            // check image folder privilege
            if (!(imageDirectoryFile.canRead() && imageDirectoryFile.canWrite())) {
                System.out.printf("Don't have privilege to write and read folder %s%n", imageDirectoryFile.getAbsolutePath());
                return;
            }
            // if real file type is image
            // copy image from tmp folder to specific folder and rename image name
            if (savedFileData.getFile().exists() && savedFileData.getFile().canRead()) {
                String newFileNameHashed = DigestUtils.sha256Hex(newFileName);
                String newFileFullName = newFileNameHashed + "." + FilenameUtils.getExtension(uploadFileName);
                try {
                    Files.copy(savedFileData.getFile().toPath(), imageDirectoryFile.toPath().resolve(newFileFullName), StandardCopyOption.REPLACE_EXISTING);
                    newImageFile = new ImageFile(imageDirectoryFile.toPath().resolve(newFileFullName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("Saved New file %s from %s.%n", imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath(), savedFileData.getFile().toPath().toAbsolutePath());
                if (savedFileData.getFile().delete()) {
                    System.out.printf("Removed tmp file from %s.%n", absolutePath);
                } else {
                    System.out.printf("Failed to remove tmp file from %s.%n", absolutePath);
                }
            } else {
                System.out.printf("Failed to get upload data in tmp folder%n");
            }
        });
        // upload non image file
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
        } else {
            multiFileUpload.setI18n(uploadENI18N);
        }
    }

    // add image to queue
    private void addImageToQueue(SampleImage entity) {
        sampleImageService.save(entity);
    }

    // upload i18n
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

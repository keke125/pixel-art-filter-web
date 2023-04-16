package com.keke125.pixel.views.generateimage;

import com.keke125.pixel.data.entity.SampleImage;
import com.keke125.pixel.data.service.SampleImageService;
import com.keke125.pixel.views.MainLayout;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Arrays;

@PageTitle("Generate Image")
@Route(value = "generate-image", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class GenerateImageView extends Div {

    // data input field
    private Select<Integer> colorDepth = new Select<>();
    private Select<Integer> pixelSize = new Select<>();
    private Select<String> smooth = new Select<>();
    private Select<String> edgeCrispening = new Select<>();
    private Select<Integer> saturation = new Select<>();
    private Select<Integer> contrastRatio = new Select<>();
    private Checkbox isPublic = new Checkbox();

    // button cancel and button save
    private Button cancel = new Button("預設配置");
    private Button save = new Button("儲存");

    // binder with Class SampleImage
    private Binder<SampleImage> binder = new Binder<>(SampleImage.class);

    // file buffer and upload
    private  MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
    private Upload multiFileUpload = new Upload(multiFileBuffer);

    public GenerateImageView(SampleImageService imageService) {
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
            imageService.update(binder.getBean());
            Notification.show("已保存圖片生成參數資訊");
            clearForm();
        });
    }

    private void clearForm() {
        binder.setBean(new SampleImage());
        // default image configure
        colorDepth.setValue(4);
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
        colorDepth.setLabel("色深");
        colorDepth.setItems(2,4,8,16);
        colorDepth.setValue(4);
        pixelSize.setLabel("像素大小");
        pixelSize.setItems(1,2,3,4,5);
        pixelSize.setValue(2);
        smooth.setLabel("平滑程度");
        smooth.setItems("無","弱","中","強");
        smooth.setValue("無");
        edgeCrispening.setLabel("邊緣銳化");
        edgeCrispening.setItems("無","弱","強");
        edgeCrispening.setValue("無");
        saturation.setLabel("色度(飽和度)");
        saturation.setItems(-250,-200,-150,-100,-50,0,50,100,150,200,250);
        saturation.setValue(0);
        contrastRatio.setLabel("對比度");
        contrastRatio.setItems(-250,-200,-150,-100,-50,0,50,100,150,200,250);
        contrastRatio.setValue(0);
        isPublic.setLabel("公開圖片");
        isPublic.setValue(false);
        formLayout.add(colorDepth, pixelSize, smooth, edgeCrispening, saturation,contrastRatio,isPublic);
        return formLayout;

    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

    private Component createUploadLayout(){
        HorizontalLayout uploadLayout = new HorizontalLayout();
        uploadLayout.addClassName("upload-layout");
        // setup upload i18n
        UploadTCI18N uploadTCI18N = new UploadTCI18N();
        multiFileUpload.setI18n(uploadTCI18N);
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
                "單一檔案大小不能超過"+maxFileSizeInMegaBytes+"MB，每次最多上傳"+maxFiles+"張，只能上傳圖片檔");
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
            String absolutePath = savedFileData.getFile().getAbsolutePath();

            System.out.printf("File saved to: %s%n", absolutePath);
        });
        // upload non image file
        multiFileUpload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.BOTTOM_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        uploadLayout.add(multiFileUpload);
        return  uploadLayout;
    }

    // upload i18n
    public class UploadTCI18N extends UploadI18N {
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
}
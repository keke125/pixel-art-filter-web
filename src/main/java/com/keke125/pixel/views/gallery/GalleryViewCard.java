package com.keke125.pixel.views.gallery;

import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.ImageService;
import com.keke125.pixel.views.generateimage.PixelTransformView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GalleryViewCard extends ListItem {

    public GalleryViewCard(ImageService imageService, ImageInfo imageInfo, User user) {
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, AlignItems.START, Padding.MEDIUM,
                BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Overflow.HIDDEN, BorderRadius.MEDIUM, Width.FULL);
        div.setHeight("160px");

        // read generated image
        File generatedFile = new File(imageInfo.getImageNewFile());
        // create stream resource from image file bytes
        StreamResource generatedResource;
        if (!FilenameUtils.getExtension(imageInfo.getUploadImageName()).equals(FilenameUtils.getExtension(generatedFile.getName()))) {
            generatedResource = new StreamResource("generated-" + FilenameUtils.removeExtension(imageInfo.getUploadImageName()) + ".jpg", () -> {
                try {
                    return new ByteArrayInputStream(Files.readAllBytes(generatedFile.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            generatedResource = new StreamResource("generated-" + imageInfo.getUploadImageName(), () -> {
                try {
                    return new ByteArrayInputStream(Files.readAllBytes(generatedFile.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // read generated image
        File originalFile = new File(imageInfo.getImageOriginalFile());
        // create stream resource from image file bytes
        StreamResource originalResource;
        originalResource = new StreamResource("original-" + imageInfo.getUploadImageName(), () -> {
            try {
                return new ByteArrayInputStream(Files.readAllBytes(originalFile.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // create image component by stream resource
        Image image = new Image(generatedResource, "generated-" + imageInfo.getUploadImageName());
        image.setWidth("100%");
        // add to div
        div.add(image);
        // gallery card header
        Span header = new Span();
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
        header.setText(imageInfo.getUploadImageName());

        // gallery card badge
        Span badge = new Span();
        badge.getElement().setAttribute("theme", "badge");
        badge.setText(imageInfo.getFilterType());

        // show image detail info
        Dialog detailDialog = new Dialog();
        detailDialog.setModal(false);
        detailDialog.setHeaderTitle("Image Info");
        detailDialog.setDraggable(true);
        detailDialog.setResizable(true);

        // click to show detail dialog
        Button detailButton = new Button("View Details");
        detailButton.addClickListener(clickEvent -> detailDialog.open());

        // show original image and generated image
        TabSheet imagesTabs = new TabSheet();
        imagesTabs.add("Original", new Div(new Image(originalResource, "original-" + imageInfo.getUploadImageName())));
        imagesTabs.add("Generated", new Div(new Image(generatedResource, "generated-" + imageInfo.getUploadImageName())));
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(imagesTabs);
        detailDialog.add(dialogLayout);

        // show image transform parameter
        Span colorNumber = new Span("Color number: " + imageInfo.getColorNumber());
        Span pixelSize = new Span("Pixel size: " + imageInfo.getPixelSize());
        Span smooth = new Span("Smooth: " + PixelTransformView.Smooth.valueToName(imageInfo.getSmooth()));
        Span edgeCrispening = new Span("Edge crispening: " + PixelTransformView.EdgeCrispening.valueToName(imageInfo.getEdgeCrispening()));
        Span saturation = new Span("Saturation: " + imageInfo.getSaturation());
        Span contrastRatio = new Span("Contras ratio: " + imageInfo.getContrastRatio());
        VerticalLayout parameterLayout = new VerticalLayout(colorNumber, pixelSize, smooth, edgeCrispening, saturation, contrastRatio);
        parameterLayout.setSpacing(false);
        parameterLayout.setPadding(false);
        Details parameterDetails = new Details("Image transform parameter", parameterLayout);
        detailDialog.add(parameterDetails);

        // download original
        Anchor downloadOriginal = new Anchor(originalResource, "Download Original");
        downloadOriginal.getElement().setAttribute("download", true);
        downloadOriginal.removeAll();
        downloadOriginal.add(new Button("Download Original"));
        detailDialog.getFooter().add(downloadOriginal);

        // download generated
        Anchor downloadGenerated = new Anchor(generatedResource, "Download Generated");
        downloadGenerated.getElement().setAttribute("download", true);
        downloadGenerated.removeAll();
        downloadGenerated.add(new Button("Download Generated"));
        detailDialog.getFooter().add(downloadGenerated);

        // close detail dialog
        Button closeButton = new Button("Close", e -> detailDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        detailDialog.getHeader().add(closeButton);


        // delete image button
        Button deleteButton = new Button("Delete");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(clickEvent -> {
            imageService.deleteImageInfo(imageInfo, user);
            UI.getCurrent().getPage().reload();
        });

        add(div, header, badge, detailButton, deleteButton, detailDialog);
    }
}

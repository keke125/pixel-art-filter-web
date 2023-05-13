package com.keke125.pixel.views.gallery;

import com.keke125.pixel.data.entity.ImageInfo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GalleryViewCard extends ListItem {

    public GalleryViewCard(ImageInfo imageInfo) {
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
        generatedResource = new StreamResource("generated-" + imageInfo.getUploadImageName(), () -> {
            try {
                return new ByteArrayInputStream(Files.readAllBytes(generatedFile.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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

        // close detail dialog
        Button closeButton = new Button("Close", e -> detailDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        detailDialog.getHeader().add(closeButton);

        add(div, header, badge, detailButton, detailDialog);
    }
}

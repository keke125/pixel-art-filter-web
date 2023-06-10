package com.keke125.pixel.views.gallery;

import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.ImageService;
import com.keke125.pixel.views.Translator;
import com.keke125.pixel.views.generateimage.PixelTransformView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
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

public class GalleryViewCard extends ListItem implements LocaleChangeObserver {

    private static final Translator translator = new Translator();

    private final ImageInfo imageInfo;

    private final Dialog detailDialog;

    private final Button detailButton;

    private final TabSheet imagesTabs;

    private final Span colorNumber;

    private final Span pixelSize;

    private final Span smooth;

    private final Span edgeCrispening;

    private final Span saturation;

    private final Span contrastRatio;

    private final Details parameterDetails;

    private final Anchor downloadOriginal;

    private final Anchor downloadGenerated;

    private final Button deleteButton;

    private final Button closeButton;

    public GalleryViewCard(ImageService imageService, ImageInfo imageInfo,
                           User user) {
        this.imageInfo = imageInfo;
        addClassNames(Background.CONTRAST_5, Display.FLEX,
                FlexDirection.COLUMN, AlignItems.START, Padding.MEDIUM,
                BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX,
                AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Overflow.HIDDEN, BorderRadius.MEDIUM,
                Width.FULL);
        div.setHeight("160px");

        // read generated image
        File generatedFile = new File(imageInfo.getImageNewFile());
        // create stream resource from image file bytes
        StreamResource generatedResource;
        if (!FilenameUtils.getExtension
                (imageInfo.getUploadImageName()).equals
                (FilenameUtils.getExtension(generatedFile.getName()))) {
            generatedResource =
                    new StreamResource("generated-" +
                            FilenameUtils.removeExtension
                                    (imageInfo.getUploadImageName()) + ".jpg"
                            , () -> {
                        try {
                            return new ByteArrayInputStream
                                    (Files.readAllBytes(generatedFile.toPath()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } else {
            generatedResource =
                    new StreamResource("generated-" +
                            imageInfo.getUploadImageName(), () -> {
                        try {
                            return new ByteArrayInputStream
                                    (Files.readAllBytes(generatedFile.toPath()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        // read generated image
        File originalFile = new File(imageInfo.getImageOriginalFile());
        // create stream resource from image file bytes
        StreamResource originalResource;
        originalResource =
                new StreamResource("original-" +
                        imageInfo.getUploadImageName(), () -> {
                    try {
                        return new ByteArrayInputStream
                                (Files.readAllBytes(originalFile.toPath()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        // create image component by stream resource
        Image image = new Image(generatedResource,
                "generated-" + imageInfo.getUploadImageName());
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
        detailDialog = new Dialog();
        detailDialog.setModal(false);
        detailDialog.setHeaderTitle(translator.getTranslation("Image-Info",
                UI.getCurrent().getLocale()));
        detailDialog.setDraggable(true);
        detailDialog.setResizable(true);

        // click to show detail dialog
        detailButton = new Button(translator.getTranslation("View-Details",
                UI.getCurrent().getLocale()));
        detailButton.addClickListener(clickEvent -> detailDialog.open());

        // show original image and generated image
        imagesTabs = new TabSheet();
        imagesTabs.add(translator.getTranslation("Original",
                        UI.getCurrent().getLocale()),
                new Div(new Image(originalResource,
                        "original-" + imageInfo.getUploadImageName())));
        imagesTabs.add(translator.getTranslation("Generated",
                        UI.getCurrent().getLocale()),
                new Div(new Image(generatedResource,
                        "generated-" + imageInfo.getUploadImageName())));
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(imagesTabs);
        detailDialog.add(dialogLayout);

        // show image transform parameter
        colorNumber = new Span(translator.getTranslation("Color-number",
                UI.getCurrent().getLocale()) + imageInfo.getColorNumber() +
                ": ");
        pixelSize = new Span(translator.getTranslation("Pixel-size",
                UI.getCurrent().getLocale()) + imageInfo.getPixelSize() + ": ");
        if (getLocale().equals(Translator.LOCALE_ZHT)) {
            smooth = new Span(translator.getTranslation("Smooth",
                    UI.getCurrent().getLocale()) +
                    PixelTransformView.Smooth.valueToName(imageInfo.getSmooth(),
                            "TC") + ": ");
            edgeCrispening = new Span(translator.getTranslation("Edge" +
                    "-crispening", UI.getCurrent().getLocale()) +
                    PixelTransformView.EdgeCrispening.valueToName
                            (imageInfo.getEdgeCrispening(), "TC") + ": ");
        } else {
            smooth = new Span(translator.getTranslation("Smooth",
                    UI.getCurrent().getLocale()) +
                    PixelTransformView.Smooth.valueToName
                            (imageInfo.getSmooth(), "EN") + ": ");
            edgeCrispening = new Span(translator.getTranslation("Edge" +
                    "-crispening", UI.getCurrent().getLocale()) +
                    PixelTransformView.EdgeCrispening.valueToName
                            (imageInfo.getEdgeCrispening(), "EN") + ": ");
        }
        saturation = new Span(translator.getTranslation("Saturation",
                UI.getCurrent().getLocale()) + imageInfo.getSaturation() + ":" +
                " ");
        contrastRatio = new Span(translator.getTranslation("Contras-ratio",
                UI.getCurrent().getLocale()) +
                imageInfo.getContrastRatio() + ": ");
        VerticalLayout parameterLayout = new VerticalLayout(colorNumber,
                pixelSize, smooth, edgeCrispening, saturation, contrastRatio);
        parameterLayout.setSpacing(false);
        parameterLayout.setPadding(false);
        parameterDetails = new Details(translator.getTranslation("Image" +
                "-transform-parameter", UI.getCurrent().getLocale()),
                parameterLayout);
        detailDialog.add(parameterDetails);

        // download original
        downloadOriginal = new Anchor(originalResource,
                translator.getTranslation("Download-Original",
                        UI.getCurrent().getLocale()));
        downloadOriginal.getElement().setAttribute("download", true);
        downloadOriginal.removeAll();
        downloadOriginal.add(new Button(translator.getTranslation("Download" +
                "-Original", UI.getCurrent().getLocale())));
        detailDialog.getFooter().add(downloadOriginal);

        // download generated
        downloadGenerated = new Anchor(generatedResource,
                translator.getTranslation("Download-Generated",
                        UI.getCurrent().getLocale()));
        downloadGenerated.getElement().setAttribute("download", true);
        downloadGenerated.removeAll();
        downloadGenerated.add(new Button(translator.getTranslation("Download" +
                "-Generated", UI.getCurrent().getLocale())));
        detailDialog.getFooter().add(downloadGenerated);

        // close detail dialog
        closeButton = new Button(translator.getTranslation("Close",
                UI.getCurrent().getLocale()), e -> detailDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        detailDialog.getHeader().add(closeButton);


        // delete image button
        deleteButton = new Button(translator.getTranslation("Delete",
                UI.getCurrent().getLocale()));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(clickEvent -> {
            imageService.deleteImageInfo(imageInfo, user);
            UI.getCurrent().getPage().reload();
        });

        add(div, header, badge, detailButton, deleteButton, detailDialog);
    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        detailDialog.setHeaderTitle(translator.getTranslation("Image-Info",
                UI.getCurrent().getLocale()));
        detailButton.setText(translator.getTranslation("View-Details",
                UI.getCurrent().getLocale()));
        imagesTabs.getTabAt(0).setLabel(translator.getTranslation
                ("Original",
                        UI.getCurrent().getLocale()));
        imagesTabs.getTabAt(1).setLabel(translator.getTranslation
                ("Generated"
                        , UI.getCurrent().getLocale()));
        colorNumber.setText(translator.getTranslation("Color-number",
                UI.getCurrent().getLocale()) + ": " + imageInfo.getColorNumber());
        pixelSize.setText(translator.getTranslation("Pixel-size",
                UI.getCurrent().getLocale()) + ": " + imageInfo.getPixelSize());
        if (getLocale().equals(Translator.LOCALE_ZHT)) {
            smooth.setText(translator.getTranslation("Smooth",
                    UI.getCurrent().getLocale()) + ": " +
                    PixelTransformView.Smooth.valueToName
                            (imageInfo.getSmooth(), "TC"));
            edgeCrispening.setText(translator.getTranslation("Edge-crispening"
                    , UI.getCurrent().getLocale()) + ": " +
                    PixelTransformView.EdgeCrispening.valueToName
                            (imageInfo.getEdgeCrispening(), "TC"));
        } else {
            smooth.setText(translator.getTranslation("Smooth",
                    UI.getCurrent().getLocale()) + ": " +
                    PixelTransformView.Smooth.valueToName
                            (imageInfo.getSmooth(), "EN"));
            edgeCrispening.setText(translator.getTranslation("Edge-crispening"
                    , UI.getCurrent().getLocale()) + ": " +
                    PixelTransformView.EdgeCrispening.valueToName
                            (imageInfo.getEdgeCrispening(), "EN"));
        }
        saturation.setText(translator.getTranslation("Saturation",
                UI.getCurrent().getLocale()) + ": " +
                imageInfo.getSaturation());
        contrastRatio.setText(translator.getTranslation("Contras-ratio",
                UI.getCurrent().getLocale()) + ": " +
                imageInfo.getContrastRatio());
        parameterDetails.setSummaryText(translator.getTranslation("Image" +
                "-transform-parameter", UI.getCurrent().getLocale()));
        downloadOriginal.removeAll();
        downloadOriginal.add(new Button(translator.getTranslation("Download" +
                "-Original", UI.getCurrent().getLocale())));
        detailDialog.getFooter().add(downloadOriginal);
        downloadGenerated.removeAll();
        downloadGenerated.add(new Button(translator.getTranslation("Download" +
                "-Generated", UI.getCurrent().getLocale())));
        detailDialog.getFooter().add(downloadGenerated);
        deleteButton.setText(translator.getTranslation("Delete",
                UI.getCurrent().getLocale()));
        closeButton.setText(translator.getTranslation("Close",
                UI.getCurrent().getLocale()));
    }
}

package com.keke125.pixel.views.gallery;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
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

public class GalleryViewCard extends ListItem {

    public GalleryViewCard(String originalPath, String generatedPath, String text, String filterType) {
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, AlignItems.START, Padding.MEDIUM,
                BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Overflow.HIDDEN, BorderRadius.MEDIUM, Width.FULL);
        div.setHeight("160px");

        Image image = new Image(generatedPath, "generated-" + text);
        image.setWidth("100%");

        div.add(image);

        // gallery card header
        Span header = new Span();
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
        header.setText(text);

        // gallery card badge
        Span badge = new Span();
        badge.getElement().setAttribute("theme", "badge");
        badge.setText(filterType);

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
        imagesTabs.add("Original", new Div(new Image(originalPath, "original-" + text)));
        imagesTabs.add("Generated", new Div(new Image(generatedPath, "generated-" + text)));
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

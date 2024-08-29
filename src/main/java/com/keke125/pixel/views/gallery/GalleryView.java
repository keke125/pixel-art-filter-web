package com.keke125.pixel.views.gallery;

import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.ImageService;
import com.keke125.pixel.security.AuthenticatedUser;
import com.keke125.pixel.views.MainLayout;
import com.keke125.pixel.views.Translator;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;

@PageTitle("Gallery")
@Route(value = "gallery", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("USER")
public class GalleryView extends Main implements HasComponents, HasStyle,
        LocaleChangeObserver {

    private static final Translator translator = new Translator();

    private OrderedList imageContainer;

    private User user;

    private H3 header;

    private NativeLabel imageSize;

    private NativeLabel imageSizeLimit;

    public GalleryView(ImageService imageService,
                       AuthenticatedUser authenticatedUser) {
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            this.user = maybeUser.get();
            constructUI();
            List<ImageInfo> imageInfoList =
                    imageService.findAllImageInfosByOwnerName(this.user.getUsername());
            if (!imageInfoList.isEmpty()) {
                for (ImageInfo i : imageInfoList) {
                    imageContainer.add(new GalleryViewCard(imageService, i,
                            user));
                }
            }
        }
    }

    private void constructUI() {
        addClassNames("gallery-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO,
                Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        header = new H3(translator.getTranslation("Generated-images",
                UI.getCurrent().getLocale()));
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.XLARGE,
                FontSize.XXXLARGE);
        headerContainer.add(header);

        imageSize = new NativeLabel(translator.getTranslation("Image-size",
                UI.getCurrent().getLocale()) + ": " + String.format("%sMB",
                Math.round(user.getImageSize())));
        imageSizeLimit = new NativeLabel(translator.getTranslation("Image-size" +
                "-limit", UI.getCurrent().getLocale()) + ": " + String.format
                ("%sMB", Math.round(user.getImageSizeLimit())));

        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.MEDIUM, Display.GRID,
                ListStyleType.NONE, Margin.NONE, Padding.NONE);

        container.add(headerContainer, imageSize, imageSizeLimit);
        add(container, imageContainer);

    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        header.setText(translator.getTranslation("Generated-images",
                UI.getCurrent().getLocale()));
        imageSize.setText(translator.getTranslation("Image-size",
                UI.getCurrent().getLocale()) + ": " + String.format("%sMB",
                Math.round(user.getImageSize())));
        imageSizeLimit.setText(translator.getTranslation("Image-size-limit",
                UI.getCurrent().getLocale()) + ": " + String.format("%sMB",
                Math.round(user.getImageSizeLimit())));
    }
}

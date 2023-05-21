package com.keke125.pixel.views.gallery;

import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;
import com.keke125.pixel.data.service.ImageService;
import com.keke125.pixel.security.AuthenticatedUser;
import com.keke125.pixel.views.MainLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
public class GalleryView extends Main implements HasComponents, HasStyle {

    private OrderedList imageContainer;

    private final AuthenticatedUser authenticatedUser;
    private User user;

    public GalleryView(ImageService imageService, AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        Optional<User> maybeUser = this.authenticatedUser.get();
        if (maybeUser.isPresent()) {
            this.user = maybeUser.get();
            constructUI();
            List<ImageInfo> imageInfoList = imageService.findAllImageInfosByOwnerName(this.user.getUsername());
            if (!imageInfoList.isEmpty()) {
                for (ImageInfo i : imageInfoList) {
                    imageContainer.add(new GalleryViewCard(imageService, i, user));
                }
            }
        }
    }

    private void constructUI() {
        addClassNames("gallery-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        H2 header = new H2("Generated images");
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.XLARGE, FontSize.XXXLARGE);
        headerContainer.add(header);

        Label imageSize = new Label(String.format("當前容量: %sMB", Math.round(user.getImageSize())));
        Label imageSizeLimit = new Label(String.format("容量限制: %sMB", Math.round(user.getImageSizeLimit())));

        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.MEDIUM, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);

        container.add(headerContainer, imageSize, imageSizeLimit);
        add(container, imageContainer);

    }
}

package com.keke125.pixel.views;

import com.vaadin.flow.i18n.I18NProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

@Component
public class Translator implements I18NProvider {

    public static final String BUNDLE_PREFIX = "translate";

    public static final Locale LOCALE_ZHT = new Locale("zh", "TW", "繁體中文(臺灣)");
    // public static final Locale LOCALE_EN = new Locale("en", "US","English(US)");

    public static final Locale English = new Locale("en");

    private List<Locale> locales = List.of(English, LOCALE_ZHT);

    private Map<String, ResourceBundle> localeMap = new HashMap<>();

    @Override
    public List<Locale> getProvidedLocales() {
        return locales;
    }

    @Override
    public String getTranslation(String s, Locale locale, Object... objects) {
        // input empty string
        if (s == null) {
            LoggerFactory.getLogger(Translator.class.getName())
                    .warn("Got lang request for key with null value!");
            return "";
        }

        final ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_PREFIX, locale);
        localeMap.put(locale.getLanguage(), resourceBundle);

        String value = null;
        try {
            value = localeMap.get(locale.getLanguage()).getString(s);
        } catch (final MissingResourceException e) {
            // can't get resource
            LoggerFactory.getLogger(Translator.class.getName())
                    .warn("Missing resource", e);
            return "!" + locale.getLanguage() + ": " + s;
        }
        if (objects.length > 0) {
            value = MessageFormat.format(value, objects);
        }
        return value;
    }

}
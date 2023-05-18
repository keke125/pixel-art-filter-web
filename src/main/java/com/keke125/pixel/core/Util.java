package com.keke125.pixel.core;

import com.keke125.pixel.Application;
import org.springframework.boot.system.ApplicationHome;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Util {
    public static ArrayList<String> acceptedImageFormat = new ArrayList<>(Arrays.asList("image/bmp", "image/jpeg", "image/png"));
    public static Path getRootPath() {
        ApplicationHome applicationHome = new ApplicationHome(Application.class);
        Path jarParentPath = applicationHome.getDir().toPath();
        String jarPath = applicationHome.getSource().getAbsolutePath();
        Path devPath = Paths.get("").toAbsolutePath();
        if (jarPath.endsWith(".jar")) {
            return jarParentPath;
        } else {
            return devPath;
        }
    }
}
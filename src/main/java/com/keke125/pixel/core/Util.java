package com.keke125.pixel.core;

import com.keke125.pixel.Application;
import com.keke125.pixel.data.entity.ImageInfo;
import org.springframework.boot.system.ApplicationHome;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
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
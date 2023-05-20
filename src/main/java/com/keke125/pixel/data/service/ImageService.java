package com.keke125.pixel.data.service;

import com.keke125.pixel.core.PixelTransform;
import com.keke125.pixel.core.Util;
import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static com.keke125.pixel.core.Util.acceptedImageFormat;

@Service
public class ImageService {
    private final UserRepository userRepository;
    private final ImageInfoRepository imageInfoRepository;

    private final ImageInfoService imageInfoService;

    // binder with Class SampleImage
    private final Binder<ImageInfo> binderImage = new Binder<>(ImageInfo.class);

    public ImageService(UserRepository userRepository, ImageInfoRepository imageInfoRepository, ImageInfoService imageInfoService) {
        this.userRepository = userRepository;
        this.imageInfoRepository = imageInfoRepository;
        this.imageInfoService = imageInfoService;
    }

    public List<User> findAllUsers(String username) {
        if (username == null || username.isEmpty()) {
            return userRepository.findAll();
        } else {
            return userRepository.findAllByUsername(username);
        }
    }

    public long countUsers() {
        return userRepository.count();
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public void saveUser(User user) {
        if (user == null) {
            System.out.println("User is null. Are you sure you have connected your form to the application?");
            return;
        }
        userRepository.save(user);
    }

    public List<ImageInfo> findAllImageInfosByOwnerName(String OwnerName) {
        if (OwnerName == null || OwnerName.isEmpty()) {
            return imageInfoRepository.findAll();
        } else {
            return imageInfoRepository.findAllByOwnerName(OwnerName);
        }
    }

    public long countImageInfos() {
        return imageInfoRepository.count();
    }

    public long countImageInfosByOwnerName(String OwnerName) {
        return imageInfoRepository.count();
    }

    public void deleteImageInfo(ImageInfo imageInfo) {
        imageInfoRepository.delete(imageInfo);
    }

    public void saveImageInfo(ImageInfo imageInfo) {
        if (imageInfo == null) {
            System.out.println("ImageInfo is null. Are you sure you have connected your form to the application?");
            return;
        }
        imageInfoRepository.save(imageInfo);
    }

    public void imageProcess(ImageInfo entity, User user) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // time stamp for distinguishing different files
        Instant instantNow = Instant.now();
        // get current directory
        Path workingDirectoryPath = Util.getRootPath();
        File originalImageDirectoryFile = new File(workingDirectoryPath.toAbsolutePath() + File.separator + "images" + File.separator + user.getId() + File.separator + "original");
        String newFileName = instantNow + "-" + entity.getUploadImageName();
        String newFileNameHashed = DigestUtils.sha256Hex(newFileName);
        newFileNameHashed = newFileNameHashed.substring(0, 8);
        // PixelTransform
        File generatedImageDirectoryFile = new File(workingDirectoryPath.toAbsolutePath() + File.separator + "images" + File.separator + user.getId() + File.separator + "generated");
        Tika tika = new Tika();
        String mimeType;
        mimeType = tika.detect(entity.getImageOriginalFile());
        File newFile;
        String newFileFullName;
        if (acceptedImageFormat.contains(mimeType)) {
            newFileFullName = newFileNameHashed + "." + FilenameUtils.getExtension(entity.getImageOriginalName());
        } else {
            newFileFullName = newFileNameHashed + "." + "jpg";
        }
        if (generatedImageDirectoryFile.mkdirs() || generatedImageDirectoryFile.exists()) {
            newFile = new File(generatedImageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath().toString());
        } else {
            newFile = new File(originalImageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath().toString());
        }
        Mat imgMat = Imgcodecs.imread(entity.getImageOriginalFile());
        PixelTransform.saveImg(PixelTransform.transform(imgMat, entity.getColorNumber(), entity.getPixelSize(), entity.getSmooth(), entity.getEdgeCrispening(), entity.getContrastRatio(), entity.getSaturation()), newFile);
        entity.setImageNewFile(newFile.getPath());
        entity.setImageNewName(newFileFullName);
        try {
            binderImage.writeBean(entity);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        imageInfoService.update(entity);
    }
}
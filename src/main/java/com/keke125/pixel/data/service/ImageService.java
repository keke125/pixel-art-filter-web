package com.keke125.pixel.data.service;

import com.keke125.pixel.core.PixelTransform;
import com.keke125.pixel.data.entity.ImageInfo;
import com.keke125.pixel.data.entity.User;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Service
public class ImageService {
    private final UserRepository userRepository;
    private final ImageInfoRepository imageInfoRepository;

    private final ImageInfoService imageInfoService;

    // binder with Class SampleImage
    private Binder<ImageInfo> binderImage = new Binder<>(ImageInfo.class);

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
        Path workingDirectoryPath = Paths.get("");
        File imageDirectoryFile = new File(workingDirectoryPath.toAbsolutePath() + File.separator + "src/main/resources/META-INF/resources/images/" + user.getId());
        String newFileName = instantNow + "-" + entity.getImageOriginalName();
        String newFileNameHashed = DigestUtils.sha256Hex(newFileName);
        String newFileFullName = newFileNameHashed + "." + FilenameUtils.getExtension(entity.getImageOriginalName());
        // PixelTransform
        File newFile = new File(imageDirectoryFile.toPath().resolve(newFileFullName).toAbsolutePath().toString());
        Mat imgMat = Imgcodecs.imread(entity.getImageOriginalFile().getAbsolutePath());
        PixelTransform.saveImg(PixelTransform.transform(imgMat, entity.getColorNumber(), entity.getPixelSize(), entity.getSmooth(), entity.getEdgeCrispening(), entity.getContrastRatio(), entity.getSaturation()), newFile);
        entity.setImageNewFile(newFile);
        try {
            binderImage.writeBean(entity);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        imageInfoService.update(entity);
    }
}
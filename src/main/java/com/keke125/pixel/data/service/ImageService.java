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
    private final ImageInfoRepository imageInfoRepository;
    private final ImageInfoService imageInfoService;
    private final UserService userService;

    // binder with Class ImageInfo
    private final Binder<ImageInfo> binderImage = new Binder<>(ImageInfo.class);

    public ImageService(ImageInfoRepository imageInfoRepository, ImageInfoService imageInfoService, UserService userService) {
        this.imageInfoRepository = imageInfoRepository;
        this.imageInfoService = imageInfoService;
        this.userService = userService;
    }

    public long countImageInfos() {
        return imageInfoRepository.count();
    }

    public long countImageInfosByOwnerName(String ownerName) {
        return imageInfoRepository.countByOwnerName(ownerName);
    }

    public void deleteImageInfo(ImageInfo imageInfo, User user) {
        File originalFile = new File(imageInfo.getImageOriginalFile());
        File newFile = new File(imageInfo.getImageNewFile());
        Double oldUserImageSize = user.getImageSize();
        Double originalFileSize = (double) originalFile.length() / 1024 / 1024;
        Long imageInfoId = imageInfo.getId();
        imageInfoService.delete(imageInfoId);
        if (originalFile.delete() && newFile.delete()) {
            user.setImageSize(oldUserImageSize - originalFileSize);
            userService.update(user);
        }
    }

    public void deleteAllImageInfoByUser(User user) {
        List<ImageInfo> imageInfoList = imageInfoService.getRepository().findAllByOwnerName(user.getUsername());
        user.setImageSize(0.0);
        user.setImageSizeLimit(0.0);
        userService.update(user);
        File originalFile;
        File newFile;
        if (!imageInfoList.isEmpty()) {
            try {
                for (ImageInfo imageInfo : imageInfoList) {
                    originalFile = new File(imageInfo.getImageOriginalFile());
                    newFile = new File(imageInfo.getImageNewFile());
                    if (originalFile.delete() && newFile.delete()) {
                        imageInfoService.delete(imageInfo.getId());
                    }
                }
            } catch (RuntimeException e) {
                System.err.printf("Can't delete user's (ID: %d) images!", user.getId());
            }
        }
    }

    public void imageProcess(ImageInfo entity, User user) {
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        nu.pattern.OpenCV.loadLocally();
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

    public List<ImageInfo> findAllImageInfosByOwnerName(String OwnerName) {
        if (OwnerName == null || OwnerName.isEmpty()) {
            return null;
        } else {
            return imageInfoRepository.findAllByOwnerName(OwnerName);
        }
    }

}
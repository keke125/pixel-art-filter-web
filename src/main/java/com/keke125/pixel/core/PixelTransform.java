package com.keke125.pixel.core;

import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PixelTransform {

    public static void saveImg(BufferedImage image, File outputFile) {
        try {
            ImageIO.write(image,
                    FilenameUtils.getExtension(outputFile.getName()),
                    outputFile);
        } catch (IOException e) {
            // throw new RuntimeException(e);
            System.err.println(e.getMessage());
        }
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        // from Mat format to image format that can be used in ImageIO
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage img = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0,
                ((DataBufferByte) img.getRaster().getDataBuffer()).getData());
        return img;
    }

    public static BufferedImage transform(Mat imgMat, int k, double scale,
                                          int blur, int erode, int contrast,
                                          int saturation) {
        Mat imageMat = imgMat;
        if (blur > 0) {
            // image blur
            Mat outBlur = new Mat();
            Imgproc.bilateralFilter(imgMat, outBlur, 15, blur, 20);
            imageMat = outBlur;
        }
        if (contrast != 0) {
            // contrast
            imageMat = contrast_and_brightness(imgMat, 0, contrast);
        }
        if (saturation != 0) {
            // saturation
            imageMat = saturation(imgMat, saturation);
        }

        int h = imageMat.height();
        int w = imageMat.width();
        //int c = imgMat.channels();
        int d_h = (int) Math.round(h / scale);
        int d_w = (int) Math.round(w / scale);

        Mat resizedMat = new Mat();
        // scale part
        Imgproc.resize(imageMat, resizedMat, new Size(d_w, d_h), 0, 0,
                Imgproc.INTER_NEAREST);
        // erode
        resizedMat = erode(resizedMat, erode);
        // k-color
        resizedMat = cluster(resizedMat, k).get(0);
        Mat result = new Mat();
        Imgproc.resize(resizedMat, result, new Size(w, h), 0, 0,
                Imgproc.INTER_NEAREST);

        return matToBufferedImage(result);
    }

    public static Mat erode(Mat image, int erode) {
        // recommend erode is 1, 2
        Mat n8 = new Mat(3, 3, CvType.CV_8UC1);
        n8.setTo(new Scalar(1));

        Mat n4 = new Mat(3, 3, CvType.CV_8UC1);
        n4.put(0, 1, 0, 1, 1, 1, 0, 1, 0);

        Mat outPut = new Mat();
        if (erode == 1) {
            Imgproc.erode(image, outPut, n4, new Point(-1, -1), 1);
            return outPut;
        } else if (erode == 2) {
            Imgproc.erode(image, outPut, n8, new Point(-1, -1), 1);
            return outPut;
        } else return image;
    }

    public static List<Mat> cluster(Mat cutout, int k) {
        // for k-means
        // color number
        Mat samples = cutout.reshape(1, cutout.cols() * cutout.rows());
        Mat samples32f = new Mat();
        samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);

        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        Mat centers = new Mat();
        Core.kmeans(samples32f, k, labels, criteria, 10,
                Core.KMEANS_PP_CENTERS, centers);

        centers.convertTo(centers, CvType.CV_8UC1, 255.0);
        centers.reshape(3);

        List<Mat> clusters = new ArrayList<>();
        for (int i = 0; i < centers.rows(); i++) {
            clusters.add(Mat.zeros(cutout.size(), cutout.type()));
        }

        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < centers.rows(); i++) counts.put(i, 0);

        int rows = 0;
        for (int y = 0; y < cutout.rows(); y++) {
            for (int x = 0; x < cutout.cols(); x++) {
                int label = (int) labels.get(rows, 0)[0];
                int r = (int) centers.get(label, 2)[0];
                int g = (int) centers.get(label, 1)[0];
                int b = (int) centers.get(label, 0)[0];
                counts.put(label, counts.get(label) + 1);
                clusters.get(0).put(y, x, b, g, r);
                rows++;
            }
        }
        return clusters;
    }

    public static Mat contrast_and_brightness(Mat img, int brightness,
                                              int contrast) {
        double B = brightness / 255.0;
        double c = contrast / 255.0;
        double k = Math.tan((45 + 44 * c) / 180 * Math.PI);

        img.convertTo(img, CvType.CV_32F);
        Core.subtract(img, new Mat(img.size(), img.type(),
                new org.opencv.core.Scalar(127.5 * (1 - B))), img);
        Core.multiply(img, new Mat(img.size(), img.type(),
                new org.opencv.core.Scalar(k)), img);
        Core.add(img, new Mat(img.size(), img.type(),
                new org.opencv.core.Scalar(127.5 * (1 + B))), img);

        Core.normalize(img, img, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);

        return img;
    }

    public static Mat saturation(Mat img, int saturation) {
        Mat saturated = new Mat();
        double scale = 1;
        img.convertTo(saturated, CvType.CV_8UC1, scale, saturation);
        return saturated;
    }
}
package com.keke125.pixel.core;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Filters {

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage img = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0, ((DataBufferByte) img.getRaster().getDataBuffer()).getData());
        return img;
    }

    public static void saveImg(BufferedImage image, String fileName) {
        try {
            File outputFile = new File(fileName);
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] get(Mat mat, int row, int col) {
        if (row < 0 || row >= mat.rows() || col < 0 || col >= mat.cols()) return null;
        double[] values = new double[mat.channels()];
        for (int i = 0; i < mat.channels(); i++) {
            values[i] = mat.get(row, col)[i];
        }
        return values;
    }

    public BufferedImage kuwahara(Mat image, int scale) {
        if (image == null) return null;
        if (scale > 5 || scale <= 0) return matToBufferedImage(image);

        Mat output;
        if (image.channels() == 3) output = new Mat(image.rows(), image.cols(), CvType.CV_8UC3);
        else output = new Mat(image.rows(), image.cols(), CvType.CV_8UC1);

        double[][][] matrix = new double[output.rows()][output.cols()][output.channels()];
        for (int i = 0; i < output.rows(); i++) {
            for (int j = 0; j < output.cols(); j++) {
                double[] data = get(image, i, j);
                if (output.channels() >= 0) System.arraycopy(data, 0, matrix[i][j], 0, output.channels());
            }
        }

        for (int i = 0; i < image.rows(); i++) {
            for (int j = 0; j < image.cols(); j++) {
                double[] data = new double[3];
                for (int channel = 0; channel < image.channels(); channel++) {
                    double[] avg = new double[4];
                    Arrays.fill(avg, 0);
                    double[] sii = new double[4];
                    Arrays.fill(sii, 0);
                    int[] count = new int[4];
                    Arrays.fill(count, 0);
                    for (int dx = -scale; dx <= scale; dx++) {
                        for (int dy = -scale; dy <= scale; dy++) {
                            int nowX = i + dx, nowY = j + dy;
                            if (nowX < 0 || nowX >= image.rows() || nowY < 0 || nowY >= image.cols()) continue;
                            if (dx + dy <= 0 && dx - dy <= 0) {
                                avg[0] += matrix[nowX][nowY][channel];
                                sii[0] += matrix[nowX][nowY][channel] * matrix[nowX][nowY][channel];
                                count[0]++;
                            }
                            if (dx + dy >= 0 && dx - dy <= 0) {
                                avg[1] += matrix[nowX][nowY][channel];
                                sii[1] += matrix[nowX][nowY][channel] * matrix[nowX][nowY][channel];
                                count[1]++;
                            }
                            if (dx + dy >= 0 && dx - dy >= 0) {
                                avg[2] += matrix[nowX][nowY][channel];
                                sii[2] += matrix[nowX][nowY][channel] * matrix[nowX][nowY][channel];
                                count[2]++;
                            }
                            if (dx + dy <= 0 && dx - dy >= 0) {
                                avg[3] += matrix[nowX][nowY][channel];
                                sii[3] += matrix[nowX][nowY][channel] * matrix[nowX][nowY][channel];
                                count[3]++;
                            }
                        }
                    }
                    for (int index = 0; index < 4; index++) {
                        avg[index] = avg[index] / count[index];
                        sii[index] = sii[index] / count[index] - avg[index] * avg[index];
                    }
                    int minIndex = 0;
                    double minData = sii[0];
                    for (int index = 0; index < 4; index++) {
                        if (sii[index] < minData) {
                            minData = sii[index];
                            minIndex = index;
                        }
                    }
                    data[channel] = avg[minIndex];
                }
                if (image.channels() == 3) output.put(i, j, data);
                else output.put(i, j, data[0]);
            }
        }
        // img.add(output);
        return matToBufferedImage(output);
    }

    public BufferedImage Normalized(Mat input) {
        if (input == null) return null;
        double[][] mask = {{0, 0, 0, 8, 4}, {2, 4, 8, 4, 2}, {1, 2, 4, 2, 1}};
        int weight = 42;
        Mat output = new Mat();
        input.copyTo(output);

        for (int i = 0; i < output.rows(); i++) {
            for (int j = 0; j < output.cols(); j++) {
                double[] diff = new double[3];
                double[] data = output.get(i, j);

                for (int channel = 0; channel < output.channels(); channel++) {
                    if (data[channel] >= 128) {
                        diff[channel] = data[channel] - 255;
                        data[channel] = 255;
                    } else {
                        diff[channel] = data[channel];
                        data[channel] = 0;
                    }
                }

                for (int a = 0; a <= 2 && i + a >= 0 && i + a < output.rows(); a++) {
                    for (int b = 0; b <= 2 && j + b >= 0 && j + b < output.cols(); b++) {
                        double[] putData = output.get(i + a, j + b);

                        for (int channel = 0; channel < output.channels(); channel++) {
                            if (j + b < 0) {
                                putData[channel] += diff[channel] * mask[a][2] / weight;
                            } else {
                                putData[channel] += diff[channel] * mask[a][b + 2] / weight;
                            }

                            if (putData[channel] >= 256) putData[channel] = 255;
                            if (putData[channel] < 0) putData[channel] = 0;
                        }

                        if (output.channels() == 3) output.put(i + a, j + b, putData);
                        else output.put(i + a, j + b, putData[0]);
                    }
                }

                if (output.channels() == 3) output.put(i, j, data);
                else output.put(i, j, data[0]);
            }
        }
        // img.add(output);
        return matToBufferedImage(output);
    }

    public BufferedImage Flame(Mat input) {
        if (input == null) return null;
        Mat output = new Mat();
        input.copyTo(output);

        for (int i = 0; i < output.rows(); i++) {
            for (int j = 0; j < output.cols(); j++) {
                double[] data = output.get(i, j);

                for (int channel = 0; channel < output.channels(); channel++) {
                    if (data[channel] >= 128) {
                        data[channel] = 255;
                    } else {
                        data[channel] = 0;
                    }
                }
                if (output.channels() == 3) output.put(i, j, data);
                else output.put(i, j, data[0]);
            }
        }
        return matToBufferedImage(output);
    }

    public BufferedImage highGaussian(Mat input) {
        if (input == null) return null;
        Mat output = new Mat();
        input.copyTo(output);
        int[][] mask = {{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};
        double[][][] matrix = new double[output.rows()][output.cols()][output.channels()];
        for (int i = 0; i < output.rows(); i++) {
            for (int j = 0; j < output.cols(); j++) {
                double[] data = get(output, i, j);
                if (output.channels() >= 0) System.arraycopy(data, 0, matrix[i][j], 0, output.channels());
            }
        }
        for (int i = 0; i < output.rows(); i++) {
            for (int j = 0; j < output.cols(); j++) {
                double[] total = {0, 0, 0};
                for (int channel = 0; channel < output.channels(); channel++) {
                    for (int a = -1; a <= 1; a++) {
                        for (int b = -1; b <= 1; b++) {
                            if (i + a >= 0 && i + a < output.rows() && j + b >= 0 && j + b < output.cols()) {
                                total[channel] += matrix[i + a][j + b][channel] * mask[a + 1][b + 1];
                            }
                        }
                    }
                }
                output.put(i, j, total);
            }
        }
        return matToBufferedImage(output);
    }

    public BufferedImage unknown(Mat input) {
        if (input == null) return null;
        double[][] mask = {{0.513, 0.272, 0.724, 0.483, 0.543, 0.302, 0.694, 0.453}, {0.151, 0.755, 0.091, 0.966, 0.181, 0.758, 0.121, 0.936}, {0.634, 0.392, 0.574, 0.332, 0.664, 0.423, 0.604, 0.362}, {0.060, 0.875, 0.211, 0.815, 0.030, 0.906, 0.241, 0.845}, {0.543, 0.302, 0.694, 0.453, 0.513, 0.272, 0.724, 0.483}, {0.181, 0.758, 0.121, 0.936, 0.151, 0.755, 0.091, 0.936}, {0.664, 0.423, 0.604, 0.362, 0.634, 0.392, 0.574, 0.332}, {0.030, 0.906, 0.241, 0.845, 0.060, 0.875, 0.211, 0.815}};
        Mat output = new Mat();
        input.copyTo(output);

        double[][][] matrix = new double[output.rows()][output.cols()][output.channels()];
        for (int i = 0; i < output.rows(); i++) {
            for (int j = 0; j < output.cols(); j++) {
                double[] data = get(input, i, j);
                if (output.channels() >= 0) System.arraycopy(data, 0, matrix[i][j], 0, output.channels());
            }
        }

        for (int i = 0; i < output.rows(); i += 8) {
            for (int j = 0; j < output.cols(); j += 8) {
                for (int a = 0; a < 8; a++) {
                    for (int b = 0; b < 8; b++) {
                        double[] data = {0, 0, 0};
                        int nowX = i + a, nowY = j + b;
                        if (nowX < 0 || nowX >= output.rows() || nowY < 0 || nowY >= output.cols()) continue;
                        for (int channel = 0; channel < output.channels(); channel++) {
                            double check = matrix[nowX][nowY][channel] / (double) 255;
                            if (check >= mask[a][b]) data[channel] = 255;
                            else data[channel] = 0;
                        }
                        if (output.channels() == 3) output.put(nowX, nowY, data);
                        else output.put(nowX, nowY, data[0]);
                    }
                }
            }
        }
        return matToBufferedImage(output);
    }
}
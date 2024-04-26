import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.awt.Graphics2D;
import net.sourceforge.tess4j.*;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

//references
//https://docs.gimp.org/2.6/en/gimp-tool-desaturate.html

public class OpenCv {


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image = Imgcodecs.imread("C:\\Users\\germa\\OneDrive\\Desktop\\CSC474- Image Proccessing\\Images\\test1.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\vfec56pp6rf51-1386706553.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\maxresdefault-1911473101.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\CameraPlate.jpg");


        //HighGui.imshow("og", image);
        Mat gray = processImage(image);
        HighGui.imshow("processed", gray);
        HighGui.waitKey();
    }

    public static Mat convertGrayScale(final Mat img) {
        if(img.empty()){
            System.err.println("Input image is null");
            return img;
        }

        Mat convertedImage = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);
        int width = img.width();
        int height = img.height();

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                //pixel color values
                double[] rgbVal = img.get(j, i);

                // Check if the retrieved pixel values are null or empty
                if (rgbVal == null || rgbVal.length < 3) {
                    System.err.println("Invalid pixel values at position (" + j + ", " + i + ").");
                    continue; // Skip this pixel and continue with the next one
                }
                //luminosity method
                int red = (int) rgbVal[0];
                int blue = (int) rgbVal[1];
                int green = (int) rgbVal[2];

                double newVal = (0.21 * red) + (0.72 * green) + (0.07 * blue);
                convertedImage.put(j, i, newVal);
            }
        }
        return convertedImage;
    }

    public static Mat GaussianBlur(Mat img, int kernalSize, double sigma){
        Mat blurredImage = new Mat(img.size(), CvType.CV_8UC3);
        int padding = kernalSize / 2;

        //make kernal
        double[][] kernal = createGaussianKernal(kernalSize, sigma);

        //apply kernal to every pixel
        for(int y = padding; y < img.rows() - padding; y++){
            for(int x = padding; x < img.cols() - padding; x++){
                double[] pixel = applyKernal(img, x, y, kernal, kernalSize);
                blurredImage.put(y, x, pixel);
            }
        }

        return blurredImage;
    }

    public static double[][] createGaussianKernal(int size, double sigma){
        double[][] kernel = new double[size][size];
        double sum = 0;

        for(int x = -size / 2; x <= size / 2; x++) {
            for(int y = -size / 2; y <= size / 2; y++) {
                double exponent = -(x * x + y * y) / (2 * sigma * sigma);
                kernel[x + size / 2][y + size / 2] = Math.exp(exponent) / (2 * Math.PI * sigma * sigma);
                sum += kernel[x + size / 2][y + size / 2];
            }
        }

        //normalize kernel
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }

        return kernel;
    }

    public static double[] applyKernal(Mat img, int x, int y, double[][] kernel, int kernelSize){
        int padding = kernelSize / 2;
        double[] pixel = new double[3];

        for(int i = -padding; i <= padding; i++) {
            for(int j = -padding; j <= padding; j++) {
                double[] sourcePixel = img.get(y + i, x + j);
                double kernelValue = kernel[i + padding][j + padding];
                for(int k = 0; k < 3; k++) {
                    pixel[k] += sourcePixel[k] * kernelValue;
                }
            }
        }
        return pixel;
    }


    public static Mat processImage(Mat mat){
        Mat processedImg = new Mat(mat.height(), mat.width(), mat.type());
        //blur
        Mat blurimg = GaussianBlur(mat, 5, 1.5);
        //HighGui.imshow("blurred", blurimg);

        //convert to grayscale
        Mat grayscaleimg = convertGrayScale(blurimg);
        //HighGui.imshow("grayscale", grayscaleimg);

        //apply thresholding
        //Imgproc.threshold(processedImg, processedImg, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        //find edges
        Mat edgeimg = new Mat(mat.height(), mat.width(), mat.type());
        Imgproc.Canny(grayscaleimg, edgeimg, 270, 25);
        HighGui.imshow("edge", edgeimg);
        //dilate
        //Imgproc.dilate(edgeimg, processedImg, new Mat(), new Point(1, 1), 1);
        //HighGui.imshow("dilated", processedImg);

        //find rectangles
        processedImg = findRectangle(edgeimg);

        //HighGui.imshow("contours", processedImg);
        return processedImg;
    }

    public static Mat findRectangle(Mat mat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat image32S = mat.clone();
        mat.convertTo(image32S, CvType.CV_32SC1);
        //Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        Imgproc.findContours(image32S, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

        System.out.println(contours.size());
        Mat result = mat.clone();

        for(int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);

            //Approximates contour with a polygon
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f cnt = new MatOfPoint2f(contour.toArray());
            double epsilon = 0.02 * Imgproc.arcLength(cnt, true);
            Imgproc.approxPolyDP(cnt, approxCurve, epsilon, true);

            //Need to convert back to MatOfPoint to count vertices in polygon
            MatOfPoint points = new MatOfPoint();
            approxCurve.convertTo(points, CvType.CV_32S);

            //Check if point has 4 vertices i.e. is a rectangle
            if(points.rows() == 4) {
                Imgproc.drawContours(result, contours, i, new Scalar(0, 255, 0, 1), 2);
                System.out.println("Drawing rect");
            }
        }

        return result;
    }

}


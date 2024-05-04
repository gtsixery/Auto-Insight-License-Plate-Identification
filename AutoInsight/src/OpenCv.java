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
/*
public class OpenCv {


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\BYE-NOW-3816943373.jpg");
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

        //dilate
        //Imgproc.dilate(edgeimg, processedImg, new Mat(), new Point(1, 1), 1);
        //HighGui.imshow("dilated", processedImg);

        //find rectangles
        processedImg = findRectangle(edgeimg);
        //Mat edge2 = new Mat(mat.height(), mat.width(), mat.type());
        //Imgproc.Canny(processedImg, edge2, 270, 25);
        //HighGui.imshow("edge2", edge2);
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

        List<MatOfPoint> groupedContours = groupContours(contours);
        Imgproc.drawContours(result, groupedContours, -1, new Scalar(0, 255, 0), 2);
        HighGui.imshow("grouped contours", result);

        return result;
    }

    public static List<MatOfPoint> groupContours(List<MatOfPoint> contours) {
        List<MatOfPoint> groupedContours = new ArrayList<>();

        // Adjust the tolerances for grouping contours
        int xTolerance = 3; // Increase the x tolerance
        int yTolerance = 3; // Increase the y tolerance

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour1 = contours.get(i);

            // Check if this contour is already grouped
            boolean isGrouped = false;
            for (MatOfPoint groupedContour : groupedContours) {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(groupedContour.toArray()), new Point(contour1.get(0, 0)), false) >= 0) {
                    isGrouped = true;
                    break;
                }
            }

            if (!isGrouped) {
                // Create a bounding rectangle for the current contour
                Rect boundingRect1 = Imgproc.boundingRect(contour1);

                // Group nearby contours within the adjusted tolerances
                List<MatOfPoint> group = new ArrayList<>();
                group.add(contour1);

                // Group contours in the horizontal direction
                for (int j = i + 1; j < contours.size(); j++) {
                    MatOfPoint contour2 = contours.get(j);
                    Rect boundingRect2 = Imgproc.boundingRect(contour2);

                    if (Math.abs(boundingRect1.x - boundingRect2.x) <= xTolerance) {
                        group.add(contour2);
                    }
                }

                // Group contours in the vertical direction
                for (int j = i + 1; j < contours.size(); j++) {
                    MatOfPoint contour2 = contours.get(j);
                    Rect boundingRect2 = Imgproc.boundingRect(contour2);

                    if (Math.abs(boundingRect1.y - boundingRect2.y) <= yTolerance) {
                        group.add(contour2);
                    }
                }

                // Merge the contours in the group
                MatOfPoint mergedContour = new MatOfPoint();
                List<Point> mergedPoints = new ArrayList<>();
                for (MatOfPoint contour : group) {
                    mergedPoints.addAll(contour.toList());
                }
                mergedContour.fromList(mergedPoints);
                groupedContours.add(mergedContour);
            }
        }

        return groupedContours;
    }

}

*/
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCv {

    static {
        // Load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat preprocessImage(String imagePath) {
        // Read the image
        Mat image = Imgcodecs.imread(imagePath);

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur to remove noise
        Mat blur = new Mat();
        Imgproc.GaussianBlur(gray, blur, new Size(5, 5), 0);

        // Adaptive thresholding
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(blur, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV, 11, 2);

        // Morphological operation to close gaps between letters
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat closing = new Mat();
        Imgproc.morphologyEx(thresh, closing, Imgproc.MORPH_CLOSE, kernel);

        // Dilation to strengthen the characters
        Mat dilated = new Mat();
        Imgproc.dilate(closing, dilated, kernel);

        return dilated;
    }

    public static void main(String[] args) {

        // Now you can use `preprocessedImage` with Tesseract OCR

        String folderPath = "AutoInsight/src/Images";
        File Folder = new File(folderPath);
        String outputPath = "AutoInsight/src/Output_Images";
        FilenameFilter imageFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // You can modify this condition to match your desired image file extensions
                return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
            }
        };
        //store files to File Array
        File[] imageFiles = Folder.listFiles(imageFilter);

        int counter = 0; // keep File Count
        while( counter < imageFiles.length ) {
            String imageFile = imageFiles[counter].getAbsolutePath();
            //Process with tesseract
            Mat image = preprocessImage(imageFile);
            //Save File to Output Folder
            Imgcodecs.imwrite(STR."AutoInsight/src/Output_Images/Preprocessed_Image \{counter} .jpg", image);
            System.out.println(STR."Processed image saved as Preprocessed_Image \{counter} .jpg");
            counter++;
        }

        // Save the processed image to file


    }
}

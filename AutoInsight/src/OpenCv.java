import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//references
//https://docs.gimp.org/2.6/en/gimp-tool-desaturate.html

public class OpenCv {


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\BYE-NOW-3816943373.jpg");
        Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\trucktest.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\test4.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\vfec56pp6rf51-1386706553.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\maxresdefault-1911473101.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\CameraPlate.jpg");


        HighGui.imshow("og", image);
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
        Imgproc.Canny(grayscaleimg, edgeimg, 265, 45);


        //find rectangles
        processedImg = findContours(edgeimg);
        Mat closedImg = new Mat();
        Mat kernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 8));
        Imgproc.morphologyEx(processedImg, closedImg, Imgproc.MORPH_CLOSE, kernal);

        int[] rowRange = MakeVerticalHistogram(closedImg, 150);
        //int[] colRange = MakeHorizontalHistogram(closedImg, 300);

        //vertical range
        int cropheight = rowRange[1] - rowRange[0];
        double bufferheight = closedImg.cols() * 0.2;
        cropheight = cropheight+(int)bufferheight;
        double paddingHeight = cropheight * 0.1;
        if(rowRange[0]+cropheight > closedImg.rows()) {
            int diff = (rowRange[0]+cropheight) - closedImg.rows();
            cropheight -= diff;
        }

        //decided to start the horizontal crop 25% in
        double cropXStart = closedImg.cols() * 0.25;
        double cropWidth = closedImg.cols() * 0.5;

        Rect roi = new Rect((int)cropXStart, rowRange[0]+(int)paddingHeight, (int)cropWidth, cropheight - (int)paddingHeight);

        Mat croppedImg = new Mat(mat, roi);
        return croppedImg;
    }

    public static int[] MakeVerticalHistogram(Mat mat, int buffer) {
        List<Integer> whitePixelCounts = new ArrayList<>();
        int[] result = new int[2];

        for(int i = 0; i < mat.rows(); i++) {
            int pCount = 0;

            for(int j = 0; j < mat.cols(); j++) {
                double pixelValue = mat.get(i, j)[0];
                if(pixelValue == 255) {
                    pCount++;
                }
            }
            whitePixelCounts.add(pCount);
        }

        // sort by amount of pCount, index = row index
        for(Integer e : whitePixelCounts) {
            if(e > whitePixelCounts.get(result[0])) {
                result[0] = whitePixelCounts.indexOf(e);
            } else if (e > whitePixelCounts.get(result[1])) {
                result[1] = whitePixelCounts.indexOf(e);
            }
        }

        //sort array
        int maxIndex = 0;
        int secondMaxIndex = 0;
        for (int i = 0; i < whitePixelCounts.size(); i++) {
            int count = whitePixelCounts.get(i);
            if (count > whitePixelCounts.get(maxIndex)) {
                secondMaxIndex = maxIndex;
                maxIndex = i;
            } else if (count > whitePixelCounts.get(secondMaxIndex)) {
                secondMaxIndex = i;
            }
        }


        result[0] = Math.min(maxIndex, secondMaxIndex);
        result[1] = Math.max(maxIndex, secondMaxIndex);

        return result;
    }

    public static int[] MakeHorizontalHistogram(Mat mat, int buffer) {
        List<Integer> whitePixelCounts = new ArrayList<>();
        int[] result = new int[2];

        for(int i = 0; i < mat.cols(); i++) {
            int pCount = 0;

            for(int j = 0; j < mat.rows(); j++) {
                double pixelValue = mat.get(i, j)[0];
                if(pixelValue == 255) {
                    pCount++;
                }
            }
            whitePixelCounts.add(pCount);
        }

        // sort by amount of pCount, index = row index
        for(Integer e : whitePixelCounts) {
            if(e > whitePixelCounts.get(result[0])) {
                result[0] = whitePixelCounts.indexOf(e);
            } else if (e > whitePixelCounts.get(result[1])) {
                result[1] = whitePixelCounts.indexOf(e);
            }
        }

        //sort array
        int maxIndex = 0;
        int secondMaxIndex = 0;
        for (int i = 0; i < whitePixelCounts.size(); i++) {
            int count = whitePixelCounts.get(i);
            if (count > whitePixelCounts.get(maxIndex)) {
                secondMaxIndex = maxIndex;
                maxIndex = i;
            } else if (count > whitePixelCounts.get(secondMaxIndex)) {
                secondMaxIndex = i;
            }
        }


        result[0] = Math.min(maxIndex, secondMaxIndex);
        result[1] = Math.max(maxIndex, secondMaxIndex);

        return result;
    }

    public static Mat findContours(Mat mat) {
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

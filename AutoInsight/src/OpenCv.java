import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class OpenCv {


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\BYE-NOW-3816943373.jpg");
        //Mat image = Imgcodecs.imread("F:\\College\\csc474\\Images\\vfec56pp6rf51-1386706553.jpg");
        Mat image = Imgcodecs.imread("/Users/jr/Downloads/IMG_7066.Jpeg");

        //convert image to grayscale
        //Mat gray = new Mat();
        //Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        //HighGui.imshow("grayscale", gray);

        //apply a threshold value to get binary image
        //Mat binary = new Mat();
        //Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        //HighGui.imshow("binary", binary);

        final Mat processed = processImage(image);
        HighGui.imshow("process", processed);
        //Mat linesImage = findRectangle(processed);
        //HighGui.imshow("find rect", linesImage);
        HighGui.waitKey();
        /*
        //find contours in image
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //iterate

        for(MatOfPoint contour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(curve, approx, Imgproc.arcLength(curve, true) * 0.02, true);

            if(approx.total() == 4) {
                System.out.println("found rectangle");
                Imgproc.drawContours(image, Collections.singletonList(contour), -1, new Scalar(0, 255, 0), 2);
                //display

            }
        }
        HighGui.imshow("result", image);
        HighGui.waitKey();
         */
    }

    public static Mat processImage(Mat mat){
        final Mat processedImg = new Mat(mat.height(), mat.width(), mat.type());
        //blur
        Imgproc.GaussianBlur(mat, processedImg, new Size(7, 7), 1);

        //convert to grayscale
        Imgproc.cvtColor(processedImg, processedImg, Imgproc.COLOR_RGB2GRAY);

        //apply thresholding
        //Imgproc.threshold(processedImg, processedImg, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        //find edges
        Imgproc.Canny(processedImg, processedImg, 270, 25);

        //dilate
        Imgproc.dilate(processedImg, processedImg, new Mat(), new Point(1, 1), 1);
        return processedImg;
    }

    public static Mat findRectangle(Mat mat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat image32S = new Mat();
        mat.convertTo(image32S, CvType.CV_32SC1);
        //Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        Imgproc.findContours(image32S, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

        System.out.println(contours.size());
        Mat result = mat.clone();

        for(MatOfPoint contour : contours) {
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
                Imgproc.drawContours(result, Collections.singletonList(points), contours.indexOf(contour), new Scalar(0, 0, 255), 2);
                System.out.println("Drawing rect");
            }
        }


        return result;
    }

    public static Mat findLines(Mat img) {
        Mat lines = new Mat();
        Imgproc.HoughLines(img, lines, 0.5, Math.PI / 180, 220, 1, 2);

        //draw lines
        Mat result = img.clone();
        for (int i = 0; i < lines.rows(); i++) {
            double rho = lines.get(i, 0)[0];
            double theta = lines.get(i, 0)[1];
            //System.out.println("Line " + i + ": rho=" + rho + ", theta=" + theta); // Debugging output
            double a = Math.cos(theta);
            double b = Math.sin(theta);
            double x0 = a * rho;
            double y0 = b * rho;
            Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));

            Imgproc.line(result, pt1, pt2, new Scalar(0, 0, 255), 5);
        }
        return result;
    }

}

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;

public class LicensePlateDetection {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String filePath = "AutoInsight/src/Images/test1.png";
        Mat src = Imgcodecs.imread(filePath);

        // Define the region of interest (ROI)
        // These coordinates might need adjustment depending on where the license plate typically appears
        int xStart = (int) (src.cols() * 0.4);  // Start 40% from the left
        int yStart = (int) (src.rows() * 0.39);   // Start 39% from the top
        int width = (int) (src.cols() * 0.2);    // Span 20% of the total width
        int height = (int) (src.rows() * 0.1);   // Span 10% of the total height
        Rect roi = new Rect(xStart, yStart, width, height);

        // Crop the image to the ROI
        Mat cropped = new Mat(src, roi);

        Mat gray = new Mat();
        Imgproc.cvtColor(cropped, gray, Imgproc.COLOR_BGR2GRAY);
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(3, 3), 0);

        // Applying Canny edge detection
        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, 200, 280);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Draw the contours on the cropped image to check accuracy
        Scalar color = new Scalar(0, 255, 0); // Green color for drawing
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            Imgproc.rectangle(cropped, rect.tl(), rect.br(), color, 2);
        }

        // Optional: Draw the ROI on the original image to verify the area
        Imgproc.rectangle(src, roi.tl(), roi.br(), new Scalar(0, 0, 255), 2); // Red box to verify ROI

        // Save results
        Imgcodecs.imwrite("AutoInsight/src/Output_Images/cropped_optimized_detected_plate.png", cropped);
        Imgcodecs.imwrite("AutoInsight/src/Output_Images/full_with_roi.png", src);
        System.out.println("Contour detection complete. Check the output image.");
    }
}

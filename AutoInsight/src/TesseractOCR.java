import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TesseractOCR {
    Mat ImageToRead;
    ITesseract tesseract;

    public TesseractOCR(Mat image){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.ImageToRead = image;
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("F:\\College\\csc474\\Tesseract\\Tess4J\\tessdata");
    }

    public String DoOCR() {
        String text = "";
        this.ImageToRead = OpenCv.GaussianBlur(this.ImageToRead, 5, 1.5);
        this.ImageToRead = OpenCv.convertGrayScale(this.ImageToRead);

        //edge detection
        //Imgproc.Canny(this.ImageToRead, this.ImageToRead, 280, 100);

        //threshold
        Imgproc.adaptiveThreshold(this.ImageToRead, this.ImageToRead,255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 20);

        Mat kernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 1));
        //dilate
        //Imgproc.dilate(this.ImageToRead, this.ImageToRead, kernal);

        //close
        Imgproc.morphologyEx(this.ImageToRead, this.ImageToRead, Imgproc.MORPH_OPEN, kernal);

        HighGui.imshow("before buffer", this.ImageToRead);

        BufferedImage TessImage = ConvertMatBufferedImage(this.ImageToRead);

        try{
            text = this.tesseract.doOCR(TessImage);
            //System.out.println(text);

        }catch(TesseractException e) {
            e.printStackTrace();
        }


        return text;
    }

    public BufferedImage ConvertMatBufferedImage(Mat mat) {
        //convert mat to byte array
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        byte[] byteArray = mob.toArray();

        //convert byte array to buffered image
        BufferedImage buffimage = null;
        try {
            buffimage = ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffimage;
    }


}

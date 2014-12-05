package com.emadbarsoum.lib;

import org.apache.hadoop.mapreduce.Mapper.*;

import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_objdetect.*;

import java.awt.*;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

/**
 * FaceDetection given a trained model, facedetection will find all faces in the image.
 */
public class FaceDetection
{
    private String model;
    private IplImage resultImage;
    private int faceCount;
    private ArrayList<Rectangle> faceLocations = new ArrayList<Rectangle>();

    public String getModel()
    {
        return this.model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public int count()
    {
        return this.faceCount;
    }

    public IplImage getResultImage()
    {
        return this.resultImage;
    }

    public ArrayList<Rectangle> getFaceLocations()
    {
        return this.faceLocations;
    }

    public void Detect(IplImage image, Context context) throws Exception
    {
        if ((this.model == null) || this.model.isEmpty())
        {
            throw new Exception("Model must be set before calling Detect.");
        }

        this.resultImage = image.clone();
        IplImage grayImage = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);

        // Convert the input image into a gray image.
        cvCvtColor(image, grayImage, CV_BGR2GRAY);

        if (context != null)
        {
            context.progress();
        }

        CvMemStorage storage = CvMemStorage.create();

        // Load the classifier.
        CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(this.model));

        // Detect all faces in the image.
        CvSeq faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 1, 0);

        if (context != null)
        {
            context.progress();
        }

        this.faceCount = faces.total();
        this.faceLocations.clear();

        for (int i = 0; i < faces.total(); i++)
        {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            cvRectangle(this.resultImage, cvPoint(r.x(), r.y()), cvPoint(r.x() + r.width(), r.y() + r.height()), CvScalar.GREEN, 1, CV_AA, 0);

            faceLocations.add(new Rectangle(r.x(), r.y(), r.width(), r.height()));
        }

        if (context != null)
        {
            context.progress();
        }
    }
}

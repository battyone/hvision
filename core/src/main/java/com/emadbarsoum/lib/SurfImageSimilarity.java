package com.emadbarsoum.lib;

import org.apache.hadoop.mapreduce.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_legacy.*;

import java.nio.FloatBuffer;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_flann.*;
import static org.bytedeco.javacpp.opencv_legacy.*;

/**
 * SurfImageSimilarity compute similarity between 2 images using SURF feature.
 */
public class SurfImageSimilarity implements ImageSimilarity
{
    private CvSURFParams params = new CvSURFParams();
    private Index flannIndex = null;
    private IndexParams indexParams = null;
    private SearchParams searchParams = null;
    private Mat image1Mat;
    private Mat image2Mat;
    private Mat indicesMat;
    private Mat distancesMat;
    private double threshold = 0.6;

    public void setThreshold(double value)
    {
        this.threshold = value;
    }

    public double getThreshold()
    {
        return this.threshold;
    }

    public SurfImageSimilarity()
    {
        params.extended(1).hessianThreshold(300).nOctaves(3).nOctaveLayers(4);
    }

    public double computeDistance(IplImage image1, IplImage image2, TaskAttemptContext context)
    {
        CvSeq keypoints1   = new CvSeq(null);
        CvSeq descriptors1 = new CvSeq(null);
        CvSeq keypoints2   = new CvSeq(null);
        CvSeq descriptors2 = new CvSeq(null);

        IplImage image1Gray = cvCreateImage(cvSize(image1.width(), image1.height()), IPL_DEPTH_8U, 1);
        IplImage image2Gray = cvCreateImage(cvSize(image2.width(), image2.height()), IPL_DEPTH_8U, 1);

        // Convert input images into a Gray images...
        cvCvtColor(image1, image1Gray, CV_BGR2GRAY);
        cvCvtColor(image2, image2Gray, CV_BGR2GRAY);

        CvMemStorage storage1 = CvMemStorage.create();
        cvClearMemStorage(storage1);
        cvExtractSURF(image1Gray, null, keypoints1, descriptors1, storage1, params, 0);

        if (context != null)
        {
            context.progress();
        }

        CvMemStorage storage2 = CvMemStorage.create();
        cvClearMemStorage(storage2);
        cvExtractSURF(image2Gray, null, keypoints2, descriptors2, storage2, params, 0);

        if (context != null)
        {
            context.progress();
        }

        int total1 = descriptors1.total();
        int size1 = descriptors1.elem_size();
        CvSURFPoint[] image1Keypoints = new CvSURFPoint[total1];
        FloatBuffer[] image1Descriptors = new FloatBuffer[total1];
        for (int i = 0; i < total1; i++)
        {
            image1Keypoints[i] = new CvSURFPoint(cvGetSeqElem(keypoints1, i));
            image1Descriptors[i] = cvGetSeqElem(descriptors1, i).capacity(size1).asByteBuffer().asFloatBuffer();
        }

        int total2 = descriptors2.total();
        int size2 = descriptors2.elem_size();
        CvSURFPoint[] image2Keypoints = new CvSURFPoint[total2];
        FloatBuffer[] image2Descriptors = new FloatBuffer[total2];
        for (int i = 0; i < total2; i++)
        {
            image2Keypoints[i] = new CvSURFPoint(cvGetSeqElem(keypoints2, i));
            image2Descriptors[i] = cvGetSeqElem(descriptors2, i).capacity(size2).asByteBuffer().asFloatBuffer();
        }

        if (context != null)
        {
            context.progress();
        }

        int total    = Math.min(total1, total2);
        int length1  = image1Descriptors[0].capacity();
        int length2  = image2Descriptors[0].capacity();

        image1Mat    = new Mat(total1, length1, CV_32F);
        image2Mat    = new Mat(total2, length2, CV_32F);
        indicesMat   = new Mat(total,        2, CV_32S);
        distancesMat = new Mat(total,        2, CV_32F);

        // Copy descriptor into Mat object.
        FloatBuffer image1Buf = this.image1Mat.getFloatBuffer();
        for (int i = 0; i < image1Descriptors.length; i++)
        {
            image1Buf.put(image1Descriptors[i]);
        }

        FloatBuffer image2Buf = this.image2Mat.getFloatBuffer();
        for (int i = 0; i < image2Descriptors.length; i++)
        {
            image2Buf.put(image2Descriptors[i]);
        }

        if (context != null)
        {
            context.progress();
        }

        this.flannIndex   = new Index();
        this.indexParams  = new KDTreeIndexParams(4);
        this.searchParams = new SearchParams(64, 0, true);

        double percentageOfMatches = computePercentageOfMatches(total);

        if (context != null)
        {
            context.progress();
        }

        cvReleaseImage(image1Gray);
        cvReleaseImage(image2Gray);

        return Math.max(1.0 - percentageOfMatches, 0.0);
    }

    private double computePercentageOfMatches(int totalCount)
    {
        this.flannIndex.build(this.image1Mat, this.indexParams, FLANN_DIST_L2);
        this.flannIndex.knnSearch(this.image2Mat, this.indicesMat, this.distancesMat, 2, this.searchParams);

        // IntBuffer indicesBuf = indicesMat.getIntBuffer();
        int matchesCount = 0;
        FloatBuffer distsBuf = this.distancesMat.getFloatBuffer();
        for (int i = 0; i < totalCount; i++)
        {
            // System.out.format("%f, %f \n", distsBuf.get(2 * i), distsBuf.get(2 * i + 1));

            if (distsBuf.get(2 * i) < this.threshold * distsBuf.get(2 * i + 1))
            {
                ++matchesCount;
            }
        }

        return (double)matchesCount / (double)totalCount;
    }
}

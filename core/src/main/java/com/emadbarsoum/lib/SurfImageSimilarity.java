package com.emadbarsoum.lib;

import org.bytedeco.javacpp.helper.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_legacy.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_flann.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
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
    private CvSURFPoint[] image1Keypoints;
    private FloatBuffer[] image1Descriptors;
    private CvSURFPoint[] image2Keypoints;
    private FloatBuffer[] image2Descriptors;
    private double threshold = 0.6;

    public SurfImageSimilarity()
    {
        params.extended(1).hessianThreshold(300).nOctaves(3).nOctaveLayers(4);
    }

    public double computeDistance(IplImage image1, IplImage image2)
    {
        CvSeq keypoints1 = new CvSeq(null);
        CvSeq descriptors1 = new CvSeq(null);
        CvSeq keypoints2 = new CvSeq(null);
        CvSeq descriptors2 = new CvSeq(null);

        IplImage image1Gray = cvCreateImage(cvSize(image1.width(), image1.height()), IPL_DEPTH_8U, 1);
        IplImage image2Gray = cvCreateImage(cvSize(image2.width(), image2.height()), IPL_DEPTH_8U, 1);

        // Convert input images into a Gray images...
        cvCvtColor(image1, image1Gray, CV_BGR2GRAY);
        cvCvtColor(image2, image2Gray, CV_BGR2GRAY);

        CvMemStorage storage1 = CvMemStorage.create();
        cvClearMemStorage(storage1);
        cvExtractSURF(image1Gray, null, keypoints1, descriptors1, storage1, params, 0);

        CvMemStorage storage2 = CvMemStorage.create();
        cvClearMemStorage(storage2);
        cvExtractSURF(image2Gray, null, keypoints2, descriptors2, storage2, params, 0);

        int total1 = descriptors1.total();
        int size1 = descriptors1.elem_size();
        image1Keypoints = new CvSURFPoint[total1];
        image1Descriptors = new FloatBuffer[total1];
        for (int i = 0; i < total1; i++)
        {
            image1Keypoints[i] = new CvSURFPoint(cvGetSeqElem(keypoints1, i));
            image1Descriptors[i] = cvGetSeqElem(descriptors1, i).capacity(size1).asByteBuffer().asFloatBuffer();
        }

        int total2 = descriptors2.total();
        int size2 = descriptors2.elem_size();
        image2Keypoints = new CvSURFPoint[total2];
        image2Descriptors = new FloatBuffer[total2];
        for (int i = 0; i < total2; i++)
        {
            image2Keypoints[i] = new CvSURFPoint(cvGetSeqElem(keypoints2, i));
            image2Descriptors[i] = cvGetSeqElem(descriptors2, i).capacity(size2).asByteBuffer().asFloatBuffer();
        }

        int length   = image2Descriptors[0].capacity();
        image1Mat    = new Mat(total1, length, CV_32F);
        image2Mat    = new Mat(total2, length, CV_32F);
        indicesMat   = new Mat(total1,      2, CV_32S);
        distancesMat = new Mat(total1,      2, CV_32F);

        /*
        image1Mat    = CvMat.create(total, length, CV_32F, 1);
        image2Mat    = CvMat.create(total, length, CV_32F, 1);
        indicesMat   = CvMat.create(total,      2, CV_32S, 1);
        distancesMat = CvMat.create(total,      2, CV_32F, 1);
        */

        flannIndex   = new Index();
        indexParams  = new KDTreeIndexParams(4);
        searchParams = new SearchParams(64, 0, true);

        return computePercentageOfMatches();
    }

    private double computePercentageOfMatches()
    {
        image1Mat.rows(image1Descriptors.length);
        image2Mat.rows(image2Descriptors.length);

        // copy descriptors
        FloatBuffer image1Buf = image1Mat.getFloatBuffer();
        for (int i = 0; i < image1Descriptors.length; i++)
        {
            image1Buf.put(image1Descriptors[i]);
        }

        FloatBuffer image2Buf = image2Mat.getFloatBuffer();
        for (int i = 0; i < image2Descriptors.length; i++)
        {
            image2Buf.put(image2Descriptors[i]);
        }

        flannIndex.build(image1Mat, indexParams, FLANN_DIST_L2);
        flannIndex.knnSearch(image2Mat, indicesMat, distancesMat, 2, searchParams);

        // IntBuffer indicesBuf = indicesMat.getIntBuffer();
        int matchesCount = 0;
        FloatBuffer distsBuf = distancesMat.getFloatBuffer();
        for (int i = 0; i < image2Descriptors.length; i++)
        {
            // System.out.format("%f, %f \n", distsBuf.get(2 * i), distsBuf.get(2 * i + 1));

            if (distsBuf.get(2 * i) < this.threshold * distsBuf.get(2 * i + 1))
            {
                ++matchesCount;
            }
        }

        return (double)matchesCount / (double)image2Descriptors.length;
    }
}

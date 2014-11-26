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
public class SurfImageSimilarity
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

        int total = descriptors1.total();
        int size = descriptors1.elem_size();
        image1Keypoints = new CvSURFPoint[total];
        image1Descriptors = new FloatBuffer[total];
        for (int i = 0; i < total; i++)
        {
            image1Keypoints[i] = new CvSURFPoint(cvGetSeqElem(keypoints1, i));
            image1Descriptors[i] = cvGetSeqElem(descriptors1, i).capacity(size).asByteBuffer().asFloatBuffer();
        }

        total = descriptors2.total();
        size = descriptors2.elem_size();
        image2Keypoints = new CvSURFPoint[total];
        image2Descriptors = new FloatBuffer[total];
        for (int i = 0; i < total; i++)
        {
            image2Keypoints[i] = new CvSURFPoint(cvGetSeqElem(keypoints2, i));
            image2Descriptors[i] = cvGetSeqElem(descriptors2, i).capacity(size).asByteBuffer().asFloatBuffer();
        }

        int length   = image2Descriptors[0].capacity();
        image1Mat    = new Mat(total, length, CV_32F);
        image2Mat    = new Mat(total, length, CV_32F);
        indicesMat   = new Mat(total,      2, CV_32S);
        distancesMat = new Mat(total,      2, CV_32F);

        /*
        image1Mat    = CvMat.create(total, length, CV_32F, 1);
        image2Mat    = CvMat.create(total, length, CV_32F, 1);
        indicesMat   = CvMat.create(total,      2, CV_32S, 1);
        distancesMat = CvMat.create(total,      2, CV_32F, 1);
        */

        flannIndex   = new Index();
        indexParams  = new KDTreeIndexParams(4);
        searchParams = new SearchParams(64, 0, true);

        findPairs();

        return 5.0;
    }

    private void findPairs()
    {
        int length = image2Descriptors[0].capacity();

        int imageRows = image1Mat.rows();
        image1Mat.rows(image1Descriptors.length);

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

        IntBuffer indicesBuf = indicesMat.getIntBuffer();
        FloatBuffer distsBuf = distancesMat.getFloatBuffer();
        for (int i = 0; i < image2Descriptors.length; i++)
        {
            /*
            if (distsBuf.get(2*i) < settings.distanceThreshold*distsBuf.get(2*i+1))
            {
                ptpairs.add(i);
                ptpairs.add(indicesBuf.get(2*i));
            }
            */
        }

        image2Mat.rows(imageRows);
    }
}

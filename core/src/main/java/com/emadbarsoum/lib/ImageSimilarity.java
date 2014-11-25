package com.emadbarsoum.lib;

import org.bytedeco.javacpp.helper.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * ImageSimilarity compute similarity between 2 images using various algorithms.
 */
public class ImageSimilarity
{
    public double computeDistance(IplImage image1, IplImage image2)
    {
        return computeHistogramDistance(image1, image2, 128);
    }

    private double computeHistogramDistance(IplImage image1, IplImage image2, int numberOfBins)
    {
        float minRange = 0.0f;
        float maxRange = 255.0f;
        int dims = 1;
        int[] sizes = new int[]{numberOfBins};
        int histType = CV_HIST_ARRAY;
        float[] minMax = new float[]{minRange, maxRange};
        float[][] ranges = new float[][]{minMax};

        IplImageArray image1Split = splitChannels(image1);
        CvHistogram hist1 = cvCreateHist(dims, sizes, histType, ranges, 1);
        cvCalcHist(image1Split, hist1, 0, null);
        cvNormalizeHist(hist1, 1.0);

        IplImageArray image2Split = splitChannels(image2);
        CvHistogram hist2 = cvCreateHist(dims, sizes, histType, ranges, 1);
        cvCalcHist(image2Split, hist2, 0, null);
        cvNormalizeHist(hist2, 1.0);

        return cvCompareHist(hist1, hist2, CV_COMP_INTERSECT);
    }

    private IplImageArray splitChannels(IplImage image)
    {
        CvSize size = image.cvSize();
        int depth = image.depth();

        IplImage channel0 = IplImage.create(size, depth, 1);
        IplImage channel1 = IplImage.create(size, depth, 1);
        IplImage channel2 = IplImage.create(size, depth, 1);

        cvSplit(image, channel0, channel1, channel2, null);

        return new IplImageArray(channel0, channel1, channel2);
    }
}

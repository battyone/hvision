package com.emadbarsoum.lib;

import org.bytedeco.javacpp.helper.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * HistogramImageSimilarity compute similarity between 2 images using histogram.
 */
public class HistogramImageSimilarity implements ImageSimilarity
{
    private int numberOfBins = 128;

    public int getNumberOfBins()
    {
        return this.numberOfBins;
    }

    public void setNumberOfBins(int numberOfBins)
    {
        this.numberOfBins = numberOfBins;
    }

    public HistogramImageSimilarity()
    {}

    public double computeDistance(IplImage image1, IplImage image2)
    {
        float minRange = 0.0f;
        float maxRange = 255.0f;
        int dims = 1;
        int[] sizes = new int[]{this.numberOfBins};
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

        return Math.max(1.0 - cvCompareHist(hist1, hist2, CV_COMP_INTERSECT), 0.0);
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

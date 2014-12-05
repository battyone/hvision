package com.emadbarsoum.lib;

import org.apache.hadoop.mapreduce.Mapper.*;
import org.bytedeco.javacpp.opencv_core.*;

/**
 * Base interface for ImageSimilarity
 * which compute similarity between 2 images using various algorithms.
 */
public interface ImageSimilarity
{
    public double computeDistance(IplImage image1, IplImage image2, Context context);
}

package com.emadbarsoum.common;

import org.bytedeco.javacpp.opencv_core.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * A helper class that simplify dealing with JavaCV images.
 */
public class ImageHelper
{
    // Creating IplImage from a raw uncompressed image data.
    public static IplImage CreateIplImageFromRawBytes(byte[] imageData, int length, int width, int height, int channelCount, int depth)
    {
        IplImage image = IplImage.create(width, height, depth, channelCount);

        ByteBuffer buffer = image.getByteBuffer();
        byte[] rawBuffer = Arrays.copyOf(imageData, length);
        buffer.put(rawBuffer);

        return image;
    }
}

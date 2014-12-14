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
    public static IplImage createIplImageFromRawBytes(byte[] imageData, int length, MetadataParser metadata)
    {
        int width = metadata.getAsInt("width");
        int height = metadata.getAsInt("height");
        int channelCount = metadata.getAsInt("channel_count");
        int depth = metadata.getAsInt("depth");

        return createIplImageFromRawBytes(imageData, length, width, height, channelCount, depth);
    }

    // Creating IplImage from a raw uncompressed image data.
    public static IplImage createIplImageFromRawBytes(byte[] imageData, int length, int width, int height, int channelCount, int depth)
    {
        IplImage image = IplImage.create(width, height, depth, channelCount);

        ByteBuffer buffer = image.getByteBuffer();
        byte[] rawBuffer = Arrays.copyOf(imageData, length);
        buffer.put(rawBuffer);

        return image;
    }

    public static void serializeMat(String name, Mat mat, String path)
    {
        FileStorage storage = new FileStorage(path, FileStorage.WRITE);
        CvMat cvMat = mat.asCvMat();
        storage.writeObj(name, cvMat);
        storage.release();
    }

    public static Mat deserializeMat(String name, String path)
    {
        FileStorage storage = new FileStorage(path, FileStorage.READ);
        CvMat cvMat = new CvMat(storage.get(name).readObj());
        Mat mat = new Mat(cvMat);

        return mat;
    }

    public static MatData matToBytes(Mat mat)
    {
        return MatData.create(mat);
    }

    public static Mat bytesToMat(MatData matData)
    {
        return matData.toMat();
    }
}

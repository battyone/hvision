package com.emadbarsoum.common;

import java.util.Arrays;
import org.bytedeco.javacpp.opencv_core.*;

/**
 * A small container to help convert Mat type to Byte array and vice versa.
 */
public class MatData
{
    private int rows;
    private int cols;
    private int type;
    private byte[] data;

    private MatData()
    {}

    public int rows()
    {
        return this.rows;
    }

    public int cols()
    {
        return this.cols;
    }

    public int type()
    {
        return this.type;
    }

    public byte[] getBytes()
    {
        return this.data;
    }

    public static MatData create(Mat mat)
    {
        MatData matData = new MatData();

        matData.rows = mat.rows();
        matData.cols = mat.cols();
        matData.type = mat.type();

        matData.data = new byte[mat.rows()*mat.cols()*(int)(mat.elemSize())];
        mat.getByteBuffer().get(matData.data);

        return matData;
    }

    public static Mat createMat(byte[] data, int length, int rows, int cols, int type)
    {
        byte[] dd = Arrays.copyOfRange(data, 0, length);;

        Mat mat = new Mat(rows, cols, type);
        mat.getByteBuffer().put(dd);

        return mat;
    }

    public static Mat createMat(byte[] data, int rows, int cols, int type)
    {
        Mat mat = new Mat(rows, cols, type);
        mat.getByteBuffer().put(data);

        return mat;
    }

    public Mat toMat()
    {
        Mat mat = new Mat(this.rows, this.cols, this.type);
        mat.getByteBuffer().put(this.data);

        return mat;
    }
}

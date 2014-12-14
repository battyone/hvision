package com.emadbarsoum.common;

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

    public Mat toMat()
    {
        Mat mat = new Mat(this.rows, this.cols, this.type);
        mat.getByteBuffer().put(this.data);

        return mat;
    }
}

package com.emadbarsoum.lib;

import com.google.common.io.Files;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * ImageSequenceFileWriter provide a simple interface to create an image sequence file,
 * write images to a sequence file and append an existing image sequence file.
 */
public class ImageSequenceFileWriter
{
    private boolean compressed = false;
    private Configuration conf = null;
    private SequenceFile.Writer writer = null;

    public ImageSequenceFileWriter(Configuration conf, boolean compressed)
    {
        if (conf == null)
        {
            throw new IllegalArgumentException("conf can't be null");
        }

        this.conf = conf;
        this.compressed = compressed;
    }

    public void append(String imageFilePath) throws Exception
    {
        append(imageFilePath, null);
    }

    public void append(String imageFilePath, String additionMetadata) throws Exception
    {
        append(new File(imageFilePath), additionMetadata);
    }

    public void append(File imageFile) throws Exception
    {
        append(imageFile, null);
    }

    public void append(File imageFile, String additionMetadata) throws Exception
    {
        if (this.writer == null)
        {
            throw new Exception("Invalid State: create() must be called before append().");
        }

        int width = 0;
        int height = 0;
        int channelCount = 0;
        int depth = 0;
        byte[] fileData;

        if (this.compressed)
        {
            fileData = Files.toByteArray(imageFile);
        }
        else
        {
            IplImage image = cvLoadImage(imageFile.getAbsolutePath());

            width = image.width();
            height = image.height();
            channelCount = image.nChannels();
            depth = image.depth();

            ByteBuffer byteBuffer = image.getByteBuffer();
            fileData = new byte[byteBuffer.capacity()];
            byteBuffer.get(fileData);

            cvReleaseImage(image);
        }

        String name;
        String extension;
        String fileName = imageFile.getName();
        String metadata;

        int pos = fileName.lastIndexOf(".");
        if (pos > 0)
        {
            name = fileName.substring(0, pos);
            extension = fileName.substring(pos + 1, fileName.length()).toLowerCase();
            metadata = "name=" + name + ";ext=" + extension;
            if (!compressed)
            {
                metadata += ";type=raw" + ";width=" + width + ";height=" + height + ";channel_count=" + channelCount + ";depth=" + depth;
            }

            if ((additionMetadata != null) && !additionMetadata.isEmpty())
            {
                metadata += ";" + additionMetadata;
            }

            metadata += ";path=" + imageFile.getAbsolutePath();

            writer.append(new Text(metadata), new BytesWritable(fileData));
        }
    }

    public void create(String outputPath) throws IOException
    {
        close();

        this.writer = SequenceFile.createWriter(
                conf,
                SequenceFile.Writer.file(new Path(outputPath)),
                SequenceFile.Writer.keyClass(Text.class),
                SequenceFile.Writer.valueClass(BytesWritable.class));
    }

    public void close() throws IOException
    {
        if (this.writer != null)
        {
            this.writer.close();
            this.writer = null;
        }
    }
}

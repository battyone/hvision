package com.emadbarsoum.lib;

import com.emadbarsoum.common.MetadataParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.bytedeco.javacpp.BytePointer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * ImageSequenceFileReader provide a simple interface to read images from an image
 * sequence file.
 */
public class ImageSequenceFileReader
{
    private Configuration conf = null;
    private SequenceFile.Reader reader = null;
    private IplImage image = null;
    private String name;
    private String ext;

    public String name()
    {
        return this.name;
    }

    public String originalExt()
    {
        return this.ext;
    }

    public IplImage image()
    {
        return this.image;
    }

    public ImageSequenceFileReader(Configuration conf)
    {
        if (conf == null)
        {
            throw new IllegalArgumentException("conf can't be null");
        }

        this.conf = conf;
    }

    public boolean next() throws Exception
    {
        if (this.reader == null)
        {
            throw new Exception("Invalid State: open() must be called before next().");
        }

        Text key = new Text();
        BytesWritable value = new BytesWritable();

        if (this.reader.next(key, value))
        {
            MetadataParser metadata = new MetadataParser(key.toString());
            metadata.parse();

            this.name = metadata.get("name");
            this.ext = metadata.get("ext");

            if (metadata.has("type") && metadata.get("type").equals("raw"))
            {
                int width = metadata.getAsInt("width");
                int height = metadata.getAsInt("height");
                int channelCount = metadata.getAsInt("channel_count");
                int depth =  metadata.getAsInt("depth");

                if (this.image == null)
                {
                    this.image = IplImage.create(width, height, depth, channelCount);
                }
                else if (!((this.image.width() == width)   &&
                           (this.image.height() == height) &&
                           (this.image.depth() == depth)   &&
                           (this.image.nChannels() == channelCount)))
                {
                    cvReleaseImage(this.image);
                    this.image = IplImage.create(width, height, depth, channelCount);
                }

                ByteBuffer buffer = this.image.getByteBuffer();
                byte[] rawBuffer = Arrays.copyOf(value.getBytes(), value.getLength());
                buffer.put(rawBuffer);
            }
            else
            {
                if (this.image != null)
                {
                    cvReleaseImage(this.image);
                }

                this.image = cvDecodeImage(cvMat(1, value.getLength(), CV_8UC1, new BytePointer(value.getBytes())));
            }

            return true;
        }

        return false;
    }

    public void open(String inputPath) throws IOException
    {
        close();

        this.reader = new SequenceFile.Reader(
            this.conf,
            SequenceFile.Reader.file(new Path(inputPath)));
    }

    public void close() throws IOException
    {
        if (this.image != null)
        {
            cvReleaseImage(this.image);
            this.image = null;
        }

        if (this.reader != null)
        {
            this.reader.close();
            this.reader = null;
        }
    }
}

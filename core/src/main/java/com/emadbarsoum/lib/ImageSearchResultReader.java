package com.emadbarsoum.lib;

import com.emadbarsoum.common.MetadataParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * ImageSearchResultReader provide a simple interface to read images from the result sequence
 * file of image search MapReduce.
 */
public class ImageSearchResultReader
{
    private Configuration conf = null;
    private SequenceFile.Reader reader = null;
    private IplImage image = null;
    private String name;
    private String ext;
    private String path;

    public String name()
    {
        return this.name;
    }

    public String ext()
    {
        return this.ext;
    }

    public String path()
    {
        return this.path;
    }

    public IplImage image()
    {
        return this.image;
    }

    public ImageSearchResultReader(Configuration conf)
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

        DoubleWritable key = new DoubleWritable();
        Text value = new Text();

        if (this.reader.next(key, value))
        {
            MetadataParser metadata = new MetadataParser(value.toString());
            metadata.parse();

            this.name = metadata.get("name");
            this.ext = metadata.get("ext");
            this.path = metadata.get("path");

            if (this.image != null)
            {
                cvReleaseImage(this.image);
            }

            this.image = cvLoadImage(this.path);

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

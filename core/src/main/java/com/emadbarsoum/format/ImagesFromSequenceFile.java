package com.emadbarsoum.format;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * com.emadbarsoum.format.ImagesFromSequenceFile
 *
 */
public class ImagesFromSequenceFile
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.err.println("Usage: ImagesFromSequenceFile <input path to sequence file> <output folder>");
            System.exit(2);
        }

        Configuration conf = new Configuration();
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        File inputFile = new File(args[0]);
        Path inputPath = new Path(inputFile.getAbsolutePath());

        SequenceFile.Reader reader = new SequenceFile.Reader(
                conf,
                SequenceFile.Reader.file(inputPath));

        Text key = new Text();
        BytesWritable value = new BytesWritable();

        while (reader.next(key, value))
        {
            String outputPath = args[1] + "/" + key.toString();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath));
            out.write(value.getBytes(), 0, value.getLength());
            out.close();
        }

        reader.close();
    }
}

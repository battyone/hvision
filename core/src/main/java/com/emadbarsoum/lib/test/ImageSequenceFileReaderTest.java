package com.emadbarsoum.lib.test;

import com.emadbarsoum.common.CommandParser;
import com.emadbarsoum.lib.ImageSequenceFileReader;
import org.apache.hadoop.conf.Configuration;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * com.emadbarsoum.lib.test.ImageSequenceFileReaderTest.
 */
public class ImageSequenceFileReaderTest
{
    public static void main(String[] args) throws Exception
    {
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() != 2) ||
            !(parser.has("i") && parser.has("o")))
        {
            showUsage();
            System.exit(2);
        }

        Configuration conf = new Configuration();
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        File inputFile = new File(parser.get("i"));

        ImageSequenceFileReader reader = new ImageSequenceFileReader(conf);

        reader.open(inputFile.getAbsolutePath());

        while (reader.next())
        {
            String outputPath = parser.get("o") + "/" + reader.name() + "." + reader.originalExt();
            CvMat imageMat = cvEncodeImage("." + reader.originalExt(), reader.image());

            // Write the result...
            byte[] data = new byte[imageMat.size()];
            imageMat.getByteBuffer().get(data);

            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath));
            out.write(data, 0, data.length);
            out.close();

            cvReleaseMat(imageMat);
            imageMat = null;
        }

        reader.close();
    }

    private static void showUsage()
    {
        System.out.println("Usage: ImageSequenceFileReaderTest -i <input path to sequence file> -o <output folder>");
    }
}


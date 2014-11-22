package com.emadbarsoum.format;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;

import com.emadbarsoum.common.CommandParser;
import com.google.common.io.Files;
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
 * A simple command line tool that convert all images in a given folder into Hadoop sequence file.
 *
 * Here the main entry point: com.emadbarsoum.format.SequenceFileFromImages
 */
public class SequenceFileFromImages
{
    public static void main(String[] args) throws Exception
    {
        boolean compressed = false;
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

        File outputFile = new File(parser.get("o"));
        Path outputPath = new Path(outputFile.getAbsolutePath());

        SequenceFile.Writer writer = SequenceFile.createWriter(
                conf,
                SequenceFile.Writer.file(outputPath),
                SequenceFile.Writer.keyClass(Text.class),
                SequenceFile.Writer.valueClass(BytesWritable.class));

        // Iterate through image files only.
        FilenameFilter fileNameFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                if (name.lastIndexOf('.') > 0)
                {
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex).toLowerCase();

                    if (str.equals(".jpg") || str.equals(".png"))
                    {
                        return true;
                    }
                }
                return false;
            }
        };

        File[] files = new File(parser.get("i")).listFiles(fileNameFilter);
        for (File file : files)
        {
            if (file.isFile() && !file.isHidden())
            {
                byte[] fileData = null;
                if (compressed)
                {
                    fileData = Files.toByteArray(file);
                }
                else
                {
                    IplImage image = cvLoadImage(file.getAbsolutePath());

                    ByteBuffer byteBuffer = image.getByteBuffer();
                    fileData = new byte[byteBuffer.capacity()];
                    byteBuffer.get(fileData);

                    cvReleaseImage(image);
                }

                writer.append(new Text(file.getName()), new BytesWritable(fileData));
            }
        }

        writer.close();
    }

    private static void showUsage()
    {
        System.err.println("Usage: SequenceFileFromImages -i <input path to folder of images> -o <output path for sequence file>");
    }
}

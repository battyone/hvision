package com.emadbarsoum.format;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;

import com.emadbarsoum.common.CommandParser;
import com.emadbarsoum.lib.ImageSequenceFileWriter;
import com.google.common.io.Files;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * A simple command line tool that convert all images in a given folder into Hadoop sequence file.
 *
 * Here the main entry point: com.emadbarsoum.format.SequenceFileFromImages
 */
public class SequenceFileFromImages
{
    public static void main(String[] args) throws Exception
    {
        boolean compressed = true;
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() < 2)  ||
            !(parser.has("i") && parser.has("o")))
        {
            showUsage();
            System.exit(2);
        }

        // Should we store the images uncompressed in the sequence file.
        if (parser.has("raw"))
        {
            compressed = false;
        }

        Configuration conf = new Configuration();
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        File outputFile = new File(parser.get("o"));

        ImageSequenceFileWriter writer = new ImageSequenceFileWriter(conf, compressed);

        writer.create(outputFile.getAbsolutePath());

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

                    if (str.equals(".jpg") || str.equals(".jpeg") || str.equals(".png"))
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
                writer.append(file);
            }
        }

        writer.close();
    }

    private static void showUsage()
    {
        System.out.println("Usage: SequenceFileFromImages -i <input path to folder of images> -o <output path for sequence file> [-raw]");
    }
}

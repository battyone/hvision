package com.emadbarsoum.format;

import com.emadbarsoum.common.*;
import com.emadbarsoum.lib.ImageSearchResultReader;
import org.apache.hadoop.conf.Configuration;

import java.io.File;

import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * com.emadbarsoum.format.ImageSearchResultDump
 *
 */
public class ImageSearchResultDump
{
    public static void main(String[] args) throws Exception
    {
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() != 3) ||
            !(parser.has("i") && parser.has("o") && parser.has("top")))
        {
            showUsage();
            System.exit(2);
        }

        int numOfImages = parser.getAsInt("top");
        if (numOfImages < 1)
        {
            System.out.println("top must be greater than or equal to 1.");
            System.exit(2);
        }

        Configuration conf = new Configuration();
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        File inputFile = new File(parser.get("i"));

        ImageSearchResultReader reader = new ImageSearchResultReader(conf);

        reader.open(inputFile.getAbsolutePath());

        int fileIndex = 0;
        while (reader.next())
        {
            String outputPath = parser.get("o") + "/" + fileIndex + "." + reader.ext();

            cvSaveImage(outputPath, reader.image());

            ++fileIndex;

            if (fileIndex >= numOfImages)
            {
                break;
            }
        }

        reader.close();
    }

    private static void showUsage()
    {
        System.out.println("Usage: hvision isrdump -i <input path to sequence file> -o <output folder> -top <number of images>");
    }
}

package com.emadbarsoum.tools;

import com.emadbarsoum.common.CommandParser;
import com.emadbarsoum.lib.BOWCluster;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_nonfree;

import java.io.File;
import java.io.FilenameFilter;

import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * A command line tool that create Bag-Of-Words cluster from a set of images.
 *
 * Here the main entry point: com.emadbarsoum.tools.BOWTrainer
 */
public class BOWTrainer
{
    public static void main(String[] args) throws Exception
    {
        // Needed for SURF feature.
        Loader.load(opencv_nonfree.class);

        String[] nonOptional = {"i", "o", "c"};
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() < 3)  ||
            !parser.has(nonOptional))
        {
            showUsage();
            System.exit(2);
        }

        int clusterCount = parser.getAsInt("c");
        if (clusterCount < 2)
        {
            showUsage();
            System.exit(2);
        }

        BOWCluster bowCluster = new BOWCluster(clusterCount);

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
                Mat imageMat = imread(file.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                bowCluster.add(imageMat);
            }
        }

        bowCluster.cluster();
        bowCluster.save(parser.get("o"));
    }

    private static void showUsage()
    {
        System.out.println("Usage: hvision bowtrainer -i <input path to folder of images> -o <output path for model file> -c <number of cluster>");
    }
}

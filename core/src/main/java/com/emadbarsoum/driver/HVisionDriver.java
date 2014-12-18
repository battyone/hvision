package com.emadbarsoum.driver;

import com.emadbarsoum.map.*;
import com.emadbarsoum.mapreduce.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.util.ProgramDriver;

/**
 * HVision main entry point: com.emadbarsoum.driver.HVisionDriver
 *
 * The first argument specify which map\reduce job to run, based on this argument
 * we will send the proper map\reduce job to hadoop or locally using ProgramDriver.
 *
 * The remaining arguments are passed to the hadopp job.
 */
public final class HVisionDriver
{
    private static final Logger log = LoggerFactory.getLogger(HVisionDriver.class);

    private HVisionDriver() 
    {
    }

    public static void main(String[] args) throws Throwable
    {
        int exitCode = -1;

        if (args.length < 2)
        {
            showUsage();
            System.exit(2);
        }

        try
        {
            long start = System.currentTimeMillis();

            String[] remainingArgs = new String[args.length - 1];
            System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);

            // Non-hadoop tasks
            if (args[0].equals("iseq"))
            {
                com.emadbarsoum.format.SequenceFileFromImages.main(remainingArgs);
            }
            else if (args[0].equals("idump"))
            {
                com.emadbarsoum.format.ImagesFromSequenceFile.main(remainingArgs);
            }
            else if (args[0].equals("isrdump"))
            {
                com.emadbarsoum.format.ImageSearchResultDump.main(remainingArgs);
            }
            else if (args[0].equals("bowtrainer"))
            {
                com.emadbarsoum.tools.BOWTrainer.main(remainingArgs);
            }
            else if (args[0].equals("iseqlab"))
            {
                com.emadbarsoum.tools.SequenceFileFromLabeledImages.main(remainingArgs);
            }
            else if (args[0].equals("svmdump"))
            {
                com.emadbarsoum.tools.SVMModelsFromSequenceFile.main(remainingArgs);
            }
            // Hadoop tasks
            else
            {
                ProgramDriver programDriver = new ProgramDriver();

                // Map only tasks
                programDriver.addClass("gaussian", Gaussian.class, "Map task that blur a set of images using Gaussian filter.");
                programDriver.addClass("median", Median.class, "Map task that blur a set of images using Median filter.");
                programDriver.addClass("thumbnail", Thumbnail.class, "Map task that create thumbnails from a set of images.");
                programDriver.addClass("findfaces", FindFaces.class, "Map task that find all faces in each image.");
                programDriver.addClass("color2gray", Gaussian.class, "Map task that convert a set of colored images to monochrome images.");
                programDriver.addClass("dilate", Dilate.class, "Map task that dilate a set of images.");
                programDriver.addClass("erode", Erode.class, "Map task that erode a set of images.");

                // MapReduce tasks
                programDriver.addClass("imagesearch", ImageSearch.class, "MapReduce task that performs content based image search using various algorithms.");
                programDriver.addClass("facestat", FaceStat.class, "MapReduce task that summarize the number of faces per image.");
                programDriver.addClass("imagesearchtotal", ImageSearchTotalOrder.class, "MapReduce task that performs content based image search using various algorithms. The resulted sequence files can be concatenated.");
                programDriver.addClass("icbowtrain", ImageClassificationBOWTrainer.class, "MapReduce task that performs BOW training using SVM.");

                // Run the task
                programDriver.driver(args);
            }

            long duration = System.currentTimeMillis() - start;

            long durationInSeconds = duration / 1000;
            long durationInMinutes = durationInSeconds / 60;
            long durationInHours = durationInMinutes / 60;
            long durationInDays = durationInHours / 24;

            System.out.format("Time taken: %d days, %d hours, %d minutes, %d seconds.\n",
                    durationInDays,
                    durationInHours - 24 * durationInDays,
                    durationInMinutes - 60 * durationInHours,
                    durationInSeconds - 60 * durationInMinutes);

            exitCode = 0;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

        System.exit(exitCode);
    }

    private static void showUsage()
    {
        System.out.println("Usage: hvision <command> <args>");
    }
}

package com.emadbarsoum.driver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
        ProgramDriver programDriver = new ProgramDriver();

        try
        {
            // Map only tasks
            programDriver.addClass("gaussian", Gaussian.class, "Map task that blur a set of images using Gaussian filter.");
            programDriver.addClass("thumbnail", Thumbnail.class, "Map task that create thumbnails from a set of images.");

            // MapReduce tasks
            programDriver.addClass("imagesearch", ImageSearch.class, "MapReduce task that performs content based image search using various algorithms.");

            // Run the task
            programDriver.driver(args);

            exitCode = 0;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

        System.exit(exitCode);
    }
}

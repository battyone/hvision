package com.emadbarsoum.driver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.util.ProgramDriver;

/**
 * HVision main entry point.
 *
 * The first argument specify which map\reduce job to run, based on this argument
 * we will send the proper map\reduce job to hadoop or locally using ProgramDriver.
 *
 * The remaining arguments are passed to the hadopp job.
 */
public final class HVisionDriver
{
    private HVisionDriver() 
    {
    }

    public static void main( String[] args ) throws Throwable
    {
        System.out.println( "Hello World!" );
    }
}

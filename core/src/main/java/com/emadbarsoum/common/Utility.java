package com.emadbarsoum.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utiltiy class for common functions.
 */
public class Utility
{
    public static String readEntireTextFile(String path) throws IOException
    {
        byte[] fileData = Files.readAllBytes(Paths.get(path));
        return new String(fileData);
    }

    public static String readEntireTextFile(String path, Charset charset) throws IOException
    {
        byte[] fileData = Files.readAllBytes(Paths.get(path));
        return new String(fileData, charset);
    }
}

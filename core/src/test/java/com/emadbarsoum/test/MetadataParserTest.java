package com.emadbarsoum.test;

import com.emadbarsoum.common.MetadataParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.InvalidPropertiesFormatException;

/**
 * Unit test for MetadataParser.
 */
public class MetadataParserTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MetadataParserTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(MetadataParserTest.class);
    }

    /**
     */
    public void testValidMetadata()
    {
        MetadataParser parser = new MetadataParser("x=1;y=2");

        try
        {
            parser.parse();

            assertTrue(parser.has("x"));
            assertTrue(parser.has("y"));
            assertTrue(parser.get("x").equals("1"));
            assertTrue(parser.get("y").equals("2"));
        }
        catch (InvalidPropertiesFormatException e)
        {
            assertTrue(false);
        }
    }

    /**
     */
    public void testValidIntMetadata()
    {
        MetadataParser parser = new MetadataParser("w=640;h=480");

        try
        {
            parser.parse();

            assertTrue(parser.has("w"));
            assertTrue(parser.has("h"));
            assertTrue(parser.getAsInt("w") == 640);
            assertTrue(parser.getAsInt("h") == 480);
        }
        catch (InvalidPropertiesFormatException e)
        {
            assertTrue(false);
        }
    }

    /**
     */
    public void testInvalidMetadata()
    {
        MetadataParser parser = new MetadataParser("x;y=2");

        try
        {
            parser.parse();
        }
        catch (InvalidPropertiesFormatException e)
        {
            assertTrue(true);
        }
    }
}

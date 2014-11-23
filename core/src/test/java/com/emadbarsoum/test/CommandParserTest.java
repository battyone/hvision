package com.emadbarsoum.test;

import com.emadbarsoum.common.CommandParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for CommandParser.
 */
public class CommandParserTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CommandParserTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(CommandParserTest.class);
    }

    /**
     */
    public void testValidNamedValueArguments()
    {
        String[] args = {"-i", "/usr/local", "-o", "/usr/local/tmp"};
        CommandParser parser = new CommandParser(args);

        assertTrue(parser.parse());

        assertTrue(parser.has("i"));
        assertTrue(parser.has("o"));
        assertTrue(parser.get("i").equals("/usr/local"));
        assertTrue(parser.get("o").equals("/usr/local/tmp"));
    }

    /**
     */
    public void testValidNamedArguments()
    {
        String[] args = {"-i", "-o"};
        CommandParser parser = new CommandParser(args);

        assertTrue(parser.parse());

        assertTrue(parser.has("i"));
        assertTrue(parser.has("o"));
        assertNull(parser.get("i"));
        assertNull(parser.get("o"));
    }

    /**
     */
    public void testValidIntArguments()
    {
        String[] args = {"-w", "640", "-h", "480"};
        CommandParser parser = new CommandParser(args);

        assertTrue(parser.parse());

        assertTrue(parser.has("w"));
        assertTrue(parser.has("h"));
        assertTrue(parser.getAsInt("w") == 640);
        assertTrue(parser.getAsInt("h") == 480);
    }

    /**
     */
    public void testInvalidArguments()
    {
        String[] args = {"-i", "/usr/local", "/usr/local/tmp"};
        CommandParser parser = new CommandParser(args);

        assertFalse(parser.parse());
    }
}

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
    public void testValidArguments()
    {
        String[] args = {"-i", "/usr/local", "-o", "/usr/local/tmp"};
        CommandParser parser = new CommandParser(args);

        parser.parse();

        assertTrue(parser.has("i"));
        assertTrue(parser.has("o"));
        assertTrue(parser.get("i").equals("/usr/local"));
        assertTrue(parser.get("o").equals("/usr/local/tmp"));
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

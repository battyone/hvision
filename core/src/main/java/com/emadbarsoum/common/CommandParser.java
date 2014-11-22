package com.emadbarsoum.common;

import java.util.HashMap;
import java.util.Map;

/**
 * A small command line parser.
 */
public class CommandParser
{
    private String[] args;
    private Map<String, String> nameValueArgs = new HashMap<String, String>();

    public CommandParser(String[] args)
    {
        this.args = args;
    }

    public int getNumberOfArgs()
    {
        return this.nameValueArgs.size();
    }

    public boolean has(String name)
    {
        return this.nameValueArgs.containsKey(name);
    }

    public String get(String name)
    {
        return this.nameValueArgs.get(name);
    }

    public boolean parse()
    {
        int index = 0;

        while (index < this.args.length)
        {
            if (this.args[index].startsWith("-"))
            {
                String name = this.args[index].substring(1).toLowerCase();

                ++index;
                if (index >= this.args.length)
                {
                    return false;
                }

                this.nameValueArgs.put(name, this.args[index]);
            }
            else
            {
                return false;
            }

            ++index;
        }

        return true;
    }
}

package com.emadbarsoum.common;

import java.util.HashMap;
import java.util.Map;

/**
 * A small command line parser that support named value argument and named only argument
 * in the following format: <cmd> -name1 value1 -name2 value2 -name3
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

    public boolean has(String[] names)
    {
        for (String name : names)
        {
            if (!has(name))
            {
                return false;
            }
        }

        return true;
    }

    public String get(String name)
    {
        return this.nameValueArgs.get(name);
    }

    public int getAsInt(String name)
    {
        return Integer.parseInt(this.nameValueArgs.get(name));
    }

    public double getAsDouble(String name)
    {
        return Double.parseDouble(this.nameValueArgs.get(name));
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
                if ((index >= this.args.length) || this.args[index].startsWith("-"))
                {
                    this.nameValueArgs.put(name, null);
                    --index;
                }
                else
                {
                    this.nameValueArgs.put(name, this.args[index]);
                }
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

package com.emadbarsoum.common;

import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Map;

/**
 * Parser for the Key value of the sequence file. Key type is text and will contains metadata.
 */
public class MetadataParser
{
    private String metadata;
    private Map<String, String> nameValues = new HashMap<String, String>();

    public MetadataParser(String metadata)
    {
        this.metadata = metadata;
    }

    public boolean has(String name)
    {
        return this.nameValues.containsKey(name);
    }

    public String get(String name)
    {
        return this.nameValues.get(name);
    }

    public int getAsInt(String name)
    {
        return Integer.parseInt(this.nameValues.get(name));
    }

    public void remove(String name)
    {
        if (this.has(name))
        {
            this.nameValues.remove(name);
        }
    }

    public void put(String name, String value)
    {
        this.nameValues.put(name, value);
    }

    public String toMetadata()
    {
        String metadata = "";
        Iterator it = this.nameValues.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pairs = (Map.Entry)it.next();
            if (metadata.isEmpty())
            {
                metadata += pairs.getKey() + "=" + pairs.getValue();
            }
            else
            {
                metadata += ";" + pairs.getKey() + "=" + pairs.getValue();
            }
        }

        return metadata;
    }

    public void parse() throws InvalidPropertiesFormatException
    {
        String[] nameValueArray = this.metadata.split(";");
        for (String nameValue : nameValueArray)
        {
            String[] nameValueSplit = nameValue.split("=");
            if (nameValueSplit.length != 2)
            {
                throw new InvalidPropertiesFormatException("Invalid metadata...");
            }

            this.nameValues.put(nameValueSplit[0], nameValueSplit[1]);
        }
    }
}

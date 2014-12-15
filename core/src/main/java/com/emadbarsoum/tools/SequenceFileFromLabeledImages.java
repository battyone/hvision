package com.emadbarsoum.tools;

import com.emadbarsoum.common.CommandParser;
import com.emadbarsoum.lib.ImageSequenceFileWriter;
import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * A simple command line tool that convert all images in a given folder into Hadoop sequence file.
 *
 * Here the main entry point: com.emadbarsoum.tools.SequenceFileFromLabeledImages
 */
public class SequenceFileFromLabeledImages
{
    public static void main(String[] args) throws Exception
    {
        boolean compressed = true;
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() < 2)  ||
            !(parser.has("i") && parser.has("o")))
        {
            showUsage();
            System.exit(2);
        }

        // Should we store the images uncompressed in the sequence file.
        if (parser.has("raw"))
        {
            compressed = false;
        }

        Configuration conf = new Configuration();
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        File outputFile = new File(parser.get("o"));

        ImageSequenceFileWriter writer = new ImageSequenceFileWriter(conf, compressed);

        writer.create(outputFile.getAbsolutePath());

        // Iterate through image files only.
        FilenameFilter fileNameFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                if (name.lastIndexOf('.') > 0)
                {
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex).toLowerCase();

                    if (str.equals(".jpg") || str.equals(".jpeg") || str.equals(".png"))
                    {
                        return true;
                    }
                }
                return false;
            }
        };

        int labelCount = 0;
        ArrayList<String> labels = new ArrayList<String>();

        // Folder name are the target label.
        File[] folders = new File(parser.get("i")).listFiles();
        for (File folder : folders)
        {
            if (folder.isDirectory() && !folder.isHidden())
            {
                labels.add(folder.getName());

                labelCount++;
            }
        }

        int labelId = 0;
        for (String folderName : labels)
        {
            File[] files = new File(parser.get("i") + "/" + folderName).listFiles(fileNameFilter);
            for (File file : files)
            {
                if (file.isFile() && !file.isHidden())
                {
                    String metadata = "label=" + folderName + ";label_id=" + labelId + ";label_count=" + labelCount;
                    writer.append(file, metadata);
                }
            }

            labelId++;
        }

        writer.close();
    }

    private static void showUsage()
    {
        System.out.println("Usage: hvision iseqlab -i <input path to folder of images> -o <output path for sequence file> [-raw]");
    }
}

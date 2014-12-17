package com.emadbarsoum.tools;

import com.emadbarsoum.common.CommandParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A simple command line tool that dump all SVM XML model in a given folder from a Hadoop sequence file.
 *
 * Here the main entry point: com.emadbarsoum.tools.SVMModelsFromSequenceFile
 *
 */
public class SVMModelsFromSequenceFile
{
    public static void main(String[] args) throws Exception
    {
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() != 2) ||
            !(parser.has("i") && parser.has("o")))
        {
            showUsage();
            System.exit(2);
        }

        Configuration conf = new Configuration();
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        File inputFile = new File(parser.get("i"));
        Path inputPath = new Path(inputFile.getAbsolutePath());

        SequenceFile.Reader reader = new SequenceFile.Reader(
                conf,
                SequenceFile.Reader.file(inputPath));

        Text key = new Text();
        Text value = new Text();

        while (reader.next(key, value))
        {
            String outputPath = parser.get("o") + "/" + key.toString() + ".xml";
            Files.write(Paths.get(outputPath), value.toString().getBytes());
        }

        reader.close();
    }

    private static void showUsage()
    {
        System.out.println("Usage: hvision svmdump -i <input path to sequence file> -o <output folder>");
    }
}

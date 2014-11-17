package com.emadbarsoum.format;

import java.io.File;
import java.io.FilenameFilter;

import com.google.common.io.Files;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 * com.emadbarsoum.format.SequenceFilesFromImages
 *
 */
public class SequenceFilesFromImages
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.err.println("Usage: SequenceFilesFromImages <input path to folder of images> <output path for sequence file>");
            System.exit(2);
        }

        Configuration conf = new Configuration();
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        File outputFile = new File(args[1]);
        Path outputPath = new Path(outputFile.getAbsolutePath());

        SequenceFile.Writer writer = SequenceFile.createWriter(
                conf,
                SequenceFile.Writer.file(outputPath),
                SequenceFile.Writer.keyClass(Text.class),
                SequenceFile.Writer.valueClass(BytesWritable.class));

        // Iterate through image files only.
        // TODO: Add support for more image extensions.
        FilenameFilter fileNameFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                if (name.lastIndexOf('.') > 0)
                {
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex).toLowerCase();

                    if (str.equals(".jpg"))
                    {
                        return true;
                    }
                }
                return false;
            }
        };

        File[] files = new File(args[0]).listFiles(fileNameFilter);
        for (File file : files)
        {
            if (file.isFile() && !file.isHidden())
            {
                System.err.println(file.getName());
                Text key = new Text(file.getName());
                byte[] fileData = Files.toByteArray(file);

                writer.append(key, new BytesWritable(fileData));
            }
        }

        writer.close();
    }
}

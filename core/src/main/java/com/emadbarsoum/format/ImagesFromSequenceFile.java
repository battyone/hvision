package com.emadbarsoum.format;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.emadbarsoum.common.CommandParser;
import com.emadbarsoum.common.MetadataParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * com.emadbarsoum.format.ImagesFromSequenceFile
 *
 */
public class ImagesFromSequenceFile
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
        BytesWritable value = new BytesWritable();
        IplImage image = null;

        while (reader.next(key, value))
        {
            MetadataParser metadata = new MetadataParser(key.toString());
            metadata.parse();

            String outputPath = parser.get("o") + "/" + metadata.get("name") + "." + metadata.get("ext");
            if (metadata.has("type") && metadata.get("type").equals("raw"))
            {
                String ext = "." + metadata.get("ext");
                int width = metadata.getAsInt("width");
                int height = metadata.getAsInt("height");
                int channelCount = metadata.getAsInt("channel_count");
                int depth =  metadata.getAsInt("depth");

                if (image == null)
                {
                    image = IplImage.create(width, height, depth, channelCount);
                }
                else if (!((image.width() == width)   &&
                           (image.height() == height) &&
                           (image.depth() == depth)   &&
                           (image.nChannels() == channelCount)))
                {
                    cvReleaseImage(image);
                    image = IplImage.create(width, height, depth, channelCount);
                }

                ByteBuffer buffer = image.getByteBuffer();
                byte[] rawBuffer = Arrays.copyOf(value.getBytes(), value.getLength());
                buffer.put(rawBuffer);

                CvMat imageMat = cvEncodeImage(ext, image);

                // Write the result...
                byte[] data = new byte[imageMat.size()];
                imageMat.getByteBuffer().get(data);

                DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath));
                out.write(data, 0, data.length);
                out.close();

                cvReleaseMat(imageMat);
                imageMat = null;
            }
            else
            {
                DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath));
                out.write(value.getBytes(), 0, value.getLength());
                out.close();
            }
        }

        if (image != null)
        {
            cvReleaseImage(image);
            image = null;
        }

        reader.close();
    }

    private static void showUsage()
    {
        System.out.println("Usage: ImagesFromSequenceFile -i <input path to sequence file> -o <output folder>");
    }
}

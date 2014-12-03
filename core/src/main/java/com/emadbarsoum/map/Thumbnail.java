package com.emadbarsoum.map;

import java.io.IOException;

import com.emadbarsoum.common.CommandParser;
import com.emadbarsoum.common.MetadataParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Thumbnail is a Map task that create thumbnail images from a sequence file of images.
 * The output is another sequence file that contains the small files. The size of the thumbnail
 * is input as parameter.
 *
 * Entry: com.emadbarsoum.map.Thumbnail
 */
public class Thumbnail extends Configured implements Tool
{
    private static final Logger log = LoggerFactory.getLogger(Thumbnail.class);

    public static class ThumbnailMapper extends Mapper<Text, BytesWritable, Text, BytesWritable>
    {
        @Override
        public void map(Text key, BytesWritable value, Context context) throws IOException,InterruptedException
        {
            Configuration conf = context.getConfiguration();

            MetadataParser metadata = new MetadataParser(key.toString());
            metadata.parse();

            int size = conf.getInt("size", 120);
            int w = size;
            int h = size;

            if (metadata.has("type") && metadata.get("type").equals("raw"))
            {
                //TODO: Add raw processing.
            }
            else
            {
                IplImage sourceImage = cvDecodeImage(cvMat(1, value.getLength(), CV_8UC1, new BytePointer(value.getBytes())));
                if (sourceImage.width() > sourceImage.height())
                {
                    h = (w * sourceImage.height()) / sourceImage.width();
                }
                else
                {
                    w = (h * sourceImage.width()) / sourceImage.height();
                }

                IplImage targetImage = IplImage.create(w, h, sourceImage.depth(), sourceImage.nChannels());

                cvResize(sourceImage, targetImage);
                CvMat targetImageMat = cvEncodeImage("." + metadata.get("ext"), targetImage);

                // Write the result...
                byte[] data = new byte[targetImageMat.size()];
                targetImageMat.getByteBuffer().get(data);
                context.write(key, new BytesWritable(data));

                cvReleaseMat(targetImageMat);
                targetImage.release();
                cvReleaseImage(sourceImage);
            }
        }
    }

    @Override
    public final int run(final String[] args) throws Exception
    {
        Configuration conf = this.getConf();
        CommandParser parser = new CommandParser(args);
        parser.parse();

        conf.set("size", parser.get("size"));

        Job job = Job.getInstance(conf, "Thumbnail Creation");
        job.setJarByClass(Thumbnail.class);

        job.setMapperClass(ThumbnailMapper.class);
        job.setNumReduceTasks(0);

        // Input Output format
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(BytesWritable.class);

        FileInputFormat.addInputPath(job, new Path(parser.get("i")));
        FileOutputFormat.setOutputPath(job, new Path(parser.get("o")));

        boolean ret = job.waitForCompletion(true);
        return ret ? 0 : 1;
    }

    public static void main(String[] args) throws Exception
    {
        String[] nonOptional = {"i", "o", "size"};
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() != 3) ||
            !(parser.has(nonOptional)))
        {
            showUsage();
            System.exit(2);
        }

        ToolRunner.run(new Configuration(), new Thumbnail(), args);
    }

    private static void showUsage()
    {
        System.out.println("Arguments: -i <input path of the sequence file> -o <output path for sequence file> -size <resolution>");
    }
}

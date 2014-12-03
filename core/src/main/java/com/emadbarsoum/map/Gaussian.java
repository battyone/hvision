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
 * Gaussian is a Map task that smooth or blur all the images in the sequence file, using
 * Gaussian filter with its parameters given in the command line.
 *
 * Entry: com.emadbarsoum.map.Gaussian
 */
public class Gaussian extends Configured implements Tool
{
    private static final Logger log = LoggerFactory.getLogger(Gaussian.class);

    public static class GaussianMapper extends Mapper<Text, BytesWritable, Text, BytesWritable>
    {
        @Override
        public void map(Text key, BytesWritable value, Context context) throws IOException,InterruptedException
        {
            Configuration conf = context.getConfiguration();

            MetadataParser metadata = new MetadataParser(key.toString());
            metadata.parse();

            int size = conf.getInt("size", 3);
            double sigma = conf.getDouble("sigma", 1.0);

            if (metadata.has("type") && metadata.get("type").equals("raw"))
            {
                //TODO: Add raw processing.
            }
            else
            {
                IplImage image = cvDecodeImage(cvMat(1, value.getLength(), CV_8UC1, new BytePointer(value.getBytes())));

                cvSmooth(image, image, CV_GAUSSIAN, size, size, sigma, sigma);

                CvMat imageMat = cvEncodeImage("." + metadata.get("ext"), image);

                // Write the result...
                byte[] data = new byte[imageMat.size()];
                imageMat.getByteBuffer().get(data);
                context.write(key, new BytesWritable(data));

                cvReleaseMat(imageMat);
                cvReleaseImage(image);
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
        conf.set("sigma", parser.get("sigma"));

        Job job = Job.getInstance(conf, "Gaussian Blur");
        job.setJarByClass(Gaussian.class);

        job.setMapperClass(GaussianMapper.class);
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
        String[] nonOptional = {"i", "o", "size", "sigma"};
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() != 4) ||
            !(parser.has(nonOptional)))
        {
            showUsage();
            System.exit(2);
        }

        if (parser.getAsInt("size") < 3)
        {
            showUsage();
            System.exit(2);
        }

        ToolRunner.run(new Configuration(), new Gaussian(), args);
    }

    private static void showUsage()
    {
        System.out.println("Arguments: -i <input path of the sequence file> -o <output path for sequence file> -size <kernel size> -sigma <gaussian sigma>");
    }
}

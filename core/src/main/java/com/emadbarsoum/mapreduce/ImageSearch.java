package com.emadbarsoum.mapreduce;

import java.io.IOException;
import java.net.URI;

import com.emadbarsoum.common.CommandParser;
import com.emadbarsoum.common.MetadataParser;
import com.emadbarsoum.lib.ImageSimilarity;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
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
 *
 * A MapReduce task, that takes an input image and a sequence file that is composed of a database of images.
 * And it returns the same sequence file with its image sorted in such a way that the top ones are closed in similarity
 * to the input image.
 *
 * com.emadbarsoum.mapreduce.ImageSearch
 *
 */
public class ImageSearch extends Configured implements Tool
{
    private static final Logger log = LoggerFactory.getLogger(ImageSearch.class);

    public static class ImageSearchMapper extends Mapper<Text, BytesWritable, DoubleWritable, Text>
    {
        @Override
        public void map(Text key, BytesWritable value, Context context) throws IOException,InterruptedException
        {
            Configuration conf = context.getConfiguration();
            ImageSimilarity imageSimilarity = new ImageSimilarity();

            MetadataParser metadata = new MetadataParser(key.toString());
            metadata.parse();

            URI[] uriPaths = context.getCacheFiles();
            if (uriPaths.length > 0)
            {
                if (metadata.has("type") && metadata.get("type").equals("raw"))
                {
                    //TODO: Add raw processing.
                }
                else
                {
                    IplImage image = cvDecodeImage(cvMat(1, value.getLength(), CV_8UC1, new BytePointer(value.getBytes())));
                    IplImage queryImage = cvLoadImage("queryImageFile");

                    double distance = imageSimilarity.computeDistance(image, queryImage);
                    context.write(new DoubleWritable(distance), key);

                    // System.out.format("%f, %s \n", distance, metadata.get("name"));

                    cvReleaseImage(image);
                    cvReleaseImage(queryImage);

                    image = null;
                    queryImage = null;
                }
            }
        }
    }

    public static class ImageSearchReducer extends Reducer<DoubleWritable, Text, DoubleWritable, Text>
    {
        @Override
        public void reduce(DoubleWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
        {
            for (Text val : values)
            {
                context.write(key, val);
            }
        }
    }

    @Override
    public final int run(final String[] args) throws Exception
    {
        Configuration conf = this.getConf();
        CommandParser parser = new CommandParser(args);
        parser.parse();

        Job job = Job.getInstance(conf, "Image Search");
        job.setJarByClass(ImageSearch.class);

        job.setMapperClass(ImageSearchMapper.class);
        job.setCombinerClass(ImageSearchReducer.class);
        job.setReducerClass(ImageSearchReducer.class);

        // Input Output format
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setOutputKeyClass(DoubleWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(parser.get("i")));
        FileOutputFormat.setOutputPath(job, new Path(parser.get("o")));

        // Use symbolic link "queryImageFile" to support different platform formats
        // and protocols.
        job.addCacheFile(new URI(parser.get("q") + "#queryImageFile"));

        boolean ret = job.waitForCompletion(true);
        return ret ? 0 : 1;
    }

    public static void main(String[] args) throws Exception
    {
        Loader.load(opencv_nonfree.class);

        String[] nonOptional = {"i", "o", "q"};
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                 ||
            (parser.getNumberOfArgs() != 3) ||
            !(parser.has(nonOptional)))
        {
            showUsage();
            System.exit(2);
        }

        ToolRunner.run(new Configuration(), new ImageSearch(), args);
    }

    private static void showUsage()
    {
        System.out.println("Arguments: -i <input path of the sequence file> -q <query image> -o <output path for the result>");
    }
}

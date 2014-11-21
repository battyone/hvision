package com.emadbarsoum.map;

import java.io.IOException;

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
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
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
 * com.emadbarsoum.map.Thumbnail
 *
 */
public class Thumbnail extends Configured implements Tool
{
    private static final Logger log = LoggerFactory.getLogger(Thumbnail.class);

    public static class ThumbnailMapper extends Mapper<Text, BytesWritable, Text, BytesWritable>
    {
        @Override
        public void map(Text key, BytesWritable value, Context context) throws IOException,InterruptedException
        {
            IplImage sourceImage = cvDecodeImage(cvMat(1, value.getLength(), CV_8UC1, new BytePointer(value.getBytes())));
            // TODO: take the size as input.
            IplImage targetImage = IplImage.create(120, 120, sourceImage.depth(), sourceImage.nChannels());

            cvResize(sourceImage, targetImage);
            CvMat targetImageMat = cvEncodeImage(".jpg", targetImage); // TODO: use same extension as input file.

            // Write the result...
            byte[] data = new byte[targetImageMat.size()];
            targetImageMat.getByteBuffer().get(data);
            context.write(key, new BytesWritable(data));

            cvReleaseImage(targetImage);
        }
    }

    @Override
    public final int run(final String[] args) throws Exception
    {
        Configuration conf = this.getConf();

        Job job = Job.getInstance(conf, "Thumbnail Creation");
        job.setJarByClass(Thumbnail.class);

        job.setMapperClass(ThumbnailMapper.class);
        job.setNumReduceTasks(0);

        // Input Output format
        job.setInputFormatClass(SequenceFileInputFormat.class);
        //job.setOutputFormatClass(NullOutputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(BytesWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        boolean ret = job.waitForCompletion(true);
        return ret ? 0 : 1;
    }

    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        //conf.set("mapreduce.framework.name", "local");
        ToolRunner.run(conf, new Thumbnail(), args);
    }
}

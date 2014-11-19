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
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * com.emadbarsoum.map.Passthrough
 *
 * A dummy mapper that output the input, used for testing.
 */
public class Passthrough extends Configured implements Tool
{
    public static class PassthroughMapper extends Mapper<Text, BytesWritable, Text, BytesWritable>
    {
        @Override
        public void map(Text key, BytesWritable value, Context context) throws IOException,InterruptedException
        {
            // Write the result...
            context.write(key, new BytesWritable(value.getBytes()));
        }
    }

    @Override
    public final int run(final String[] args) throws Exception
    {
        Configuration conf = this.getConf();

        Job job = Job.getInstance(conf, "Passthrough");
        job.setJarByClass(Thumbnail.class);

        job.setMapperClass(PassthroughMapper.class);
        job.setNumReduceTasks(0);

        // Input Output format
        job.setInputFormatClass(SequenceFileInputFormat.class);
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
        ToolRunner.run(new Configuration(), new Passthrough(), args);
    }
}

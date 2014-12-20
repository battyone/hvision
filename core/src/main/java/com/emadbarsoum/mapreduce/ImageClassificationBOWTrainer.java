package com.emadbarsoum.mapreduce;

import com.emadbarsoum.common.*;
import com.emadbarsoum.lib.BOWCluster;
import com.emadbarsoum.lib.Tuple;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_ml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 *
 * A MapReduce task, that takes an input a BOW xml file contains the BOW cluster, and a sequence file that is composed of a database of images
 * and a metadata containing the target label. ImageClassificationBOWTrainer will train using SVM on this data and return another sequence file,
 * in which each row contains an XML model for one of the label.
 *
 * Entry: com.emadbarsoum.mapreduce.ImageClassificationBOWTrainer
 *
 */
public class ImageClassificationBOWTrainer extends Configured implements Tool
{
    private static final Logger log = LoggerFactory.getLogger(ImageClassificationBOWTrainer.class);

    public static class ImageClassificationBOWTrainerMapper extends Mapper<Text, BytesWritable, IntWritable, Tuple>
    {
        @Override
        public void map(Text key, BytesWritable value, Context context) throws IOException,InterruptedException
        {
            // Needed for SURF feature.
            Loader.load(opencv_nonfree.class);

            context.setStatus("Status: map started");

            Configuration conf = context.getConfiguration();

            MetadataParser metadata = new MetadataParser(key.toString());
            metadata.parse();

            String label = metadata.get("label");
            int labelId = metadata.getAsInt("label_id");
            int labelCount = metadata.getAsInt("label_count");
            int clusterCount = conf.getInt("cluster_count", labelCount);

            BOWCluster bowCluster = new BOWCluster(clusterCount);

            context.setStatus("Status: Metadata parsed");

            URI[] uriPaths = context.getCacheFiles();
            if (uriPaths.length > 0)
            {
                boolean isRaw = metadata.has("type") && metadata.get("type").equals("raw");
                String bowClusterPath = uriPaths[0].getPath();
                IplImage image;

                bowCluster.load(bowClusterPath);
                context.setStatus("Status: BOW Cluster loaded");
                context.progress();

                if (isRaw)
                {
                    image = ImageHelper.createIplImageFromRawBytes(value.getBytes(), value.getLength(), metadata);

                    context.setStatus("Status: Image loaded");
                    context.progress();
                }
                else
                {
                    image = cvDecodeImage(cvMat(1, value.getLength(), CV_8UC1, new BytePointer(value.getBytes())));

                    context.setStatus("Status: Image loaded");
                    context.progress();
                }

                IplImage grayImage = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);

                // Convert the input image into a gray image.
                cvCvtColor(image, grayImage, CV_BGR2GRAY);

                Mat imageMat = new Mat(grayImage.asCvMat());
                bowCluster.compute(imageMat);
                MatData matData = MatData.create(bowCluster.getBowDescriptor());

                context.setStatus("Status: BOW descriptor Computed");
                context.progress();

                for (int i = 0; i < labelCount; ++i)
                {
                    if (i == labelId)
                    {
                        Writable[] writables =
                            {
                                new BytesWritable(matData.getBytes()),
                                new IntWritable(matData.rows()),
                                new IntWritable(matData.cols()),
                                new IntWritable(matData.type()),
                                new Text(label),
                                new IntWritable(1)
                            };

                        context.write(new IntWritable(i), new Tuple(writables));
                    }
                    else
                    {
                        Writable[] writables =
                            {
                                new BytesWritable(matData.getBytes()),
                                new IntWritable(matData.rows()),
                                new IntWritable(matData.cols()),
                                new IntWritable(matData.type()),
                                new Text(""),
                                new IntWritable(-1)
                            };

                        context.write(new IntWritable(i), new Tuple(writables));
                    }

                    grayImage.release();
                }

                context.setStatus("Status: map completed");

                // Releasing the images...
                if (isRaw)
                {
                    image.release();
                }
                else
                {
                    cvReleaseImage(image);
                }
            }
        }
    }

    public static class ImageClassificationBOWTrainerReducer extends Reducer<IntWritable, Tuple, Text, Text>
    {
        @Override
        public void reduce(IntWritable key, Iterable<Tuple> values, Context context) throws IOException, InterruptedException
        {
            Mat x = new Mat();
            ArrayList<Integer> labelList = new ArrayList<Integer>();

            boolean first = true;
            String label = "";
            for (Tuple val : values)
            {
                BytesWritable matWritable = (BytesWritable)val.get(0);
                int rows = ((IntWritable)val.get(1)).get();
                int cols = ((IntWritable)val.get(2)).get();
                int type = ((IntWritable)val.get(3)).get();
                int target = ((IntWritable)val.get(5)).get();

                if (first && (target == 1))
                {
                    label = val.get(4).toString();
                    first = false;
                }

                Mat m = MatData.createMat(matWritable.getBytes(), matWritable.getLength(), rows, cols, type);

                x.push_back(m);
                labelList.add(target);

                m.release();
            }

            int rowIndex = 0;
            Mat labels = new Mat(labelList.size(), 1, CV_32FC1);
            for (Integer val : labelList)
            {
                labels.col(0).row(rowIndex).put(new Scalar(val.floatValue()));

                rowIndex++;
            }

            TermCriteria criteria = new TermCriteria(CV_TERMCRIT_ITER, 100, 1e-6);
            CvSVM svm = new CvSVM();
            CvSVMParams params = new CvSVMParams();
            params.svm_type(CvSVM.C_SVC);
            params.kernel_type(CvSVM.LINEAR);
            params.term_crit(criteria.asCvTermCriteria());

            boolean svmRet = svm.train(x, labels, new Mat(), new Mat(), params);
            if (svmRet)
            {
                // Is there a better way then to save it to a temporary file?
                svm.save(label + ".xml");

                String svmXml = Utility.readEntireTextFile(label + ".xml", StandardCharsets.UTF_8);
                context.write(new Text(label), new Text(svmXml));
            }
        }
    }

    @Override
    public final int run(final String[] args) throws Exception
    {
        Configuration conf = this.getConf();
        CommandParser parser = new CommandParser(args);
        parser.parse();

        if (parser.has("c"))
        {
            conf.set("cluster_count", parser.get("c"));
        }

        Job job = Job.getInstance(conf, "Image Classification BOW Trainer");
        job.setJarByClass(ImageClassificationBOWTrainer.class);

        job.setMapperClass(ImageClassificationBOWTrainerMapper.class);
        job.setReducerClass(ImageClassificationBOWTrainerReducer.class);

        // Input Output format
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Tuple.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(parser.get("i")));
        FileOutputFormat.setOutputPath(job, new Path(parser.get("o")));

        // Use symbolic link "bowClusterFile" to support different platform formats
        // and protocols.
        job.addCacheFile(new URI(parser.get("cf")));
        // job.addCacheFile(new URI(parser.get("cf") + "#bowClusterFile"));

        boolean ret = job.waitForCompletion(true);
        return ret ? 0 : 1;
    }

    public static void main(String[] args) throws Exception
    {
        String[] nonOptional = {"i", "o", "cf"};
        CommandParser parser = new CommandParser(args);
        if (!parser.parse()                ||
            (parser.getNumberOfArgs() < 3) ||
            !parser.has(nonOptional))
        {
            showUsage();
            System.exit(2);
        }

        ToolRunner.run(new Configuration(), new ImageClassificationBOWTrainer(), args);
    }

    private static void showUsage()
    {
        System.out.println("Usage: hvision icbowtrain -i <input path of the sequence file> -cf <BOW cluster file> -o <output path for the result> [-c <cluster count>]");
    }
}


HVision
=======
Hadoop Vision (HVision) is a vision based platform on the top of Apache Hadoop MapReduce. It exposes a lot of Computer Vision and image processing algorithms from the command line or Java program. All mappers and MapReducers can be run locally or on Hadoop cluster.

Run HVision local
-----------------
Inspired by Mahout to run HVision local, you need to set HVISION_LOCAL to any value in your environment variables or in your ".bash_profile" file.

For example, put the below line in your .bash_profile file:

    export HVISION_LOCAL="Local"

Build HVision
-------------
HVision uses maven build system, Java version 1.7 and OpenCV, so make sure you install them first. And offcourse you will need Hadoop if you plan to run HVision on Hadoop, for local mode you don't need Hadoop. 

To build HVision clone the depot or download the zip file from GitHub into your machine, and run the following:

    maven package -Dplatform.dependencies=true
    maven install -Dplatform.dependencies=true

Setting "platform.dependencies" is important to download JavaCV native dependencies.

Running HVision
---------------
To run most algorithms from the command line there is a bash script called "hvision" in the bin folder, that takes command as first arguments and the arguments of the command next. So let's run a number of examples:

Convert a folder of images into HVision compatible sequence file:

    ./bin/hvision iseq -i <image folder path> -o <output sequence file path>

Convert HVision sequence file back to images:

    ./bin/hvision idump -i <path to sequence file> -o <folder path of the result>

Create a thumbnails from a database of images stored in a sequence file:

    ./bin/hvision thumbnail -i <sequence file path> -o <folder path of the result> -size <thumbnail size in pixel>

Find faces on a database of images stored in a sequence file:

    ./bin/hvision findfaces -i <sequence file path> -o <folder path of the result> -m < model XML path i.e. haarcascade_frontalface_alt.xml>

Blur all images in a database of images stored in a sequence file using Gaussian:

    ./bin/hvision gaussian -i <sequence file path> -o <folder path of the result> -size <kernel size> -sigma <gaussian sigma>

Blur all images in a database of images stored in a sequence file using Median:

    ./bin/hvision gaussian -i <sequence file path> -o <folder path of the result> -size <kernel size>

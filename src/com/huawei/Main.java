package com.huawei;


import com.huawei.object.Car;
import com.huawei.object.Cross;
import com.huawei.object.Road;
import com.huawei.simulator.Simulator;
import com.huawei.utl.FormatUtils;
import com.huawei.utl.MapUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author : qiuyeliang
 * create at:  2019/3/9  12:41
 * @description:
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    public static void main(String[] args)
    {
//        if (args.length != 4) {
//            logger.error("please input args: inputFilePath, resultFilePath");
//            return;
//        }

        logger.info("Start...");
//
//        String carPath = args[0];
//        String roadPath = args[1];
//        String crossPath = args[2];
//        String answerPath = args[3];
        String carPath = "/Users/qiuyeliang/Desktop/map/config_11/car.txt";
        String roadPath = "/Users/qiuyeliang/Desktop/map/config_11/road.txt";
        String crossPath = "/Users/qiuyeliang/Desktop/map/config_11/cross.txt";
        String answerPath = "/Users/qiuyeliang/Desktop/map/config_11/answer.txt";
        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        // TODO:read input files
      //  logger.info("start read input files");
        mymain.run(carPath,roadPath,crossPath,answerPath);
        // TODO: calc

        // TODO: write answer.txt
      //  logger.info("Start write output file");

       // logger.info("End...");
    }
}

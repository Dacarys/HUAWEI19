package com.huawei;

import com.huawei.Factory.DataFactory;
import com.huawei.object.Car;
import com.huawei.object.Cross;
import com.huawei.object.Road;
import com.huawei.simulator.Simulator;
import com.huawei.utl.FormatUtils;
import com.huawei.utl.MapUtils;

import java.util.Map;

/**
 * @author : qiuyeliang
 * create at:  2019/3/16  13:36
 * @description:
 */
public class mymain {
    public static void initFactory(Map<Integer, Car> carMap, Map<Integer, Road> roadMap, Map<Integer, Cross> crossMap) {
        DataFactory.loadData(carMap, roadMap, crossMap);
    }
    public static void run(String carPath,String roadPath,String crossPath,String answerPath){

        //logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        Map<Integer, Car> carMap = FormatUtils.createCar(FormatUtils.load(carPath));
        Map<Integer, Road> roadMap = FormatUtils.createRoad(FormatUtils.load(roadPath));
        Map<Integer, Cross> crossMap = FormatUtils.createCross(FormatUtils.load(crossPath),roadMap);
        Simulator simulator = new Simulator(carMap,roadMap,crossMap, MapUtils.createCrossMap(crossMap,roadMap));
        Map<Integer,Car> cars = simulator.TheFirstStage();
        simulator.oneByOne(10);
        //simulator.randStartTime(5);
        FormatUtils.writeAnswer(answerPath,cars);

        //初始化工厂-杜
        initFactory(carMap, roadMap, crossMap);

        simulator.update();

    }
}

package com.huawei.simulator;

import com.huawei.controller.CRController;
import com.huawei.object.Car;
import com.huawei.object.CarStatus;
import com.huawei.object.Cross;
import com.huawei.object.Road;
import com.huawei.utl.FormatUtils;
import com.huawei.utl.MapUtils;

import java.util.*;

/**
 * @author : qiuyeliang
 * create at:  2019/3/9  18:04
 * @description: 进行模拟
 */
public class Simulator {
    private int currentdSystemTime = 0; //系统时间
    private PriorityQueue<Car> readyQueue; //等待上路的队列
    private Map<Integer,Car> cars;
    private Map<Integer,Road> roads;
    private Map<Integer,Cross> crosses;
    private int[][] crossMatrix;

    public Simulator(Map<Integer, Car> cars, Map<Integer, Road> roads, Map<Integer, Cross> crosses, int[][] crossMatrix) {
        this.cars = cars;
        this.roads = roads;
        this.crosses = crosses;
        this.crossMatrix = crossMatrix;
    }

    public Map<Integer,Car> TheFirstStage(){
        for(int i=1;i<crossMatrix.length;i++){
            for(int j=1;j<crossMatrix[i].length;j++) {
                System.out.print(crossMatrix[i][j]+",");
            }
            System.out.println();
        }
        for(Map.Entry<Integer,Car> map:cars.entrySet()){
            double[][] weightMatrix = MapUtils.createWeightMatrix(crossMatrix,crosses,roads,map.getValue());
            System.out.println("此车的id是:"+map.getValue().getId()+"出发时间是"+map.getValue().getPlantime()+"此车的出发地是:"+map.getValue().getFrom()+"此车的目的地是:"+map.getValue().getTo()+"速度是"+map.getValue().getSpeed());
            MapUtils.SPFA(crossMatrix,weightMatrix,map.getValue());
        }
        return cars;
        //MapUtils.SPFA(weightMap,start,end);
    }

    //按id降序排列
    public static Comparator<Car> readyComparator = new Comparator<Car>() {
        @Override
        public int compare(Car c1, Car c2) {
            if(c1.getPlantime() > c2.getPlantime())
                return 1;
            else if(c1.getPlantime() < c2.getPlantime())
                return -1;
            if(c1.getId() > c2.getId())
                return 1;
            else
                return -1;
        }
    };

    //一辆车一辆车走
    public void oneByOne(double rate){
        readyQueue = new PriorityQueue<>(readyComparator);
        for(Map.Entry<Integer,Car> map:cars.entrySet()){
            readyQueue.add(map.getValue());
        }
        int plusTime = readyQueue.peek().getCompleteTime() + readyQueue.peek().getPlantime();
        readyQueue.poll();
        while(!readyQueue.isEmpty()){
            Car car = readyQueue.poll();
            //System.out.println("车的ID是"+car.getId()+"出发时间是"+car.getPlantime()+"最短路径时间是"+car.getCompleteTime());
            //System.out.println(plusTime);
            car.setPlantime(plusTime);
            plusTime = (int)(rate * car.getCompleteTime()) + car.getPlantime();
        }
    }

    //将出发时间相同的车加上随机时间
    public void randStartTime(int repeat){
        Map<Integer,Integer> startTimeCount = new HashMap<>();
        Set<Integer> repeatSet = new HashSet<>();
        for(Map.Entry<Integer,Car> map:cars.entrySet()){
            Car car = map.getValue();
            if(startTimeCount.containsKey(car.getPlantime()))
                startTimeCount.put(car.getPlantime(),startTimeCount.get(car.getPlantime())+1);
            else
                startTimeCount.put(car.getPlantime(),1);
        }
        for(Map.Entry<Integer,Integer> map:startTimeCount.entrySet()){
            if(map.getValue() >= repeat)
                repeatSet.add(map.getKey());
        }
        for(Map.Entry<Integer,Car> map:cars.entrySet()){
            Car car = map.getValue();
            if(repeatSet.contains(car.getPlantime())) {
                car.setPlantime(car.getPlantime() + (int) (Math.random() * (10 - 1 + 1)));
            }
        }
    }

    public  List<Car> mapToList(){
        List<Car> list = new ArrayList<>();
        for(Map.Entry<Integer,Car> map:cars.entrySet()){
            list.add(map.getValue());
        }
        return list;
    }

    public void update() {
        readyQueue = new PriorityQueue<>(readyComparator); //先拿优先队列排序，出发时间早的在前面，相同出发时间的要看id大小
        for(Map.Entry<Integer,Car> map:cars.entrySet()){
            readyQueue.add(map.getValue());
        }
        Map<Integer,List<Car>> SerializationCar = new HashMap<>(); //将car按出发时间序列化
        while(!readyQueue.isEmpty()){
            Car car = readyQueue.poll();
            if(SerializationCar.containsKey(car.getPlantime())){
                List<Car> list = SerializationCar.get(car.getPlantime());
                list.add(car);
            }else{
                List<Car> carList = new ArrayList<>();
                carList.add(car);
                SerializationCar.put(car.getPlantime(),carList);
            }
        }
        CRController controller = CRController.getInstance();
        controller.init(SerializationCar.get(1)); //出发时间是1的车
        System.out.println("Size="+SerializationCar.get(1).size());
        int count = 1;
        while(!controller.finished()) {
            try {
                controller.update();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("counr=" + count);
                break;
            }
            List<Car> joinCars = SerializationCar.get(controller.getAllTime() + 1);
            System.out.println(controller.getAllTime());
            //System.out.println("SysTime="+controller.getAllTime()+"joinCars="+joinCars.size());
            if(joinCars != null && joinCars.size() != 0) {
                controller.addMapListCar(joinCars);
                System.out.println("Size="+joinCars.size());
                count+=joinCars.size();
            }
//    		controller.addMapCar(null); //加入要跑图的车子序列
        }
        System.out.println(controller.getAllTime());
    }




}

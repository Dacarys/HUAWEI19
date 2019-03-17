package com.huawei.utl;

import com.huawei.object.Car;
import com.huawei.object.Channel;
import com.huawei.object.Cross;
import com.huawei.object.Road;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : qiuyeliang
 * create at:  2019/3/9  20:25
 * @description: 格式化
 */
public class FormatUtils {
    public static List<String> load(String path){
        List<String> res = null;
        try{
            res = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            res = res.stream()
                    .filter(v->!v.contains("#")||v.length()<2)  //替换#号
                    .map(v->v.replaceAll("\\(|\\)|","")) //替换括号
                    .map(v->v.replaceAll(" ",""))   //替换空格
                    .collect(Collectors.toList());  //转为List
//            for(String a:res)
//                System.out.println(a);
        }catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }

    //将结果写入文件
    private final static String ansHead = "#(carId,startTime,RoadId...)";
    public static void writeAnswer(String path,Map<Integer,Car> cars) {
        try {
            File f = Paths.get(path).toFile();
            if (!f.exists()) f.createNewFile();
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
                bw.write(ansHead+"\n");
                for(Map.Entry<Integer,Car> map:cars.entrySet()){
                    Car car = map.getValue();
                    List list = car.getPlanWayList();
                    String listWay = "";
                    String stringAns = "("+car.getId()+","+car.getPlantime()+",";
                    for(int i=0;i<list.size();i++){
                        if(i != list.size()-1)
                            listWay = listWay + list.get(i) + ",";
                        else
                            listWay = listWay + list.get(i) + ")";
                    }
                    stringAns += listWay;
                    bw.write(stringAns + "\n");
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("写入已完成");
    }
    //构建Car对象
    public static Map<Integer,Car> createCar (List<String> lines){
        Map<Integer,Car> cars = new HashMap<>();
        for(String line:lines){
            int[] car = strToint(line.split(","));
            cars.put(car[0],new Car(car[0],car[1],car[2],car[3],car[4]));
        }
        return cars;
    }

    //构建Road对象
    public static Map<Integer, Road> createRoad (List<String> lines){
        Map<Integer,Road> roads = new HashMap<>();
        for(String line:lines){
            int[] road = strToint(line.split(","));
            for(int i=0;i<road[6];i++) {
                ArrayList<ArrayList<Car>> chennllist = new ArrayList<>();//channel[0]代表从from到to的通道，channel[1]代表从to到from的道路
                for (int j = 0; j < road[3]; j++) {
                    ArrayList<Car> chennelCarList = new ArrayList<>();
                    chennllist.add(chennelCarList);
                }
            }
            roads.put(road[0],new Road(road[0],road[1],road[2],road[3],road[4],road[5],road[6]));
        }
        return roads;
    }

    //构建Cross对象,这里需要检查这个节点是否能从这条道路出发,以及与这条路相连的crossId
    public static Map<Integer,Cross> createCross(List<String> lines,Map<Integer,Road> roadMap){
        Map<Integer,Cross> crosses = new HashMap<>();
        for(String line:lines){
            List<Integer> roadId_fromCross = new ArrayList<>();
            List<Integer> roadId_toCross = new ArrayList<>();
            int[] roads = new int[4];
            int[] cross = strToint(line.split(","));
            for(int i=1 ;i<5;i++){
                if(cross[i] == -1) {
                    roadId_fromCross.add(-1);
                    roadId_toCross.add(-1);
                    continue;
                }
                if(roadMap.containsKey(cross[i])){
                    Road road = roadMap.get(cross[i]);
                    if(road.getDuplex() == 1) {
                        roads[i - 1] = road.getId();
                        roadId_fromCross.add(road.getId());
                        roadId_toCross.add(road.getId());
                    }
                    else if(road.getFrom() == cross[0]) {
                        roads[i - 1] = road.getId();
                        roadId_fromCross.add(road.getId());
                        roadId_toCross.add(-1);
                    }else{
                        roadId_fromCross.add(-1);
                        roadId_toCross.add(road.getId());
                    }
                }
            }
            crosses.put(cross[0],new Cross(cross[0],roads,roadId_fromCross,roadId_toCross));
        }
        return crosses;
    }

    private static int[] strToint(String[] str){
        int[] res = new int[str.length];
        for(int i=0;i<res.length;i++)
            res[i] = Integer.parseInt(str[i]);
        return res;
    }
}

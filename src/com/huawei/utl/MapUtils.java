package com.huawei.utl;

import com.huawei.object.Car;
import com.huawei.object.Cross;
import com.huawei.object.Road;

import java.util.*;

/**
 * @author : qiuyeliang
 * create at:  2019/3/10  15:27
 * @description:
 */
public class MapUtils {
    public static int[][]createCrossMap(Map<Integer,Cross> crossMap, Map<Integer, Road>roadMap){
        int[][] crossMatrix = new int[crossMap.size()+1][crossMap.size()+1]; //路口的id没有0
        for(Map.Entry<Integer,Road> map:roadMap.entrySet()){
            Road road = map.getValue();  //路对象
            int start = road.getFrom(); //开始的路口
            int end = road.getTo();  //结束的路口
            int id = road.getId(); //道路的ID
            int duplex = road.getDuplex();//是否双向
            if(duplex == 1) { //如果双向
                crossMatrix[start][end] = id;
                crossMatrix[end][start] = id;
            }else//如果单向
                crossMatrix[start][end] = id;
        }
        return crossMatrix;
    }

    // 对每辆车都有一个自己的权重矩阵
    public static double[][] createWeightMatrix(int[][] crossMatrix, Map<Integer,Cross>crossMap, Map<Integer,Road>roadMap, Car car){
        double weightMatrix[][] = new double[crossMatrix.length][crossMatrix.length];
//        for(Map.Entry<Integer,Cross> map:crossMap.entrySet()){
//            int[] id = map.getValue().getRoads();
//        }
        int carSpeed = car.getSpeed(); //车的速度
        for(int i=1;i<crossMatrix.length;i++){
            for(int j=1;j<crossMatrix[i].length;j++){
                if( crossMatrix[i][j] == 0 || i == j) //如果路口不通或出发点与结束点一样
                    continue;
                int roadSpeed = roadMap.get(crossMatrix[i][j]).getSpeed(); //获取道路速度
                int roadlenth = roadMap.get(crossMatrix[i][j]).getLength(); //获取道路长度
                if(roadSpeed > carSpeed) {
                    weightMatrix[i][j] = roadlenth * 1.0 / carSpeed;
                }
                else {
                    weightMatrix[i][j] = roadlenth * 1.0 / roadSpeed;
                }
            }
        }
        return weightMatrix;
    }

    //利用SPFA算法找到最短路径，并且把最短路径存在对象car
    public static void SPFA(int[][] crossMatrix,double[][] weightMatrix,Car car){
        int crossNum = weightMatrix.length;
        double[] dist = new double[crossNum]; //源点到顶点i的最短距离
        int[] path = new int[crossNum]; //恢复路径用
        int start = car.getFrom();
        int end = car.getTo();
        List<Integer> planWay = new ArrayList<>();
        List<Integer> planCross = new ArrayList<>();
        Queue<Integer> queue = new ArrayDeque<>();
        Stack<Integer> stack = new Stack<>();
        queue.add(start);
        while(!queue.isEmpty()){
            int cross = queue.poll();
            for(int i=1;i<crossNum;i++){ //遍历所有结点
                if(i == start)
                    continue;
                if(weightMatrix[cross][i]!=0 && (weightMatrix[cross][i] + dist[cross] < dist[i] || dist[i] == 0)){
                    dist[i] = weightMatrix[cross][i] + dist[cross];
                    path[i] = cross;
                    queue.add(i);
                }
            }
        }
        System.out.println("最短路径(花费时间)"+dist[end]);
        car.setCompleteTime((int)dist[end]);
        int node =  end; //用于迭代路径
        stack.push(node);
        while(start != node){
            stack.push(path[node]);
            node = path[node];
        }
        while(!stack.empty()){
            planCross.add(stack.peek());
            System.out.println("-->" + stack.pop());
        }
        System.out.println("经过的道路");
        for(int i=1;i<planCross.size();i++){
            System.out.println("-->");
            System.out.println(crossMatrix[planCross.get(i-1)][planCross.get(i)]);
            planWay.add(crossMatrix[planCross.get(i-1)][planCross.get(i)]);
        }
        car.setPlanWayList(planWay);
        car.setPlanCrossList(planCross);
    }


    //找到两个cross之间道路的速度
    private static int roadspeed(int start,int end,Map<Integer,Cross> crossMap,Map<Integer,Road> roadMap){
        Cross cross = crossMap.get(start); //首先根据结点id拿到路口map
        int[] roadId = cross.getRoads();//取出路由四条道路的id
        int res = 0;
        for(int i=0;i<4;i++){ //遍历4个id
            if(roadId[i] == 0) //如果id为0说明路口的这个方向没有这条路
                continue;
            if((roadMap.get(roadId[i]).getFrom()==start && roadMap.get(roadId[i]).getDuplex()==1 )|| roadMap.get(roadId[i]).getTo() == end) {
                res = roadMap.get(roadId[i]).getSpeed();
                break;
            }
        }
        return res;
    }
}

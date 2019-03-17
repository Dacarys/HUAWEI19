package com.huawei.object;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : qiuyeliang
 * create at:  2019/3/9  13:10
 * @description: 车辆信息以及车辆状态
 */
public class Car implements Comparable<Car>{
    private Integer id;
    private int from;
    private int to;
    private int speed;
    private int plantime;
    private int realtime = -1;//实际的出发时间
    private int carstat = -1; //车子是否进入队列，-1表示没进入任何队列，0表示进入等待出发队列，1表示runing，2表示调度
    private int completeTime = 0;
    private List<Integer> planWayList;
    private List<Integer> planCrossList;
    private CarStatus carStatus;

    public Car(int id, int from, int to, int speed, int plantime) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.speed = speed;
        this.plantime = plantime;
    }

    public int getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(int completeTime) {
        this.completeTime = completeTime;
    }

    public void setPlanCrossList(List<Integer> planCrossList) {
        this.planCrossList = planCrossList;
    }

    public List<Integer> getPlanCrossList() {
        return planCrossList;
    }

    public int getCarstat() {
        return carstat;
    }

    public void setCarstat(int carstat) {
        this.carstat = carstat;
    }

    public List<Integer> getPlanWayList() {
        return planWayList;
    }

    public void setPlanWayList(List<Integer> planWayList) {
        this.planWayList = planWayList;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setPlantime(int plantime) {
        this.plantime = plantime;
    }

    public void setRealtime(int realtime) {
        this.realtime = realtime;
    }

    public void setCarStatus(CarStatus carStatus) {
        this.carStatus = carStatus;
    }

    public Integer getId() {
        return id;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getSpeed() {
        return speed;
    }

    public int getPlantime() {
        return plantime;
    }

    public int getRealtime() {
        return realtime;
    }

    public CarStatus getCarStatus() {
        if(this.carStatus == null)
            this.carStatus = new CarStatus();
        return carStatus;
    }

    @Override
    public String toString() {
        return "Car [id=" + id + ",carStatus=" + carStatus + ", from=" + from + ", to=" + to + ", speed=" + speed + ", plantime=" + plantime
                + ", realtime=" + realtime + ", planWayList=" + planWayList + "]";
    }

    @Override
    public int compareTo(Car o) {
        return this.id.compareTo(o.getId());
    }




}

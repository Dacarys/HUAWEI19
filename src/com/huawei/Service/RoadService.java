package com.huawei.Service;

import com.huawei.Status.Location;
import com.huawei.object.Car;
import com.huawei.object.CarStatus;
import com.huawei.object.Channel;
import com.huawei.object.Road;

public interface RoadService {
	
	/**
	 * 根据roadId获取road对象
	 * @param roadId
	 * @return
	 */
	public Road getRoadById(int roadId);
	/**
	 * 计算car在道路roadId下的车速
	 * @param roadId
	 * @param car
	 * @return
	 */
	public int calculate(int roadId, int vCar);
	/**
	 * car按照当前速度行驶是否超出当前道路
	 * @param roadId
	 * @param car
	 * @return true为超出, false为未超出
	 */
	public boolean isOut(int roadId, Location loc, int needLength);
	/**
	 * 获取car在道路roadId下向前行走一个单位时间碰到的车的id
	 * @param roadId
	 * @param car
	 * @return -1为无车
	 * @throws Exception 
	 */
	public CarStatus getSameChaCarId(Location location, int needLength) throws Exception;
	/**
	 * 获取与car相同方向比自己车道号小的车道上车的id
	 * @param roadId
	 * @param car
	 * @return -1位无车
	 * @throws Exception 
	 */
	public CarStatus getSmallChaCaId(int roadId, Location location) throws Exception;
	/**
	 * 获取道路上第一辆要进入路口toCrossId的车id
	 * @param roadId
	 * @param fromCrossId 车子从路口id进入道路
	 * @param toCrossId 车子进入道路终点路口id
	 * @return -1位无车可开
	 * @throws Exception 
	 */
	public Car getLastCarIdByCRId(int roadId, int toCrossId) throws Exception;
	/**
	 * 获取道路上fromCrossId->roadId方向,能放下状态为carStatus的车的位置信息, 只能得到同一个道路上直行的车
	 * @param carStatus
	 * @return null为没位置
	 */
	public CarStatus getCanOccupyStatus(CarStatus carStatus);
	/**
	 * 获取道路上roadId->toCrossId方向, 能放下状态为carStatus的车的位置信息, 适用于经过路口的车到达下一个道路
	 * @param distance
	 * @param carStatus parking为-1
	 * @return 
	 * @throws Exception 
	 */
	public CarStatus getFirstCanOccupyStatus(int distance, CarStatus carStaus) throws Exception;
	/**
	 * 获取抵达道路进入的入口id
	 * @param roadId
	 * @param fromCrossId
	 * @return
	 */
	public int getToCrossId(int roadId, int fromCrossId);
	/**
	 * 获取某个车道的第一或最后一辆车
	 * @param channel
	 * @param isFirst true为第一辆车, false为最后一辆车
	 * @return
	 */
	public CarStatus getFLCarIdWithChannel(Channel channel, boolean isFirst);
	/**
	 * car在道路roadId上行驶，也就是更新道路roadId上某个车道的某个具体位置信息
	 * @param roadId
	 * @param car
	 * @throws Exception 
	 */
	public void driveCar(int roadId, Car car);
	
	/**
	 * 清空道路在loc位置的信息
	 * @param channel
	 * @param loc
	 * @throws Exception 
	 */
	public void clearChannelByLoc(Location loc);
	
}

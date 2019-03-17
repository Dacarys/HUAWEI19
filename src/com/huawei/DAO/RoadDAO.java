package com.huawei.DAO;

import com.huawei.Factory.DataFactory;
import com.huawei.Status.Location;
import com.huawei.Status.RunStatus;
import com.huawei.object.Car;
import com.huawei.object.CarStatus;
import com.huawei.object.Channel;
import com.huawei.object.Road;

import java.util.List;
import java.util.Map;

public class RoadDAO {
	
	private Map<Integer, Road> roadMap = DataFactory.roadsMap;
	
	public Road getRoad(int roadId) {
		return roadMap.get(roadId);
	}
	
	public int getRoadV(int roadId) {
		return roadMap.get(roadId).getSpeed();
	}
	
	//获取公路roadId从fromCrossId->toCrossId方向的道路
	public List<Channel> getChannelByKey(int roadId, int fromCrossId, int toCrossId) {
		Road road = getRoad(roadId);
		return road.getChannelMap().get(fromCrossId+"-"+toCrossId);
	}
	
	//获取车道上loc位置的车id
	public CarStatus getChannelCarStatus(List<Channel> channels, Location loc) {
		Car car = channels.get(loc.getChannel()).getParkings().get(loc.getParking());
		if(car == null)
			return null;
		return car.getCarStatus();
	}
	
	//获取道路上其中一个车道的车通过下一个路口的第一辆车
	public Car getLastCarIdWithChannels(List<Channel> channels, int pSize) {
//		List<Car> parkings = channels.get(channel).getParkings();
		int cSize = channels.size();
		for(int i = pSize - 1; i >= 0; i--) {
			for(int j = 0; j < cSize; j++) {
				Car car = channels.get(j).getParkings().get(i);
				if(car != null) {
					if(car.getCarStatus().getRunStatus() == RunStatus.WAITING)
						return car;
					else
						return null;
				}
			}
		}
		
		
		return null;
	}
	
	/**
	 * 获取某个车道的第一辆车
	 * @param channel
	 * @return
	 */
	public CarStatus getFirstCarIdWithChannel(Channel channel) {
		for(Car car: channel.getParkings())
			if(car != null)
				return new CarStatus(car.getCarStatus());
		return null;
	}
	
	/**
	 * 获取某个车道的最后一辆车
	 * @param channel
	 * @return
	 */
	public CarStatus getLastCarIdWithChannel(Channel channel) {
		List<Car> parkings = channel.getParkings();
		for(int i = parkings.size() - 1; i >= 0; i--)
			if(parkings.get(i) != null)
				return new CarStatus(parkings.get(i).getCarStatus());
		return null;
	}
	
	//更新车道的loc位置的车位信息
	public void updateChanByLoc(List<Channel> channel, Car car) {
		
		Location loc = car.getCarStatus().getLocation();
		
		channel.get(loc.getChannel()).getParkings().set(loc.getParking(), car);
		
	}
	
	//清空车道的loc位置的车位信息
	public void clearChannelByLoc(List<Channel> channel, Location loc) {
		if(loc.getParking() < 0)
			return;
		try {
			channel.get(loc.getChannel()).getParkings().set(loc.getParking(), null);
		} catch (Exception e) {
			System.out.println(loc);
		}
	}
	
}

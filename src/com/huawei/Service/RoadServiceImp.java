package com.huawei.Service;

import com.huawei.DAO.RoadDAO;
import com.huawei.Status.Location;
import com.huawei.Status.RunStatus;
import com.huawei.object.Car;
import com.huawei.object.CarStatus;
import com.huawei.object.Channel;
import com.huawei.object.Road;

import java.util.List;

public class RoadServiceImp implements RoadService {
	
	RoadDAO roadDao = new RoadDAO();
	
	@Override
	public Road getRoadById(int roadId) {
		return roadDao.getRoad(roadId);
	}
	
	@Override
	public int calculate(int roadId, int vCar) {
		
		int speed = roadDao.getRoad(roadId).getSpeed();
		if(speed > vCar)
			speed = vCar;
		
		return speed;
	}
	
	@Override
	public boolean isOut(int roadId, Location loc, int needLength) {
		Road road = roadDao.getRoad(roadId);
		int parking = loc.getParking();
		int restLength = road.getLength() - 1 - parking;
		if(restLength <= 0 || restLength < needLength)
			return true;
		return false;
	}

	@Override
	public CarStatus getSameChaCarId(Location location, int needLength) throws Exception {
		int roadId = location.getRoadId();
		Location loc = new Location(location);
		List<Channel> channels = roadDao.getChannelByKey(roadId, loc.getFromCrossId(), loc.getToCrossId());
		int length = isOut(roadId, location, needLength) ? roadDao.getRoad(roadId).getLength() - 1 : loc.getParking() + needLength;
		for (int i = loc.getParking() + 1; i <= length; i++) {
			loc.setParking(i);
			CarStatus occupyCarStatus = roadDao.getChannelCarStatus(channels, loc);
			if(occupyCarStatus != null)
				return new CarStatus(occupyCarStatus);
		}
		
		return null;
	}
	
	/**
	 * 和getSameChaCarId差不多, 但是方向不一样, 使用与通过路口找空位信息
	 * @param location parking属性为-1
	 * @param needLength
	 * @return
	 */
//	public CarStatus getSameChanReverCarId(Location location, int needLength) {
//		int roadId = location.getRoadId();
//		Location loc = new Location(location);
//		List<Channel> channels = roadDao.getChannelByKey(roadId, loc.getFromCrossId(), loc.getToCrossId());
//		int length = isOut(roadId, location, needLength) ? roadDao.getRoad(roadId).getLength() - 1 : loc.getParking() + needLength;
//		CarStatus occupyStatus = null;
//		for (int i = length; i >= loc.getParking() + 1; i--) {
//			loc.setParking(i);
//			CarStatus s = roadDao.getChannelCarStatus(channels, loc);
//			if(s == null)
//				return occupyStatus;
//			occupyStatus = s;
//		}
//		
//		return occupyStatus;
//	}

	@Override
	public CarStatus getSmallChaCaId(int roadId, Location location) throws Exception {
		Location loc = new Location(location);
		List<Channel> channels = roadDao.getChannelByKey(roadId, loc.getFromCrossId(), loc.getToCrossId());
		for(int i = loc.getChannel() - 1; i >= 0; i--){
			loc.setChannel(i);
			CarStatus occupyCarStatus = roadDao.getChannelCarStatus(channels, loc);
			if(occupyCarStatus != null)
				return new CarStatus(occupyCarStatus);
		}
		
		return null;
	}

	@Override
	public Car getLastCarIdByCRId(int roadId, int toCrossId) throws Exception {
		
		Road road = roadDao.getRoad(roadId);
		int fromCrossId = road.getFrom();
		if(road.getFrom() == toCrossId)
			fromCrossId = road.getTo();
		List<Channel> channels = roadDao.getChannelByKey(roadId, fromCrossId, toCrossId);
		
		return roadDao.getLastCarIdWithChannels(channels, road.getLength());
	}
	
	public int getNumChannlesByRoadId(int roadId) {
		return getRoadById(roadId).getChannel();
	}
	
	@Override
	public CarStatus getCanOccupyStatus(CarStatus carStatus) {
		
		Location loc = carStatus.getLocation();
		CarStatus occupyCarStatus = null;
		//获取在当前车道行驶自己可以占用的空位
		try {
			occupyCarStatus = getSameChaCarId(loc, carStatus.getNowSpeed());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(occupyCarStatus != null)
			occupyCarStatus.getLocation().setParking(occupyCarStatus.getLocation().getParking() - 1);
		else
			return null;
		return new CarStatus(occupyCarStatus);
		
	}
	
	@Override
	public CarStatus getFirstCanOccupyStatus(int distance, CarStatus carStauts) throws Exception {
		
		Location loc = carStauts.getLocation();
		Road road = roadDao.getRoad(loc.getRoadId());
		CarStatus occupyStatus = null;
		boolean can = false;
		for(int i = 0; i < road.getChannel(); i++) {
			loc.setChannel(i);
			occupyStatus = getSameChaCarId(loc, distance);
			if(occupyStatus == null || occupyStatus.getLocation().getParking() != 0) {
				can = true;
				break;
			}
		}
		
		if(!can && occupyStatus.getRunStatus() == RunStatus.FINISHED)
			return null;
		
		if(occupyStatus == null) {
			carStauts.getLocation().setParking(distance - 1);
			return carStauts;
		}
		
		carStauts.getLocation().setParking(occupyStatus.getLocation().getParking() - 1);
		carStauts.setRunStatus(occupyStatus.getRunStatus());
		
		return carStauts;
		
		
	}
	
	@Override
	public int getToCrossId(int roadId, int fromCrossId) {
		Road road = getRoadById(roadId);
		if(road.getFrom() == fromCrossId)
			return road.getTo();
		
		return road.getFrom();
	}
	
	@Override
	public CarStatus getFLCarIdWithChannel(Channel channel, boolean isFirst) {
		
		if(isFirst)
			return roadDao.getFirstCarIdWithChannel(channel);
		else
			return roadDao.getLastCarIdWithChannel(channel);
	}

	@Override
	public void driveCar(int roadId, Car car){
		CarStatus carStatus = car.getCarStatus();
		Location loc = carStatus.getLocation();
//		int distance = carStatus.getNowSpeed();
//		loc.setParking(loc.getParking() + distance);
		List<Channel> channels = roadDao.getChannelByKey(roadId, loc.getFromCrossId(), loc.getToCrossId());
		roadDao.updateChanByLoc(channels, car);

	}

	@Override
	public void clearChannelByLoc(Location loc){
		
		List<Channel> channels = roadDao.getChannelByKey(loc.getRoadId(), loc.getFromCrossId(), loc.getToCrossId());
		roadDao.clearChannelByLoc(channels, loc);
		
	}
	
	

}

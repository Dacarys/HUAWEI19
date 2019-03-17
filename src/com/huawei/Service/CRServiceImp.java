package com.huawei.Service;

import com.huawei.Factory.ServiceFactory;
import com.huawei.Status.CarDirection;
import com.huawei.Status.Location;
import com.huawei.Status.RunStatus;
import com.huawei.object.Car;
import com.huawei.object.CarStatus;
import com.huawei.object.Road;

public class CRServiceImp implements CRService {
	
	RoadService roadService = null;
	CrossService crossService = null;
	CarService carService = null;
	
	public CRServiceImp() {
		roadService = ServiceFactory.roadService;
		crossService = ServiceFactory.crossService;
		carService = ServiceFactory.carService;
	}
	
	@Override
	public CarStatus getThroughCrossNextStatus(int distance, int nextRoadId, Car car) throws Exception {
		
		if(distance == 0)
			return null;
		
		Road road = roadService.getRoadById(nextRoadId);
		CarStatus carStatus = car.getCarStatus();
		Location loc = carStatus.getLocation();
		int toCrossId = loc.getToCrossId();
		int toCrossId2 = road.getFrom();
		if(toCrossId == road.getFrom())
			toCrossId2 = road.getTo();
		int next2RoadId = carService.getNext2RoadId(car);
		CarDirection direction = crossService.getDirection(toCrossId2, nextRoadId, next2RoadId);
		CarStatus s = new CarStatus(direction,
				new Location(nextRoadId, 0, -1, toCrossId, toCrossId2), RunStatus.FINISHED, roadService.calculate(nextRoadId, car.getSpeed()));
		
		return roadService.getFirstCanOccupyStatus(distance, s);
	}
	
	@Override
	public int getThroughCrossNextDis(CarStatus carStatus, int vCar, int nextRoadId) {
		
		int nowRoadId = carStatus.getLocation().getRoadId();
		Road nowRoad = roadService.getRoadById(nowRoadId);
		int nowRestLength = nowRoad.getLength() - 1 - carStatus.getLocation().getParking();
		int nowGetSpeed = roadService.calculate(nextRoadId, vCar);
		int nextLength = nowGetSpeed - nowRestLength;
		nextLength = nextLength < 0 ? 0 : nextLength;
		return nextLength;
		
	}
	
	@Override
	public CarStatus getBeginDepStatus(Car car) throws Exception {
		int nextRoadId = car.getPlanWayList().get(0);
		int distance = roadService.calculate(nextRoadId, car.getSpeed());
		car.getCarStatus().getLocation().setToCrossId(car.getFrom());

		return getThroughCrossNextStatus(distance, nextRoadId, car);
	}
	
	@Override
	public CarStatus getStraDepStatus(Car car) throws Exception {
		
		CarStatus carStatus = car.getCarStatus();
		
		int straNextRoadId = crossService.getNextRoadID(car);
		if(straNextRoadId == -1) {
			throw new Exception(car.getId() + " have path with problem" + car);
		}
		
		if(carStatus.getLocation().getRoadId() == straNextRoadId) {
			int rest = roadService.getRoadById(straNextRoadId).getLength() 
					- 1 - carStatus.getLocation().getParking();
			CarStatus occupy = roadService.getSameChaCarId(carStatus.getLocation(), rest);
			if(rest < carStatus.getNowSpeed() && occupy == null)
				return new CarStatus(true);
		}
		
		int distance = getThroughCrossNextDis(carStatus, car.getSpeed(), straNextRoadId);
		
		return getThroughCrossNextStatus(distance, straNextRoadId, car);
	}
	
	private CarStatus getDepCarStatus(int depRoadiD, CarStatus carStatus) throws Exception {
		Car car = roadService.getLastCarIdByCRId(depRoadiD, carStatus.getLocation().getToCrossId());
		if(car == null)
			return null;
		return car.getCarStatus();
	}

	@Override
	public CarStatus getLeftDepStatus(Car car) throws Exception {
		
		CarStatus carStatus = car.getCarStatus();
		int depRoaId = crossService.getDepRoadId(car, 0);
		if(depRoaId != -1) {
			CarStatus depCarStatus = getDepCarStatus(depRoaId, carStatus);
			if(depCarStatus != null
					&& depCarStatus.getDirection() == CarDirection.STRAIGHT 
					&& depCarStatus.getRunStatus() == RunStatus.WAITING)
				return null;
		}
			
		depRoaId = crossService.getNextRoadID(car);
		if(depRoaId == -1) {
			throw new Exception(car.getId() + " have path with problem");
		}
		int distance = getThroughCrossNextDis(carStatus, car.getSpeed(), depRoaId);
		
		return getThroughCrossNextStatus(distance, depRoaId, car);
	}

	@Override
	public CarStatus getRightDepStatus(Car car) throws Exception {
		
		CarStatus carStatus = car.getCarStatus();
		int depRoaId = crossService.getDepRoadId(car, 0);
		if(depRoaId != -1) {
			CarStatus depCarStatus = getDepCarStatus(depRoaId, carStatus);;
			if(depCarStatus != null 
					&& depCarStatus.getDirection() == CarDirection.LEFT
					&& depCarStatus.getRunStatus() == RunStatus.WAITING)
				return null;
			
		}
		
		depRoaId = crossService.getDepRoadId(car, 1);
		if(depRoaId != -1) {
			CarStatus depCarStatus = getDepCarStatus(depRoaId, carStatus);
			if(depCarStatus != null 
					&& depCarStatus.getDirection() == CarDirection.STRAIGHT 
					&& depCarStatus.getRunStatus() == RunStatus.WAITING)
				return null;
		}
		
		depRoaId = crossService.getNextRoadID(car);
		if(depRoaId == -1) {
			throw new Exception(car.getId() + " have path with problem" + car);
		}
		int distance = getThroughCrossNextDis(carStatus, car.getSpeed(), depRoaId);
		
		return getThroughCrossNextStatus(distance, depRoaId, car);
	}

	@Override
	public void driveCar(Car car, CarStatus carStatus) {
		
		Location loc = car.getCarStatus().getLocation();
		if(car.getCarStatus().getDirection() != CarDirection.NONE)
			roadService.clearChannelByLoc(loc);
		if(carStatus.getisFinished()) {
			car.setCarStatus(new CarStatus());
			return;
		}
		int nextRoadId = carStatus.getLocation().getRoadId();
		carService.updateCarStatus(car, carStatus);
		roadService.driveCar(nextRoadId, car);

	}

}

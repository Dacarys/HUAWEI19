package com.huawei.controller;

import com.huawei.Factory.ServiceFactory;
import com.huawei.Service.CRService;
import com.huawei.Service.CarService;
import com.huawei.Service.CrossService;
import com.huawei.Service.RoadService;
import com.huawei.Status.CarDirection;
import com.huawei.Status.Location;
import com.huawei.Status.RunStatus;
import com.huawei.object.Car;
import com.huawei.object.CarStatus;
import com.huawei.object.Cross;
import com.huawei.object.Road;

import java.util.*;

public class CRController {
	
	RoadService roadService = null;
	CrossService crossService = null;
	CarService carService = null;
	CRService crService = null;
	private List<Car> mapCar = null;
	private List<Car> waitingCar = null;
	private List<Car> finishedCar = null;
	private List<Car> allFinishedCar = null;
	private Map<Integer, Set<Car>> readyCross = null;
	int time = 0;
	public static CRController instance = null;
	int beforeFiniedLength = 0;
	
	public List<Car> getMapCar() {
		return mapCar;
	}

	private CRController(){
		roadService = ServiceFactory.roadService;
		crossService = ServiceFactory.crossService;
		carService = ServiceFactory.carService;
		crService = ServiceFactory.crService;
		mapCar = new ArrayList<Car>(); //地图上的所有车
		waitingCar = new ArrayList<Car>(); //等待本轮调度得车子
		finishedCar = new ArrayList<Car>(); //完成调本轮度得车子
		allFinishedCar = new ArrayList<Car>(); //完成所有路径的车
		readyCross = new HashMap<Integer, Set<Car>>();//车子始发地
		
	}
	
	public static CRController getInstance() {
		if(instance == null) {
			synchronized (CRController.class) {
				if(instance == null) {
					instance = new CRController();
				}
			}
		}
		
		return instance;
	}
	
	public void init(List<Car> initCars) {
		clearAll();
		mapCar.addAll(initCars);
		updateCarToWaiting();
	}
	/**
	 * 是否完成
	 * @return true为完成, false为未完成
	 */
	public boolean finished() {
		return allFinishedCar.size() == carService.getAllCarSize();
	}
	
	/**
	 * 刚开始初始化的时候需要调用的
	 */
	public void updateCarToWaiting() {
		addAllCarToWaiting();
		for(Car car: waitingCar) {
			carService.updateCarStatusBegin(car);
		}
	}
	
	/**
	 * 地图上加入car序列
	 * @param car
	 */
	public void addMapListCar(List<Car> cars) {
		waitingCar.addAll(cars);
		mapCar.addAll(cars);
	}

	/**
	 *
	 * @param car
	 */
	public void addMapOneCar(Car car) {
		waitingCar.add(car);
		mapCar.add(car);
	}
	
	/**
	 * 最终完成的car数量
	 * @return
	 */
	public int getAllFinishedSize() {
		return allFinishedCar.size();
	}
	
	public int getAllTime() {
		return time;
	}
	
	public void update() throws Exception {
		
		int beforeUpdateLength = -1;
		while(waitingCar.size() != 0) {
			beforeUpdateLength = finishedCar.size();
			beforeFiniedLength = allFinishedCar.size();
			Set<Integer> usedCrossId = updateRoad();
			updateCross(usedCrossId);
			if(beforeUpdateLength == finishedCar.size())
				throw new Exception("lock");
		}
		System.out.println("***update: finishedSize: " + finishedCar.size() + ",MapCarSize: " + mapCar.size());
		initStep();
		time++;
	}
	
	/**
	 * 更新直行车辆
	 * @return
	 * @throws Exception
	 */
	public Set<Integer> updateRoad() throws Exception{
		//车子走的过程中需要使用到的crossId
		Set<Integer> usedCrossId = new TreeSet<Integer>();
		for(Car car: waitingCar) {
			CarStatus carStatus = car.getCarStatus();
			if(carStatus.getDirection() == CarDirection.NONE) {
//				updateCarBegin(car);
				int crossId = car.getFrom();
				Set<Car> cars = readyCross.get(crossId);
				if(cars == null) {
					cars = new TreeSet<Car>();
					readyCross.put(crossId, cars);
				}
				cars.add(car);
				usedCrossId.add(crossId);
				continue;
			}
			Location loc = carStatus.getLocation();
			int roadId = loc.getRoadId();
			//获取向前走前方是否有车
			CarStatus occupyStatus = roadService.getCanOccupyStatus(carStatus);
			if(occupyStatus != null) {
				RunStatus nextRunStatus = occupyStatus.getRunStatus();
				if(nextRunStatus == RunStatus.WAITING)
					continue;
				occupyStatus.setDirection(carStatus.getDirection());
				occupyStatus.setNowSpeed(carStatus.getNowSpeed());
				driveCar(car, occupyStatus);
			}else{
				//获取向前走是否出道路
				boolean isOut = roadService.isOut(roadId, loc, carStatus.getNowSpeed());
				if(isOut) {
					usedCrossId.add(loc.getToCrossId());
					continue;
				}
				occupyStatus = new CarStatus(carStatus);
				occupyStatus.getLocation().setParking(loc.getParking() + carStatus.getNowSpeed());
				occupyStatus.setRunStatus(RunStatus.FINISHED);
				driveCar(car, occupyStatus);
			}
		}
		
		System.out.println("updateRoad : finishedSize: " + finishedCar.size() + ",MapCarSize: " + mapCar.size() + ",allFinishedSize: " + allFinishedCar.size());
		
		updateWatingCars();
		
		return usedCrossId;
		
	}
	
	public void updateCarBegin(Car car) throws Exception {
		CarStatus depStatus = crService.getBeginDepStatus(car);
		if(depStatus == null) {
			car.getCarStatus().setRunStatus(RunStatus.FINISHED);
			driveBeginCar(car);
			return;
		}
		Location loc = depStatus.getLocation();
		if(loc.getFromCrossId() == loc.getToCrossId()) {
			System.out.println(loc);
		}
		if(depStatus.getRunStatus() == RunStatus.WAITING)
			return;
		updateThroughCross(car, depStatus);
	}
	
	public void updateCross(Set<Integer> usedCrossId) throws Exception {
		
		List<Car> readyRemove = new ArrayList<Car>();
		for(Integer crossId: usedCrossId) {
			
			int beforeUpdateLength = -1;
			Cross cross = crossService.getCrossById(crossId);
			Set<Integer> roadId_toCross = new TreeSet<Integer>();
			roadId_toCross.addAll(cross.getRoadId_toCross());
			while(beforeUpdateLength != finishedCar.size()) {
				beforeUpdateLength = finishedCar.size();
				for(int roadId: roadId_toCross) {
					if(roadId == -1)
						continue;
					updateRoadThroughCross(crossId, roadId);
				}
			}
			
			Set<Car> readCars = readyCross.get(crossId);
			if(readCars == null || readCars.size() == 0)
				continue;
			for(Car car: readCars) {
				updateCarBegin(car);
				if(car.getCarStatus().getRunStatus() == RunStatus.FINISHED)
					readyRemove.add(car);
			}
			for(Car car : readyRemove) {
				readCars.remove(car);
			}
			readyRemove.clear();
		}
//		System.out.println("updateCross");
		System.out.println("updateCross : finishedSize: " + finishedCar.size() + ",MapCarSize: " + mapCar.size() + ",allFinishedSize: " + allFinishedCar.size());
		
		updateWatingCars();
		
	}
	
	public void updateRoadThroughCross(int toCrossId, int roadId) {
		try {
			int beforeUpdateLength = -1;
			while(beforeUpdateLength != finishedCar.size()) {
				beforeUpdateLength = finishedCar.size();
				Car car = roadService.getLastCarIdByCRId(roadId, toCrossId);
				if(car == null)
					return;
				CarDirection carDirection = car.getCarStatus().getDirection();
				if(carDirection == CarDirection.STRAIGHT) {
					updateStraThroughCross(car);
				}else if(carDirection == CarDirection.LEFT) {
					updateLeftThroughCross(car);
				}else if(carDirection == CarDirection.RIGHT) {
					updateRightThroughCross(car);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 通过路口
	 * @param car
	 */
	public void updateThroughCross(Car car, CarStatus nextStatus) {
		
		driveCar(car, nextStatus);
		
	}
	
	public void updateStraThroughCross(Car car) throws Exception {
		CarStatus depStatus = crService.getStraDepStatus(car);
		if(depStatus == null) {
			car.getCarStatus().setRunStatus(RunStatus.FINISHED);
			driveBeginCar(car);
			return;
		}
		
		if(depStatus.getRunStatus() == RunStatus.WAITING)
			return;
		updateThroughCross(car, depStatus);
	}
	
	/**
	 * 左拐通过路口
	 * @param car
	 * @throws Exception 
	 */
	public void updateLeftThroughCross(Car car) throws Exception {
		CarStatus depStatus = crService.getLeftDepStatus(car);
		if(depStatus == null) {
			car.getCarStatus().setRunStatus(RunStatus.FINISHED);
			driveBeginCar(car);
			return;
		}
		if(depStatus.getRunStatus() == RunStatus.WAITING)
			return;
		updateThroughCross(car, depStatus);
	}
	
	/**
	 * 右转通过路口
	 * @param car
	 * @throws Exception 
	 */
	public void updateRightThroughCross(Car car) throws Exception {
		
		CarStatus depStatus = crService.getRightDepStatus(car);
		if(depStatus == null) {
			car.getCarStatus().setRunStatus(RunStatus.FINISHED);
			driveBeginCar(car);
			return;
		}
		if(depStatus.getRunStatus() == RunStatus.WAITING)
			return;
		updateThroughCross(car, depStatus);
	}
	
	/**
	 * 获取经过路口后进入下一个车道可以行使的距离
	 * @param carStatus
	 * @param nextRoadId
	 * @return
	 */
	private int throughCrossNextDis(CarStatus carStatus, int nextRoadId) {
		int nowRoadId = carStatus.getLocation().getRoadId();
		Road nowRoad = roadService.getRoadById(nowRoadId);
		Road nextRoad = roadService.getRoadById(nextRoadId);
		int nowRestLength = nowRoad.getLength() - 1 - carStatus.getLocation().getParking();
		int nextLength = nextRoad.getSpeed() - nowRestLength;
		nextLength = nextLength < 0 ? 0 : nextLength;
		return nextLength;
	}
	
	public void driveBeginCar(Car car) {
		finishedCar.add(car);
	}
	
	/**
	 * 开车，清空当前车位信息，更新T+1时间的车位信息，更新车子的状态信息
	 * @param nowRoadId
	 * @param nextCarStatus
	 * @param car
	 * @throws Exception
	 */
	public void driveCar(Car car, CarStatus nextStatus){
//		Location loc = car.getCarStatus().getLocation();
//		roadService.clearChannelByLoc(nowRoadId, loc);
//		int nextRoadId = nextCarStatus.getLocation().getRoadId();
//		carService.updateParkingById(car, nextCarStatus);
//		roadService.driveCar(nextRoadId, car);
		if(nextStatus.getisFinished()) {
			mapCar.remove(car);
			allFinishedCar.add(car);
		}
//		if(car.getCarStatus().getDirection() == CarDirection.NONE) {
//			readyCross.get(car.getFrom()).remove(car);
//		}
		finishedCar.add(car);
		crService.driveCar(car, nextStatus);
	}
	
	public void updateWatingCars() {
		for(Car car: finishedCar) {
			if(waitingCar.contains(car))
				waitingCar.remove(car);
		}
	}
	
	private void addAllCarToWaiting() {
		waitingCar.addAll(mapCar);
	}
	
	private void initStep() {
		finishedCar.clear();
		for(Car car: mapCar)
			car.getCarStatus().setRunStatus(RunStatus.WAITING);
		addAllCarToWaiting();
	}
	
	public void clearAll(){
		mapCar.clear();
		waitingCar.clear();
		finishedCar.clear();
		allFinishedCar.clear();
		readyCross.clear();
		beforeFiniedLength = 0;
		time = 0;
	}
	
}

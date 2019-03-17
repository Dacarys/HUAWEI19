package com.huawei.Service;


import com.huawei.DAO.CarDAO;
import com.huawei.Status.CarDirection;
import com.huawei.Status.RunStatus;
import com.huawei.object.Car;
import com.huawei.object.CarStatus;

import java.util.List;

public class CarServiceImp implements CarService {
	
	CarDAO carDao = new CarDAO();
	
	@Override
	public Car getCarById(int carId) {
		return carDao.getCar(carId);
	}
	
	@Override
	public void updateCarStatus(Car car, CarStatus carStatus) {
		car.setCarStatus(carStatus);
	}

	@Override
	public int getNext2RoadId(Car car) {
		List<Integer> planWayList = car.getPlanWayList();
		int nowIndex = planWayList.indexOf(car.getCarStatus().getLocation().getRoadId());
		nowIndex += 2;
		if (nowIndex >= planWayList.size())
			return planWayList.get(planWayList.size() - 1);

		return planWayList.get(nowIndex);
	}

	@Override
	public int getAllCarSize() {
		return carDao.getAllSize();
	}

	@Override
	public void updateCarStatusBegin(Car car) {
		car.getCarStatus().setDirection(CarDirection.NONE);
		car.getCarStatus().setRunStatus(RunStatus.WAITING);
	}


}

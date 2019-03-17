package com.huawei.Service;

import com.huawei.object.Car;
import com.huawei.object.CarStatus;

public interface CarService {
	
	public Car getCarById(int carId);
	public int getAllCarSize();
	public void updateCarStatus(Car car, CarStatus carStatus);
	public int getNext2RoadId(Car car);
	public void updateCarStatusBegin(Car car);
	
}

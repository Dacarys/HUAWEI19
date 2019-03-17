package com.huawei.Status;

public enum CarDirection {
	
	NONE(-1),
	STRAIGHT(0),
	LEFT(1),
	RIGHT(2);
	private final int direction;
	private CarDirection(int direction){
		this.direction = direction;
	}
	public int getDirection() {
		return direction;
	}
}

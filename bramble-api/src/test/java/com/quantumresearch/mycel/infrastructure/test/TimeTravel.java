package com.quantumresearch.mycel.infrastructure.test;

public interface TimeTravel {

	void setCurrentTimeMillis(long now) throws InterruptedException;

	void addCurrentTimeMillis(long add) throws InterruptedException;
}

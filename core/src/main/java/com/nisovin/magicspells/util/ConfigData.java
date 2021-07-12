package com.nisovin.magicspells.util;

public class ConfigData {

	private Object startData;
	private Object endData;

	public ConfigData() {

	}

	public ConfigData(Object startData, Object endData) {
		this.startData = startData;
		this.endData = endData;
	}

	public Object getStartData() {
		return startData;
	}

	public Object getEndData() {
		return endData;
	}

}

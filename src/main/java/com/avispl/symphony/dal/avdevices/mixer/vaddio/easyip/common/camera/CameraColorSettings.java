/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum representing different Camera Color Settings and their corresponding descriptions.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/23/2023
 * @since 1.0.0
 */
public enum CameraColorSettings {
	AUTO_IRIS("AutoIris", "auto_iris(.*?)\r\n", "", ""),
	AUTO_WHITE_BALANCE("AutoWhiteBalance", "auto_white_balance(.*?)\r\n", "", ""),
	BACKLIGHT_COMPENSATION("BacklightCompensation", "backlight_compensation(.*?)\r\n", "", ""),
	BLUE_GAIN("BlueGain", "blue_gain(.*?)\r\n", "0", "255"),
	CHROMA("Chroma(Saturation)", "chroma(.*?)\r\n", "0", "14"),
	DETAIL("Detail(Sharpness)", "detail(.*?)\r\n", "0", "15"),
	GAIN("Gain(dB)", "gain(.*?)\r\n", "1", "12"),
	GAMMA("Gamma", "gamma(.*?)\r\n", "-64", "64"),
	IRIS("Iris", "iris(.*?)\r\n", "0", "21"),
	RED_GAIN("RedGain", "red_gain(.*?)\r\n", "0", "255"),
	WIDE_DYNAMIC_RANGE("WideDynamicRange", "wide_dynamic_range(.*?)\r\n", "", ""),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param command of {@link #command}
	 * @param minValue  value of {@link #minValue}
	 * @param maxValue  value of {@link #maxValue}
	 */
	CameraColorSettings(String name, String command, String minValue, String maxValue) {
		this.name = name;
		this.command = command;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	private String name;
	private String command;
	private String minValue;
	private String maxValue;

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #command}
	 *
	 * @return value of {@link #command}
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Retrieves {@link #minValue}
	 *
	 * @return value of {@link #minValue}
	 */
	public String getMinValue() {
		return minValue;
	}

	/**
	 * Retrieves {@link #maxValue}
	 *
	 * @return value of {@link #maxValue}
	 */
	public String getMaxValue() {
		return maxValue;
	}

	/**
	 * Retrieves the CameraColorSettings enum associated with the given name.
	 *
	 * @param name the name of the CameraColorSettings enum to search for
	 * @return the CameraColorSettings enum matching the given name
	 * @throws IllegalStateException if the provided name does not match any CameraColorSettings enum
	 */
	public static CameraColorSettings getByName(String name) {
		Optional<CameraColorSettings> property = Arrays.stream(CameraColorSettings.values()).filter(group -> group.getName().equals(name)).findFirst();
		if (property.isPresent()) {
			return property.get();
		} else {
			throw new IllegalStateException(String.format("control group %s is not supported.", name));
		}
	}
}

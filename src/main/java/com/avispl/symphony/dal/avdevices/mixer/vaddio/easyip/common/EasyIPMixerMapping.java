/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

import java.util.Arrays;

/**
 * Enum representing different mapping values and their corresponding descriptions.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 12/6/2023
 * @since 1.0.0
 */
public enum EasyIPMixerMapping {
	AUDIO_0("EasyIPCamera1", "2"),
	AUDIO_1("EasyIPCamera2", "3"),
	SYSTEM_VERSION("EasyIPCamera3", "4"),
	USB_VERSION("EasyIPCamera4", "5"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param value of {@link #value}
	 */
	EasyIPMixerMapping(String name, String value) {
		this.name = name;
		this.value = value;
	}

	private final String name;
	private final String value;

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Retrieves the value associated with the given name from the EasyIPMixerMapping enum.
	 *
	 * @param name the name of the property to search for in the EasyIPMixerMapping enum
	 * @return the value associated with the given name if found, or EasyIPMixerConstant.NONE if not found
	 */
	public static String getValueByName(String name) {
		EasyIPMixerMapping matchedEnum = Arrays.stream(EasyIPMixerMapping.values())
				.filter(definition -> definition.getName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : EasyIPMixerConstant.NONE;
	}

	/**
	 * Retrieves the name associated with the given value from the EasyIPMixerMapping enum.
	 *
	 * @param value the value of the property to search for in the EasyIPMixerMapping enum
	 * @return the name associated with the given value if found, or null if not found
	 */
	public static String getNameByValue(String value) {
		EasyIPMixerMapping matchedEnum = Arrays.stream(EasyIPMixerMapping.values())
				.filter(definition -> definition.getValue().equals(value))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getName() : null;
	}
}

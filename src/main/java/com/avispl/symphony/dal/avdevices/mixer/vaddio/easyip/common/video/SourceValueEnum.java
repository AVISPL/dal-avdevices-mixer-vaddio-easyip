/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.video;

import java.util.Arrays;

/**
 * Enum representing different source values and their corresponding descriptions.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/28/2023
 * @since 1.0.0
 */
public enum SourceValueEnum {
	INPUT_1("HDMI in", "input1"),
	INPUT_2("Easy IP 1", "input2"),
	INPUT_3("Easy IP 2", "input3"),
	INPUT_4("Easy IP 3", "input4"),
	INPUT_5("Easy IP 4", "input5"),

	;
	private final String name;
	private final String value;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param value of {@link #value}
	 */
	SourceValueEnum(String name, String value) {
		this.name = name;
		this.value = value;
	}

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
	 * Retrieves the value associated with the given name from the SourceValueEnum.
	 *
	 * @param name the name of the property to search for in the SourceValueEnum
	 * @return the value associated with the given name if found, or null if not found
	 */
	public static String getValueByName(String name) {
		SourceValueEnum matchedEnum = Arrays.stream(SourceValueEnum.values())
				.filter(definition -> definition.getName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : null;
	}

	/**
	 * Retrieves the name associated with the given value from the SourceValueEnum.
	 *
	 * @param value the value of the property to search for in the SourceValueEnum
	 * @return the name associated with the given value if found, or null if not found
	 */
	public static String getNameByValue(String value) {
		SourceValueEnum matchedEnum = Arrays.stream(SourceValueEnum.values())
				.filter(definition -> definition.getValue().equals(value))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getName() : null;
	}
}

/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera;

import java.util.Arrays;

/**
 * Enum representing different preset values and their corresponding descriptions.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/27/2023
 * @since 1.0.0
 */
public enum PresetValueEnum {
	PRESET_1("Preset 1", "1"),
	PRESET_2("Preset 2", "2"),
	PRESET_3("Preset 3", "3"),
	PRESET_4("Preset 4", "4"),
	PRESET_5("Preset 5", "5"),
	;
	private final String name;
	private final String value;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param value of {@link #value}
	 */
	PresetValueEnum(String name, String value) {
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
	 * Retrieves the value associated with the given name from the PresetValueEnum.
	 *
	 * @param name the name of the property to search for in the PresetValueEnum
	 * @return the value associated with the given name if found, or null if not found
	 */
	public static String getValueByName(String name) {
		PresetValueEnum matchedEnum = Arrays.stream(PresetValueEnum.values())
				.filter(definition -> definition.getName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : null;
	}
}

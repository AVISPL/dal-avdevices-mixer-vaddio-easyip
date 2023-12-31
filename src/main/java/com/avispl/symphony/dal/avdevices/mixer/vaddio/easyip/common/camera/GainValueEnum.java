/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera;

import java.util.Arrays;

import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIPMixerConstant;

/**
 * Enum representing different gain values and their corresponding descriptions.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 12/5/2023
 * @since 1.0.0
 */
public enum GainValueEnum {
	PRESET_1("0", "1"),
	PRESET_2("3", "2"),
	PRESET_3("6", "3"),
	PRESET_4("9", "4"),
	PRESET_5("12", "5"),
	PRESET_6("15", "6"),
	PRESET_7("18", "7"),
	PRESET_8("21", "8"),
	PRESET_9("24", "9"),
	PRESET_10("27", "10"),
	PRESET_11("30", "11"),
	PRESET_12("33", "12"),
			;
	private final String name;
	private final String value;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param value of {@link #value}
	 */
	GainValueEnum(String name, String value) {
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
	 * Retrieves the value associated with the given name from the GainValueEnum.
	 *
	 * @param name the name of the property to search for in the GainValueEnum
	 * @return the value associated with the given name if found, or EasyIPMixerConstant.NONE if not found
	 */
	public static String getValueByName(String name) {
		GainValueEnum matchedEnum = Arrays.stream(GainValueEnum.values())
				.filter(definition -> definition.getName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : EasyIPMixerConstant.NONE;
	}

	/**
	 * Retrieves the name associated with the given value from the GainValueEnum.
	 *
	 * @param value the value of the property to search for in the GainValueEnum
	 * @return the name associated with the given value if found, or null if not found
	 */
	public static String getNameByValue(String value) {
		GainValueEnum matchedEnum = Arrays.stream(GainValueEnum.values())
				.filter(definition -> definition.getValue().equals(value))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getName() : null;
	}
}

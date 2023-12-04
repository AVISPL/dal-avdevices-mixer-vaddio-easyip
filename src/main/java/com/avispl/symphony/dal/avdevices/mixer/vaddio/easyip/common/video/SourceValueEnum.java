package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.video;

import java.util.Arrays;

/**
 * SourceValueEnum
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/28/2023
 * @since 1.0.0
 */
public enum SourceValueEnum {
	INPUT_1("HDMI in", "input1"),
	INPUT_2("Input 2", "input2"),
	INPUT_3("Input 2", "input3"),
	INPUT_4("Input 2", "input4"),
	INPUT_5("Input 2", "input5"),

	;
	private final String name;
	private final String value;

	/**
	 *
	 * @param name
	 * @param value
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

	public static String getValueByName(String name) {
		SourceValueEnum matchedEnum = Arrays.stream(SourceValueEnum.values())
				.filter(definition -> definition.getName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : null;
	}

	public static String getNameByValue(String value) {
		SourceValueEnum matchedEnum = Arrays.stream(SourceValueEnum.values())
				.filter(definition -> definition.getValue().equals(value))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getName() : null;
	}
}

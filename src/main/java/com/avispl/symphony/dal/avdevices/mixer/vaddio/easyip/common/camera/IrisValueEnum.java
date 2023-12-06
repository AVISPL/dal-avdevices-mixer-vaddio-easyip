package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera;

import java.util.Arrays;

/**
 * IrisValueEnum
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 12/5/2023
 * @since 1.0.0
 */
public enum IrisValueEnum {
	PRESET_0("Close", "0"),
	PRESET_1("f/11", "1"),
	PRESET_2("f/10", "2"),
	PRESET_3("f/9.6", "3"),
	PRESET_4("f/8.7", "4"),
	PRESET_5("f/8.0", "5"),
	PRESET_6("f/7.3", "6"),
	PRESET_7("f/6.8", "7"),
	PRESET_8("f/6.2", "8"),
	PRESET_9("f/5.6", "9"),
	PRESET_10("f/5.2", "10"),
	PRESET_11("f/4.8", "11"),
	PRESET_12("f/4.4", "12"),
	PRESET_13("f/4.0", "13"),
	PRESET_14("f/3.7", "14"),
	PRESET_15("f/3.4", "15"),
	PRESET_16("f/3.1", "16"),
	PRESET_17("f/2.8", "17"),
	PRESET_18("f/2.6", "18"),
	PRESET_19("f/2.4", "19"),
	PRESET_20("f/2.0", "20"),
	PRESET_21("f/2.0", "21"),
	;
	private final String name;
	private final String value;

	/**
	 * Creates a new BatteryStatusEnum with the specified name and value.
	 *
	 * @param name The name of the battery status.
	 * @param value The corresponding value of the battery status.
	 */
	IrisValueEnum(String name, String value) {
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
	 * Retrieves the name associated with the given value from the IrisValueEnum.
	 *
	 * @param value the value of the property to search for in the IrisValueEnum
	 * @return the name associated with the given value if found, or null if not found
	 */
	public static String getNameByValue(String value) {
		IrisValueEnum matchedEnum = Arrays.stream(IrisValueEnum.values())
				.filter(definition -> definition.getValue().equals(value))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getName() : null;
	}
}

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera;

import java.util.Arrays;

/**
 * PresetValueEnum
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
	PRESET_6("Preset 6", "6"),
	PRESET_7("Preset 7", "7"),
	PRESET_8("Preset 8", "8"),
	PRESET_9("Preset 9", "9"),
	PRESET_10("Preset 10", "10"),
	PRESET_11("Preset 11", "11"),
	PRESET_12("Preset 12", "12"),
	PRESET_13("Preset 13", "13"),
	PRESET_14("Preset 14", "14"),
	PRESET_15("Preset 15", "15"),
	PRESET_16("Preset 16", "16"),
	;
	private final String name;
	private final String value;

	/**
	 * Creates a new BatteryStatusEnum with the specified name and value.
	 *
	 * @param name  The name of the battery status.
	 * @param value The corresponding value of the battery status.
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

	public static String getValueByName(String name) {
		PresetValueEnum matchedEnum = Arrays.stream(PresetValueEnum.values())
				.filter(definition -> definition.getName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : null;
	}
}

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera;

import java.util.Arrays;

/**
 * CCUSceneValueEnum
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 11/27/2023
 * @since 1.0.0
 */
public enum CCUSceneValueEnum {
	FACTORY_1("Factory 1", "factory 1"),
	FACTORY_2("Factory 2", "factory 2"),
	FACTORY_3("Factory 3", "factory 3"),
	FACTORY_4("Factory 4", "factory 4"),
	FACTORY_5("Factory 5", "factory 5"),
	FACTORY_6("Factory 6", "factory 6"),
	CUSTOM_1("Custom 1", "custom 1"),
	CUSTOM_2("Preset 2", "custom 2"),
	CUSTOM_3("Preset 3", "custom 3"),

	;
	private final String name;
	private final String value;

	/**
	 * Creates a new BatteryStatusEnum with the specified name and value.
	 *
	 * @param name  The name of the battery status.
	 * @param value The corresponding value of the battery status.
	 */
	CCUSceneValueEnum(String name, String value) {
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
	 * Retrieves the value associated with the given name from the CCUSceneValueEnum.
	 *
	 * @param name the name of the property to search for in the CCUSceneValueEnum
	 * @return the value associated with the given name if found, or null if not found
	 */
	public static String getValueByName(String name) {
		CCUSceneValueEnum matchedEnum = Arrays.stream(CCUSceneValueEnum.values())
				.filter(definition -> definition.getName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : null;
	}
}

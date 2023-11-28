package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

/**
 * VersionInformation
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/21/2023
 * @since 1.0.0
 */
public enum VersionInformation {
	AUDIO("AudioVersion", "Audio(.*?)\r\n"),
	SYSTEM_VERSION("SystemVersion", "System Version(.*?)\r\n"),
	USB_VERSION("USBVersion", "USB(.*?)\r\n"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @command value of {@link #value}
	 */
	VersionInformation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	final private String name;
	final private String value;

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
}

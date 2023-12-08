/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

/**
 *  VersionInformation class provides all regex and name of system information
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/21/2023
 * @since 1.0.0
 */
public enum VersionInformation {
	AUDIO_0("Audio0", "Audio 0(.*?)\r\n"),
	AUDIO_1("Audio1", "Audio 1(.*?)\r\n"),
	SYSTEM_VERSION("SystemVersion", "System Version(.*?)\r\n"),
	USB_VERSION("USB", "USB(.*?)\r\n"),
	DANTE_CORE("DanteCore", "Dante Core(.*?)\r\n"),
	DANTE_APP("DanteApp", "Dante App(.*?)\r\n"),
	VIDEO_HW("VideoHW", "Video HW(.*?)\r\n"),
	VIDEO_SW("VideoSW", "Video SW(.*?)\r\n"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param  value of {@link #value}
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

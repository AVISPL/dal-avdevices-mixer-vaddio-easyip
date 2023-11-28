/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

/**
 * StreamSettings class provides all regex and name of Streaming settings
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/6/2023
 * @since 1.0.0
 */
public enum StreamSettings {

	USB_ACTIVE("USBActive", "USB Active(.*?)\r\n"),
	USB_DEVICE("USBDeviceName", "USB Device(.*?)\r\n"),
	USB_FRAME_RATE("USBFrameRate", "USB Frame_Rate(.*?)\r\n"),
	USB_RESOLUTION("USBResolution", "USB Resolution(.*?)\r\n"),
	USB_VERSION("USBVersion", "USB Version(.*?)\r\n"),
	UVC_EXTENSIONS_ENABLED("UVCExtensionsEnabled", "UVC Extensions_Enabled(.*?)\r\n"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @command value of {@link #value}
	 */
	StreamSettings(String name, String value) {
		this.name = name;
		this.value = value;
	}

	private String name;
	private String value;

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
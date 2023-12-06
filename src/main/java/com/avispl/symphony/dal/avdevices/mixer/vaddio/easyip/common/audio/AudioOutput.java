/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.audio;

import java.util.Arrays;

import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIPMixerConstant;

/**
 * AudioOutput class defined the enum contains all command of audio crosspoint matrix
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 10/30/2023
 * @since 1.0.0
 */
public enum AudioOutput {

	OUTPUT1("Output1", "line_out_1"),
	OUTPUT2("Output2", "line_out_2"),
	USB_RECORD_LEFT("USBRecordLeft", "usb3_record_left"),
	USB_RECORD_RIGHT("USBRecordRight", "usb3_record_right"),
	HDMI_OUT_LEFT("HDMIOutLeft", "hdmi_out_left"),
	HDMI_OUT_RIGHT("HDMIOutRight", "hdmi_out_right"),
	DANTE_OUT_1("DanteOut1", "dante_out_1"),
	DANTE_OUT_2("DanteOut2", "dante_out_2"),
	DANTE_OUT_3("DanteOut3", "dante_out_3"),
	DANTE_OUT_4("DanteOut4", "dante_out_4"),
	;

	/**
	 * AudioCrosspoint constructor
	 *
	 * @name name of {@link #propertyName}
	 * @command command of {@link #value}
	 */
	AudioOutput(String propertyName, String value) {
		this.propertyName = propertyName;
		this.value = value;
	}

	private String propertyName;
	private String value;

	/**
	 * Retrieves {@link #propertyName}
	 *
	 * @return value of {@link #propertyName}
	 */
	public String getPropertyName() {
		return propertyName;
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
	 * Retrieves the value associated with the given property name from the AudioOutput enum.
	 *
	 * @param name the name of the property to search for in the AudioOutput enum
	 * @return the value associated with the given property name if found, or EasyIPMixerConstant.NONE if not found
	 */
	public static String getValueByName(String name) {
		AudioOutput matchedEnum = Arrays.stream(AudioOutput.values())
				.filter(definition -> definition.getPropertyName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : EasyIPMixerConstant.NONE;
	}
}
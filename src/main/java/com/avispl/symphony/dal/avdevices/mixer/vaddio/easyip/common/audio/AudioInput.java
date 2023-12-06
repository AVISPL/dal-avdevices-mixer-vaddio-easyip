/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.audio;

import java.util.Arrays;

import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIPMixerConstant;

/**
 * AudioInput class defined the enum contains all command of audio input
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 10/30/2023
 * @since 1.0.0
 */
public enum AudioInput {
	AUTO_MIC_MIXER("Auto Mic Mixer", "auto_mic_mix", "AutoMicMixer"),
	LINE_MIC_1("Line Mic 1", "line_in_1", "LineMic1"),
	LINE_MIC_2("Line Mic 2", "line_in_2", "LineMic2"),
	USB_PLAYBACK_LEFT("USB Playback Left", "usb3_playback_left", "USBPlaybackLeft"),
	USB_PLAYBACK_RIGHT("USB Playback Right", "usb3_playback_right", "USBPlaybackRight"),
	HDMI_IN_LEFT("HDMI In Left", "hdmi_in_left", "HDMIInLeft"),
	HDMI_IN_RIGHT("HDMI In Right", "hdmi_in_right", "HDMIInRight"),
	DANTE_IN_1("Dante In 1", "dante_in_1", "DanteIn1"),
	DANTE_IN_2("Dante In 2", "dante_in_2", "DanteIn2"),
	DANTE_IN_3("Dante In 3", "dante_in_3", "DanteIn3"),
	DANTE_IN_4("Dante In 4", "dante_in_4", "DanteIn4"),
	;

	/**
	 * AudioInput constructor
	 *
	 * @name name of {@link #name}
	 * @command command of {@link #value}
	 * @command propertyName of {@link #propertyName}
	 */
	AudioInput(String name, String value, String propertyName) {
		this.name = name;
		this.value = value;
		this.propertyName = propertyName;
	}

	private String name;
	private String value;
	private String propertyName;

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
	 * Retrieves {@link #propertyName}
	 *
	 * @return value of {@link #propertyName}
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Retrieves the value associated with the given property name from the AudioInput enum.
	 *
	 * @param name the name of the property to search for in the AudioInput enum
	 * @return the value associated with the given property name if found, or EasyIPMixerConstant.NONE if not found
	 */
	public static String getValueByName(String name) {
		AudioInput matchedEnum = Arrays.stream(AudioInput.values())
				.filter(definition -> definition.getPropertyName().equals(name))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getValue() : EasyIPMixerConstant.NONE;
	}
}
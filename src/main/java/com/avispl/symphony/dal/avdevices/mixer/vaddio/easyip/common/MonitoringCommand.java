/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

/**
 * Enumeration representing monitoring command properties for EasyIP Mixer camera control.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/22/2023
 * @since 1.0.0
 */
public enum MonitoringCommand {
	VIDEO_MUTE("VideoMute", "video mute get", "mute:(.*?)\r\n"),
	VIDEO_PIP("VideoPIP", "video pip get", "pip:(.*?)\r\n"),
	VIDEO_SOURCE("VideoSource", "video source get", "source:(.*?)\r\n"),
	AUDIO_MUTE("AudioMute", "audio master mute get", "mute:(.*?)\r\n"),
	STREAMING("USBStreamingDeviceName", "streaming settings get", "USB Device(.*?)\r\n"),
	NETWORK("Network", "network settings get", ""),
	SYSTEM_STANDBY("SystemStandby", "system standby get", "standby:(.*?)\r\n"),
	VERSION("Version", "version", ""),
	CAMERA_COLOR("CameraColor", "camera $ ccu get all", ""),
	PAN("Pan", "camera $ pan get", ""),
	TILT("Tilt", "camera $ tilt get", ""),
	ZOOM("Zoom", "camera $ zoom get", ""),
	FOCUS_MODE("FocusMode", "camera $ focus mode get", "auto_focus:(.*?)\r\n"),
	CAMERA_STANDBY("Standby", "camera $ standby get", "standby:(.*?)\r\n"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param command value of {@link #command}
	 * @param regex of {@link #regex}
	 */
	MonitoringCommand(String name, String command, String regex) {
		this.name = name;
		this.command = command;
		this.regex = regex;
	}

	private final String name;
	private final String command;
	private final String regex;

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #command}
	 *
	 * @return value of {@link #command}
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Retrieves {@link #regex}
	 *
	 * @return value of {@link #regex}
	 */
	public String getRegex() {
		return regex;
	}
}

/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

/**
 * EasyIPMixerConstant provides Constant class during the monitoring and controlling process
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/16/2023
 * @since 1.0.0
 */
public class EasyIPMixerConstant {
	public static final String TRUE = "True";
	public static final String EMPTY = "";
	public static final String NONE = "None";
	public static final String REBOOT = "Reboot";
	public static final String REBOOT_NOW = "Reboot Now";
	public static final String REBOOTING = "Rebooting";
	public static final String HOME = "Home";
	public static final String SET = "Set";
	public static final String SETTING = "Setting";
	public static final String REGEX_RESPONSE = "\u001B|\\[|0;37m|0m";
	public static final String NETWORK_GROUP = "NetworkSettings#";
	public static final String VIDEO_OUTPUT_GROUP = "VideoOutput#";
	public static final String VIDEO_INPUT = "VideoInput";
	public static final String SYSTEM_INFORMATION_GROUP = "SystemInformation#";
	public static final String CROSSPOINT = "Crosspoint";
	public static final String MUTE = "Mute";
	public static final String CURRENT_VALUE = "CurrentValue";
	public static final String VOLUME_DB = "Volume(dB)";
	public static final String GAIN_DB = "Gain(dB)";
	public static final String VOLUME = "Volume";
	public static final String GAIN = "Gain";
	public static final String ROUTE = "Route";
	public static final String HASH = "#";
	public static final String DOT = ".";
	public static final String DOT_REGEX = "\\.";
	public static final String DASH = "-";
	public static final String SPACE = " ";
	public static final String ERROR_RESPONSE = "Syntax error";
	public static final String OK = "OK";
	public static final String NUMBER_ONE = "1";
	public static final String ZERO = "0";
	public static final String ON = "On";
	public static final String OFF = "Off";
	public static final String ON_VALUE = "on";
	public static final String OFF_VALUE = "off";
	public static final String MANUAL = "Manual";
	public static final String AUTO = "Auto";
	public static final String MUTE_REGEX = "mute:(.*?)\r\n";
	public static final String VOLUME_REGEX = "volume:(.*?)\r\n";
	public static final String PRESET_MESSAGE = "Please select a preset";
	public static final String GAIN_CURRENT_VALUE = "GainCurrentValue(dB)";
	public static final String VOLUME_CURRENT_VALUE = "VolumeCurrentValue(dB)";
	public static final String IRIS_CURRENT_VALUE = "IrisCurrentValue";
	public static final String RED_GAIN_CURRENT_VALUE = "RedGainCurrentValue";
	public static final String BLUE_GAIN_CURRENT_VALUE = "BlueGainCurrentValue";
}
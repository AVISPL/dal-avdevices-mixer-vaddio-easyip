/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration representing various properties for EasyIP Mixer control.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/22/2023
 * @since 1.0.0
 */
public enum EasyIPMixerProperty {
	CAMERA_PAN("Pan", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 pan set $2"),
	CAMERA_TILT("Tilt", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 tilt set $2"),
	CAMERA_ZOOM("Zoom", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 zoom set $2"),
	CAMERA_FOCUS_MODE("FocusMode", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 focus mode $2"),
	CAMERA_STANDBY("Standby", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 standby $2"),
	CAMERA_HOME("Home", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 home"),
	CAMERA_PRESET("Preset", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 preset recall $2"),
	VIDEO_MUTE("VideoMute", "","video mute $"),
	VIDEO_PIP("VideoPIP", EasyIPMixerConstant.VIDEO_OUTPUT_GROUP,"video pip $"),
	VIDEO_SOURCE("VideoSource", EasyIPMixerConstant.VIDEO_OUTPUT_GROUP,"video source set $"),
	AUDIO_MUTE("AudioMute", "","audio master mute $"),
	SYSTEM_STANDBY("SystemStandby", "","system standby $"),
	SYSTEM_REBOOT("SystemReboot", "","system reboot"),
	AUDIO_0("Audio0", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	AUDIO_1("Audio1", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	DANTE_APP("DanteApp", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	DANTE_CORE("DanteCore", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	VIDEO_HW("VideoHW", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	VIDEO_SW("VideoSW", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	SYSTEM_VERSION("SystemVersion", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	USB_SYSTEM_VERSION("USB", EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP,""),
	USB_DEVICE("USBStreamingDeviceName", "",""),
	INTERFACE_NAME("InterfaceName", EasyIPMixerConstant.NETWORK_GROUP,""),
	MAC_ADDRESS("MACAddress", EasyIPMixerConstant.NETWORK_GROUP,""),
	IP_ADDRESS("IPAddress", EasyIPMixerConstant.NETWORK_GROUP,""),
	SUBNET_MASK("SubnetMask", EasyIPMixerConstant.NETWORK_GROUP,""),
	GATEWAY("Gateway", EasyIPMixerConstant.NETWORK_GROUP,""),
	HOSTNAME("Hostname", EasyIPMixerConstant.NETWORK_GROUP,""),
	AUTO_IRIS("AutoIris", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set auto_iris $2"),
	AUTO_WHITE_BALANCE("AutoWhiteBalance", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set auto_white_balance $2"),
	BACKLIGHT_COMPENSATION("BacklightCompensation", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set backlight_compensation $2"),
	BLUE_GAIN("BlueGain", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set blue_gain $2"),
	CHROMA("Chroma(Saturation)", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set chroma $2"),
	DETAIL("Detail(Sharpness)", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set detail $2"),
	GAIN("Gain(dB)", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set gain $2"),
	GAMMA("Gamma", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set gamma $2"),
	IRIS("Iris", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set iris $2"),
	RED_GAIN("RedGain", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set red_gain $2"),
	WIDE_DYNAMIC_RANGE("WideDynamicRange", EasyIPMixerConstant.VIDEO_INPUT,"camera $1 ccu set wide_dynamic_range $2"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param group of {@link #group}
	 * @param controlCommand of {@link #controlCommand}
	 */
	EasyIPMixerProperty(String name, String group, String controlCommand) {
		this.name = name;
		this.group = group;
		this.controlCommand = controlCommand;
	}

	private final String name;
	private final String group;
	private final String controlCommand;

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #group}
	 *
	 * @return value of {@link #group}
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Retrieves {@link #controlCommand}
	 *
	 * @return value of {@link #controlCommand}
	 */
	public String getControlCommand() {
		return controlCommand;
	}

	/**
	 * This method is used to get properties metric group by name
	 *
	 * @param name is the name of device metric group that want to get
	 * @return UPSPropertiesList is the device metric group that want to get
	 */
	public static EasyIPMixerProperty getByName(String name) {
		Optional<EasyIPMixerProperty> property = Arrays.stream(EasyIPMixerProperty.values()).filter(group -> group.getName().equals(name)).findFirst();
		if (property.isPresent()) {
			return property.get();
		} else {
			throw new IllegalStateException(String.format("control group %s is not supported.", name));
		}
	}
}

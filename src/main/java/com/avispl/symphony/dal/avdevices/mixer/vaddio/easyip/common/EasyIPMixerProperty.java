package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * MonitoringProperty
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/22/2023
 * @since 1.0.0
 */
public enum EasyIPMixerProperty {
	CAMERA_PAN("Pan", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 pan set $"),
	CAMERA_TILT("Tilt", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 tilt set $"),
	CAMERA_ZOOM("Zoom", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 zoom set $"),
	CAMERA_FOCUS_MODE("FocusMode", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 focus mode $"),
	CAMERA_STANDBY("Standby", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 standby $"),
	CAMERA_HOME("Home", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 home"),
	CAMERA_CCU_SCENE("CCUScene", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 ccu scene recall $"),
	CAMERA_PRESET("Preset", EasyIPMixerConstant.CAMERA_SETTINGS_GROUP,"camera 2 preset recall $"),
	VIDEO_MUTE("VideoMute", "","video mute $"),
	VIDEO_PIP("VideoPIP", EasyIPMixerConstant.VIDEO_OUTPUT ,"video pip $"),
	VIDEO_SOURCE("VideoSource", EasyIPMixerConstant.VIDEO_OUTPUT ,"video source set $"),
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
	AUTO_IRIS("AutoIris", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set auto_iris $"),
	AUTO_WHITE_BALANCE("AutoWhiteBalance", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set auto_white_balance $"),
	BACKLIGHT_COMPENSATION("BacklightCompensation", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set backlight_compensation $"),
	BLUE_GAIN("BlueGain", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set blue_gain $"),
	CHROMA("Chroma", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set chroma $"),
	DETAIL("Detail", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set detail $"),
	GAIN("Gain(dB)", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set gain $"),
	GAMMA("Gamma", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set gamma $"),
	IRIS("Iris", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set iris $"),
	RED_GAIN("RedGain", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set red_gain $"),
	WIDE_DYNAMIC_RANGE("WideDynamicRange", EasyIPMixerConstant.CAMERA_COLOR_GROUP,"camera 2 ccu set wide_dynamic_range $"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @command value of {@link #group}
	 */
	EasyIPMixerProperty(String name, String group, String controlCommand) {
		this.name = name;
		this.group = group;
		this.controlCommand = controlCommand;
	}

	private String name;
	private String group;
	private String controlCommand;

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

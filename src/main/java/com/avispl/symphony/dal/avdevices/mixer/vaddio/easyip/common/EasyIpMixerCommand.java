/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common;

/**
 * Class containing constants representing various commands used in EasyIP Mixer.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/22/2023
 * @since 1.0.0
 */
public class EasyIpMixerCommand {
	public static final String ROUTE_COMMAND = "audio $ route get";
	public static final String ROUTE_CONTROL = "audio $1 route set $2";
	public static final String MUTE_MONITOR = "audio $ mute get";
	public static final String MUTE_CONTROL = "audio $1 mute $2";
	public static final String VOLUME_MONITOR = "audio $ volume get";
	public static final String VOLUME_CONTROL = "audio $1 volume set $2";
	public static final String GAIN_MONITOR = "audio $1 crosspoint-gain $2 get";
	public static final String GAIN_CONTROL = "audio $1 crosspoint-gain $2 set $3";
	public static final String HOST_CAMERA = "camera $ comm host get";
}

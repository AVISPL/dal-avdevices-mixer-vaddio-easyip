/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.CollectionUtils;

import javax.security.auth.login.FailedLoginException;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIPMixerConstant;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIPMixerMapping;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIPMixerProperty;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIpMixerCommand;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.MonitoringCommand;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.NetworkInformation;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.VersionInformation;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.audio.AudioInput;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.audio.AudioOutput;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera.CameraColorSettings;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera.GainValueEnum;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera.IrisValueEnum;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera.PresetValueEnum;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.video.SourceValueEnum;
import com.avispl.symphony.dal.communicator.SshCommunicator;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * EasyIPMixerCommunicator An implementation of SshCommunicator to provide communication and interaction with Vaddio Mixer EasyIP device
 *  Monitoring
 *  Network information
 *  System Information
 *
 *  Controlling
 *  Mute
 *  Volume(dB)
 *  Gain(dB)
 *  Reboot
 *  Video Mute
 *  Audio Mute
 *  Camera Settings
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/16/2023
 * @since 1.0.0
 */
public class EasyIPMixerCommunicator extends SshCommunicator implements Monitorable, Controller {

	/**
	 * To avoid timeout errors, caused by the unavailability of the control protocol, all polling-dependent communication operations (monitoring)
	 * should be performed asynchronously. This executor service executes such operations.
	 */
	private ExecutorService executorService;
	/**
	 * Data collector
	 */
	private CompletableFuture dataCollector;

	/**
	 * cache to store key and value
	 */
	private final Map<String, String> cacheKeyAndValue = new HashMap<>();

	/**
	 * count the failed command
	 */
	private final Map<String, String> failedMonitor = new HashMap<>();

	/**
	 * ReentrantLock to prevent telnet session is closed when adapter is retrieving statistics from the device.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * Store previous/current ExtendedStatistics
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * Number Of Camera
	 */
	private Set<String> numberOfCamera = new HashSet<>();

	/**
	 * Num Of Polling Interval
	 */
	private int numOfPollingInterval;

	/**
	 * Current polling interval
	 */
	private int currentPollingInterval = 1;

	/**
	 * configManagement imported from the user interface
	 */
	private String configManagement;

	/**
	 * configManagement in boolean value
	 */
	private boolean isConfigManagement;

	/**
	 * enableCrosspointGain imported from the user interface
	 */
	private String enableCrosspointGain;

	/**
	 * isEnableCrosspointGain in boolean value, if true the crosspoint gain will display
	 */
	private boolean isEnableCrosspointGain;

	/**
	 * Emergency Delivery in boolean value
	 */
	private boolean isEmergencyDelivery;

	/**
	 * Retrieves {@link #configManagement}
	 *
	 * @return value of {@link #configManagement}
	 */
	public String getConfigManagement() {
		return configManagement;
	}

	/**
	 * Sets {@link #configManagement} value
	 *
	 * @param configManagement new value of {@link #configManagement}
	 */
	public void setConfigManagement(String configManagement) {
		this.configManagement = configManagement;
	}

	/**
	 * Retrieves {@link #enableCrosspointGain}
	 *
	 * @return value of {@link #enableCrosspointGain}
	 */
	public String getEnableCrosspointGain() {
		return enableCrosspointGain;
	}

	/**
	 * Sets {@link #enableCrosspointGain} value
	 *
	 * @param enableCrosspointGain new value of {@link #enableCrosspointGain}
	 */
	public void setEnableCrosspointGain(String enableCrosspointGain) {
		this.enableCrosspointGain = enableCrosspointGain;
	}

	/**
	 * MiddleAtlanticPowerUnitCommunicator constructor
	 */
	public EasyIPMixerCommunicator() {
		this.setCommandErrorList(Collections.singletonList("Syntax error: Unknown or incomplete command"));
		this.setCommandSuccessList(Collections.singletonList("> "));
		this.setLoginSuccessList(Collections.singletonList("> "));
		this.setLoginErrorList(Collections.singletonList("Permission denied, please try again."));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		ExtendedStatistics extendedStatistics = new ExtendedStatistics();
		List<AdvancedControllableProperty> advancedControllableProperties = new ArrayList<>();
		Map<String, String> stats = new HashMap<>();
		Map<String, String> controlStats = new HashMap<>();
		reentrantLock.lock();
		try {
			if (!isEmergencyDelivery) {
				convertConfigManagement();
				convertEnableCrosspointGain();
				retrieveAllData();
				populateAllData(stats, controlStats, advancedControllableProperties);
				if (isConfigManagement) {
					stats.putAll(controlStats);
					extendedStatistics.setControllableProperties(advancedControllableProperties);
				}
				extendedStatistics.setStatistics(stats);
				localExtendedStatistics = extendedStatistics;
			}
			isEmergencyDelivery = false;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		reentrantLock.lock();
		try {
			if (localExtendedStatistics == null || localExtendedStatistics.getStatistics() == null) {
				return;
			}
			isEmergencyDelivery = true;
			String property = controllableProperty.getProperty();
			String value = String.valueOf(controllableProperty.getValue());
			Map<String, String> stats = this.localExtendedStatistics.getStatistics();
			List<AdvancedControllableProperty> advancedControllableProperties = this.localExtendedStatistics.getControllableProperties();

			String group = EasyIPMixerConstant.EMPTY;
			String[] propertyList = property.split(EasyIPMixerConstant.HASH);
			String propertyKey = property;
			if (property.contains(EasyIPMixerConstant.HASH)) {
				propertyKey = propertyList[1];
				group = propertyList[0];
			}

			if (group.contains(EasyIPMixerConstant.CROSSPOINT)) {
				//Crosspoint
				String command;
				String inputPropertyName;
				String outputPropertyName = group.replace(EasyIPMixerConstant.CROSSPOINT, EasyIPMixerConstant.EMPTY);
				if (propertyKey.contains(EasyIPMixerConstant.GAIN)) {
					inputPropertyName = propertyKey.replace(EasyIPMixerConstant.GAIN_DB, EasyIPMixerConstant.EMPTY);
					command = EasyIpMixerCommand.GAIN_CONTROL.replace("$1", AudioOutput.getValueByName(outputPropertyName)).replace("$2", AudioInput.getValueByName(inputPropertyName)).replace("$3", value);
					sendCommandToControlDevice(command, value, propertyKey);
					stats.put(group + EasyIPMixerConstant.HASH + inputPropertyName + EasyIPMixerConstant.GAIN_CURRENT_VALUE, convertFloatToIntString(value));
					updateCachedDeviceData(cacheKeyAndValue, property, value);
				}

				if (propertyKey.contains(EasyIPMixerConstant.ROUTE)) {
					inputPropertyName = propertyKey.replace(EasyIPMixerConstant.ROUTE, EasyIPMixerConstant.EMPTY);
					String localName = group + EasyIPMixerConstant.HASH + outputPropertyName;

					command = EasyIpMixerCommand.ROUTE_COMMAND.replace("$", AudioOutput.getValueByName(outputPropertyName));
					String response = sendCommandDetails(command);
					String localValue = replaceDraftInResponse(response, command);

					if (EasyIPMixerConstant.NUMBER_ONE.equals(value) && !localValue.contains(AudioInput.getValueByName(inputPropertyName))) {
						localValue = localValue + EasyIPMixerConstant.SPACE + AudioInput.getValueByName(inputPropertyName);
					}
					if (EasyIPMixerConstant.ZERO.equals(value)) {
						localValue = localValue.replace(AudioInput.getValueByName(inputPropertyName), EasyIPMixerConstant.EMPTY).replace("  ", EasyIPMixerConstant.SPACE).trim();
					}
					command = EasyIpMixerCommand.ROUTE_CONTROL.replace("$1", AudioOutput.getValueByName(outputPropertyName)).replace("$2", localValue);
					sendCommandToControlDevice(command, EasyIPMixerConstant.NUMBER_ONE.equals(value) ? EasyIPMixerConstant.ON : EasyIPMixerConstant.OFF, propertyKey);
					updateCachedDeviceData(cacheKeyAndValue, localName, localValue);
				}
				updateValueForTheControllableProperty(property, value, stats, advancedControllableProperties);
			} else if (checkAudioVolumeControl(group)) {
				//Audio Volume
				String command;
				String audioValue = AudioInput.getValueByName(group);
				if (EasyIPMixerConstant.NONE.equals(audioValue)) {
					audioValue = AudioOutput.getValueByName(group);
				}
				switch (propertyKey) {
					case EasyIPMixerConstant.VOLUME_DB:
						if (!EasyIPMixerConstant.NONE.equals(audioValue)) {
							command = EasyIpMixerCommand.VOLUME_CONTROL.replace("$1", audioValue).replace("$2", value);
							sendCommandToControlDevice(command, value, EasyIPMixerConstant.VOLUME_DB);
							stats.put(group + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_CURRENT_VALUE, convertFloatToIntString(value));
							updateCachedDeviceData(cacheKeyAndValue, property, value);
						}
						break;
					case EasyIPMixerConstant.MUTE:
						String status = getStatusSwitch(value);
						if (!EasyIPMixerConstant.NONE.equals(audioValue)) {
							command = EasyIpMixerCommand.MUTE_CONTROL.replace("$1", audioValue).replace("$2", status);
							sendCommandToControlDevice(command, status, EasyIPMixerConstant.MUTE);
							updateCachedDeviceData(cacheKeyAndValue, property, status);
						}
						break;
					default:
						logger.debug(String.format("Property name %s doesn't support", propertyKey));
				}
			} else {
				EasyIPMixerProperty propertyItem = EasyIPMixerProperty.getByName(propertyKey);
				switch (propertyItem) {
					case SYSTEM_REBOOT:
						sendCommandToControlDevice(propertyItem.getControlCommand(), EasyIPMixerConstant.REBOOT, propertyKey);
						Thread.sleep(3000);
						break;
					case AUDIO_MUTE:
						String status = getStatusSwitch(value);
						String command = propertyItem.getControlCommand().replace("$", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);
						retrieveMuteOfAudioVolumeWhenControlMasterMute();
						populateAudioVolume(stats, advancedControllableProperties);
						break;
					case VIDEO_SOURCE:
						String newValue = SourceValueEnum.getValueByName(value);
						if (newValue != null) {
							command = propertyItem.getControlCommand().replace("$", newValue);
							sendCommandToControlDevice(command, value, propertyKey);
							updateCachedDeviceData(cacheKeyAndValue, property, newValue);
						}
						break;
					case VIDEO_MUTE:
					case VIDEO_PIP:
						status = getStatusSwitch(value);
						command = propertyItem.getControlCommand().replace("$", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);
						break;
					case SYSTEM_STANDBY:
						status = getStatusSwitch(value);
						command = propertyItem.getControlCommand().replace("$", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);

						String response = sendCommandDetails(MonitoringCommand.AUDIO_MUTE.getCommand());
						String referenceValue = extractResponseValue(response, MonitoringCommand.AUDIO_MUTE.getRegex());
						updateCachedDeviceData(cacheKeyAndValue, MonitoringCommand.AUDIO_MUTE.getName(), referenceValue);
						response = sendCommandDetails(MonitoringCommand.VIDEO_MUTE.getCommand());
						referenceValue = extractResponseValue(response, MonitoringCommand.VIDEO_MUTE.getRegex());
						updateCachedDeviceData(cacheKeyAndValue, MonitoringCommand.VIDEO_MUTE.getName(), referenceValue);
						retrieveMuteOfAudioVolumeWhenControlMasterMute();
						populateAllData(stats, stats, advancedControllableProperties);
						break;
					case CAMERA_STANDBY:
						String indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						status = getStatusSwitch(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);
						String colorResponse = sendCommandDetails(MonitoringCommand.CAMERA_COLOR.getCommand().replace("$", indexCamera));
						retrieveCameraColor(colorResponse, indexCamera);
						if (EasyIPMixerConstant.ZERO.equals(value)) {
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.AUTO_IRIS.getName(), EasyIPMixerConstant.ON_VALUE);
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.AUTO_WHITE_BALANCE.getName(), EasyIPMixerConstant.ON_VALUE);
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.PAN.getName(), EasyIPMixerConstant.PAN_HOME_VALUE);
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.TILT.getName(), EasyIPMixerConstant.TILT_HOME_VALUE);
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.ZOOM.getName(), EasyIPMixerConstant.ZOOM_HOME_VALUE);
						} else {
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.PAN.getName(), EasyIPMixerConstant.PAN_STANDBY_VALUE);
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.TILT.getName(), EasyIPMixerConstant.TILT_STANDBY_VALUE);
							cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.ZOOM.getName(), EasyIPMixerConstant.ZOOM_STANDBY_VALUE);
						}
						populateMonitoringAndControllingData(stats, stats, advancedControllableProperties);
						break;
					case BACKLIGHT_COMPENSATION:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						status = getStatusSwitch(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);
						if (EasyIPMixerConstant.ON_VALUE.equals(status)) {
							String name = group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.WIDE_DYNAMIC_RANGE.getName();
							addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(name, 0, EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON), EasyIPMixerConstant.ZERO);
							updateCachedDeviceData(cacheKeyAndValue, name, EasyIPMixerConstant.ZERO);
						}
						break;
					case WIDE_DYNAMIC_RANGE:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						status = getStatusSwitch(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);
						if (EasyIPMixerConstant.ON_VALUE.equals(status)) {
							String name = group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.BACKLIGHT_COMPENSATION.getName();
							addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(name, 0, EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON), EasyIPMixerConstant.ZERO);
							updateCachedDeviceData(cacheKeyAndValue, name, EasyIPMixerConstant.ZERO);
						}
						break;
					case AUTO_IRIS:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						status = getStatusSwitch(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);
						if (EasyIPMixerConstant.ON_VALUE.equals(status)) {
							removeValueForTheControllableProperty(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.IRIS.getName(), stats, advancedControllableProperties);
							removeValueForTheControllableProperty(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.GAIN.getName(), stats, advancedControllableProperties);
							stats.remove(group + EasyIPMixerConstant.HASH + EasyIPMixerConstant.IRIS_CURRENT_VALUE);
							stats.remove(group + EasyIPMixerConstant.HASH + EasyIPMixerConstant.GAIN_CURRENT_VALUE);

							String backlightCompensation = cacheKeyAndValue.get(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.BACKLIGHT_COMPENSATION.getName());
							String backlightValue = EasyIPMixerConstant.ON_VALUE.equals(backlightCompensation) ? "1" : "0";
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createSwitch(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.BACKLIGHT_COMPENSATION.getName(), Integer.parseInt(backlightValue), EasyIPMixerConstant.OFF,
											EasyIPMixerConstant.ON), backlightValue);

							String wideDynamicRange = cacheKeyAndValue.get(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.WIDE_DYNAMIC_RANGE.getName());
							String wideValue = EasyIPMixerConstant.ON_VALUE.equals(wideDynamicRange) ? "1" : "0";
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createSwitch(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.WIDE_DYNAMIC_RANGE.getName(), Integer.parseInt(wideValue), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON),
									wideValue);

						} else {
							retrieveCameraColor(sendCommandDetails(MonitoringCommand.CAMERA_COLOR.getCommand().replace("$", indexCamera)), indexCamera);
							String irisValue = cacheKeyAndValue.get(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.IRIS.getName());
							List<String> arrayValues = Arrays.stream(IrisValueEnum.values()).map(IrisValueEnum::getName).collect(Collectors.toList());
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createDropdown(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.IRIS.getName(), arrayValues.toArray(new String[0]), IrisValueEnum.getNameByValue(irisValue)), irisValue);

							String gainValue = cacheKeyAndValue.get(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.GAIN.getName());
							arrayValues = Arrays.stream(GainValueEnum.values()).map(GainValueEnum::getName).collect(Collectors.toList());
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createDropdown(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.GAIN.getName(), arrayValues.toArray(new String[0]), GainValueEnum.getNameByValue(gainValue)), gainValue);

							removeValueForTheControllableProperty(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.WIDE_DYNAMIC_RANGE.getName(), stats, advancedControllableProperties);
							removeValueForTheControllableProperty(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.BACKLIGHT_COMPENSATION.getName(), stats, advancedControllableProperties);
						}
						break;
					case AUTO_WHITE_BALANCE:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						status = getStatusSwitch(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, status);
						if (EasyIPMixerConstant.ON_VALUE.equals(status)) {
							removeValueForTheControllableProperty(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.RED_GAIN.getName(), stats, advancedControllableProperties);
							removeValueForTheControllableProperty(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.BLUE_GAIN.getName(), stats, advancedControllableProperties);
							stats.remove(group + EasyIPMixerConstant.HASH + EasyIPMixerConstant.RED_GAIN_CURRENT_VALUE);
							stats.remove(group + EasyIPMixerConstant.HASH + EasyIPMixerConstant.BLUE_GAIN_CURRENT_VALUE);
						} else {
							retrieveCameraColor(sendCommandDetails(MonitoringCommand.CAMERA_COLOR.getCommand().replace("$", indexCamera)), indexCamera);
							String redGainValue = cacheKeyAndValue.get(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.RED_GAIN.getName());
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createSlider(stats, group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.RED_GAIN.getName(), "0", "255", 0f, 255f, Float.parseFloat(redGainValue)), redGainValue);
							stats.put(group + EasyIPMixerConstant.HASH + EasyIPMixerConstant.RED_GAIN_CURRENT_VALUE, redGainValue);

							String blueGainValue = cacheKeyAndValue.get(group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.BLUE_GAIN.getName());
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createSlider(stats, group + EasyIPMixerConstant.HASH + EasyIPMixerProperty.BLUE_GAIN.getName(), "0", "255", 0f, 255f, Float.parseFloat(blueGainValue)), blueGainValue);
							stats.put(group + EasyIPMixerConstant.HASH + EasyIPMixerConstant.BLUE_GAIN_CURRENT_VALUE, blueGainValue);
						}
						break;
					case BLUE_GAIN:
					case DETAIL:
					case RED_GAIN:
					case CHROMA:
					case GAMMA:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						value = convertFloatToIntString(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", value);
						sendCommandToControlDevice(command, value, propertyKey);
						stats.put(property.replace(EasyIPMixerConstant.SHARPNESS, EasyIPMixerConstant.EMPTY).replace(EasyIPMixerConstant.SATURATION, EasyIPMixerConstant.EMPTY) + EasyIPMixerConstant.CURRENT_VALUE,
								value);
						updateCachedDeviceData(cacheKeyAndValue, property, value);
						break;
					case IRIS:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						newValue = IrisValueEnum.getValueByName(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", newValue);
						sendCommandToControlDevice(command, value, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, newValue);
						break;
					case GAIN:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						newValue = GainValueEnum.getValueByName(value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", newValue);
						sendCommandToControlDevice(command, value, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, newValue);
						break;
					case CAMERA_ZOOM:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						value = checkValidInput(1, 20, value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", value);
						sendCommandToControlDeviceWithExecutor(command, value, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, value);
						break;
					case CAMERA_PAN:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						value = checkValidInput(-156.3, 151.7, value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", value);
						sendCommandToControlDeviceWithExecutor(command, value, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, value);
						break;
					case CAMERA_TILT:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						value = checkValidInput(-30, 92.5, value);
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", value);
						sendCommandToControlDeviceWithExecutor(command, value, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, value);
						break;
					case CAMERA_FOCUS_MODE:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						status = EasyIPMixerConstant.NUMBER_ONE.equals(value) ? "auto" : "manual";
						command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", status);
						sendCommandToControlDevice(command, status, propertyKey);
						updateCachedDeviceData(cacheKeyAndValue, property, EasyIPMixerConstant.NUMBER_ONE.equals(value) ? EasyIPMixerConstant.ON_VALUE : EasyIPMixerConstant.OFF_VALUE);
						break;
					case CAMERA_HOME:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						sendCommandToControlDevice(propertyItem.getControlCommand().replace("$1", indexCamera), EasyIPMixerConstant.HOME, propertyKey);
						cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.PAN.getName(), EasyIPMixerConstant.PAN_HOME_VALUE);
						cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.TILT.getName(), EasyIPMixerConstant.TILT_HOME_VALUE);
						cacheKeyAndValue.put(group + EasyIPMixerConstant.HASH + MonitoringCommand.ZOOM.getName(), EasyIPMixerConstant.ZOOM_HOME_VALUE);
						populateCameraPosition(stats, advancedControllableProperties, group);
						break;
					case CAMERA_PRESET:
						indexCamera = EasyIPMixerMapping.getValueByName(group.replace(EasyIPMixerConstant.VIDEO_INPUT, EasyIPMixerConstant.EMPTY));
						newValue = PresetValueEnum.getValueByName(value);
						if (newValue != null) {
							command = propertyItem.getControlCommand().replace("$1", indexCamera).replace("$2", newValue);
							sendCommandToControlDevice(command, value, propertyKey);
						} else {
							throw new IllegalArgumentException("Please select valid value.");
						}
						break;
					default:
						logger.debug(String.format("Property name %s doesn't support", propertyKey));
				}
			}
			updateValueForTheControllableProperty(property, value, stats, advancedControllableProperties);
		} finally {
			reentrantLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperties(List<ControllableProperty> list) throws Exception {
		if (CollectionUtils.isEmpty(list)) {
			throw new IllegalArgumentException("ControllableProperties can not be null or empty");
		}
		for (ControllableProperty p : list) {
			try {
				controlProperty(p);
			} catch (Exception e) {
				logger.error(String.format("Error when control property %s", p.getProperty()), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * Check for available devices before retrieving the value
	 * ping latency information to Symphony
	 */
	@Override
	public int ping() throws Exception {
		if (isInitialized()) {
			long pingResultTotal = 0L;

			for (int i = 0; i < this.getPingAttempts(); i++) {
				long startTime = System.currentTimeMillis();

				try (Socket puSocketConnection = new Socket(this.host, this.getPort())) {
					puSocketConnection.setSoTimeout(this.getPingTimeout());
					if (puSocketConnection.isConnected()) {
						long pingResult = System.currentTimeMillis() - startTime;
						pingResultTotal += pingResult;
						if (this.logger.isTraceEnabled()) {
							this.logger.trace(String.format("PING OK: Attempt #%s to connect to %s on port %s succeeded in %s ms", i + 1, host, this.getPort(), pingResult));
						}
					} else {
						if (this.logger.isDebugEnabled()) {
							logger.debug(String.format("PING DISCONNECTED: Connection to %s did not succeed within the timeout period of %sms", host, this.getPingTimeout()));
						}
						return this.getPingTimeout();
					}
				} catch (SocketTimeoutException | ConnectException tex) {
					throw new SocketTimeoutException("Socket connection timed out");
				} catch (UnknownHostException tex) {
					throw new SocketTimeoutException("Socket connection timed out" + tex.getMessage());
				} catch (Exception e) {
					if (this.logger.isWarnEnabled()) {
						this.logger.warn(String.format("PING TIMEOUT: Connection to %s did not succeed, UNKNOWN ERROR %s: ", host, e.getMessage()));
					}
					return this.getPingTimeout();
				}
			}
			return Math.max(1, Math.toIntExact(pingResultTotal / this.getPingAttempts()));
		} else {
			throw new IllegalStateException("Cannot use device class without calling init() first");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		executorService = Executors.newFixedThreadPool(1);
		super.internalInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		if (localExtendedStatistics != null && localExtendedStatistics.getStatistics() != null && localExtendedStatistics.getControllableProperties() != null) {
			localExtendedStatistics.getStatistics().clear();
			localExtendedStatistics.getControllableProperties().clear();
		}
		dataCollector.cancel(true);
		executorService.shutdownNow();
		currentPollingInterval = 1;
		cacheKeyAndValue.clear();
		super.internalDestroy();
	}

	/**
	 * Retrieve data based on the current polling interval.
	 * This method retrieves different sets of data based on the current polling interval.
	 * It calls specific retrieval methods for each interval and updates the current polling interval.
	 *
	 * @throws Exception if there's an error during data retrieval.
	 */
	private void retrieveAllData() throws Exception {
		getNumOfPollingInterval();
		if (currentPollingInterval == 1) {
			getNumberCamera();
			retrieveMonitoring();
			retrieveEnabledRoute();
		}
		if (currentPollingInterval == 2) {
			retrieveAudioVolume();
		}
		if (currentPollingInterval == 3) {
			retrieveCrossPointGain(1);
		}
		if (currentPollingInterval == 4) {
			retrieveCrossPointGain(2);
		}
	}

	/**
	 * Retrieves the number of connected cameras within a range of specified IDs.
	 *
	 * @throws FailedLoginException if the login attempt fails during the command execution
	 */
	private void getNumberCamera() throws FailedLoginException {
		String command;
		String response;
		numberOfCamera.clear();
		for (int i = 2; i <= 5; i++) {
			command = EasyIpMixerCommand.HOST_CAMERA.replace("$", String.valueOf(i));
			response = extractResponseValue(sendCommandDetails(command), "host:(.*?)\r\n");
			if (!response.contains("unconnected")) {
				numberOfCamera.add(String.valueOf(i));
			}
		}
	}

	/**
	 * Retrieves monitoring data by sending commands based on MonitoringCommand enum values.
	 * Updates cacheKeyAndValue with extracted information based on different commands.
	 *
	 * @throws FailedLoginException if the login attempt fails while sending the command.
	 */
	private void retrieveMonitoring() throws FailedLoginException {
		String response = EasyIPMixerConstant.EMPTY;
		for (MonitoringCommand command : MonitoringCommand.values()) {
			if (!command.getCommand().contains("$")) {
				response = sendCommandDetails(command.getCommand());
			}
			switch (command) {
				case NETWORK:
					for (NetworkInformation network : NetworkInformation.values()) {
						cacheKeyAndValue.put(EasyIPMixerConstant.NETWORK_GROUP + network.getName(), extractResponseValue(response, network.getValue()));
					}
					break;
				case VERSION:
					for (VersionInformation version : VersionInformation.values()) {
						cacheKeyAndValue.put(EasyIPMixerConstant.SYSTEM_INFORMATION_GROUP + version.getName(), extractResponseValue(response, version.getValue()));
					}
					break;
				case CAMERA_COLOR:
					for (String item : numberOfCamera) {
						response = sendCommandDetails(command.getCommand().replace("$", item));
						retrieveCameraColor(response, item);
					}
					break;
				case PAN:
				case TILT:
				case ZOOM:
					for (String item : numberOfCamera) {
						response = sendCommandDetails(command.getCommand().replace("$", item));
						cacheKeyAndValue.put(EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + command.getName(),
								replaceDraftInResponse(response, command.getCommand().replace("$", item)));
					}
					break;
				case FOCUS_MODE:
				case CAMERA_STANDBY:
					for (String item : numberOfCamera) {
						response = sendCommandDetails(command.getCommand().replace("$", item));
						cacheKeyAndValue.put(EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + command.getName(),
								extractResponseValue(response, command.getRegex()));
					}
					break;
				case VIDEO_PIP:
				case VIDEO_SOURCE:
					cacheKeyAndValue.put(EasyIPMixerConstant.VIDEO_OUTPUT_GROUP + command.getName(), extractResponseValue(response, command.getRegex()));
					break;
				default:
					cacheKeyAndValue.put(command.getName(), extractResponseValue(response, command.getRegex()));
					break;
			}
		}
	}

	/**
	 * Retrieves camera color settings from the response and stores them in the cache.
	 *
	 * @param response the response containing camera color settings information
	 * @param cameraIndex the index of camera
	 */
	private void retrieveCameraColor(String response, String cameraIndex) {
		for (CameraColorSettings colorSettings : CameraColorSettings.values()) {
			cacheKeyAndValue.put(EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(cameraIndex) + EasyIPMixerConstant.HASH + colorSettings.getName(),
					extractResponseValue(response, colorSettings.getCommand()));
			if (CameraColorSettings.AUTO_IRIS.equals(colorSettings)) {
				response = response.replace("auto_iris", EasyIPMixerConstant.SPACE);
			}
			if (colorSettings.equals(CameraColorSettings.BLUE_GAIN)) {
				response = response.replace("blue_gain", EasyIPMixerConstant.SPACE);
			}
			if (colorSettings.equals(CameraColorSettings.RED_GAIN)) {
				response = response.replace("red_gain", EasyIPMixerConstant.SPACE);
			}
		}
	}

	/**
	 * Retrieves enabled audio routes by sending specific commands for each AudioOutput.
	 * Updates cacheKeyAndValue with the response for each route command.
	 *
	 * @throws FailedLoginException if the login attempt fails while sending the command.
	 */
	private void retrieveEnabledRoute() throws FailedLoginException {
		String command;
		String response;
		for (AudioOutput output : AudioOutput.values()) {
			command = EasyIpMixerCommand.ROUTE_COMMAND.replace("$", output.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(EasyIPMixerConstant.CROSSPOINT + output.getPropertyName() + EasyIPMixerConstant.HASH + output.getPropertyName(), replaceDraftInResponse(response, command));
		}
	}

	/**
	 * Retrieves audio volume information for both outputs and inputs.
	 * Updates cacheKeyAndValue with mute status and volume information for each AudioOutput and AudioInput.
	 *
	 * @throws FailedLoginException if the login attempt fails while sending the command.
	 */
	private void retrieveAudioVolume() throws FailedLoginException {
		String command;
		String response;
		for (AudioOutput output : AudioOutput.values()) {
			command = EasyIpMixerCommand.MUTE_MONITOR.replace("$", output.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(output.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE, extractResponseValue(response, EasyIPMixerConstant.MUTE_REGEX));

			command = EasyIpMixerCommand.VOLUME_MONITOR.replace("$", output.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(output.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_DB, removeUnit(extractResponseValue(response, EasyIPMixerConstant.VOLUME_REGEX)));
		}

		for (AudioInput input : AudioInput.values()) {
			if (input.equals(AudioInput.AUTO_MIC_MIXER)) {
				continue;
			}
			command = EasyIpMixerCommand.MUTE_MONITOR.replace("$", input.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(input.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE, extractResponseValue(response, EasyIPMixerConstant.MUTE_REGEX));

			command = EasyIpMixerCommand.VOLUME_MONITOR.replace("$", input.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(input.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_DB, removeUnit(extractResponseValue(response, EasyIPMixerConstant.VOLUME_REGEX)));
		}
	}

	/**
	 * Retrieves the mute status of audio outputs and inputs, and stores them in the cache.
	 *
	 * @throws FailedLoginException if the login to the system fails
	 */
	private void retrieveMuteOfAudioVolumeWhenControlMasterMute() throws FailedLoginException {
		String command;
		String response;
		for (AudioOutput output : AudioOutput.values()) {
			if (output.equals(AudioOutput.USB_RECORD_LEFT) || output.equals(AudioOutput.USB_RECORD_RIGHT)) {
				command = EasyIpMixerCommand.MUTE_MONITOR.replace("$", output.getValue());
				response = sendCommandDetails(command);
				cacheKeyAndValue.put(output.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE, extractResponseValue(response, EasyIPMixerConstant.MUTE_REGEX));
			}
		}

		for (AudioInput input : AudioInput.values()) {
			if (input.equals(AudioInput.AUTO_MIC_MIXER)) {
				continue;
			}
			command = EasyIpMixerCommand.MUTE_MONITOR.replace("$", input.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(input.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE, extractResponseValue(response, EasyIPMixerConstant.MUTE_REGEX));
		}
	}

	/**
	 * Retrieves crosspoint gain information based on the specified part number.
	 * For part 1, retrieves crosspoint gain information for the first half of AudioOutputs.
	 * For part 2, retrieves crosspoint gain information for the second half of AudioOutputs.
	 *
	 * @param part an integer indicating the part number (1 or 2) to determine the range of AudioOutputs to retrieve gain information from.
	 * @throws FailedLoginException if the login attempt fails while sending the command.
	 */
	private void retrieveCrossPointGain(int part) throws FailedLoginException {
		List<AudioOutput> allValues = Arrays.stream(AudioOutput.values()).collect(Collectors.toList());
		List<AudioOutput> outputValues;
		if (part == 1) {
			outputValues = allValues.subList(0, allValues.size() / 2);
		} else {
			outputValues = allValues.subList(allValues.size() / 2, allValues.size());
		}

		String command;
		String response;
		for (AudioOutput output : outputValues) {
			String group = output.getPropertyName();
			String valueOutput = output.getValue();
			for (AudioInput input : AudioInput.values()) {
				command = EasyIpMixerCommand.GAIN_MONITOR.replace("$1", valueOutput).replace("$2", input.getValue());
				response = sendCommandDetails(command);
				cacheKeyAndValue.put(EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + EasyIPMixerConstant.GAIN_DB, replaceDraftInResponse(response, command));
			}
		}
	}

	/**
	 * Populates camera position statistics and properties based on the retrieved data.
	 * Adds advanced control properties and updates stats with the current values.
	 *
	 * @param stats The statistics to be populated with camera position data.
	 * @param advancedControllableProperties The list of advanced controllable properties.
	 */
	private void populateCameraPosition(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties, String group) {
		String propertyName = group + EasyIPMixerConstant.HASH + MonitoringCommand.PAN.getName();
		String value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
		addAdvancedControlProperties(advancedControllableProperties, stats, createText(propertyName, value), value);

		propertyName = group + EasyIPMixerConstant.HASH + MonitoringCommand.TILT.getName();
		value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
		addAdvancedControlProperties(advancedControllableProperties, stats, createText(propertyName, value), value);

		propertyName = group + EasyIPMixerConstant.HASH + MonitoringCommand.ZOOM.getName();
		value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
		addAdvancedControlProperties(advancedControllableProperties, stats, createText(propertyName, value), value);
	}

	/**
	 * Populates all necessary data for monitoring and controlling based on the specified polling interval.
	 *
	 * @param stats The map to store statistics-related data.
	 * @param controlStats The map to store control-related data.
	 * @param advancedControllableProperties The list of advanced controllable properties.
	 */
	private void populateAllData(Map<String, String> stats, Map<String, String> controlStats, List<AdvancedControllableProperty> advancedControllableProperties) {
		populateMonitoringAndControllingData(stats, controlStats, advancedControllableProperties);
		populateEnabledRoute(controlStats, advancedControllableProperties);
		if (numOfPollingInterval == 2) {
			populateAudioVolume(controlStats, advancedControllableProperties);
		}
		if (numOfPollingInterval == 4) {
			populateAudioVolume(controlStats, advancedControllableProperties);
			populateCrossPointGain(controlStats, advancedControllableProperties);
		}
		currentPollingInterval = currentPollingInterval < numOfPollingInterval ? currentPollingInterval + 1 : 1;
	}

	/**
	 * Populates the monitoring and controlling data for various properties based on defined rules and configurations.
	 *
	 * @param stats Map containing monitoring statistics
	 * @param controlStats Map containing control-related statistics
	 * @param advancedControllableProperties List of advanced controllable properties
	 */
	private void populateMonitoringAndControllingData(Map<String, String> stats, Map<String, String> controlStats, List<AdvancedControllableProperty> advancedControllableProperties) {
		String value;
		String propertyName;
		String status;
		String standbyValue;
		for (EasyIPMixerProperty property : EasyIPMixerProperty.values()) {

			if (EasyIPMixerConstant.VIDEO_INPUT.equals(property.getGroup())) {
				for (String item : numberOfCamera) {
					propertyName = EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + property.getName();
					standbyValue = EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + EasyIPMixerProperty.CAMERA_STANDBY.getName();
					value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
					switch (property) {
						case CAMERA_PAN:
						case CAMERA_TILT:
						case CAMERA_ZOOM:
							if (EasyIPMixerConstant.NONE.equals(value)) {
								controlStats.put(propertyName, value);
							} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
								removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
								controlStats.put(propertyName, uppercaseFirstCharacter(value));
							} else {
								addAdvancedControlProperties(advancedControllableProperties, controlStats, createText(propertyName, value), value);
							}
							break;
						case GAIN:
							removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
							controlStats.remove(propertyName);
							if (cacheKeyAndValue.get(EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + EasyIPMixerProperty.AUTO_IRIS.getName())
									.equals(EasyIPMixerConstant.OFF_VALUE)) {
								if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
									removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
									controlStats.put(propertyName, GainValueEnum.getNameByValue(value));
								} else {
									List<String> arrayValues = Arrays.stream(GainValueEnum.values()).map(GainValueEnum::getName).collect(Collectors.toList());
									addAdvancedControlProperties(advancedControllableProperties, controlStats, createDropdown(propertyName, arrayValues.toArray(new String[0]), GainValueEnum.getNameByValue(value)),
											value);
								}
							}
							break;
						case IRIS:
							removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
							controlStats.remove(propertyName);
							if (cacheKeyAndValue.get(EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + EasyIPMixerProperty.AUTO_IRIS.getName())
									.equals(EasyIPMixerConstant.OFF_VALUE)) {
								if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
									removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
									controlStats.put(propertyName, IrisValueEnum.getNameByValue(value));
								} else {
									List<String> arrayValues = Arrays.stream(IrisValueEnum.values()).map(IrisValueEnum::getName).collect(Collectors.toList());
									addAdvancedControlProperties(advancedControllableProperties, controlStats, createDropdown(propertyName, arrayValues.toArray(new String[0]), IrisValueEnum.getNameByValue(value)),
											value);
								}
							}
							break;
						case RED_GAIN:
						case BLUE_GAIN:
							removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
							controlStats.remove(propertyName);
							if (cacheKeyAndValue.get(EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + EasyIPMixerProperty.AUTO_WHITE_BALANCE.getName())
									.equals(EasyIPMixerConstant.OFF_VALUE)) {
								if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
									removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
									controlStats.remove(propertyName + EasyIPMixerConstant.CURRENT_VALUE);
									controlStats.put(propertyName, uppercaseFirstCharacter(value));
								} else {
									CameraColorSettings colorSettings = CameraColorSettings.getByName(property.getName());
									addAdvancedControlProperties(advancedControllableProperties, controlStats,
											createSlider(controlStats, propertyName, colorSettings.getMinValue(), colorSettings.getMaxValue(), Float.parseFloat(colorSettings.getMinValue()),
													Float.parseFloat(colorSettings.getMaxValue()),
													Float.parseFloat(value)), value);
									controlStats.put(propertyName + EasyIPMixerConstant.CURRENT_VALUE, value);
								}
							}
							break;
						case GAMMA:
						case CHROMA:
						case DETAIL:
							String name = propertyName.replace(EasyIPMixerConstant.SHARPNESS, EasyIPMixerConstant.EMPTY).replace(EasyIPMixerConstant.SATURATION, EasyIPMixerConstant.EMPTY) + EasyIPMixerConstant.CURRENT_VALUE;
							if (EasyIPMixerConstant.NONE.equals(value)) {
								controlStats.put(propertyName, value);
							} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
								removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
								controlStats.remove(name);
								controlStats.put(propertyName, uppercaseFirstCharacter(value));
							} else {
								CameraColorSettings colorSettings = CameraColorSettings.getByName(property.getName());
								addAdvancedControlProperties(advancedControllableProperties, controlStats,
										createSlider(controlStats, propertyName, colorSettings.getMinValue(), colorSettings.getMaxValue(), Float.parseFloat(colorSettings.getMinValue()),
												Float.parseFloat(colorSettings.getMaxValue()),
												Float.parseFloat(value)), value);
								controlStats.put(name, value);
							}
							break;
						case CAMERA_FOCUS_MODE:
							if (EasyIPMixerConstant.NONE.equals(value)) {
								controlStats.put(propertyName, value);
							} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
								removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
								controlStats.put(propertyName, EasyIPMixerConstant.ON_VALUE.equals(value) ? EasyIPMixerConstant.AUTO : EasyIPMixerConstant.MANUAL);
							} else {
								status = EasyIPMixerConstant.ON_VALUE.equals(value) ? "1" : "0";
								addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), EasyIPMixerConstant.MANUAL, EasyIPMixerConstant.AUTO),
										status);
							}
							break;
						case CAMERA_HOME:
							if (!EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
								addAdvancedControlProperties(advancedControllableProperties, controlStats, createButton(propertyName, EasyIPMixerConstant.SET, EasyIPMixerConstant.SETTING, 0),
										EasyIPMixerConstant.NONE);
							} else {
								removeValueForTheControllableProperty(propertyName, stats, advancedControllableProperties);
							}
							break;
						case CAMERA_STANDBY:
							if (EasyIPMixerConstant.NONE.equals(value)) {
								controlStats.put(propertyName, value);
							} else {
								status = EasyIPMixerConstant.ON_VALUE.equals(value) ? "1" : "0";
								addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON),
										status);
							}
							break;
						case AUTO_IRIS:
						case AUTO_WHITE_BALANCE:
							if (EasyIPMixerConstant.NONE.equals(value)) {
								controlStats.put(propertyName, value);
							} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
								removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
								controlStats.put(propertyName, uppercaseFirstCharacter(value));
							} else {
								status = EasyIPMixerConstant.ON_VALUE.equals(value) ? "1" : "0";
								addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON),
										status);
							}
							break;
						case WIDE_DYNAMIC_RANGE:
						case BACKLIGHT_COMPENSATION:
							if (cacheKeyAndValue.get(EasyIPMixerConstant.VIDEO_INPUT + EasyIPMixerMapping.getNameByValue(item) + EasyIPMixerConstant.HASH + EasyIPMixerProperty.AUTO_IRIS.getName())
									.equals(EasyIPMixerConstant.ON_VALUE)) {
								if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
									removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
									controlStats.put(propertyName, uppercaseFirstCharacter(value));
								} else {
									status = EasyIPMixerConstant.ON_VALUE.equals(value) ? "1" : "0";
									addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON),
											status);
								}
							}
							break;
						case CAMERA_PRESET:
							if (!EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(standbyValue))) {
								List<String> arrayValues = Arrays.stream(PresetValueEnum.values()).map(PresetValueEnum::getName).collect(Collectors.toList());
								arrayValues.add(0, EasyIPMixerConstant.PRESET_MESSAGE);
								addAdvancedControlProperties(advancedControllableProperties, controlStats, createDropdown(propertyName, arrayValues.toArray(new String[0]), EasyIPMixerConstant.PRESET_MESSAGE),
										EasyIPMixerConstant.PRESET_MESSAGE);
							} else {
								removeValueForTheControllableProperty(propertyName, stats, advancedControllableProperties);
							}
							break;
						default:
							stats.put(propertyName, uppercaseFirstCharacter(value));
							break;
					}
				}
			} else {
				propertyName = property.getGroup().concat(property.getName());
				value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
				switch (property) {
					case SYSTEM_REBOOT:
						addAdvancedControlProperties(advancedControllableProperties, controlStats, createButton(propertyName, EasyIPMixerConstant.REBOOT_NOW, EasyIPMixerConstant.REBOOTING, 0),
								EasyIPMixerConstant.NONE);
						break;
					case VIDEO_MUTE:
					case VIDEO_PIP:
					case AUDIO_MUTE:
						if (EasyIPMixerConstant.NONE.equals(value)) {
							controlStats.put(propertyName, value);
						} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(EasyIPMixerProperty.SYSTEM_STANDBY.getName()))) {
							removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
							controlStats.put(propertyName, uppercaseFirstCharacter(value));
						} else {
							status = EasyIPMixerConstant.ON_VALUE.equals(value) ? "1" : "0";
							addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON), status);
						}
						break;
					case SYSTEM_STANDBY:
						if (EasyIPMixerConstant.NONE.equals(value)) {
							controlStats.put(propertyName, value);
						} else {
							status = EasyIPMixerConstant.ON_VALUE.equals(value) ? "1" : "0";
							addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON), status);
						}
						break;
					case VIDEO_SOURCE:
						if (EasyIPMixerConstant.NONE.equals(value)) {
							controlStats.put(propertyName, value);
						} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(EasyIPMixerProperty.SYSTEM_STANDBY.getName()))) {
							removeValueForTheControllableProperty(propertyName, controlStats, advancedControllableProperties);
							controlStats.put(propertyName, SourceValueEnum.getNameByValue(value));
						} else {
							List<String> arrayValues = Arrays.stream(SourceValueEnum.values()).map(SourceValueEnum::getName).collect(Collectors.toList());
							addAdvancedControlProperties(advancedControllableProperties, controlStats, createDropdown(propertyName, arrayValues.toArray(new String[0]), SourceValueEnum.getNameByValue(value)),
									value);
						}
						break;
					default:
						stats.put(propertyName, uppercaseFirstCharacter(value));
						break;
				}
			}
		}
	}

	/**
	 * Populates audio volume-related statistics and controls based on provided audio properties.
	 *
	 * @param stats Map containing audio-related statistics
	 * @param advancedControllableProperties List of advanced controllable properties for audio
	 */
	private void populateAudioVolume(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		String mutePropertyName;
		String volumePropertyName;
		String muteValue;
		String volumeValue;
		List<String> propertyNameList = Stream.concat(
				Arrays.stream(AudioInput.values()).map(AudioInput::getPropertyName),
				Arrays.stream(AudioOutput.values()).map(AudioOutput::getPropertyName)).collect(Collectors.toList());
		propertyNameList.remove(0);
		for (String propertyName : propertyNameList) {
			String min = "-42";
			String max = "6";
			if (propertyName.equals(AudioInput.HDMI_IN_LEFT.getPropertyName()) || propertyName.equals(AudioInput.HDMI_IN_RIGHT.getPropertyName())
					|| propertyName.equals(AudioInput.LINE_MIC_1.getPropertyName()) || propertyName.equals(AudioInput.LINE_MIC_2.getPropertyName())
					|| propertyName.equals(AudioOutput.OUTPUT1.getPropertyName()) || propertyName.equals(AudioOutput.OUTPUT2.getPropertyName())) {
				min = "-50";
				max = "20";
			}
			mutePropertyName = propertyName + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE;
			volumePropertyName = propertyName + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_DB;
			volumeValue = getDefaultValueForNullData(cacheKeyAndValue.get(volumePropertyName));
			muteValue = getDefaultValueForNullData(cacheKeyAndValue.get(mutePropertyName));

			if (EasyIPMixerConstant.NONE.equals(muteValue)) {
				stats.put(mutePropertyName, EasyIPMixerConstant.NONE);
			} else {
				if (checkAudioMute(propertyName) || checkSystemStandby(propertyName)) {
					removeValueForTheControllableProperty(mutePropertyName, stats, advancedControllableProperties);
					stats.put(mutePropertyName, uppercaseFirstCharacter(muteValue));
				} else {
					String status = EasyIPMixerConstant.ON_VALUE.equals(muteValue) ? "1" : "0";
					addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(mutePropertyName, Integer.parseInt(status), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON), status);
				}
			}

			if (EasyIPMixerConstant.NONE.equals(volumeValue)) {
				stats.put(volumePropertyName, EasyIPMixerConstant.NONE);
			} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(EasyIPMixerProperty.SYSTEM_STANDBY.getName()))) {
				if (propertyName.equals(AudioInput.DANTE_IN_1.getPropertyName()) || propertyName.equals(AudioInput.DANTE_IN_2.getPropertyName())
						|| propertyName.equals(AudioInput.DANTE_IN_3.getPropertyName()) || propertyName.equals(AudioInput.DANTE_IN_4.getPropertyName())
						|| propertyName.equals(AudioInput.LINE_MIC_1.getPropertyName()) || propertyName.equals(AudioInput.LINE_MIC_2.getPropertyName())
						|| propertyName.equals(AudioInput.USB_PLAYBACK_LEFT.getPropertyName()) || propertyName.equals(AudioInput.USB_PLAYBACK_RIGHT.getPropertyName())
						|| propertyName.equals(AudioInput.HDMI_IN_LEFT.getPropertyName()) || propertyName.equals(AudioInput.HDMI_IN_RIGHT.getPropertyName())) {
					addAdvancedControlProperties(advancedControllableProperties, stats,
							createSlider(stats, volumePropertyName, min, max, Float.parseFloat(min), Float.parseFloat(max), Float.parseFloat(volumeValue)), volumeValue);
					stats.put(propertyName + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_CURRENT_VALUE, convertFloatToIntString(volumeValue));
				} else {
					removeValueForTheControllableProperty(volumePropertyName, stats, advancedControllableProperties);
					stats.remove(propertyName + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_CURRENT_VALUE);
					stats.put(volumePropertyName, convertFloatToIntString(volumeValue));
				}
			} else {
				addAdvancedControlProperties(advancedControllableProperties, stats,
						createSlider(stats, volumePropertyName, min, max, Float.parseFloat(min), Float.parseFloat(max), Float.parseFloat(volumeValue)), volumeValue);
				stats.put(propertyName + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_CURRENT_VALUE, convertFloatToIntString(volumeValue));
			}
		}
	}

	/**
	 * Checks if the audio is muted based on the property name and the state of the audio mute in the cache.
	 *
	 * @param propertyName the name of the audio property to check
	 * @return if the audio is muted for the specified property
	 */
	private boolean checkAudioMute(String propertyName) {
		return EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(EasyIPMixerProperty.AUDIO_MUTE.getName())) &&
				(propertyName.equals(AudioInput.DANTE_IN_1.getPropertyName()) || propertyName.equals(AudioInput.DANTE_IN_2.getPropertyName())
						|| propertyName.equals(AudioInput.DANTE_IN_3.getPropertyName()) || propertyName.equals(AudioInput.DANTE_IN_4.getPropertyName())
						|| propertyName.equals(AudioInput.LINE_MIC_1.getPropertyName()) || propertyName.equals(AudioInput.LINE_MIC_2.getPropertyName())
						|| propertyName.equals(AudioOutput.USB_RECORD_LEFT.getPropertyName()) || propertyName.equals(AudioOutput.USB_RECORD_RIGHT.getPropertyName())
						|| propertyName.equals(AudioInput.HDMI_IN_LEFT.getPropertyName()) || propertyName.equals(AudioInput.HDMI_IN_RIGHT.getPropertyName()));
	}

	/**
	 * Checks if the system is in standby mode based on the property name and the state of the system standby in the cache.
	 *
	 * @param propertyName the name of the property to check for system standby
	 * @return if the system is in standby mode for the specified property
	 */
	private boolean checkSystemStandby(String propertyName) {
		return EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(EasyIPMixerProperty.SYSTEM_STANDBY.getName())) &&
				(propertyName.equals(AudioOutput.DANTE_OUT_1.getPropertyName()) || propertyName.equals(AudioOutput.DANTE_OUT_2.getPropertyName())
						|| propertyName.equals(AudioOutput.DANTE_OUT_3.getPropertyName()) || propertyName.equals(AudioOutput.DANTE_OUT_4.getPropertyName())
						|| propertyName.equals(AudioOutput.OUTPUT1.getPropertyName()) || propertyName.equals(AudioOutput.OUTPUT2.getPropertyName())
						|| propertyName.equals(AudioOutput.HDMI_OUT_LEFT.getPropertyName()) || propertyName.equals(AudioOutput.HDMI_OUT_RIGHT.getPropertyName()));
	}

	/**
	 * Populates the enabled routes for audio inputs and outputs, creating related statistics and controls.
	 *
	 * @param stats Map containing audio-related statistics
	 * @param advancedControllableProperties List of advanced controllable properties for audio
	 */
	private void populateEnabledRoute(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		String value;
		String propertyName;
		String status;
		for (AudioOutput output : AudioOutput.values()) {
			String group = output.getPropertyName();
			String localCacheName = EasyIPMixerConstant.CROSSPOINT + output.getPropertyName() + EasyIPMixerConstant.HASH + output.getPropertyName();
			String cacheValue = getDefaultValueForNullData(cacheKeyAndValue.get(localCacheName));
			for (AudioInput input : AudioInput.values()) {
				if (group.contains(EasyIPMixerConstant.USB) && input.getPropertyName().contains(EasyIPMixerConstant.USB)) {
					continue;
				}
				propertyName = EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + EasyIPMixerConstant.ROUTE;
				value = input.getValue();
				status = EasyIPMixerConstant.ZERO;
				if (cacheValue.contains(value)) {
					status = EasyIPMixerConstant.NUMBER_ONE;
				}
				if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(EasyIPMixerProperty.SYSTEM_STANDBY.getName()))) {
					removeValueForTheControllableProperty(propertyName, stats, advancedControllableProperties);
					stats.put(propertyName, EasyIPMixerConstant.NUMBER_ONE.equals(status) ? EasyIPMixerConstant.ON : EasyIPMixerConstant.OFF);
				} else {
					addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(propertyName, Integer.parseInt(status), EasyIPMixerConstant.OFF, EasyIPMixerConstant.ON), status);
				}
			}
		}
	}

	/**
	 * Populates the cross-point gains for audio inputs and outputs, creating related statistics and controls.
	 *
	 * @param stats Map containing audio-related statistics
	 * @param advancedControllableProperties List of advanced controllable properties for audio
	 */
	private void populateCrossPointGain(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		String value;
		String propertyName;
		for (AudioOutput output : AudioOutput.values()) {
			String group = output.getPropertyName();
			for (AudioInput input : AudioInput.values()) {
				if (group.contains(EasyIPMixerConstant.USB) && input.getPropertyName().contains(EasyIPMixerConstant.USB)) {
					continue;
				}
				propertyName = EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + EasyIPMixerConstant.GAIN_DB;
				value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
				if (EasyIPMixerConstant.NONE.equals(value)) {
					stats.put(propertyName, value);
				} else if (EasyIPMixerConstant.ON_VALUE.equals(cacheKeyAndValue.get(EasyIPMixerProperty.SYSTEM_STANDBY.getName()))) {
					removeValueForTheControllableProperty(propertyName, stats, advancedControllableProperties);
					stats.remove(EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + EasyIPMixerConstant.GAIN_CURRENT_VALUE);
					stats.put(propertyName, convertFloatToIntString(value));
				} else {
					addAdvancedControlProperties(advancedControllableProperties, stats, createSlider(stats, propertyName, "-12", "12", -12.0f, 12.0f, Float.parseFloat(value)), value);
					stats.put(EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + EasyIPMixerConstant.GAIN_CURRENT_VALUE, convertFloatToIntString(value));
				}
			}
		}
	}

	/**
	 * Send command to control device by value
	 *
	 * @param command the command is command to send to the device
	 * @param value the value is value of the command
	 * @param name the name is group name
	 */
	private void sendCommandToControlDevice(String command, String value, String name) {
		try {
			String response = send(command.contains("\r") ? command : command.concat("\r"));
			if (StringUtils.isNullOrEmpty(response)) {
				throw new IllegalArgumentException(String.format("Error when control %s, Syntax error command: %s", name, response));
			}
			if (response.contains("Preset cannot be recalled, may not be set")) {
				throw new IllegalArgumentException(String.format("Error when control %s, Preset cannot be recalled, may not be set", name));
			}
			if (response.contains("Invalid routing request")) {
				throw new IllegalArgumentException(String.format("Error when control %s, Route is unavailable", name));
			}
			if (response.contains(EasyIPMixerConstant.ERROR_RESPONSE) || !response.contains(EasyIPMixerConstant.OK) || response.contains(EasyIPMixerConstant.ERROR)) {
				throw new IllegalArgumentException(String.format("Error when control %s, Syntax error command: %s", name, response));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Can't control %s with %s value. %s", name, value, e.getMessage()));
		}
	}

	/**
	 * Send command detail to get the data from device
	 *
	 * @param command the command is command to get data
	 * @throws FailedLoginException if authentication fails
	 */
	private String sendCommandDetails(String command) throws FailedLoginException {
		try {
			String response = send(command.contains("\r") ? command : command.concat("\r"));
			if (response.contains("Syntax error")) {
				return EasyIPMixerConstant.NONE;
			}
			return response.replaceAll(EasyIPMixerConstant.REGEX_RESPONSE, EasyIPMixerConstant.EMPTY);
		} catch (FailedLoginException e) {
			throw new FailedLoginException("Login failed: " + e);
		} catch (Exception ex) {
			logger.error(String.format("Error when get command: %s", command), ex);
			failedMonitor.put(command, ex.getMessage());
		}
		return EasyIPMixerConstant.EMPTY;
	}

	/**
	 * Executes the 'sendCommandToControlDevice' method using an ExecutorService,
	 * ensuring the command execution is performed asynchronously.
	 *
	 * @param command The command string to be executed.
	 * @param value The value associated with the command.
	 * @param name The name of the command.
	 */
	private void sendCommandToControlDeviceWithExecutor(String command, String value, String name) {
		if (dataCollector == null || dataCollector.isDone()) {
			dataCollector = runAsync(() -> {
				reentrantLock.lock();
				try {
					sendCommandToControlDevice(command, value, name);
				} catch (Exception ce) {
					logger.debug("Exception white collecting device data.", ce);
				} finally {
					reentrantLock.unlock();
				}
			}, executorService);
		}
	}

	/**
	 * Extract value received from device
	 *
	 * @param response the response is response of device
	 * @param regex the regex is regex to extract the response value
	 * @return String is value of the device
	 */
	private static String extractResponseValue(String response, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {
			return matcher.group(1).trim();
		}

		return EasyIPMixerConstant.NONE;
	}

	/**
	 * Converts a floating-point number string to an integer string.
	 *
	 * @param floatString the string representation of a floating-point number
	 * @return the string representation of the integer value of the input floatString
	 */
	public static String convertFloatToIntString(String floatString) {
		try {
			float floatValue = Float.parseFloat(floatString);
			int intValue = (int) floatValue;
			return String.valueOf(intValue);
		} catch (Exception e) {
			return EasyIPMixerConstant.NONE;
		}
	}

	/**
	 * Removes the "(dB)" unit from the given string and trims any leading or trailing whitespace.
	 *
	 * @param input The input string from which the "(dB)" unit will be removed.
	 * @return The modified string after removing the "(dB)" unit and trimming whitespace.
	 */
	public String removeUnit(String input) {
		String result = input.replace("dB", "");
		return result.trim();
	}

	/**
	 * Replaces draft and irrelevant elements from a response string based on the command executed.
	 *
	 * @param response the original response string from the executed command
	 * @param command  the command string that needs to be replaced in the response
	 * @return a cleaned response string with draft and irrelevant elements removed
	 */
	private String replaceDraftInResponse(String response, String command) {
		return response.replace(command, "").replace("OK", "")
				.replace(">", "").replace("]", "").trim();
	}

	/**
	 * Determines the number of polling intervals based on configuration settings.
	 * If the configuration management is enabled, sets the number of polling intervals to 4 if crosspoint gain is enabled, otherwise sets it to 2.
	 */
	private void getNumOfPollingInterval() {
		numOfPollingInterval = 1;
		if (isConfigManagement) {
			numOfPollingInterval = isEnableCrosspointGain ? 4 : 2;
		}
	}

	/**
	 * Checks if the provided group exists in the list of audio input and output property names.
	 *
	 * @param group The group name to check.
	 * @return true if the group exists in the list
	 */
	private boolean checkAudioVolumeControl(String group) {
		List<String> propertyNameList = Stream.concat(
				Arrays.stream(AudioInput.values()).map(AudioInput::getPropertyName),
				Arrays.stream(AudioOutput.values()).map(AudioOutput::getPropertyName)).collect(Collectors.toList());
		for (String item : propertyNameList) {
			if (group.equals(item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) ? value : EasyIPMixerConstant.NONE;
	}

	/**
	 * This method is used to validate input config management from user
	 */
	private void convertConfigManagement() {
		isConfigManagement = StringUtils.isNotNullOrEmpty(this.configManagement) && this.configManagement.equalsIgnoreCase(EasyIPMixerConstant.TRUE);
	}

	/**
	 * This method is used to validate input config management from user
	 */
	private void convertEnableCrosspointGain() {
		isEnableCrosspointGain = StringUtils.isNotNullOrEmpty(this.enableCrosspointGain) && this.enableCrosspointGain.equalsIgnoreCase(EasyIPMixerConstant.TRUE);
	}

	/**
	 * capitalize the first character of the string
	 *
	 * @param input input string
	 * @return string after fix
	 */
	private String uppercaseFirstCharacter(String input) {
		char firstChar = input.charAt(0);
		return Character.toUpperCase(firstChar) + input.substring(1);
	}

	/**
	 * Converts a status switch value to its corresponding ON or OFF value.
	 *
	 * @param value the status switch value to be converted
	 * @return the corresponding ON or OFF value based on the input status switch value
	 */
	private String getStatusSwitch(String value) {
		return EasyIPMixerConstant.NUMBER_ONE.equals(value) ? EasyIPMixerConstant.ON_VALUE : EasyIPMixerConstant.OFF_VALUE;
	}

	/**
	 * Checks if the input value is valid and converts it to an integer.
	 *
	 * @param value The input value to be checked and converted to an integer.
	 * @param min is the minimum value
	 * @param max is the maximum value
	 * @return The converted integer value if the input is valid.
	 * @throws IllegalArgumentException if the input value is not a valid integer.
	 */
	private String checkValidInput(double min, double max, String value) {
		double initial = min;
		try {
			double valueCompare = Double.parseDouble(value);
			if (min <= valueCompare && valueCompare <= max) {
				DecimalFormat decimalFormat = new DecimalFormat("#.##");
				String formattedNumber = decimalFormat.format(valueCompare);
				if (!formattedNumber.contains(EasyIPMixerConstant.DOT)) {
					formattedNumber = formattedNumber.concat(".0");
				}
				return formattedNumber;
			}
			if (valueCompare > max) {
				initial = max;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("The invalid input. " + e.getMessage());
		}
		String result = String.valueOf(initial);
		return !result.contains(EasyIPMixerConstant.DOT) ? result.concat(".0") : result;
	}

	/**
	 * Add advancedControllableProperties if advancedControllableProperties different empty
	 *
	 * @param advancedControllableProperties advancedControllableProperties is the list that store all controllable properties
	 * @param stats store all statistics
	 * @param property the property is item advancedControllableProperties
	 * @throws IllegalStateException when exception occur
	 */
	private void addAdvancedControlProperties(List<AdvancedControllableProperty> advancedControllableProperties, Map<String, String> stats, AdvancedControllableProperty property, String value) {
		if (property != null) {
			for (AdvancedControllableProperty controllableProperty : advancedControllableProperties) {
				if (controllableProperty.getName().equals(property.getName())) {
					advancedControllableProperties.remove(controllableProperty);
					break;
				}
			}
			if (StringUtils.isNotNullOrEmpty(value)) {
				stats.put(property.getName(), value);
			} else {
				stats.put(property.getName(), EasyIPMixerConstant.EMPTY);
			}
			advancedControllableProperties.add(property);
		}
	}

	/**
	 * Update cache device data
	 *
	 * @param cacheMapOfPropertyNameAndValue the cacheMapOfPropertyNameAndValue are map key and value of it
	 * @param property the key is property name
	 * @param value the value is String value
	 */
	private void updateCachedDeviceData(Map<String, String> cacheMapOfPropertyNameAndValue, String property, String value) {
		cacheMapOfPropertyNameAndValue.put(property, value);
	}

	/**
	 * Create switch is control property for metric
	 *
	 * @param name the name of property
	 * @param status initial status (0|1)
	 * @return AdvancedControllableProperty switch instance
	 */
	private AdvancedControllableProperty createSwitch(String name, int status, String labelOff, String labelOn) {
		AdvancedControllableProperty.Switch toggle = new AdvancedControllableProperty.Switch();
		toggle.setLabelOff(labelOff);
		toggle.setLabelOn(labelOn);

		AdvancedControllableProperty advancedControllableProperty = new AdvancedControllableProperty();
		advancedControllableProperty.setName(name);
		advancedControllableProperty.setValue(status);
		advancedControllableProperty.setType(toggle);
		advancedControllableProperty.setTimestamp(new Date());

		return advancedControllableProperty;
	}

	/**
	 * Create a button.
	 *
	 * @param name name of the button
	 * @param label label of the button
	 * @param labelPressed label of the button after pressing it
	 * @param gracePeriod grace period of button
	 * @return This returns the instance of {@link AdvancedControllableProperty} type Button.
	 */
	private AdvancedControllableProperty createButton(String name, String label, String labelPressed, long gracePeriod) {
		AdvancedControllableProperty.Button button = new AdvancedControllableProperty.Button();
		button.setLabel(label);
		button.setLabelPressed(labelPressed);
		button.setGracePeriod(gracePeriod);
		return new AdvancedControllableProperty(name, new Date(), button, "");
	}

	/***
	 * Create dropdown advanced controllable property
	 *
	 * @param name the name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty dropdown instance
	 */
	private AdvancedControllableProperty createDropdown(String name, String[] values, String initialValue) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(values);
		dropDown.setLabels(values);

		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}

	/***
	 * Create AdvancedControllableProperty slider instance
	 *
	 * @param stats extended statistics
	 * @param name name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty slider instance
	 */
	private AdvancedControllableProperty createSlider(Map<String, String> stats, String name, String labelStart, String labelEnd, Float rangeStart, Float rangeEnd, Float initialValue) {
		stats.put(name, initialValue.toString());
		AdvancedControllableProperty.Slider slider = new AdvancedControllableProperty.Slider();
		slider.setLabelStart(labelStart);
		slider.setLabelEnd(labelEnd);
		slider.setRangeStart(rangeStart);
		slider.setRangeEnd(rangeEnd);

		return new AdvancedControllableProperty(name, new Date(), slider, initialValue);
	}

	/**
	 * Create text is control property for metric
	 *
	 * @param name the name of the property
	 * @param stringValue character string
	 * @return AdvancedControllableProperty Text instance
	 */
	private AdvancedControllableProperty createText(String name, String stringValue) {
		AdvancedControllableProperty.Text text = new AdvancedControllableProperty.Text();
		return new AdvancedControllableProperty(name, new Date(), text, stringValue);
	}

	/**
	 * Update the value for the control metric
	 *
	 * @param property is name of the metric
	 * @param value the value is value of properties
	 * @param extendedStatistics list statistics property
	 * @param advancedControllableProperties the advancedControllableProperties is list AdvancedControllableProperties
	 */
	private void updateValueForTheControllableProperty(String property, String value, Map<String, String> extendedStatistics, List<AdvancedControllableProperty> advancedControllableProperties) {
		extendedStatistics.put(property, value);
		advancedControllableProperties.stream().filter(advancedControllableProperty ->
				property.equals(advancedControllableProperty.getName())).findFirst().ifPresent(advancedControllableProperty ->
				advancedControllableProperty.setValue(value));
	}

	/**
	 * Remove the value for the control metric
	 *
	 * @param property is name of the metric
	 * @param extendedStatistics list statistics property
	 * @param advancedControllableProperties the advancedControllableProperties is list AdvancedControllableProperties
	 */
	private void removeValueForTheControllableProperty(String property, Map<String, String> extendedStatistics, List<AdvancedControllableProperty> advancedControllableProperties) {
		if (!advancedControllableProperties.isEmpty()) {
			for (AdvancedControllableProperty advancedControllableProperty : advancedControllableProperties) {
				if (advancedControllableProperty.getName().equals(property)) {
					extendedStatistics.remove(property);
					advancedControllableProperties.remove(advancedControllableProperty);
					break;
				}
			}
		}
	}
}
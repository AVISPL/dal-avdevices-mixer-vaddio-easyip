/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIPMixerProperty;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.EasyIpMixerCommand;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.MonitoringCommand;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.NetworkInformation;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.StreamSettings;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.VersionInformation;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.audio.AudioInput;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.audio.AudioOutput;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera.CCUSceneValueEnum;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera.CameraColorSettings;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.camera.PresetValueEnum;
import com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip.common.video.SourceValueEnum;
import com.avispl.symphony.dal.communicator.SshCommunicator;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * EasyIPMixerCommunicator An implementation of SshCommunicator to provide communication and interaction with Vaddio Mixer EasyIP device
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 11/16/2023
 * @since 1.0.0
 */
public class EasyIPMixerCommunicator extends SshCommunicator implements Monitorable, Controller {

	/**
	 * cache to store key and value
	 */
	private final Map<String, String> cacheKeyAndValue = new HashMap<>();

	/**
	 * count the failed command
	 */
	private final Map<String, String> failedMonitor = new HashMap<>();

	/**
	 * the getMultipleStatistics interval if it's fail to doGet the cmd
	 */
	private static final int controlSSHTimeout = 3000;
	/**
	 * Set back to default timeout value in {@link SshCommunicator}
	 */
	private static final int statisticsSSHTimeout = 30000;

	/**
	 * ReentrantLock to prevent telnet session is closed when adapter is retrieving statistics from the device.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * Store previous/current ExtendedStatistics
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * Num Of Polling Interval
	 */
	private int numOfPollingInterval;

	/**
	 * Current polling interval
	 */
	private int currentPollingInterval;

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
	 * isEnableCrosspointGain in boolean value
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
	 * MiddleAtlanticPowerUnitCommunicator constructor
	 */
	public EasyIPMixerCommunicator() {
		this.setCommandErrorList(Collections.singletonList("Error: response error"));
		this.setCommandSuccessList(Collections.singletonList("> "));
		this.setLoginSuccessList(Collections.singletonList("********************************************\r\n        \r\nWelcome admin\r\n> "));
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
				stats.putAll(controlStats);
				extendedStatistics.setControllableProperties(advancedControllableProperties);
				extendedStatistics.setStatistics(stats);
			}
			localExtendedStatistics = extendedStatistics;
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
	public void controlProperty(ControllableProperty controllableProperty) {
		String property = controllableProperty.getProperty();
		String value = String.valueOf(controllableProperty.getValue());
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Perform control operation with property: %s and value: %s", property, value));
		}
		reentrantLock.lock();
		try {
			this.timeout = controlSSHTimeout;
			if (localExtendedStatistics == null) {
				return;
			}
		} finally {
			this.timeout = statisticsSSHTimeout;
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
		super.internalInit();
		this.createChannel();
		currentPollingInterval = 1;
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
	private void retrieveAllData() throws FailedLoginException {
		getNumOfPollingInterval();
		if (currentPollingInterval == 1) {
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
		currentPollingInterval = currentPollingInterval < numOfPollingInterval ? currentPollingInterval + 1 : 1;
	}

	/**
	 * Retrieves monitoring data by sending commands based on MonitoringCommand enum values.
	 * Updates cacheKeyAndValue with extracted information based on different commands.
	 *
	 * @throws FailedLoginException if the login attempt fails while sending the command.
	 */
	private void retrieveMonitoring() throws FailedLoginException {
		String response;
		for (MonitoringCommand command : MonitoringCommand.values()) {
			response = sendCommandDetails(command.getCommand());
			switch (command) {
				case NETWORK:
					for (NetworkInformation network : NetworkInformation.values()) {
						cacheKeyAndValue.put(EasyIPMixerConstant.NETWORK_GROUP + network.getName(), extractResponseValue(response, network.getValue()));
					}
					break;
				case VERSION:
					for (VersionInformation version : VersionInformation.values()) {
						cacheKeyAndValue.put(version.getName(), extractResponseValue(response, version.getValue()));
					}
					break;
				case STREAMING:
					for (StreamSettings stream : StreamSettings.values()) {
						cacheKeyAndValue.put(EasyIPMixerConstant.STREAMING_GROUP + stream.getName(), extractResponseValue(response, stream.getValue()));
					}
					break;
				case CAMERA_COLOR:
					for (CameraColorSettings colorSettings : CameraColorSettings.values()) {
						cacheKeyAndValue.put(EasyIPMixerConstant.CAMERA_COLOR_GROUP + colorSettings.getName(), extractResponseValue(response, colorSettings.getCommand()));
					}
					break;
				case FACTORY_RESET:
					cacheKeyAndValue.put("FactoryResetSoftwareStatus", extractResponseValue(response, "factory-reset (software):(.*?)\r\n"));
					cacheKeyAndValue.put("FactoryResetHardwareStatus", extractResponseValue(response, "factory-reset (hardware):(.*?)\r\n"));
					break;
				case PAN:
				case TILT:
				case ZOOM:
				case FOCUS_MODE:
				case CAMERA_STANDBY:
					cacheKeyAndValue.put(EasyIPMixerConstant.CAMERA_SETTINGS_GROUP + command.getName(), extractResponseValue(response, command.getRegex()));
					break;
				default:
					cacheKeyAndValue.put(command.getName(), extractResponseValue(response, command.getRegex()));
					break;
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
			cacheKeyAndValue.put(EasyIPMixerConstant.CROSSPOINT + output.getPropertyName() + EasyIPMixerConstant.HASH + output.getPropertyName(), response);
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
		//Output
		for (AudioOutput output : AudioOutput.values()) {
			command = EasyIpMixerCommand.MUTE_MONITOR.replace("$", output.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(output.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE, extractResponseValue(response, "mute:(.*?)\r\n"));

			command = EasyIpMixerCommand.VOLUME_MONITOR.replace("$", output.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(output.getPropertyName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME, extractResponseValue(response, "volume:(.*?)\r\n"));
		}

		//Input
		for (AudioInput input : AudioInput.values()) {
			command = EasyIpMixerCommand.MUTE_MONITOR.replace("$", input.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(input.getName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE, extractResponseValue(response, "mute:(.*?)\r\n"));

			command = EasyIpMixerCommand.VOLUME_MONITOR.replace("$", input.getValue());
			response = sendCommandDetails(command);
			cacheKeyAndValue.put(input.getName() + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME, extractResponseValue(response, "volume:(.*?)\r\n"));
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
				cacheKeyAndValue.put(EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + EasyIPMixerConstant.GAIN, response);
			}
		}
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
		for (EasyIPMixerProperty property : EasyIPMixerProperty.values()) {
			propertyName = property.getGroup().concat(property.getName());
			value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
			switch (property) {
				case CAMERA_PAN:
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createSlider(stats, propertyName, "-150", "150", -150.0f, 150.0f, Float.parseFloat(value)), value);
					controlStats.put(propertyName + EasyIPMixerConstant.CURRENT_VALUE, value);
					break;
				case CAMERA_TILT:
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createSlider(stats, propertyName, "-30", "90", -30.0f, 90.0f, Float.parseFloat(value)), value);
					controlStats.put(propertyName + EasyIPMixerConstant.CURRENT_VALUE, value);
					break;
				case CAMERA_ZOOM:
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createSlider(stats, propertyName, "1", "20", -1.0f, 20.0f, Float.parseFloat(value)), value);
					controlStats.put(propertyName + EasyIPMixerConstant.CURRENT_VALUE, value);
					break;
				case GAIN:
				case IRIS:
				case GAMMA:
				case CHROMA:
				case DETAIL:
				case RED_GAIN:
				case BLUE_GAIN:
					if (EasyIPMixerConstant.NONE.equals(value)) {
						controlStats.put(propertyName, value);
					} else {
						CameraColorSettings colorSettings = CameraColorSettings.getByName(property.getName());
						addAdvancedControlProperties(advancedControllableProperties, controlStats,
								createSlider(stats, propertyName, colorSettings.getMinValue(), colorSettings.getMaxValue(), Float.parseFloat(colorSettings.getMinValue()),
										Float.parseFloat(colorSettings.getMaxValue()),
										Float.parseFloat(value)), value);
						controlStats.put(propertyName + EasyIPMixerConstant.CURRENT_VALUE, value);
					}
					break;
				case CAMERA_FOCUS_MODE:
					String status = "on".equals(value) ? "1" : "0";
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), "Manual", "Auto"), status);
					break;
				case CAMERA_HOME:
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createButton(propertyName, "Set", "Setting", 0), EasyIPMixerConstant.NONE);
					break;
				case SYSTEM_REBOOT:
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createButton(propertyName, "Reboot", "Rebooting", 0), EasyIPMixerConstant.NONE);
					break;
				case CAMERA_STANDBY:
				case VIDEO_MUTE:
				case VIDEO_PIP:
				case AUDIO_MUTE:
				case SYSTEM_STANDBY:
				case AUTO_IRIS:
				case WIDE_DYNAMIC_RANGE:
				case AUTO_WHITE_BALANCE:
				case BACKLIGHT_COMPENSETION:
					if (EasyIPMixerConstant.NONE.equals(value)) {
						controlStats.put(propertyName, value);
					} else {
						status = "on".equals(value) ? "1" : "0";
						addAdvancedControlProperties(advancedControllableProperties, controlStats, createSwitch(propertyName, Integer.parseInt(status), "Off", "On"), status);
					}
					break;
				case CAMERA_PRESET:
					List<String> arrayValues = Arrays.stream(PresetValueEnum.values()).map(PresetValueEnum::getName).collect(Collectors.toList());
					arrayValues.add(0, "Please select a preset");
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createDropdown(propertyName, arrayValues.toArray(new String[0]), "Please select a preset"),
							"Please select a preset");
					break;
				case CAMERA_CCU_SCENE:
					arrayValues = Arrays.stream(CCUSceneValueEnum.values()).map(CCUSceneValueEnum::getName).collect(Collectors.toList());
					arrayValues.add(0, "Please select a CCU Scene");
					addAdvancedControlProperties(advancedControllableProperties, controlStats, createDropdown(propertyName, arrayValues.toArray(new String[0]), "Please select a CCU Scene"),
							"Please select a CCU Scene");
					break;
				case VIDEO_SOURCE:
					if (EasyIPMixerConstant.NONE.equals(value)) {
						controlStats.put(propertyName, value);
					} else {
						arrayValues = Arrays.stream(SourceValueEnum.values()).map(SourceValueEnum::getName).collect(Collectors.toList());
						addAdvancedControlProperties(advancedControllableProperties, controlStats, createDropdown(propertyName, arrayValues.toArray(new String[0]), SourceValueEnum.getNameByValue(value)), value);
					}
					break;
				default:
					stats.put(propertyName, uppercaseFirstCharacter(value));
					break;
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

		for (String propertyName : propertyNameList) {
			mutePropertyName = propertyName + EasyIPMixerConstant.HASH + EasyIPMixerConstant.MUTE;
			volumePropertyName = propertyName + EasyIPMixerConstant.HASH + EasyIPMixerConstant.VOLUME_DB;

			volumeValue = getDefaultValueForNullData(cacheKeyAndValue.get(volumePropertyName));
			muteValue = getDefaultValueForNullData(cacheKeyAndValue.get(mutePropertyName));

			//Mute
			if (EasyIPMixerConstant.NONE.equals(muteValue)) {
				stats.put(mutePropertyName, EasyIPMixerConstant.NONE);
			} else {
				String status = "on".equals(muteValue) ? "1" : "0";
				addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(mutePropertyName, Integer.parseInt(status), "Off", "On"), status);
			}

			//Volume
			if (EasyIPMixerConstant.NONE.equals(volumeValue)) {
				stats.put(volumePropertyName, EasyIPMixerConstant.NONE);
			} else {
				addAdvancedControlProperties(advancedControllableProperties, stats, createSlider(stats, volumePropertyName, "-42", "6", -42.0f, 6.0f, Float.parseFloat(volumeValue)), volumeValue);
				stats.put(propertyName + EasyIPMixerConstant.HASH + "VolumeCurrentValue(dB)", volumeValue);
			}
		}
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
				propertyName = EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + EasyIPMixerConstant.ROUTE;
				value = input.getValue();
				status = "0";
				if (cacheValue.contains(value)) {
					status = "1";
				}
				addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(propertyName, Integer.parseInt(status), "Off", "On"), status);
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
				propertyName = EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + "Gain(dB)";
				value = getDefaultValueForNullData(cacheKeyAndValue.get(propertyName));
				if (EasyIPMixerConstant.NONE.equals(value)) {
					stats.put(propertyName, value);
				} else {
					addAdvancedControlProperties(advancedControllableProperties, stats, createSlider(stats, propertyName, "-12", "12", -12.0f, 12.0f, Float.parseFloat(value)), value);
					stats.put(EasyIPMixerConstant.CROSSPOINT + group + EasyIPMixerConstant.HASH + input.getPropertyName() + "GainCurrentValue(dB)", value);
				}
			}
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
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) ? value : EasyIPMixerConstant.NONE;
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
	 * Add advancedControllableProperties if advancedControllableProperties different empty
	 *
	 * @param advancedControllableProperties advancedControllableProperties is the list that store all controllable properties
	 * @param stats store all statistics
	 * @param property the property is item advancedControllableProperties
	 * @return String response
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
}
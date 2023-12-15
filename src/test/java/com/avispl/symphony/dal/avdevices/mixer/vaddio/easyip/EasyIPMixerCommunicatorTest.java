/*
 *  Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

/**
 * EasyIPMixerCommunicatorTest class
 */
public class EasyIPMixerCommunicatorTest {
	private EasyIPMixerCommunicator easyIPMixerCommunicator;
	static ExtendedStatistics extendedStatistic;

	@BeforeEach()
	public void setUp() throws Exception {
		easyIPMixerCommunicator = new EasyIPMixerCommunicator();

		easyIPMixerCommunicator.setHost("");
		easyIPMixerCommunicator.setPort(22);
		easyIPMixerCommunicator.setLogin("");
		easyIPMixerCommunicator.setPassword("");
		easyIPMixerCommunicator.init();
		easyIPMixerCommunicator.connect();
		easyIPMixerCommunicator.setConfigManagement("true");
	}

	@AfterEach()
	public void destroy() throws Exception {
		easyIPMixerCommunicator.disconnect();
	}

	@Test
	void testGetMultipleStatistics() throws Exception {
//		easyIPMixerCommunicator.setEnableCrosspointGain("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(142, statistics.size());
		Assert.assertEquals(26, advancedControllablePropertyList.size());
	}

	@Test
	void testGetMultipleStatisticsWith2PollingCycle() throws Exception {
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Thread.sleep(30000);
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(484, statistics.size());
		Assert.assertEquals(42, advancedControllablePropertyList.size());
	}

	@Test
	void testGetMultipleStatisticsWith4PollingCycle() throws Exception {
		easyIPMixerCommunicator.setEnableCrosspointGain("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(484, statistics.size());
		Assert.assertEquals(42, advancedControllablePropertyList.size());
	}

	@Test
	void testMasterMute() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "AudioMute";
		String value = "1";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testCameraPosition() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "VideoInputEasyIPCamera2#Pan";
		String value = "0";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		long a = System.currentTimeMillis();
		easyIPMixerCommunicator.controlProperty(controllableProperty);
		System.out.println(System.currentTimeMillis() - a);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testColorSetting() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "VideoInputEasyIPCamera2#Gamma";
		String value = "10.0";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testSliderControl() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "VideoInputEasyIPCamera2#Detail(Sharpness)";
		String value = "10.0";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testSwitchControl() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "VideoInputEasyIPCamera2#AutoIris";
		String value = "0";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}
	@Test
	void testIrisColor() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "VideoInputEasyIPCamera2#Iris";
		String value = "f/6.2";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testPreset() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "VideoInputEasyIPCamera2#Preset";
		String value = "Preset 4";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testCrosspointRoute() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "CrosspointOutput1#LineMic1Route";
		String value = "0";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testDisableCrosspointRoute() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "CrosspointUSBRecordRight#AutoMicMixerRoute";
		String value = "0";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testCameraStandby() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "VideoInputEasyIPCamera2#Standby";
		String value = "1";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}

	@Test
	void testStandby() throws Exception {
		easyIPMixerCommunicator.setConfigManagement("true");
		easyIPMixerCommunicator.setEnableCrosspointGain("true");
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();

		String property = "SystemStandby";
		String value = "1";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		easyIPMixerCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		Assertions.assertEquals(value, statistics.get(property));
	}
}

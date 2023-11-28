/*
 *  Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

/**
 * VaddioBridgeNanoCommunicatorTest class
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
		extendedStatistic = (ExtendedStatistics) easyIPMixerCommunicator.getMultipleStatistics().get(0);
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(42, statistics.size());
		Assert.assertEquals(18, advancedControllablePropertyList.size());
	}
}

/*
 *  Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.mixer.vaddio.easyip;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * VaddioBridgeNanoCommunicatorTest class
 */
public class EasyIPMixerCommunicatorTest {
	private EasyIPMixerCommunicator easyIPMixerCommunicator;

	@BeforeEach()
	public void setUp() throws Exception {
		easyIPMixerCommunicator = new EasyIPMixerCommunicator();

		easyIPMixerCommunicator.setHost("");
		easyIPMixerCommunicator.setPort(22);
		easyIPMixerCommunicator.setLogin("");
		easyIPMixerCommunicator.setPassword("");
		easyIPMixerCommunicator.init();
		easyIPMixerCommunicator.connect();
	}

	@AfterEach()
	public void destroy() throws Exception {
		easyIPMixerCommunicator.disconnect();
	}
}

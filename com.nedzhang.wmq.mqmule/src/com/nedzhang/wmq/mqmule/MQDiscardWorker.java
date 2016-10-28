package com.nedzhang.wmq.mqmule;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.nedzhang.wmq.mqmule.base.MQGetBase;

public class MQDiscardWorker extends MQGetBase {

	private static final Logger logger = Logger.getLogger(MQDiscardWorker.class
			.getName());

	private int msgCount = 0;

	private final int numOfMessageToDiscard;

	public MQDiscardWorker(final int numOfMessageToDiscard,
			final int waitDuration) {
		super(waitDuration);
		this.numOfMessageToDiscard = numOfMessageToDiscard;

	}

	@Override
	protected boolean processMessage(final String messageContent) {

		if (messageContent != null) {
			msgCount++;

			// log if needed
			logMessageToDiscard(messageContent);

			return numOfMessageToDiscard == 0
					|| msgCount < numOfMessageToDiscard;

		} else {

			// log if needed
			logNullMessage();

			// We read all message in the queue. Stop now
			return false;
		}

	}

	private void logMessageToDiscard(final String messageContent) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("discard message #" + msgCount);
		}

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("message content: " + messageContent);
		}
	}

	private void logNullMessage() {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("message #" + (msgCount + 1) + " is null.");
		}
	}
}

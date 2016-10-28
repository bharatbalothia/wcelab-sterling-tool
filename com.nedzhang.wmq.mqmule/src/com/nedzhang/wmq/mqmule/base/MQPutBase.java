package com.nedzhang.wmq.mqmule.base;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

/***
 * Abstract class that puts message into MQ. Subclass implements the logic of
 * creating the message.
 * 
 * @author NZhang
 * 
 */
public abstract class MQPutBase extends MQClientBase {

	private static final Logger logger = Logger.getLogger(MQPutBase.class
			.getName());

	@Override
	protected Object performMQTask(final QueueConnection connection,
			final QueueSession session, final Queue queue) throws JMSException,
			IOException {

		logger.entering(MQPutBase.class.getName(), "performMQTask");

		final long mqTaskStartTime = System.currentTimeMillis();

		// 6: create the MessageProducer object
		final MessageProducer messageProducer = session.createProducer(queue);

		if (logger.isLoggable(Level.INFO)) {
			final long messageProducerCreatedTime = System.currentTimeMillis();
			
			logger.info("7. MessageProducer object for " + queue.getQueueName() + " created in " + (messageProducerCreatedTime - mqTaskStartTime) + " msec.");
		}

		int messageCount = 0;

		String messageContent = null;

		do {

			final long itemStartTime = System.currentTimeMillis();

			messageContent = produceMessage();

			if (messageContent != null) {
				final TextMessage txtMsg = session
						.createTextMessage(messageContent);
				// 7: send the message now using messageProducer object
				messageProducer.send(txtMsg);

				final long itemEndTime = System.currentTimeMillis();

				if (logger.isLoggable(Level.INFO)) {
					logger.info("8 == " + ++messageCount
							+ " == Sent the " + messageCount + "th message successfully in "
							+ (itemEndTime - itemStartTime) + " msec");
				}
			}
			
			onMessageSent(messageContent);
			
		} while (messageContent != null);

		final long mqTaskEndTime = System.currentTimeMillis();

		if (logger.isLoggable(Level.INFO)) {
			logger.info(new StringBuilder("9 ******  send ")
					.append(messageCount).append(" messages in ")
					.append(mqTaskEndTime - mqTaskStartTime).append(" msec")
					.toString());
		}

		logger.exiting(MQPutBase.class.getName(), "performMQTask");

		return null;

	}

	/***
	 * Subclass can implement the event handler after a message is sent.
	 * 
	 * @param messageContent
	 */
	protected void onMessageSent(String messageContent) {
		// Default implementation does nothing.
	}

	/****
	 * Subclass must produce message for PutBase to put into MQ. If this returns
	 * null, the PutBase stops. Otherwise (including empty string), PutBase puts
	 * the message into the queue and ask for next message.
	 * 
	 * @return
	 * @throws IOException
	 */
	abstract protected String produceMessage() throws IOException;

}

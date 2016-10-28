package com.nedzhang.wmq.mqmule.base;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

/***
 * Abstract class that gets message from MQ. Subclass implements the logic of
 * processing the message if the message is a javax.jms.TextMessage.
 * 
 * @author NZhang
 * 
 */
public abstract class MQGetBase extends MQClientBase {
	
	private static final Logger logger = Logger.getLogger(MQGetBase.class.getName());
	
	private final int waitDuration;

	public MQGetBase(int waitDuration) {
		this.waitDuration = waitDuration;
	}



	@Override
	protected Object performMQTask(QueueConnection connection,
			QueueSession session, Queue queue) throws JMSException, IOException {

		logger.entering(MQGetBase.class.getName(), "performMQTask");

		final long mqTaskStartTime = System.currentTimeMillis();

		// 6: create the QueueReciever object
		final QueueReceiver qrcv = session.createReceiver(queue);
		
		if (logger.isLoggable(Level.INFO)) {
			final long mqReceiverCreatedTime = System.currentTimeMillis();
			
			logger.info("7. Qrecv object for " + queue.getQueueName() + " created in " + (mqReceiverCreatedTime-mqTaskStartTime) + " msec");
		}

		int messageCount = 0;
		boolean keepGoing = true;

		do {
			// 7: Receive the message using messageConsumer object

			final long beforeReceiveMessage = System.currentTimeMillis();

			final Message message = qrcv.receive(waitDuration);

			final long afterReceiveMessage = System.currentTimeMillis();

			if (logger.isLoggable(Level.INFO)) {
				
				messageCount += (message == null) ? 0 : 1;

				logger.info("8 == "
						+ messageCount
						+ " == Received "
						+ (message == null ? "null" : messageCount
								+ "th message object successfully") + " in "
						+ (afterReceiveMessage - beforeReceiveMessage)
						+ " msec");
			}

			if (message == null) {
				keepGoing = processMessage(null);
			}else if (message instanceof TextMessage) {
				final TextMessage txtMsg = (TextMessage) message;
				keepGoing = processMessage(txtMsg.getText());
			}

		} while (keepGoing);

		final long mqTaskEndTime = System.currentTimeMillis();

		if (logger.isLoggable(Level.INFO)) {
			logger.info(new StringBuilder("9 ******  Received ")
					.append(messageCount).append(" messages in ")
					.append(mqTaskEndTime - mqTaskStartTime).append(" msec")
					.toString());
		}

		logger.exiting(MQGetBase.class.getName(), "performMQTask");

		return null;

	}

	/***
	 * Subclass must process the message content and return ture if need more
	 * message or return false if stop getting any futher message
	 * 
	 * @param messageContent
	 * @return
	 * @throws IOException
	 */
	abstract protected boolean processMessage(String messageContent)
			throws IOException;

}

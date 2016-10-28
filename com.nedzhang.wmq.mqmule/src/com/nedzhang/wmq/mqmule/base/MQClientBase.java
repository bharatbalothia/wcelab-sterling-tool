package com.nedzhang.wmq.mqmule.base;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/***
 * Base MQ class that opens a mq connection, executes subclass' mq activity,
 * closes the mq connection.
 * 
 * 
 * @author NZhang
 * 
 */
public abstract class MQClientBase {

	private static final Logger logger = Logger.getLogger(MQClientBase.class
			.getName());

	private QueueConnection connection;

	private QueueSession session;

	private Queue queue;

	/***
	 * Overall execute step. It opens connection to mq, performs the mq tasks
	 * that subclasses implement and then close the mq connection.
	 * 
	 * @param providerUrl
	 * @param qcfName
	 * @param mqUserID
	 * @param mqUserPassword
	 * @param queueName
	 * @return
	 * @throws Exception
	 */
	public Object execute(final String providerUrl, final String qcfName,
			final String mqUserID, final String mqUserPassword,
			final String queueName) throws Exception {
		try {
			// Open the connection to the mq queue
			openConnection(providerUrl, qcfName, mqUserID, mqUserPassword,
					queueName);

			// Perform the tasks - subclass implements the logic
			Object result = performMQTask(connection, session, queue);

			// Return the result
			return result;

		} finally {
			closeConnection();
		}
	}

	/***
	 * open JMS connection
	 * 
	 * @param providerUrl
	 * @param qcfName
	 * @param mqUserID
	 * @param mqUserPassword
	 * @param queueName
	 * @throws Exception
	 */
	private void openConnection(final String providerUrl, final String qcfName,
			final String mqUserID, final String mqUserPassword,
			final String queueName) throws Exception {

		final Hashtable<String, String> env = new Hashtable<String, String>(); // added
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.fscontext.RefFSContextFactory");
		// env.put(Context.PROVIDER_URL,"corbaloc::saapste02.academy.com:2809");
		env.put(Context.PROVIDER_URL, providerUrl);
		// If JMS Security is enable then we need provide below two lines
		// env.put(Context.SECURITY_PRINCIPAL,"sterling");
		// env.put(Context.SECURITY_CREDENTIALS,"Ord3rup!");

		// 1: Create the initialContext
		final Context intialContext = new InitialContext(env);

		logger.entering(MQClientBase.class.getName(), "openConnection");

		logger.info("1. Create the initialContext");

		// 2: get the ConnectionFactory object from lookUp
		final QueueConnectionFactory qcf = (QueueConnectionFactory) intialContext
				.lookup(qcfName);// look up for JNDI of
									// QueueConnectionFactory

		logger.info("2. Create the ConnectionFactory object from lookUp");

		// 3: get the Queue object from lookUp STR.AGNT.SCHEDULEORDER
		queue = (Queue) intialContext.lookup(queueName);
		logger.info("3. Create the Queue object");

		final long startTime = System.currentTimeMillis();

		if (logger.isLoggable(Level.INFO)) {
			logger.info(new StringBuilder("4a. open connection with providerUrl: ").append(providerUrl)
					.append(" qcfName: ").append(qcfName)
					.append(" userID: ")
					.append(mqUserID)
					.append(" password: ")
					.append(mqUserPassword == null || mqUserPassword.length() == 0 ? 
							"[empty]" : "*******")
					.toString());
		}

		// 4: Establish the connection with username and password
		connection = qcf.createQueueConnection(mqUserID, mqUserPassword);

		final long afterCreateQueueConneciton = System.currentTimeMillis();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("4b. Establish the connection in "
					+ (afterCreateQueueConneciton - startTime) + " msec");
		}
		// 5: create the Session
		session = connection
				.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

		final long afterCreateQueueSession = System.currentTimeMillis();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("5. create the Session in "
					+ (afterCreateQueueSession - afterCreateQueueConneciton)
					+ " msec");
		}

		connection.start();

		final long afterConnectionStart = System.currentTimeMillis();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("6. Connection started in "
					+ (afterConnectionStart - afterCreateQueueSession)
					+ " msec");
		}

		logger.exiting(MQClientBase.class.getName(), "openConnection");
	}

	private void closeConnection() throws JMSException {

		if (connection != null && connection instanceof Connection) {
			connection.close();
		}
	}

	/***
	 * Subclass implement to activity to perform with the mq connection
	 * 
	 * @param connection
	 * @param session
	 * @param queue
	 * @return
	 * @throws JMSException
	 * @throws IOException
	 */
	protected abstract Object performMQTask(final QueueConnection connection,
			final QueueSession session, final Queue queue) throws JMSException,
			IOException;

}

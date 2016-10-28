package com.nedzhang.wmq.mqmule;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nedzhang.wmq.mqmule.base.MQGetBase;
import com.nedzhang.wmq.mqmule.base.MQPutBase;

public class MainApp {

	private static final String FILE_ENCODING = "UTF-8";

	private static final String MQMSGFILE_PREFIX = "MQMSG";

	private static final int MQ_GET_WAIT_DURATION = 10000;

	private static final Logger logger = Logger.getLogger(MainApp.class
			.getName());

	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(final String[] args) throws Throwable {

		if (args == null || args.length < 2) {
			System.out
					.println("MQMule main Usage: -get|-put|-clear -providerUrl PROVIDER_URL -qcfName QCF_NAME -queueName QUEUE_NAME [-userId USER_ID] [-password PASSWORD] [-numOfMessge NUM_OF_MESSAGE] [-repositoryPath REPOSITORY_PATH] [-waitDuration WAIT_DURATION_MILLISEC]");
			System.out.println("  specify operation of get, put, or clear ");
			System.out
					.println("  if numOfMessage is not provided or 0. Process until no message/file left");
			System.out
					.println("  clear operaton does not require repositoryPath");
			System.out
					.println("  get or clear operation can specify time to wait for a message on the queue. Default is 10,000 milliseconds");

		} else {

			String providerUrl = null;
			String qcfName = null;
			String mqUserID = null;
			String mqUserPassword = null;
			String queueName = null;
			String mode = null;
			String repositoryPath = null;
			int numOfMessage = 0;
			int waitDuration = MQ_GET_WAIT_DURATION;

			final Map<String, String> parameter = parseParameter(args);

			for (final String parameterName : parameter.keySet()) {

				final String parameterValue = parameter.get(parameterName);
				final String parametername = parameterName.toLowerCase();

				if ("providerurl".equals(parametername)) {
					providerUrl = parameterValue;
				} else if ("qcfname".equals(parametername)) {
					qcfName = parameterValue;
				} else if ("userid".equals(parametername)) {
					mqUserID = parameterValue;
				} else if ("password".equals(parametername)) {
					mqUserPassword = parameterValue;
				} else if ("queuename".equals(parametername)) {
					queueName = parameterValue;
				} else if ("repositorypath".equals(parametername)) {
					repositoryPath = parameterValue;
				} else if ("put".equals(parametername)) {
					mode = "put";
				} else if ("get".equals(parametername)) {
					mode = "get";
				} else if ("clear".equals(parametername)) {
					mode = "clear";
				} else if ("numofmessage".equals(parametername)) {
					numOfMessage = Integer.parseInt(parameterValue);
				} else if ("waitduration".equals(parametername)) {
					waitDuration = Integer.parseInt(parameterValue);
				}
			}

			if (isNullOrEmpty(mode)) {
				throw new IllegalArgumentException(
						"must provide either -put or -get or -clear");
			}

			if (isNullOrEmpty(providerUrl)) {
				throw new IllegalArgumentException(
						"must provide value for -providerUrl");
			}

			if (isNullOrEmpty(qcfName)) {
				throw new IllegalArgumentException(
						"must provide value for -qcfName");
			}

			if (isNullOrEmpty(queueName)) {
				throw new IllegalArgumentException(
						"must provide value for -queueName");
			}

			if (!"clear".equals(mode) && isNullOrEmpty(repositoryPath)) {
				throw new IllegalArgumentException(
						"must provide value for -repositoryPath for put or get");
			}

			try {
				if ("clear".equals(mode)) {

					clearQueue(providerUrl, qcfName, mqUserID, mqUserPassword,
							queueName, numOfMessage, waitDuration);

				} else if ("put".equals(mode)) {

					putQueue(providerUrl, qcfName, mqUserID, mqUserPassword,
							queueName, repositoryPath, numOfMessage);

				} else if ("get".equals(mode)) {

					getQueue(providerUrl, qcfName, mqUserID, mqUserPassword,
							queueName, repositoryPath, numOfMessage,
							waitDuration);

				}
			} catch (final Throwable e) {
				logger.log(Level.SEVERE, "mainapp1", e);
				throw e;
			}
		}
	}

	private static void getQueue(final String providerUrl,
			final String qcfName, final String mqUserID,
			final String mqUserPassword, final String queueName,
			final String repositoryPath, final int numOfMessageToGet,
			final int waitDurationMills) throws Exception {

		final MQGetBase mqGetter = new MQSaveWorker(repositoryPath,
				MQMSGFILE_PREFIX, FILE_ENCODING, numOfMessageToGet,
				waitDurationMills);

		mqGetter.execute(providerUrl, qcfName, mqUserID, mqUserPassword,
				queueName);

	}

	private static void clearQueue(final String providerUrl,
			final String qcfName, final String mqUserID,
			final String mqUserPassword, final String queueName,
			final int numOfMessageToDiscard, final int waitDurationMills)
			throws Exception {

		final MQGetBase mqClearer = new MQDiscardWorker(numOfMessageToDiscard,
				waitDurationMills);

		mqClearer.execute(providerUrl, qcfName, mqUserID, mqUserPassword,
				queueName);
	}

	private static void putQueue(final String providerUrl,
			final String qcfName, final String mqUserID,
			final String mqUserPassword, final String queueName,
			final String repositoryPath, final int numOfMessageToPut)
			throws Exception {

		final MQPutBase mqFilePutter = new MQLoadWorker(repositoryPath,
				MQMSGFILE_PREFIX, FILE_ENCODING, numOfMessageToPut);

		mqFilePutter.execute(providerUrl, qcfName, mqUserID, mqUserPassword,
				queueName);
	}

	private static boolean isNullOrEmpty(final String providerUrl) {
		return providerUrl == null || providerUrl.length() == 0;
	}

	private static Map<String, String> parseParameter(final String[] args) {

		final Hashtable<String, String> parameterMap = new Hashtable<String, String>();

		String parameterKey = "UNKNOWN";

		for (final String arg : args) {
			if (arg != null && arg.length() > 0) {
				if (arg.charAt(0) == '-') {
					if (arg.length() > 1) {
						parameterKey = arg.substring(1);
						parameterMap.put(parameterKey, "");
					}
				} else {

					final String currentValue = parameterMap.get(parameterKey);

					if (currentValue != null && currentValue.length() > 0) {
						parameterMap
								.put(parameterKey, currentValue + " " + arg);
					} else {
						parameterMap.put(parameterKey, arg);
					}

				}
			}
		}

		return parameterMap;
	}

}

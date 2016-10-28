package com.nedzhang.wmq.mqmule;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nedzhang.util.TextFileUtil;
import com.nedzhang.wmq.mqmule.base.MQGetBase;

public class MQSaveWorker extends MQGetBase {

	private static final Logger logger = Logger.getLogger(MQSaveWorker.class
			.getName());

	private final int lastMsgFileNameNumber;

	private final int numOfMessageToGet;

	private final String mqMsgFilePrefix;

	private final String fileEncoding;

	private final File folderToSaveTo;

	private int msgCount = 0;

	public MQSaveWorker(final String repositoryPath,
			final String mqMsgFilePrefix, final String fileEncoding,
			final int numOfMessageToGet, final int waitDuration) {

		super(waitDuration);

		this.numOfMessageToGet = numOfMessageToGet;

		this.mqMsgFilePrefix = mqMsgFilePrefix;

		this.fileEncoding = fileEncoding;

		folderToSaveTo = new File(repositoryPath);
		if (!folderToSaveTo.exists()) {
			folderToSaveTo.mkdirs();
		}

		// Find any existing mq message file in the folder
		final String[] existingFiles = folderToSaveTo
				.list(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return (name != null && name
								.startsWith(mqMsgFilePrefix));
					}
				});

		if (existingFiles != null && existingFiles.length > 0) {
			Arrays.sort(existingFiles);

			int lastMsgFileNameNumberBuff = 0;
			try {
				lastMsgFileNameNumberBuff = Integer
						.parseInt(existingFiles[existingFiles.length - 1]
								.substring(mqMsgFilePrefix.length()));
			} catch (NumberFormatException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Failed to extract number from file: "
							+ existingFiles[existingFiles.length - 1]
							+ " set the sequence to 0.");
				}
				
				lastMsgFileNameNumberBuff = 0;
			}
			
			lastMsgFileNameNumber = lastMsgFileNameNumberBuff;

		} else {
			lastMsgFileNameNumber = 0;
		}
	}

	@Override
	protected boolean processMessage(final String messageContent)
			throws IOException {

		if (messageContent != null) {

			msgCount++;

			final String fileName = String.format("%s%09d", mqMsgFilePrefix,
					lastMsgFileNameNumber + msgCount);

			final String messageFilePath = new StringBuilder(
					folderToSaveTo.getAbsolutePath())
					.append(File.separatorChar).append(fileName).toString();

			if (logger.isLoggable(Level.INFO)) {
				logger.info("save message #" + msgCount + ": " + fileName);
			}

			TextFileUtil.writeTextFile(new File(messageFilePath), fileEncoding,
					false, messageContent);

			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("message content: " + messageContent);
			}

			return (numOfMessageToGet == 0 || msgCount < numOfMessageToGet);

		} else {

			if (logger.isLoggable(Level.INFO)) {
				logger.info("message #" + (msgCount + 1) + " is NULL");
			}

			return false;
		}
	}
}

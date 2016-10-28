package com.nedzhang.wmq.mqmule;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nedzhang.util.TextFileUtil;
import com.nedzhang.wmq.mqmule.base.MQPutBase;


/***
 * Loads files from a folder to MQ
 * 
 * @author NZhang
 *
 */
public class MQLoadWorker extends MQPutBase {

	
	private static final Logger logger = Logger.getLogger(MQLoadWorker.class.getName());
	
	private final File[] messageFiles;
	private final int numberOfMessageToPut;
	private final String fileEncoding;
	private final String repositoryPath;
	
	private int messageCounter = 0;
	
	private File fileLoading;

	public MQLoadWorker(
			final String repositoryPath,
			final String mqMsgFilePrefix, 
			final String fileEncoding,
			final int numberOfMessageToPut){
		
		this.repositoryPath = repositoryPath;
		this.fileEncoding = fileEncoding;
//		this.mqMsgFilePrefix = mqMsgFilePrefix;		
		this.numberOfMessageToPut = numberOfMessageToPut;
		
		File folder = new File(repositoryPath);
		
		if (mqMsgFilePrefix == null || mqMsgFilePrefix.length() == 0) {
			messageFiles = folder.listFiles();
		} else {
			
			final String mqMsgFilePrefixLowerCase = mqMsgFilePrefix.toLowerCase();
			
			messageFiles = folder.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name != null && name.toLowerCase().startsWith(mqMsgFilePrefixLowerCase);
				}
			});
		}
		
		if (messageFiles != null && messageFiles.length > 1) {
			
			Arrays.sort(messageFiles, new Comparator<File>() {

				@Override
				public int compare(File o1, File o2) {
					if (o1 == null && o2 == null) {
						return 0;
					} else if (o1 == null) {
						return -1;
					} else if (o2 == null) {
						return 1;
					} else {
						return o1.getName().compareTo(o2.getName());
					}
				}
			});
		}
		
	}

	@Override
	protected String produceMessage() throws IOException {

		fileLoading = null;
		
		if (numberOfMessageToPut == 0
			|| numberOfMessageToPut > messageCounter) {
			
			if (messageFiles != null
				&& messageFiles.length > messageCounter
				&& messageFiles[messageCounter] != null) {
				
				fileLoading = messageFiles[messageCounter++];
				
				if (logger.isLoggable(Level.INFO)) {
					logger.info("load file# " + messageCounter + ": " + fileLoading.getName());
				}
				
				String messageContent = TextFileUtil.readTextFile(
						fileLoading, fileEncoding);
				
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("message content: " + messageContent);
				}
				
				return messageContent;
			} else {
				
				if (logger.isLoggable(Level.INFO)) {
					logger.info("finish loading all " + messageCounter + " files in " + repositoryPath);
				}
				
				return null;
			}
		} else {
			
			if (logger.isLoggable(Level.INFO)) {
				logger.info("finish loading first " + messageCounter + " files in " + repositoryPath);
			}
			
			return null;
		}
	}
	
	@Override
	protected void onMessageSent(String messageContent) {
		
		if (fileLoading != null) {
//			fileLoading.deleteOnExit();
			fileLoading.delete();
		}
	}

}

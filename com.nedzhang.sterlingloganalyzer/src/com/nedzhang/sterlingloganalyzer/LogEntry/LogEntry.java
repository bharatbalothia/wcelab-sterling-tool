package com.nedzhang.sterlingloganalyzer.LogEntry;

import java.util.Collection;

import org.joda.time.DateTime;

public class LogEntry // extends Observable
{

	private DateTime time;
	private String level;
	private String thread;
	private String service;
	private String actClass;
	// private String loggerName;

	private String message;

	private String logText;

	public DateTime getTime() {
		return time;
	}

	public void setTime(final DateTime time) {
		this.time = time;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(final String level) {
		this.level = level;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(final String thread) {
		this.thread = thread;
	}

	public String getService() {
		return service;
	}

	public void setService(final String service) {
		this.service = service;

	}

	public String getActClass() {
		return actClass;
	}

	public void setActClass(final String actClass) {
		this.actClass = actClass;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	// public String getLoggerName() {
	// return loggerName;
	// }
	//
	// public void setLoggerName(String loggerName) {
	// this.loggerName = loggerName;
	// }

	public String getLogText() {
		return logText;
	}

	public void setLogText(final String logText) {
		this.logText = logText;
	}

	public void setLogText(final Collection<String> loglines) {
		if (loglines != null && loglines.size() > 0) {
			StringBuilder logTextBuilder = new StringBuilder();

			for (String line : loglines) {
				if (logTextBuilder.length() > 0) {
					logTextBuilder.append('\n');
				}
				logTextBuilder.append(line);
			}
			
			this.logText = logTextBuilder.toString();
			
		} else {
			this.logText = null;
		}

	}

	@Override
	public String toString() {
		final StringBuilder outputBuilder = new StringBuilder();

		outputBuilder.append("Time=").append(getTime());
		outputBuilder.append("|Level=").append(getLevel());
		outputBuilder.append("|Thread=").append(getThread());
		outputBuilder.append("|Service=").append(getService());
		outputBuilder.append("|ActClass=").append(getActClass());
		// outputBuilder.append("|Logger=").append(getLoggerName());
		outputBuilder.append("|Message=").append(getMessage());

		return outputBuilder.toString();

	}
}

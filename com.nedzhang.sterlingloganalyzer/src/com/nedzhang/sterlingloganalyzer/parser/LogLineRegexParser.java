package com.nedzhang.sterlingloganalyzer.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntry;
import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntryTimer;
import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntryTimerBegin;
import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntryTimerEnd;

/***
 * Parse Sterling log using Regular Express templates. 
 * It needs validation RegEx, parsing RegEx, and timeFormat String.
 * 
 * Validation RegEx is the regular expression string to validate if
 * a log line can be the start of a Sterling log entry.
 * 
 * parsing RegEx is the regular expression string to parse one or 
 * two lines into Sterling log entry. The RegEx must provide 
 * following groups:
 * time, level, thread, service, message, actClass.
 * 
 * The time format is the format of timestamp in the log. For example:
 * yyyy-MM-dd HH:mm:ss,SSS
 * 			
 * @author Ned Zhang
 *
 */
public class LogLineRegexParser implements LogLineParser {

	private final Pattern logLineValidationPattern;

	private final Pattern logLineParserPattern;

	private final DateTimeFormatter logTimeForamtter;
	
	private final Map<String, String> propertyMap;

	public LogLineRegexParser(String validationExpression,
			String parsingExpression, String timeFormat) {
		logLineValidationPattern = Pattern.compile(validationExpression);
		logLineParserPattern = Pattern.compile(parsingExpression);
		logTimeForamtter = DateTimeFormat.forPattern(timeFormat);
		
		propertyMap = new HashMap<String, String>();
	}

	@Override
	public boolean isLogLine(String line) {

		if (line != null && line.length() > 0) {
			return logLineValidationPattern.matcher(line).find();
		} else {
			return false;
		}

	}

	@Override
	public LogEntry parseLogLineToLogEntry(List<String> logEntry)
			throws Exception {

		if (logEntry == null || logEntry.size() == 0) {
			return null;
		} else {
			
			String stringForRegExParse;
			
			if (logEntry.size() > 1) {
				StringBuilder builder = new StringBuilder();
				for (String entry : logEntry) {
					if (builder.length() > 0) {
						builder.append('\n');
					}
					builder.append(entry);
				}
				stringForRegExParse = builder.toString();
			} else {
				stringForRegExParse = logEntry.get(0);
			}

			Matcher matchResult = logLineParserPattern
					.matcher(stringForRegExParse);

			if (matchResult.find()) {
				String time = matchResult.group("time");
				String level = matchResult.group("level");
				String thread = matchResult.group("thread");
				String service = matchResult.group("service");
				String message = matchResult.group("message");
				String actClass = matchResult.group("actClass");

				LogEntry entry;
				
				if (level != null && level.length()>0) {
					level = level.trim();
				}

				if ("TIMER".equalsIgnoreCase(level)) {
					entry = parseTimeLogEntry(message);
				} else {
					entry = new LogEntry();
				}

				entry.setActClass(actClass);
				entry.setLevel(level);
				entry.setService(service);
				entry.setThread(thread);

				if (time != null && time.length() > 0) {
					entry.setTime(logTimeForamtter.parseDateTime(time));
				}

				entry.setLogText(logEntry);

				entry.setMessage(message);
				
				return entry;

			} else {
				throw new IllegalArgumentException("Cannot match: " + logEntry + "\n" + logLineParserPattern);
				// return null;
			}
		}
	}

	private LogEntry parseTimeLogEntry(String message) throws Exception {
		LogEntry entry;
		LogEntryTimer timerEntry;

		int beginMarkIndex;
		int endMarkIndex;

		if ((beginMarkIndex = message.lastIndexOf(" - Begin")) > 0) {
			timerEntry = new LogEntryTimerBegin();

			timerEntry.setTimerTarget(getTarget(message, beginMarkIndex));

		} else if ((endMarkIndex = message.lastIndexOf(" - End -")) > 0) {
			timerEntry = new LogEntryTimerEnd();

			timerEntry.setTimerTarget(getTarget(message, endMarkIndex));

			((LogEntryTimerEnd) timerEntry).setReportedDuration(getDuration(
					message, endMarkIndex + " - End -".length() + 1));
		} else {
			throw new Exception("No \" - Begin\" or \" - End -\" found");
		}

		entry = timerEntry;
		return entry;
	}

	private static int getDuration(final String message, final int endMarkIndex) {
		int durationNum = 0;

		for (int i = endMarkIndex; i < message.length(); i++) {
			if (Character.isDigit(message.charAt(i))) {
				durationNum = durationNum * 10 + message.charAt(i) - '0';
			} else {
				if (durationNum > 0) {
					break;
				}
			}
		}

		return durationNum == 0 ? -1 : durationNum;

	}

	private String getTarget(final String message, final int targetEndPos) {
		String target = message.substring(0, targetEndPos);

		if (target != null && target.length() > 0) {
			target = target.trim();
			if (target.contains(" ")) {
				final String[] tokens = target.split(" ");
				return tokens[0];
			} else {
				return target;
			}
		} else {
			return null;
		}
	}

	@Override
	public String getProperty(String propertyName) {
		if (propertyMap.containsKey(propertyName)) {
			return propertyMap.get(propertyName);
		} else {
			return null;
		}
	}
	
	public void setProperty(String propertyName, String value) {
		propertyMap.put(propertyName, value);
	}
}

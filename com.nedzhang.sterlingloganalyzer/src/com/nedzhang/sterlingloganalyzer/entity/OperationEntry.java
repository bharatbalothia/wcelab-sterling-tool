package com.nedzhang.sterlingloganalyzer.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntry;
import com.nedzhang.sterlingloganalyzer.util.DateTimeFormatterUtil;

/***
 * 
 * Represent a operation from the log entry. 
 * This is the base class for other Operation classes.
 * 
 * @author Ned Zhang
 *
 */
public class OperationEntry {
		
	public enum OperationProperty {
		ActClass, Level, LogText, Message, Service, Thread, Time, StartTime, EndTime, Duration, PerformanceList, LogPath, ShortTime;
	}

	private final Map<OperationProperty, String> propertyMap = new HashMap<OperationProperty, String>();

	private final List<OperationEntry> children = new ArrayList<OperationEntry>();

	private final LogEntry logEntry;
	
//	private final OperationEntry parent;

	public OperationEntry(final LogEntry logEntry) {//, final OperationEntry parent) {

//		this.parent = parent;
		this.logEntry = logEntry;

		if (logEntry != null) {
			propertyMap.put(OperationProperty.ActClass, logEntry.getActClass());
			propertyMap.put(OperationProperty.Level, logEntry.getLevel());
			propertyMap.put(OperationProperty.LogText, logEntry.getLogText());
			propertyMap.put(OperationProperty.Message, logEntry.getMessage());
			propertyMap.put(OperationProperty.Service, logEntry.getService());
			propertyMap.put(OperationProperty.Thread, logEntry.getThread());
			propertyMap.put(OperationProperty.Time, DateTimeFormatterUtil.sterLogTimeFormatter.print(logEntry.getTime()));
			propertyMap.put(OperationProperty.ShortTime, DateTimeFormatterUtil.veryShortFormatter.print(logEntry.getTime()));
		}
	}

	public Map<OperationProperty, String> getPropertyMap() {
		return propertyMap;
	}

	
	public List<OperationEntry> getChildren() {
		return children;
	}

	public LogEntry getLog() {
		return logEntry;
	}
	
	
	@Override
	public String toString() {
		String presentationString = propertyMap.get(OperationProperty.ActClass);
		
		if (presentationString != null && presentationString.length() > 0) {
			return presentationString.trim() + " @ " + propertyMap.get(OperationProperty.ShortTime);
		} else {
			return propertyMap.get(OperationProperty.Level) + " @ " + propertyMap.get(OperationProperty.ShortTime);
		}
	}
}

package com.nedzhang.sterlingloganalyzer.parser;

import java.util.List;

import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntry;

public interface LogLineParser {

	public abstract boolean isLogLine(String line);

	public abstract LogEntry parseLogLineToLogEntry(List<String> line)
			throws Exception;
	
	public abstract String getProperty(String propertyName);

}
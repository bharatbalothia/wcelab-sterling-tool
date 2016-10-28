package com.nedzhang.sterlingloganalyzer.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntry;
import com.nedzhang.sterlingloganalyzer.entity.OperationThread;

public class LogFileParser {

	private LogLineParser lineParser;

	public LogFileParser(final String parserClassName) {
		try {
			lineParser = (LogLineParser) Class.forName(parserClassName)
					.newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			System.out.println("Failed to create LogLineParser from class: "
					+ parserClassName);
			e.printStackTrace();
		}
	}

	public LogFileParser(final LogLineParser lineParser) {
		this.lineParser = lineParser;
	}

	public List<OperationThread> readLog(final InputStream logStream,
			final boolean includeTimer, final boolean includeSqldebug,
			final boolean includeDebug, final boolean includeVerbose)
			throws IOException, Exception {

		final Map<String, OperationThread> threadMap = new HashMap<String, OperationThread>();

		final List<OperationThread> threadList = new ArrayList<OperationThread>();

		final BufferedReader in = new BufferedReader(new InputStreamReader(
				logStream));

		final List<String> lineBuffer = new ArrayList<String>();

		String line = null;
		while ((line = in.readLine()) != null) {

			// Check if the line is a log line
			if (lineParser.isLogLine(line)) {

				// If this line is a log line then the previous buffer needs to
				// be committed
				// and need to put current line into lineBuffer

				if (lineBuffer.size() > 0) {
					// If buffer has content, we need to flush it to parser
					// and then set the buffer to current line
					logLine(threadMap, threadList, lineBuffer, includeTimer,
							includeSqldebug, includeDebug, includeVerbose);
					lineBuffer.clear();
				}
				// Add the log line to the buffer
				lineBuffer.add(line);

			} else if (lineBuffer.size() > 0) {
				// This is a not a log line. Assume it is a continuation of
				// previous line
				lineBuffer.add(line);
			} else {
				System.out.println("Unparseable line. Discarded: " + line);
			}
		}

		// The last line need to be parsed
		if (lineBuffer != null && lineBuffer.size() > 0) {
			logLine(threadMap, threadList, lineBuffer, includeTimer,
					includeSqldebug, includeDebug, includeVerbose);
		}
		return threadList;
	}
	
	protected void onLineParsingException(final List<String> line, Throwable e) {
		
		System.out.println("Failed to parse the line: *********************");
		
		for (String inputLine : line) {
			System.out.println(inputLine);
		}
		
		System.out.println("Ignored line End: *********************");
		
		System.out.println(e.getMessage());
		
	}

	private void logLine(final Map<String, OperationThread> threadMap,
			final List<OperationThread> threadList, final List<String> line,
			final boolean includeTimer, final boolean includeSqldebug,
			final boolean includeDebug, final boolean includeVerbose)
			{

		try {
			final LogEntry entry = lineParser.parseLogLineToLogEntry(line);

			if (("VERBOSE".equalsIgnoreCase(entry.getLevel()) && !includeVerbose)
					|| ("DEBUG".equalsIgnoreCase(entry.getLevel()) && !includeDebug)
					|| ("SQLDEBUG".equalsIgnoreCase(entry.getLevel()) && !includeSqldebug)
					|| ("TIMER".equalsIgnoreCase(entry.getLevel()) && !includeTimer)) {

				// The log level is filtered out. So we are going to ignore the
				// log
				// System.out.println("Filter out: " + entry);

			} else {

				OperationThread loggingThread;

				if (threadMap.containsKey(entry.getThread())) {
					loggingThread = threadMap.get(entry.getThread());
				} else {
					loggingThread = new OperationThread(entry);
					loggingThread.setThreadName(entry.getThread());
					threadMap.put(entry.getThread(), loggingThread);
					threadList.add(loggingThread);
				}
				loggingThread.log(entry);
			}
		} catch (Throwable e) {
			onLineParsingException(line, e);
		}
	}
}

package com.nedzhang.sterlingloganalyzer.entity;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntry;
import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntryTimer;
import com.nedzhang.sterlingloganalyzer.util.DateTimeFormatterUtil;

public class OperationTimed extends OperationEntry {

	// private DateTime startTime;
	// private DateTime endTime;
	// private String level;
	// private String thread;

	private LogEntryTimer startLogEntry;
	private LogEntryTimer endLogEntry;

	private int durationInMilli = -1;
	// private String target;

	private OperationTimed parentOperationTimed = null;

	// private List<OperationTimed> childTimedOperations = new
	// ArrayList<OperationTimed>();

	// private List<LogEntry> operations = new ArrayList<LogEntry>();

	// private Observer startLogEntryObserver = new Observer() {
	//
	// @Override
	// public void update(Observable o, Object arg) {
	//
	// }
	// };
	//
	//
	// private Observer endLogEntryObserver = new Observer() {
	//
	// @Override
	// public void update(Observable o, Object arg) {
	//
	// }
	// };

	public OperationTimed(final LogEntry operation,
			final OperationTimed parentOperation) {
		super(operation);
		this.parentOperationTimed = parentOperation;
	}

	public DateTime getStartTime() {
		return startLogEntry == null ? null : startLogEntry.getTime();
	}

	// public void setStartTime(DateTime startTime) {
	// this.startTime = startTime;
	// }

	public DateTime getEndTime() {
		return endLogEntry == null ? null : endLogEntry.getTime();
	}

	// public void setEndTime(DateTime endTime) {
	// this.endTime = endTime;
	// }

	public String getThread() {
		return startLogEntry == null ? null : startLogEntry.getThread();
	}

	// public void setThread(String thread) {
	// this.thread = thread;
	// }

	public String getTarget() {
		return startLogEntry == null ? null : startLogEntry.getTimerTarget();
	}

	// public void setTarget(String target) {
	// this.target = target;
	// }

	public LogEntryTimer getStartLogEntry() {
		return startLogEntry;
	}

	public void setStartLogEntry(final LogEntryTimer startLogEntry) {
		this.startLogEntry = startLogEntry;
		getPropertyMap().put(
				OperationProperty.StartTime,
				DateTimeFormatterUtil.sterLogTimeFormatter.print(startLogEntry
						.getTime()));
		
		updateLogText();
	}


	public LogEntryTimer getEndLogEntry() {
		return endLogEntry;
	}

	public void setEndLogEntry(final LogEntryTimer endLogEntry) {
		this.endLogEntry = endLogEntry;
		getPropertyMap().put(
				OperationProperty.EndTime,
				DateTimeFormatterUtil.sterLogTimeFormatter.print(endLogEntry
						.getTime()));
		final Period durationPeriod = new Period(startLogEntry.getTime(),
				endLogEntry.getTime(), PeriodType.millis());

		durationInMilli = durationPeriod.getMillis();

		getPropertyMap().put(OperationProperty.Duration,
				durationInMilli + " millis");

		updateLogText();

	}

	public OperationTimed getParentOperationTimed() {
		return parentOperationTimed;
	}

	public String getChildProcessTree() {
		// TODO: lazy create this string
		final StringBuilder toStringBuilder = new StringBuilder();

		buildChildProcessTree(toStringBuilder, "");

		return toStringBuilder.toString();
	}

	public int getDurationInMilli() {
		return durationInMilli;
	}

	private void buildChildProcessTree(final StringBuilder toStringBuilder,
			final String linePrefix) {

		if (linePrefix != null && linePrefix.length() > 0) {
			toStringBuilder.append(linePrefix);
		}

		toStringBuilder.append(getTarget()).append('[').append(getStartTime())
				.append('-').append(getEndTime()).append("]\n");

		if (getChildren() != null && getChildren().size() > 0) {
			for (final OperationEntry op : getChildren()) {
				if (op instanceof OperationTimed) {
					toStringBuilder.append(linePrefix).append('.');
				} else {
					toStringBuilder.append(linePrefix).append('-');
				}
				toStringBuilder.append(op);
			}
		}
	}

	private void updateLogText() {
		
		String logText = "<<<<\n"
				+ getStartLogEntry().getLogText()
				+ "\n<<<<\n>>>>\n"
				+ (endLogEntry == null ? "*** Timer "
						+ getTarget()
						+ " starts @ "
						+ DateTimeFormatterUtil.sterLogTimeFormatter
								.print(getStartTime())
						+ " does not have a end ****" : endLogEntry
						.getLogText()) + "\n>>>>";

		getPropertyMap().put(OperationProperty.LogText,
				logText);
	}

	
	@Override
	public String toString() {

		DateTime startTime = getStartTime();

		final StringBuilder toStringBuilder = new StringBuilder(getTarget())
				.append(" [")
				.append(durationInMilli)
				.append("] ")
				.append(DateTimeFormatterUtil.veryShortFormatter
						.print(startTime))
				.append("~");

		if (getEndTime() == null) {
			toStringBuilder.append("???");
		} else {

			DateTime endTime = getEndTime();

			if (startTime.getHourOfDay() != endTime.getHourOfDay()) {
				toStringBuilder.append(DateTimeFormatterUtil.hhmmssSSSFormatter
						.print(endTime));
			} else if (startTime.getMinuteOfHour() != endTime.getMinuteOfHour()) {
				toStringBuilder.append(DateTimeFormatterUtil.mmssSSSFormatter
						.print(endTime));
			} else if (startTime.getSecondOfMinute() != endTime
					.getSecondOfMinute()) {
				toStringBuilder.append(DateTimeFormatterUtil.ssSSSFormatter
						.print(endTime));
			} else {
				toStringBuilder.append(endTime.getMillisOfSecond());
			}
		}

		return toStringBuilder.toString();
	}
}

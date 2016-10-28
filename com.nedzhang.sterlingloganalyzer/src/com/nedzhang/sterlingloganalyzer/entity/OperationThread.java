package com.nedzhang.sterlingloganalyzer.entity;

import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntry;
import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntryTimerBegin;
import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntryTimerEnd;
import com.nedzhang.sterlingloganalyzer.util.DateTimeFormatterUtil;

/***
 * Represents a thread in a log file. This is the highest level
 * of consolidation in the log viewer. All OperationEntry belong to 
 * one and only one OperationThread. 
 * 
 * @author Ned Zhang
 *
 */
public class OperationThread extends OperationEntry {

	public OperationThread(final LogEntry logEntry) {
		
		super(null);
		getPropertyMap().put(OperationProperty.Thread, logEntry.getThread());
		getPropertyMap().put(OperationProperty.Time,
				logEntry.getTime().toString());
	}

	private String threadName;

	/***
	 * The current TIMER operation we are putting operation under
	 */
	private OperationTimed currentTimedOperation = null;

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(final String threadName) {
		this.threadName = threadName;
	}

	public void log(final LogEntryTimerBegin timerEntry) {

		final OperationTimed newOperation = new OperationTimed(timerEntry,
				currentTimedOperation);

		newOperation.setStartLogEntry(timerEntry);

		if (currentTimedOperation == null) {
			getChildren().add(newOperation);
		} else {
			// newOperation.setParentOperation(currentOperation);
			currentTimedOperation.getChildren().add(newOperation);
		}
		currentTimedOperation = newOperation;
	}

	public void log(final LogEntryTimerEnd timerEndEntry) {

		if (currentTimedOperation == null) {
			// We have a end timer without begin timer.
			// We don't have a complete log.
			processTimerEndWithoutBegin(timerEndEntry);

		} else if (!currentTimedOperation.getStartLogEntry().getTimerTarget()
				.equals(timerEndEntry.getTimerTarget())) {
			// We have END does not match with BEGIN
			// Assume the BEGIN does not have an END
			// Close the BEGIN
			final String message = String.format(
					"%s %s: END [%s-%s] not match BEGIN [%s-%s]",
					timerEndEntry.getTime().toString(
							DateTimeFormatterUtil.veryShortFormatter),
					getThreadName(),
					timerEndEntry.getTime().toString(
							DateTimeFormatterUtil.shortFormatter),
					timerEndEntry.getTimerTarget(),
					currentTimedOperation.getStartLogEntry().getTime()
							.toString(DateTimeFormatterUtil.shortFormatter),
					currentTimedOperation.getStartLogEntry().getTimerTarget());

			System.out.println(message);

			final OperationTimed parentOp = currentTimedOperation
					.getParentOperationTimed();

			if (parentOp == null) {
				processTimerEndWithoutBegin(timerEndEntry);
			} else {
				parentOp.getChildren().addAll(
						currentTimedOperation.getChildren());

				currentTimedOperation.getChildren().clear();

				currentTimedOperation = currentTimedOperation
						.getParentOperationTimed();

				log(timerEndEntry);
			}
		} else {
			currentTimedOperation.setEndLogEntry(timerEndEntry);

			currentTimedOperation = currentTimedOperation
					.getParentOperationTimed();

		}
	}

	public void log(final LogEntry logEntry) {

		if (logEntry instanceof LogEntryTimerBegin) {
			log((LogEntryTimerBegin) logEntry);
		} else if (logEntry instanceof LogEntryTimerEnd) {
			log((LogEntryTimerEnd) logEntry);
		} else if (currentTimedOperation != null) {
			currentTimedOperation.getChildren().add(
					new OperationEntry(logEntry));
		} else {
			getChildren().add(new OperationEntry(logEntry));
		}
	}

	private void processTimerEndWithoutBegin(final LogEntryTimerEnd timerEntry) {

		final String message = String
				.format("%s %s: !!WARN!! No BEGIN for %s. Create artifical BEGIN at %s - %d",
						timerEntry.getTime().toString(
								DateTimeFormatterUtil.veryShortFormatter),
						getThreadName(),
						timerEntry.getTimerTarget(),
						timerEntry.getTime().toString(
								DateTimeFormatterUtil.veryShortFormatter),
						timerEntry.getReportedDuration());

		System.out.println(message);

		final LogEntryTimerBegin artificalBegin = new LogEntryTimerBegin();

		artificalBegin.setTime(timerEntry.getTime().minusMillis(
				timerEntry.getReportedDuration()));

		artificalBegin.setTimerTarget(timerEntry.getTimerTarget());
		artificalBegin.setLogText("*** Timer "
				+ timerEntry.getTimerTarget()
				+ " ends @ "
				+ DateTimeFormatterUtil.sterLogTimeFormatter.print(timerEntry
						.getTime()) + " does not have a start ****");

		final OperationTimed artificalOp = new OperationTimed(artificalBegin,
				null);

		artificalOp.setStartLogEntry(artificalBegin);

		artificalOp.setEndLogEntry(timerEntry);

		artificalOp.getChildren().addAll(this.getChildren());
		getChildren().clear();
		getChildren().add(artificalOp);

		currentTimedOperation = null;
	}

	@Override
	public String toString() {
		return getThreadName();
	}
}

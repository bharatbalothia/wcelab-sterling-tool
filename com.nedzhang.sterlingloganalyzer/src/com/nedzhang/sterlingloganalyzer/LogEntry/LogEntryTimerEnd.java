package com.nedzhang.sterlingloganalyzer.LogEntry;

public class LogEntryTimerEnd extends LogEntryTimer {

	private int reportedDuration = -1;

//	@Override
//	public TimerType getTimerType() {
//		return TimerType.END;
//	}
	
	public int getReportedDuration() {
		return reportedDuration;
	}

	public void setReportedDuration(final int reportedDuration) {
		this.reportedDuration = reportedDuration;
	}
}

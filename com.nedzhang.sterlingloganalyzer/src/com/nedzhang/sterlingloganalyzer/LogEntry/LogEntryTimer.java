package com.nedzhang.sterlingloganalyzer.LogEntry;

public abstract class LogEntryTimer extends LogEntry {

//	public enum TimerType {
//		BEGIN, END
//	}

	private String timerTarget;

//	public abstract TimerType getTimerType();

	public String getTimerTarget() {
		return timerTarget;
	}

	public void setTimerTarget(final String target) {
		this.timerTarget = target;
	}

	@Override
	public String toString() {
		final StringBuilder outputBuilder = new StringBuilder(super.toString());

//		outputBuilder.append("|TimerType=").append(getTimerType());
		// outputBuilder.append("|Duration=").append(getDuration());
		outputBuilder.append("|TimerTarget=").append(getTimerTarget());
		return outputBuilder.toString();

	}

}

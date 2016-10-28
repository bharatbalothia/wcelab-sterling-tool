package com.nedzhang.sterlingloganalyzer.util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeFormatterUtil {


	public final static DateTimeFormatter mmddhhmmssSSSFormatter = DateTimeFormat
			.forPattern("MM-dd HH:mm:ss.SSS");
	
	public final static DateTimeFormatter hhmmssSSSFormatter = DateTimeFormat
			.forPattern("HH:mm:ss.SSS");
	
	public final static DateTimeFormatter mmssSSSFormatter = DateTimeFormat
			.forPattern("mm:ss.SSS");
	
	public final static DateTimeFormatter ssSSSFormatter = DateTimeFormat
			.forPattern("ss.SSS");
	
	
	public final static DateTimeFormatter veryShortFormatter = hhmmssSSSFormatter;

	public final static DateTimeFormatter shortFormatter = mmddhhmmssSSSFormatter;
	

	public final static DateTimeFormatter longFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public final static DateTimeFormatter sterLogTimeFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd HH:mm:ss,SSS");
	
	private DateTimeFormatterUtil() {

	}

}

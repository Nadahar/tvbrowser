/*
 *******************************************************************
 *              TVBConsole plugin for TVBrowser                    *
 *                                                                 *
 * Copyright (C) 2010 Tomas Schackert.                             *
 * Contact koumori@web.de                                          *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 3 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called LICENSE in the top
 directory of the distribution; if not, write to 
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 Boston, MA  02111-1307  USA
 
 *******************************************************************/
package aconsole.gui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Tomas
 *
 */
public class RecordFormatter extends Formatter {
	Date dat = new Date();
	private String timeformat = "{0,date} {0,time}";
	private MessageFormat formatter;

	private Object args[] = new Object[1];

	// Line separator string.  This is the value of the line.separator
	// property at the moment that the SimpleFormatter was created.
	private String lineSeparator = System.getProperty("line.separator");
	boolean showclass;
	boolean showmethod;
	public void setStyle(boolean showDate,boolean showTime,boolean showclass,boolean showmethod){
		if (showDate){
			if (showTime){
				timeformat= "{0,date} {0,time}";
			}else{
				timeformat= "{0,date}";
			}
		}else if (showTime){
			timeformat= "{0,time}";
		}else{
			timeformat= "";
		}
		if (formatter != null) {
			formatter.applyPattern(timeformat);
		}
		this.showclass=showclass;
		this.showmethod=showmethod;
	}
	
	/**
	 * Format the given LogRecord.
	 * @param record the log record to be formatted.
	 * @return a formatted log record
	 */
	public synchronized String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();
		// Minimize memory allocations here.
		dat.setTime(record.getMillis());
		args[0] = dat;
		StringBuffer text = new StringBuffer();
		if (formatter == null) {
			formatter = new MessageFormat(timeformat);
		}
		formatter.format(args, text, null);
		sb.append(text);
		sb.append(" ");
		if (showclass){
			if (record.getSourceClassName() != null) {	
				sb.append(record.getSourceClassName());
			} else {
				sb.append(record.getLoggerName());
			}
		}
		if (showmethod){
			if (record.getSourceMethodName() != null) {	
				sb.append(".");
				sb.append(record.getSourceMethodName());
				sb.append("(...)");
			}
		}
		if (showclass || showmethod){
			sb.append(":\n\t");
		}else{
			sb.append(" ");
		}
		String message = formatMessage(record);
		sb.append(record.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(message);
		sb.append(lineSeparator);
		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				if (showclass || showmethod)sb.append("\t");
			sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}
}
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
package aconsole.data;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import aconsole.AConsole;
import aconsole.gui.AbstractGuiTask;
import aconsole.gui.AbstractNotifyTask;
import aconsole.properties.IntProperty;

/**
 * @author Tomas
 *
 */
public class Console {
	static public class LoggerConsoleEvent{
		LogRecord record;
		LoggerConsoleEvent(LogRecord record){
			this.record=record;
		}
		public String toString(Formatter f){
			return f.format(this.record);
		}
		/**
		 * Get the source Logger name's
		 *
		 * @return source logger name (may be null)
		 */
		public String getLoggerName() {
			return record.getLoggerName();
		}
		/**
		 * Get the localization resource bundle
		 * <p>
		 * This is the ResourceBundle that should be used to localize
		 * the message string before formatting it.  The result may
		 * be null if the message is not localizable, or if no suitable
		 * ResourceBundle is available.
		 */
		public ResourceBundle getResourceBundle() {
			return record.getResourceBundle();
		}
		/**
		 * Get the localization resource bundle name
		 * <p>
		 * This is the name for the ResourceBundle that should be
		 * used to localize the message string before formatting it.
		 * The result may be null if the message is not localizable.
		 */
		public String getResourceBundleName() {
			return record.getResourceBundleName();
		}

		/**
		 * Get the logging message level, for example Level.SEVERE.
		 * @return the logging message level
		 */
		public Level getLevel() {
			return record.getLevel();
		}
		/**
		 * Get the sequence number.
		 * <p>
		 * Sequence numbers are normally assigned in the LogRecord
		 * constructor, which assigns unique sequence numbers to
		 * each new LogRecord in increasing order.
		 * @return the sequence number
		 */
		public long getSequenceNumber() {
			return record.getSequenceNumber();
		}
		/**
		 * Get the  name of the class that (allegedly) issued the logging request.
		 * <p>
		 * Note that this sourceClassName is not verified and may be spoofed.
		 * This information may either have been provided as part of the
		 * logging call, or it may have been inferred automatically by the
		 * logging framework.  In the latter case, the information may only
		 * be approximate and may in fact describe an earlier call on the
		 * stack frame.
		 * <p>
		 * May be null if no information could be obtained.
		 *
		 * @return the source class name
		 */
		public String getSourceClassName() {
			return record.getSourceClassName();
		}
		/**
		 * Get the  name of the method that (allegedly) issued the logging request.
		 * <p>
		 * Note that this sourceMethodName is not verified and may be spoofed.
		 * This information may either have been provided as part of the
		 * logging call, or it may have been inferred automatically by the
		 * logging framework.  In the latter case, the information may only
		 * be approximate and may in fact describe an earlier call on the
		 * stack frame.
		 * <p>
		 * May be null if no information could be obtained.
		 *
		 * @return the source method name
		 */
		public String getSourceMethodName() {
			return record.getSourceMethodName();
		}
		/**
		 * Get the "raw" log message, before localization or formatting.
		 * <p>
		 * May be null, which is equivalent to the empty string "".
		 * <p>
		 * This message may be either the final text or a localization key.
		 * <p>
		 * During formatting, if the source logger has a localization
		 * ResourceBundle and if that ResourceBundle has an entry for
		 * this message string, then the message string is replaced
		 * with the localized value.
		 *
		 * @return the raw message string
		 */
		public String getMessage() {
			return record.getMessage();
		}
		/**
		 * Get the parameters to the log message.
		 *
		 * @return the log message parameters.  May be null if
		 *			there are no parameters.
		 */
		public Object[] getParameters() {
			return record.getParameters() ;
		}

		/**
		 * Get an identifier for the thread where the message originated.
		 * <p>
		 * This is a thread identifier within the Java VM and may or
		 * may not map to any operating system ID.
		 *
		 * @return thread ID
		 */
		public int getThreadID() {
			return record.getThreadID();
		}

		/**
		 * Get event time in milliseconds since 1970.
		 *
		 * @return event time in millis since 1970
		 */
		public long getMillis() {
			return record.getMillis();
		}
		/**
		 * Get any throwable associated with the log record.
		 * <p>
		 * If the event involved an exception, this will be the
		 * exception object. Otherwise null.
		 *
		 * @return a throwable
		 */
		public Throwable getThrown() {
			return record.getThrown();
		}
	}
	public interface Listener{
		public void addText(LoggerConsoleEvent ce);
		public void shutdownConsole();
	}
	public static final String SYSTEMOUT_LOGGER="out";
	public static final String SYSTEMERR_LOGGER="err";
	Vector<LoggerConsoleEvent> history=new Vector<LoggerConsoleEvent>();
	private int historysize=-1;
	ConsoleStream outConsoleStream=new ConsoleStream(SYSTEMOUT_LOGGER,Level.INFO,System.out);
	private PrintStream outPrintStream=new PrintStream(outConsoleStream);	//Stream to print Output
	ConsoleStream errConsoleStream=new ConsoleStream(SYSTEMERR_LOGGER,Level.SEVERE,System.err);
	private PrintStream errPrintStream=new PrintStream(errConsoleStream);	//Stream to print Error
	public PrintStream systemout=null;
	public PrintStream systemerr=null;
	boolean activ=false;
	Vector<Listener> listeners=new Vector<Listener>();
	static private Console instance=new Console();
	MyStreamHandler sh;

	synchronized public void addListener(final Listener l){
		new AbstractGuiTask() {
			@Override
			public void runGui() {
				Iterator<LoggerConsoleEvent> it=new Vector<LoggerConsoleEvent>(history).iterator();
				while (it.hasNext()){
					LoggerConsoleEvent ce=it.next();
					l.addText(ce);
				}
				listeners.add(l);
			}
		}.invokeLater();


	}
	synchronized public void removeListener(Listener l){
		this.listeners.remove(l);
	}
	synchronized private void fireAddText(final LoggerConsoleEvent ce){
		if (history!=null) {
			if (historysize<0){
				IntProperty p=AConsole.getConsoleBufferSize();
				if (p!=null)this.historysize=p.get();
			}else{
				while (history.size()>historysize){
					history.remove(0);
				}
			}
			history.add(ce);
		}
//		Iterator<Listener> it=new Vector<Listener>(listeners).iterator();
//		while (it.hasNext()){
//			Listener l=it.next();
//			l.addText(ce);
//		}
		new AbstractNotifyTask<Listener>(listeners) {
			@Override
			public void notify(Listener l) {
				l.addText(ce);
			}
		};

	}
	synchronized private void fireShutdown(){
		Iterator<Listener> it=new Vector<Listener>(listeners).iterator();
		while (it.hasNext()){
			Listener l=it.next();
			l.shutdownConsole();
		}
	}
	private Console(){
		activate();
	}
	synchronized public static Console getConsole(){
		if (instance==null)instance=new Console();
		return instance;
	}
	private class MyStreamHandler extends Handler{
		/**
		 *
		 */
		public MyStreamHandler() {
			super();
		}

		/**
		 * @param out
		 * @param formatter
		 */
		public void publish(LogRecord record) {
			fireAddText(new LoggerConsoleEvent(record));
		}

		/**
		 * @see java.util.logging.Handler#flush()
		 */
		public void flush() {
		}

		/**
		 * @see java.util.logging.Handler#close()
		 */
		public void close() throws SecurityException {
		}
	}
	synchronized public void activate(){
		if (activ) return;
		activ=true;
		systemout=System.out;
		systemerr=System.err;
		System.setErr(errPrintStream);
		System.setOut(outPrintStream);
		sh=new MyStreamHandler();
		sh.setLevel(java.util.logging.Level.ALL);
		java.util.logging.Logger.getLogger("").addHandler(sh);
	}
	synchronized public void deactivate(){
		if (!activ) return;
		activ=false;
		System.setErr(systemerr);
		System.setOut(systemout);
		java.util.logging.Logger.global.removeHandler(sh);
		java.util.logging.Logger.getLogger("").removeHandler(sh);

	}
	synchronized public void shutdownConsole(){
		if (instance!=null){
			deactivate();
			fireShutdown();
			instance=null;
			errConsoleStream.forward=null;
			outConsoleStream.forward=null;
			errConsoleStream.close();
			outConsoleStream.close();
			history.clear();
		}
	}
	//ConsoleStream is the base for the outputPrintStream. adding input to the outputTextArea
	class ConsoleStream extends OutputStream{
		PrintStream forward;
		String loggername;
		Level level;
		String buffer="";
		public ConsoleStream(String loggername,Level level,PrintStream forward){
			this.forward=forward;
			this.loggername=loggername;
			this.level=level;
		}
		synchronized public void write(byte[] b){
			write(b, 0, b.length);
		}
		synchronized public void write(byte[] b, int off, int len){
			String s=buffer+new String(b,off,len);
			int nl_index=s.lastIndexOf('\n');
			if (nl_index>0){
				if (s.length()>nl_index+1){
					buffer=s.substring(nl_index+1);
				}else{
					buffer="";
				}
				s=s.substring(0,nl_index);
			}else{
				buffer=s;
				s="";
			}
			if (s.length()>0){
				LogRecord lr=new LogRecord(level, s);
				lr.setLoggerName(loggername);
				fireAddText(new LoggerConsoleEvent(lr));
			}
			if (forward!=null){
				forward.write(b,off,len);
			}
		}
		synchronized public void write(int b){
			byte[] ba={(byte)b};
			write(ba, 0,1);
		}
		synchronized public void close(){
			if (forward!=null)forward.close();
		}
	}
}

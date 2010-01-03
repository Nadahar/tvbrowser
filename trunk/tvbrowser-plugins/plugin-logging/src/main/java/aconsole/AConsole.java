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
package aconsole;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import aconsole.config.PropertyPanel;
import aconsole.data.Console;
import aconsole.gui.ConsoleFrame;
import aconsole.properties.BooleanProperty;
import aconsole.properties.ColorProperty;
import aconsole.properties.IntProperty;
import aconsole.properties.StringProperty;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;
/**
 * Das TVBConsole-Plugin ist als Hilfsmittel für (Plugin-)Entwickler gedacht. Es zeigt die Ausgaben der 
 * Standard-Streams sowie die durch das java.util.logging-Package erstellten
 * Nachrichten. Die Ausgabe kann dynamisch nach Logger-Klasse und Nachrichten-Level gefiltert werden.
 * 
 * The TVBConsole plugin is meant als utility for (plugin) developers.
 * It shows the output of the system streams and the message sent to the java.util.logging package. 
 * The output can be filtered by the logger-class and the message-level.
 * 
 * @author Tomas
 *
 *
 * TODO: Beim Verbreitern des Einstellungsdialogs und anschließendem Verkleinern bleibt das Panel auf maximaler Breite. und die Fenserposition wird nicht gespeichert 
 */
public class AConsole extends Plugin {
	public static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(AConsole.class.getName());
	static private final Localizer mLocalizer= Localizer.getLocalizerFor(AConsole.class);
	private static Version PLUGINVERSION = new Version(0, 04,0,false);
	Properties settings=new Properties();
	static ColorProperty colorBg=null;
	static ColorProperty colorSelection=null;
	static ColorProperty colorSelectionText=null;
	static ColorProperty colorSystemOutText=null;
	static ColorProperty colorSystemErrText=null;
	static ColorProperty colorLevelSevereText=null;
	static ColorProperty colorLevelWarningText=null;
	static ColorProperty colorLevelInfoText=null;
	static ColorProperty colorLevelOtherText=null;
	static StringProperty stringLoggerFilter=null;
	private static BooleanProperty boolShowDate=null;
	private static BooleanProperty boolShowTime=null;
	private static BooleanProperty boolShowClass=null;
	private static BooleanProperty boolShowMethod=null;
	private static BooleanProperty boolShowOut=null;
	private static BooleanProperty boolShowErr=null;
	private static IntProperty intLoggerLevel=null;
	private static IntProperty intConsoleBufferSize=null;
	
	public static ColorProperty getBg(){return colorBg;}
	public static ColorProperty getSelection(){return colorSelection;}
	public static ColorProperty getSelectionText(){return colorSelectionText;}
	public static ColorProperty getSystemOutText(){return colorSystemOutText;}
	public static ColorProperty getSystemErrText(){return colorSystemErrText;}
	public static ColorProperty getLevelSevereText(){return colorLevelSevereText;}
	public static ColorProperty getLevelWarningText(){return colorLevelWarningText;}
	public static ColorProperty getLevelInfoText(){return colorLevelInfoText;}
	public static ColorProperty getLevelOtherText(){return colorLevelOtherText;}
	
	public static StringProperty getLoggerFilter(){return stringLoggerFilter;}
	public static BooleanProperty getShowDate(){return boolShowDate;}
	public static BooleanProperty getShowTime(){return boolShowTime;}
	public static BooleanProperty getShowClass(){return boolShowClass;}
	public static BooleanProperty getShowMethod(){return boolShowMethod;}
	public static BooleanProperty getShowOut(){return boolShowOut;}
	public static BooleanProperty getShowErr(){return boolShowErr;}
	public static IntProperty getLoggerLevel(){return intLoggerLevel;}
	public static IntProperty getConsoleBufferSize(){return intConsoleBufferSize;}
	
	private static AConsole instance;
	
	public AConsole(){
		instance=this;
	}
	/**
	 * @see devplugin.Plugin#getButtonAction()
	 */
	public ActionMenu getButtonAction() {
		Action action = new AbstractAction() {
			private static final long serialVersionUID = -1842050666077072569L;
			public void actionPerformed(ActionEvent evt) {
				try{
					new ConsoleFrame();
				}catch (Exception e){
					foundABug(e);
				}
			}
		};
		action.putValue(Action.NAME,mLocalizer.msg("buttonname","TVBConsole"));
		action.putValue(Action.SMALL_ICON, createImageIcon("aconsole/log16.gif"));
		action.putValue(BIG_ICON, createImageIcon("aconsole/log24.gif"));
		return new ActionMenu(action);  
	}
	public PluginInfo getInfo() {
		String name = "TVBConsole";
		String desc = mLocalizer.msg("description","show the java default output"); 
		String author = "Tomas Schackert";
		return new PluginInfo(AConsole.class,name, desc, author);
	}
	public static Version getVersion() {
	    return PLUGINVERSION;
	}
	/**
	 * @see devplugin.Plugin#getSettingsTab()
	 */
	public SettingsTab getSettingsTab() {
		return new PropertyPanel();
	}

	/**
	 * @see devplugin.Plugin#loadSettings(java.util.Properties)
	 */
	public void loadSettings(Properties settings0) {
		this.settings.putAll(settings0);
		colorBg=new ColorProperty(settings,"colorBg",Color.WHITE);
		colorSelection=new ColorProperty(settings,"colorSelection",Color.LIGHT_GRAY);
		colorSelectionText=new ColorProperty(settings,"colorSelectionText",Color.black);
		colorSystemOutText=new ColorProperty(settings,"colorSystemOutText",Color.blue);
		colorSystemErrText=new ColorProperty(settings,"colorSystemErrText",Color.RED);
		colorLevelSevereText=new ColorProperty(settings,"colorLevelSevereText",Color.RED);
		colorLevelWarningText=new ColorProperty(settings,"colorLevelWarningText",Color.pink);
		colorLevelInfoText=new ColorProperty(settings,"colorLevelInfoText",Color.BLACK);
		colorLevelOtherText=new ColorProperty(settings,"colorLevelOtherText",Color.GRAY);
		stringLoggerFilter=new StringProperty(settings,"loggerFilter","");
		boolShowDate=new BooleanProperty(settings,"showDate",false);
		boolShowTime=new BooleanProperty(settings,"showTime",true);
		boolShowClass=new BooleanProperty(settings,"showClass",false);
		boolShowMethod=new BooleanProperty(settings,"showMethod",false);
		boolShowOut=new BooleanProperty(settings,"showOut",true);
		boolShowErr=new BooleanProperty(settings,"showErr",true);
		intLoggerLevel=new IntProperty(settings,"loggerlevel",2);
		intConsoleBufferSize=new IntProperty(settings,"consoleBufferSize",400);
	}

	/**
	 * @see devplugin.Plugin#readData(java.io.ObjectInputStream)
	 */
	public void readData(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		super.readData(in);
	}

	/**
	 * @see devplugin.Plugin#storeSettings()
	 */
	public Properties storeSettings() {
		Properties result=new Properties();
		result.putAll(settings);
		return result;
	}

	/**
	 * @see devplugin.Plugin#writeData(java.io.ObjectOutputStream)
	 */
	public void writeData(ObjectOutputStream out) throws IOException {
		super.writeData(out);
	}

	/**
	 * @see devplugin.Plugin#onActivation()
	 */
	public void onActivation() {
		Console.getConsole().activate();
		super.onActivation();
	}

	/**
	 * @see devplugin.Plugin#onDeactivation()
	 */
	public void onDeactivation() {
		Console.getConsole().shutdownConsole();
		super.onDeactivation();
	}
	
	static public Properties getProperties() {
		return instance.settings;
	}
	public static URL getHelpUrl(String filename){
		final String filenamebase="/aconsole/help";
		String filename_local=null;
		String filename_default=filenamebase+"/default/"+filename;
		Locale locale = Locale.getDefault();
		String localeSuffix = locale.toString();
		if (localeSuffix.length() > 0) filename_local = filenamebase+"/" + localeSuffix+"/"+filename;
		else filename_local = filename_default;
		InputStream in = AConsole.class.getResourceAsStream(filename_local);
		if (in == null) {
			filename_local=filename_default;
		}
		return AConsole.class.getResource(filename_local);
	}
	public static void foundABug(Exception ex) {
//		silent crash: high risk of stack overflow otherwise
//		mLog.log(Level.WARNING,"bug in TVBConsole",ex);
//		ErrorHandler.handle(mLocalizer.msg("found_a_bug","It seems you found a bug (an program error) in the TVBConsole plugin. please visit the tv-browser forum (http://hilfe.tvbrowser.org/) and post the error stack (see details) to help me to improve the plugin"), ex);
	}
	
}

/*
 * GrowlPlugin by Bodo Tasche
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package growlplugin;

import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

import util.exc.ErrorHandler;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.misc.AppleScriptRunner;

import devplugin.Program;

/**
 * This is the Container-Class for Growl
 * 
 * It uses AppleScript to call Growl.
 * 
 * @author bodum
 *
 */
public class GrowlContainer {

  private static Logger mLog = Logger.getLogger(GrowlContainer.class.getName());

  /** Parser for Text */
  private ParamParser mParser;
  
  /**
   * Create the Growl-Container and register the NOTIFICATION
   * 
   * @throws Exception
   */
  public GrowlContainer() throws Exception {
    mParser = new ParamParser();
  }
  
  /**
   * Notifies Growl
   * 
   * @param settings Settings to use
   * @param prg Program to use
   */
  public void notifyGrowl(Properties settings, Program prg) {
    String title = mParser.analyse(settings.getProperty("title"), prg);
    String desc = mParser.analyse(settings.getProperty("description"), prg);
    AppleScriptRunner runner = new AppleScriptRunner();
    try {
      runner.executeScript("tell application \"GrowlHelperApp\"\n" +
          "\tset the allNotificationsList to {\"TVBrowserSendProgram\"}\n" +
          "\t\n" +
          "\tregister as application ¬\n" +
          "\t\t\"TV-Browser\" all notifications allNotificationsList ¬\n" +
          "\t\tdefault notifications allNotificationsList ¬\n" +
          "\t\ticon of application \"TV-Browser\"\n" +
          "\t\n" +
          "\t--\tSend a Notification...\n" +
          "\tnotify with name ¬\n" +
          "\t\t\"TVBrowserSendProgram\" title ¬\n" +
          "\t\t\""+ title +"\" description ¬\n" +
          "\t\t\""+ desc + "\" application name \"TV-Browser\"\n" +
          "\t\n" +
          "end tell");
    } catch (IOException e) {
      mLog.log(Level.SEVERE, "Can't execute AppleScript", e);
    }

  }
  
}
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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.libgrowl.Application;
import net.sf.libgrowl.GrowlConnector;
import net.sf.libgrowl.Notification;
import net.sf.libgrowl.NotificationType;
import util.misc.AppleScriptRunner;
import util.misc.OperatingSystem;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
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

  private static final Logger mLog = Logger.getLogger(GrowlContainer.class
      .getName());
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(GrowlContainer.class);

  /** Parser for Text */
  private ParamParser mParser;
  private Application mApplication;
  private NotificationType mNotificationProgram;
  private GrowlConnector mGrowlConnector;

  /**
   * Create the Growl-Container
   */
  public GrowlContainer() {
    mParser = new ParamParser();
  }

  /**
   * Notifies Growl
   * 
   * @param settings
   *          Settings to use
   * @param program
   *          Program to use
   */
  public void notifyGrowl(final GrowlSettings settings, final Program program) {
    final String title = mParser.analyse(settings.getTitle(), program);
    final String desc = mParser.analyse(settings.getDescription(), program);
    if (OperatingSystem.isMacOs()) {
      final AppleScriptRunner runner = new AppleScriptRunner();
      final String script = "tell application \"GrowlHelperApp\"\n"
          + "   set the allNotificationsList to {\"TVBrowserSendProgram\"}\n"
          + "   register as application \"TV-Browser\" all notifications allNotificationsList default notifications allNotificationsList icon of application \"TV-Browser\"\n"
          + "   notify with name \"TVBrowserSendProgram\" title \""
          + runner.formatTextAsParam(title) + "\" description \""
          + runner.formatTextAsParam(desc)
          + "\" application name \"TV-Browser\"\n" + "end tell";
      try {
        runner.executeScript(script);
      } catch (IOException e) {
        mLog.log(Level.SEVERE, "Can't execute AppleScript\n\n" + script, e);
      }
    } else {
      final Notification notification = new Notification(mApplication,
          mNotificationProgram, title, desc, program.getID());
      mGrowlConnector.notify(notification);
    }
  }

  protected void registerApplication() {
    if (!OperatingSystem.isMacOs()) {
      mGrowlConnector = new GrowlConnector();
      mApplication = new Application("TV-Browser");
      mNotificationProgram = new NotificationType("program_notification",
          mLocalizer.msg(
          "notification", "program notification"));
      final NotificationType[] notificationTypes = new NotificationType[] { mNotificationProgram };
      mGrowlConnector.register(mApplication, notificationTypes);
    }
  }
}
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

import util.exc.ErrorHandler;
import util.ui.Localizer;

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.foundation.NSDictionary;

import devplugin.Program;

/**
 * This is the Container-Class for Growl
 * 
 * It gets a Program and sends it to Growl using the Growl-Class from
 * the SDK of Growl
 * 
 * @author bodum
 *
 */
public class GrowlContainer {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GrowlPlugin.class);

  /** Nofiction-Name */
  private final static String NOTIFICATION = "TVBrowserSendProgram";
  
  /** Growl-Instance */
  private Growl mGrowl;
  
  /** Image for Notification */
  private NSImage mNotifyImg;
  
  /**
   * Create the Growl-Container and register the NOTIFICATION
   * 
   * @throws Exception
   */
  public GrowlContainer() throws Exception {
    
    mNotifyImg = new NSImage("./imgs/TVBrowser32.gif", false);
    
    mGrowl = new Growl("TV-Browser", mNotifyImg);
    
    String[] notes = {NOTIFICATION};
    
    mGrowl.setAllowedNotifications(notes);
    mGrowl.setDefaultNotifications(notes);
    mGrowl.register();

  }
  
  /**
   * Notifies Growl
   * 
   * @param prg Program to use
   */
  public void notifyGrowl(Program prg) {
    try {
      mGrowl.notifyGrowlOf(NOTIFICATION, mNotifyImg, prg.getTitle(), "bla desc", (NSDictionary)null);
    } catch (Exception e) {
      ErrorHandler.handle("Error while Sending Program to Growl", e);
    }
  }
  
}
/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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


package tvbrowser.ui.update;

import java.awt.Window;
import java.io.File;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.UiUtilities;


public class PluginsSoftwareUpdateItem extends SoftwareUpdateItem {
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PluginsSoftwareUpdateItem.class);

  public PluginsSoftwareUpdateItem(String name) {
    super(name);
  }

  protected boolean downloadFrom(final String url) throws TvBrowserException {
    if(!isStable()) {
      JOptionPane pane = new JOptionPane();
      
      String ok = mLocalizer.msg("betawarning.oktext","Install beta version");
      String cancel = mLocalizer.msg("betawarning.canceltext","Cancel installation");
      
      pane.setOptions(new String[] {ok,cancel});
      pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
      pane.setOptionType(JOptionPane.YES_NO_OPTION);
      pane.setInitialValue(cancel);
      pane.setMessage(mLocalizer.msg("betawarning.text","<html>The plugin <b>{0}</b> is a beta version.<br>Beta versions are possible unstable and could contain errors.<br><br>Are you sure that you want to install this version?</html>",getName() + " " + getVersion()));
      
      Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
      
      JDialog dialog = pane.createDialog(w, mLocalizer.msg("betawarning.title","Beta version"));
      dialog.setModal(true);
      dialog.setLocationRelativeTo(w);
      dialog.setVisible(true);
      
      if(!ok.equals(pane.getValue())) {
        return false;
      }
    }

    final File toFile=new File(Settings.propPluginsDirectory.getString(),getClassName() + ".jar.inst");
    try {
        IOUtilities.download(new URL(url),toFile);
    }catch (Exception exc) {
      throw new TvBrowserException(SoftwareUpdateItem.class, "error.1",
                "Download failed", url,toFile.getAbsolutePath(),exc);
    }
    return true;
  }

  /**
   * Gets the id of this plugins update.
   * 
   * @return The id of this plugins update.
   */
  public String getId() {
    StringBuilder id = new StringBuilder();
    
    if(this instanceof PluginSoftwareUpdateItem) {
      id.append("java.");
    }
    
    return id.append(getClassName().toLowerCase()).append(".").append(getClassName()).toString();
  }
}
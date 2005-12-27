/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package onlinereminder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * This Plugin contacts TVAddicted and stores Reminder Online.
 * The Reminder will be used to Inform the User via Mail, ICQ etc.
 * 
 * @author bodum
 */
public class OnlineReminder extends Plugin {
  /** Localisation */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(OnlineReminder.class);
  /** Configuration */
  private Configuration mConfig;
  
  private static OnlineReminder INSTANCE;
  
  /**
   * Creates the Plugin
   */
  public OnlineReminder() {
    mConfig = new Configuration(getRootNode());
    INSTANCE = this;
  }

  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "Online Reminder");
    String desc = mLocalizer.msg("description", "Remindes you via EMail, AIM or Jabber Messages.");
    String author = "Bodo Tasche";
    return new PluginInfo(name, desc, author, new Version(0, 10));
  }

  protected String getMarkIconName() {
    return "onlinereminder/icons/bell16.png";
  }

  public ActionMenu getContextMenuActions(Program program) {
    if (!program.equals(getPluginManager().getExampleProgram()))
      if (program.isExpired() || program.isOnAir())
        return null;

    ContextMenuAction menu = new ContextMenuAction();

    final Program prog = program;

    if (!getRootNode().contains(prog)) {
      menu.setSmallIcon(createImageIcon("onlinereminder/icons/bell16.png"));
      menu.setText(mLocalizer.msg("contextMenuRemind","Add to Online-Reminder"));
      menu.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mConfig.addProgram(prog);
        }
      });
    } else {
      menu.setSmallIcon(createImageIcon("onlinereminder/icons/bell16.png"));
      menu.setText(mLocalizer.msg("contextMenuRemove","Remove from Online-Reminder"));
      menu.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mConfig.removeProgram(prog);
        }
      });
    }

    return new ActionMenu(menu);
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {

      public void actionPerformed(ActionEvent evt) {
        showDialog();
      }
      
    };
    action.putValue(Action.NAME, mLocalizer.msg("pluginName", "Online Reminder"));
    action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar(
        "onlinereminder/icons/bell16.png", OnlineReminder.class)));
    action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar("onlinereminder/icons/bell22.png",
        OnlineReminder.class)));

    return new ActionMenu(action);
  }

  /**
   * Show the Dialog with the Program-List
   * 
   */
  public void showDialog() {
    ReminderDialog dlg = new ReminderDialog(getParentFrame(), mConfig);
    UiUtilities.centerAndShow(dlg);
  }
  
  public boolean canUseProgramTree() {
    return true;
  }

  public boolean canReceivePrograms() {
    return true;
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    mConfig.writeData(out);
  }
  
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    mConfig = new Configuration(getRootNode(), in);
  }
  
  public void receivePrograms(Program[] programArr) {
    for (int i = 0; i < programArr.length; i++) {
      mConfig.addProgram(programArr[i], false);
    }
    getRootNode().update();
  }

  public static Plugin getInstance() {
    return INSTANCE;
  }
}

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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package tvbrowsermini;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import tvbrowsermini.devices.AbstractExportDevice;
import tvbrowsermini.devices.Android;
import tvbrowsermini.devices.PDA;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin exports Data into a SQLite DB
 *
 * @author bodum
 */
public class TVBrowserMini extends Plugin {

  private static final boolean IS_STABLE = false;
  private static final Version mVersion = new Version(0, 6, IS_STABLE);
  /**
   * Translator
   */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(TVBrowserMini.class);
  private static TVBrowserMini mInstance;
  private TVBrowserMiniSettings mSettings;
  private boolean mExportRuns;
  // The channel array to export
  private Channel[] mSelectedChannels;

  /**
   * Constructor, stores current instance in static field
   */
  public TVBrowserMini() {
    mInstance = this;
    mExportRuns = false;
  }

  /**
   * @return Instance of this Plugin.
   */
  public static TVBrowserMini getInstance() {
    return mInstance;
  }

  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "TV-Browser Mini Export");
    String desc = mLocalizer.msg("description", "Exports the Data for your Pocket PC");
    String author = "Bodo Tasche, René Mach, Benedikt Grabenmeier, Michael Keppler";
    return new PluginInfo(TVBrowserMini.class, name, desc, author);
  }

  public static Version getVersion() {
    return mVersion;
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        new Thread() {
          public void run() {
            if (TVBrowserMini.mInstance.mSettings.getAccepted()) {
              export();
              JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()), "Export fertig!");
            } else {
              JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()),
                  "Sie m�ssen erst die Einverst�ndniserkl�rung akzeptieren!");
            }
          }
        }.start();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("buttonName", "Start Export"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    action.putValue(BIG_ICON, createImageIcon("actions", "document-save-as", 22));
    return new ActionMenu(action);
  }

  public void loadSettings(Properties properties) {
    mSettings = new TVBrowserMiniSettings(properties);
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public SettingsTab getSettingsTab() {
    return new TVBrowserMiniSettingsTab(mSettings, mInstance.getParentFrame());
  }

  public void handleTvDataUpdateFinished() {
    // export();
  }

  /**
   * Opens a filechooser and exports the Data to sqlite db
   */
  private void export() {
    if (mExportRuns) {
      return;
    }

    mExportRuns = true;
    JDialog infoDialog = null;

    if (mSettings.getAccepted()) {
      Window parentWindow = UiUtilities.getBestDialogParent(getParentFrame());

      if (parentWindow instanceof Dialog) {
        infoDialog = new JDialog((Dialog) parentWindow);
      } else if (parentWindow instanceof Frame) {
        infoDialog = new JDialog((Frame) parentWindow);
      } else {
        infoDialog = new JDialog(getParentFrame());
      }

      infoDialog.setTitle(mLocalizer.msg("pluginName", "TV-Browser Mini Export"));
      infoDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      JProgressBar progress = new JProgressBar();
      progress.setIndeterminate(false);
      progress.setStringPainted(true);
      progress.setMaximum(mSelectedChannels.length * getProgressMaximum());

      progress.setValue(0);
      infoDialog.getContentPane().setLayout(new BoxLayout(infoDialog.getContentPane(), BoxLayout.Y_AXIS));
      JLabel label1 = new JLabel(mLocalizer.msg("waitForExport",
          "Wait until data is exported. This could take some time."));
      label1.setAlignmentX(Component.CENTER_ALIGNMENT);
      label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
      infoDialog.getContentPane().add(label1);
      infoDialog.getContentPane().add(progress);
      ((JPanel) infoDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

      infoDialog.getContentPane().add(Box.createRigidArea(new Dimension(0, 10)));
      infoDialog.pack();

      UiUtilities.centerAndShow(infoDialog);
      AbstractExportDevice device;
      if (mSettings.isDeviceAndroid()) {
        device = new Android(mSettings, mSelectedChannels, progress);
      } else {
        device = new PDA(mSettings, mSelectedChannels, progress);
      }
      device.export(getParentFrame());
    }

    if (infoDialog != null) {
      infoDialog.dispose();
    }

    mExportRuns = false;
  }

  private int getProgressMaximum() {
    // Progressbar Maximum
    Date date = new Date();
    date = date.addDays(-2);
    int maxDays = mSettings.getDaysToExport();
    maxDays++; // first day is always yesterday
    List<Date> dates = new ArrayList<Date>();
    for (int d = 0; d < maxDays; d++) {
      date = date.addDays(1);
      for (Channel mSelectedChannel : mSelectedChannels) {
        Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, mSelectedChannel);
        if ((it != null) && (it.hasNext())) {
          dates.add(date);
          break;
        }
      }
    }
    return dates.size();
  }

  public void onActivation() {
    // if there is no channel array we take all subscribed channels
    if (mSelectedChannels == null) {
      mSelectedChannels = Plugin.getPluginManager().getSubscribedChannels();
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // file version
    out.writeInt(mSelectedChannels.length); // store size of array
    // save the complete channel array
    for (Channel ch : mSelectedChannels) {
      ch.writeData(out);
    }
  }

  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    try {
      in.readInt(); // read version

      int size = in.readInt(); // read size of channel array

      /*
       * First save the channels in an array list because we didn't know now, if
       * all channels can be loaded, maybe a channel was removed.
       *
       * So we cannot use an array because it possibly have the wrong size and
       * could contain null values.
       */
      ArrayList<Channel> channels = new ArrayList<Channel>(size);

      for (int i = 0; i < size; i++) {
        Channel ch = Channel.readData(in, true);
        if (ch != null) {
          channels.add(ch);
        }
      }

      mSelectedChannels = channels.toArray(new Channel[channels.size()]);
    } catch (Exception e) {
      // ignore
    }
  }

  protected Channel[] getSelectedChannels() {
    return mSelectedChannels;
  }

  protected void setSelectedChannels(Channel[] channels) {
    mSelectedChannels = channels;
  }

  protected Icon getPluginIcon() {
    return createImageIcon("actions", "document-save-as", 16);
  }

  ProgramFieldType[] getAvailableFields() {
    ArrayList<ProgramFieldType> list = new ArrayList<ProgramFieldType>();
    for (Iterator<ProgramFieldType> iterator = ProgramFieldType.getTypeIterator(); iterator.hasNext();) {
      ProgramFieldType type = iterator.next();
      if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        boolean isAvailable = true;
        for (ProgramFieldType always : mSettings.getAlwaysExportedFields()) {
          if (type.equals(always)) {
            isAvailable = false;
            break;
          }
        }
        if (isAvailable) {
          list.add(type);
        }
      }
    }
    return list.toArray(new ProgramFieldType[list.size()]);
  }

  ProgramFieldType[] getSelectedFields() {
    ArrayList<ProgramFieldType> selected = new ArrayList<ProgramFieldType>();
    ProgramFieldType[] availableFields = getAvailableFields();
    for (int i = 0; i < availableFields.length; i++) {
      if (mSettings.getProgramField(availableFields[i])) {
        selected.add(availableFields[i]);
      }
    }
    return selected.toArray(new ProgramFieldType[selected.size()]);
  }
}

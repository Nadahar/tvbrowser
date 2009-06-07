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

import devplugin.*;
import tvbrowsermini.devices.AbstractExportDevice;
import tvbrowsermini.devices.Android;
import tvbrowsermini.devices.PDA;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Iterator;


//import org.sqlite.JDBC;


/**
 * This Plugin exports Data into a SQLite DB
 *
 * @author bodum
 */
public class TVBrowserMini extends Plugin {
  /**
   * Translator
   */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(TVBrowserMini.class);
  private static TVBrowserMini mInstance;
  private Properties mSettings;
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
    String author = "Bodo Tasche, René Mach, Benedikt Grabenmeier";
    return new PluginInfo(name, desc, author, new Version(0, 52, false));
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        new Thread() {
          public void run() {
            if (TVBrowserMini.mInstance.mSettings.getProperty("accept").equals("1")) {
              export();
              JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()), "Export fertig!");
            } else {
              JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()), "Sie müssen erst die Einverständniserklärung akzeptieren!");
            }
          }
        }.start();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("buttonName", "Start Export"));
    action.putValue(Action.SMALL_ICON, createImageIcon("actions", "document-save-as", 16));
    action.putValue(BIG_ICON, createImageIcon("actions", "document-save-as", 22));
    return new ActionMenu(action);
  }

  public void loadSettings(Properties settings) {
    if (settings == null) {
      settings = new Properties();
    }
    if (settings.getProperty("path") == null) {
      settings.setProperty("path", getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + File.separator + "tvdata.tvd");
    }
    if (settings.getProperty("exportDays") == null) {
      settings.setProperty("exportDays", "0");
    }

    if (settings.getProperty("accept") == null) {
      settings.setProperty("accept", "0");
    }
    if (settings.getProperty("elementShortDescription") == null) {
      settings.setProperty("elementshortdescription", "true");
    }
    if (settings.getProperty("elementDescription") == null) {
      settings.setProperty("elementDescription", "false");
    }
    if (settings.getProperty("elementGenre") == null) {
      settings.setProperty("elementGenre", "false");
    }
    if (settings.getProperty("elementProductionLocation") == null) {
      settings.setProperty("elementProductionLocation", "false");
    }
    if (settings.getProperty("elementProductionTime") == null) {
      settings.setProperty("elementProductionTime", "false");
    }
    if (settings.getProperty("elementDirector") == null) {
      settings.setProperty("elementDirector", "false");
    }
    if (settings.getProperty("elementScript") == null) {
      settings.setProperty("elementScript", "false");
    }
    if (settings.getProperty("elementActor") == null) {
      settings.setProperty("elementActor", "false");
    }
    if (settings.getProperty("elementMusic") == null) {
      settings.setProperty("elementMusic", "false");
    }
    if (settings.getProperty("elementOriginalTitel") == null) {
      settings.setProperty("elementOriginalTitel", "false");
    }
    if (settings.getProperty("elementFSK") == null) {
      settings.setProperty("elementFSK", "false");
    }
    if (settings.getProperty("elementForminformation") == null) {
      settings.setProperty("elementForminformation", "false");
    }
    if (settings.getProperty("elementShowView") == null) {
      settings.setProperty("elementShowView", "false");
    }
    if (settings.getProperty("elementEpisode") == null) {
      settings.setProperty("elementEpisode", "true");
    }
    if (settings.getProperty("elementOriginalEpisode") == null) {
      settings.setProperty("elementOriginalEpisode", "false");
    }
    if (settings.getProperty("elementModeration") == null) {
      settings.setProperty("elementModeration", "false");
    }
    if (settings.getProperty("elementWebside") == null) {
      settings.setProperty("elementWebside", "false");
    }
    if (settings.getProperty("elementRepetitionOn") == null) {
      settings.setProperty("elementRepetitionOn", "false");
    }
    if (settings.getProperty("elementRepetitionOf") == null) {
      settings.setProperty("elementRepetitionOf", "false");
    }
    if (settings.getProperty("elementVPS") == null) {
      settings.setProperty("elementVPS", "false");
    }
    mSettings = settings;
  }

  public Properties storeSettings() {
    return mSettings;
  }

  public SettingsTab getSettingsTab() {
    return new TVBrowserMiniSettingsTab(mSettings, mInstance.getParentFrame());
  }

  public void handleTvDataUpdateFinished() {
    //export();
  }

  /**
   * Opens a filechooser and exports the Data to sqlite db
   */
  private void export() {
    if (mExportRuns)
      return;

    mExportRuns = true;
    JDialog infoDialog = null;

    if (mSettings.getProperty("accept").equals("1")) {
      Window parent = UiUtilities.getBestDialogParent(getParentFrame());

      if (parent instanceof Dialog)
        infoDialog = new JDialog((Dialog) parent);
      else if (parent instanceof Frame)
        infoDialog = new JDialog((Frame) parent);
      else
        infoDialog = new JDialog(getParentFrame());

      infoDialog.setTitle(mLocalizer.msg("pluginName", "TV-Browser Mini Export"));
      infoDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

      JProgressBar progress = new JProgressBar();
      progress.setIndeterminate(false);

      progress.setMaximum(mSelectedChannels.length * getProgressMaximum());

      progress.setValue(0);
      infoDialog.getContentPane().setLayout(
              new BoxLayout(infoDialog.getContentPane(), BoxLayout.Y_AXIS));
      JLabel label1 = new JLabel(mLocalizer.msg("waitForExport",
              "Wait until data is exported. This could take some time."));
      label1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
      infoDialog.getContentPane().add(label1);
      infoDialog.getContentPane().add(progress);
      ((JPanel) infoDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

      infoDialog.getContentPane().add(
              Box.createRigidArea(new Dimension(0, 10)));
      infoDialog.pack();

      UiUtilities.centerAndShow(infoDialog);
      AbstractExportDevice device;
      if (mSettings.getProperty("device", "0").equals("1"))
        device = new Android(mSettings, mSelectedChannels, progress);
      else
        device = new PDA(mSettings, mSelectedChannels, progress);
      device.export(getParentFrame());
    }

    if (infoDialog != null)
      infoDialog.dispose();

    mExportRuns = false;
  }

  public int getProgressMaximum() {
    //Progressbar Maximum
    Date date = new Date();
    date = date.addDays(-2);
    int maxDays = Integer.parseInt(mSettings.getProperty("exportDays"));
    if (maxDays == 0)
      maxDays = 32;
    maxDays++; //first day is always yesterday
    java.util.List<Date> dates = new ArrayList<Date>();
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

  /**
   * Gets the Start-Time as Calendar
   *
   * @param p Program
   * @return Start-Time
   */
  public static Calendar getStartAsCalendar(Program p) {
    Calendar cal = p.getDate().getCalendar();
    int min = p.getStartTime();
    int hour = min % 60;
    min = min - (hour * 60);
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, min);
    cal.set(Calendar.SECOND, 0);

    return cal;
  }

  /**
   * Gets the End-Time as Calendar
   *
   * @param p Program
   * @return End-Time
   */
  public static Calendar getEndAsCalendar(Program p) {
    Calendar cal = getStartAsCalendar(p);

    int leng = p.getLength();

    if (leng <= 0) {
      leng = 0;
    }

    cal.add(Calendar.MINUTE, leng);

    return cal;
  }

  /*
  * (non-Javadoc)
  *
  * @see devplugin.Plugin#getMarkIconFromTheme()
  */
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "preferences-desktop-locale", 16);
  }

  public void onActivation() {
    // if there is no channel array we take all subscribed channels
    if (mSelectedChannels == null)
      mSelectedChannels = Plugin.getPluginManager().getSubscribedChannels();
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // file version
    out.writeInt(mSelectedChannels.length); // store size of array
    // save the complete channel array
    for (Channel ch : mSelectedChannels)
      ch.writeData(out);
  }

  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    try {
      in.readInt(); // read version

      int n = in.readInt(); // read size of channel array

      /* First save the channels in an array list
      * because we didn't know now, if all channels
      * can be loaded, maybe a channel was removed.
      *
      * So we cannot use an array because it possibly have
      * the wrong size and could contain null values.
      */
      ArrayList<Channel> channels = new ArrayList<Channel>();

      for (int i = 0; i < n; i++) {
        Channel ch = Channel.readData(in, true);
        if (ch != null)
          channels.add(ch);
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
}

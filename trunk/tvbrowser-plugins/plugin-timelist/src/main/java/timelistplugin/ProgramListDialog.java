/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package timelistplugin;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import util.program.ProgramUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * @author bananeweizen
 * @deprecated to be removed after 3.0. instead use util.
 * 
 */
public final class ProgramListDialog extends JDialog implements WindowClosingIf {

  /**
   * vector of programs shown in the dialog
   */
  private ArrayList<Program> programs;

  private ProgramList programList;

  private JScrollPane scrollPane;

  /**
   * translator of this class
   */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ProgramListDialog.class);

  /**
   * creates the dialog
   * 
   * @param parent
   *          parent frame
   */
  public ProgramListDialog(final Frame parent) {
    super(parent, true);
    generateList();
    createGUI();
  }

  /**
   * Creates the Dialog
   * 
   * @param parent
   *          parent frame
   */
  public ProgramListDialog(final Dialog parent) {
    super(parent, true);
    generateList();
    createGUI();
  }

  /**
   * find all programs to display
   */
  private void generateList() {
    final boolean showExpired = TimeListPlugin.getInstance().isShowExpired();
    programs = new ArrayList<Program>();
    final Channel[] channels = Plugin.getPluginManager()
        .getSubscribedChannels();

    Date date = Plugin.getPluginManager().getCurrentDate();
    final int startTime = Plugin.getPluginManager().getTvBrowserSettings()
        .getProgramTableStartOfDay();
    final int endTime = Plugin.getPluginManager().getTvBrowserSettings()
        .getProgramTableEndOfDay();
    for (int d = 0; d < 2; d++) {

      for (int i = 0; i < channels.length; i++) {
        final Iterator<Program> it = Plugin.getPluginManager()
            .getChannelDayProgram(
            date, channels[i]);
        if (it != null) {
          while (it.hasNext()) {
            final Program program = it.next();
            if (showExpired || !program.isExpired()) {
              if ((d == 0 && program.getStartTime() >= startTime)
                  || (d == 1 && program.getStartTime() <= endTime)) {
                programs.add(program);
              }
            }
          }
        }
      }
      date = date.addDays(1);
    }
    Collections.sort(programs, ProgramUtilities.getProgramComparator());
  }

  /**
   * create the dialog UI
   */
  private void createGUI() {
    final Date date = Plugin.getPluginManager().getCurrentDate();
    setTitle(mLocalizer.msg("title", "Programs by time for {0}", date
        .getShortDayLongMonthString()));
    UiUtilities.registerForClosing(this);

    final JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());

    Program[] prg = new Program[programs.size()];

    int scrollIndex = -1;
    for (int i = 0; i < programs.size(); i++) {
      prg[i] = programs.get(i);
      if (scrollIndex == -1 && !prg[i].isExpired()) {
        scrollIndex = i;
      }
    }

    final ProgramPanelSettings settings = new ProgramPanelSettings(
        new PluginPictureSettings(PluginPictureSettings.NO_PICTURE_TYPE), true);
    programList = new ProgramList(prg, settings);
    programList.setCellRenderer(new TimeListCellRenderer());

    programList.addMouseListeners(null);

    scrollPane = new JScrollPane(programList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

    content.add(scrollPane, BorderLayout.CENTER);

    final JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    content.add(buttonPn, BorderLayout.SOUTH);
    
    final JButton buttonSettings = new JButton(TimeListPlugin.getInstance()
        .createImageIcon("categories",
        "preferences-system", 16));    
    buttonSettings.setToolTipText(mLocalizer.msg("settings","Open settings"));

    buttonSettings.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          close();
          Plugin.getPluginManager()
              .showSettings(TimeListPlugin.getInstance());
        }
      });
    buttonPn.add(buttonSettings, BorderLayout.WEST);

    final JButton closeButton = new JButton(Localizer
        .getLocalization(Localizer.I18N_CLOSE));
    closeButton.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(closeButton, BorderLayout.EAST);

    getRootPane().setDefaultButton(closeButton);
    pack();
    setMinimumSize(new Dimension(300, 400));
    scrollToIndex(scrollIndex);
  }

  public void close() {
    dispose();
  }

  /**
   * scroll to the program with the given index
   * @param index index of the program
   */
  private void scrollToIndex(final int index) {
    if (index < 0) {
      return;
    }

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getHorizontalScrollBar().setValue(0);

        final Rectangle cellBounds = programList.getCellBounds(index, index);
        if (cellBounds != null) {
          cellBounds.setLocation(cellBounds.x, cellBounds.y
              + scrollPane.getHeight() - cellBounds.height - 5);
          programList.scrollRectToVisible(cellBounds);
        }
      }
    });
  }

}
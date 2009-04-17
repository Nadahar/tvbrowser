/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.ui.CaretPositionCorrector;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.TabLayout;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ButtonsSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ButtonsSettingsTab.class);

  private JPanel mSettingsPn;

  private TimesListPanel mTimeButtonsPn;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {

    mSettingsPn = new JPanel(new FormLayout("5dlu, pref, fill:pref:grow", "pref, 5dlu, fill:pref:grow, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("buttons.time", "Time buttons")), cc.xyw(1, 1, 3));

    mTimeButtonsPn = new TimesListPanel(Settings.propTimeButtons.getIntArray());
    
    JScrollPane pane = new JScrollPane(mTimeButtonsPn);
    pane.setBorder(BorderFactory.createEmptyBorder());
    pane.setViewportBorder(BorderFactory.createEmptyBorder());
    
    mSettingsPn.add(pane, cc.xy(2, 3));
    
    if(TVBrowser.isUsingSystemTray()) {
      JEditorPane helpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("info","The times of the  buttons are also used for the '<a href=\"#link\">{0}</a>' in the tray menu.", TrayOnTimeSettingsTab.getName()), new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            Plugin.getPluginManager().showSettings(SettingsItem.TRAYONTIMEPROGRAMS);
          }
        }
      });
      
      mSettingsPn.add(helpLabel, cc.xyw(1,4,3));
    }
    
    return mSettingsPn;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    int[] times = mTimeButtonsPn.getTimes();
    Arrays.sort(times);
    
    Settings.propTimeButtons.setIntArray(times);
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-specific-time", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("buttons", "Buttons");
  }

  private static class TimePanel extends JPanel {

    private JSpinner mTimeSp;

    public TimePanel(int minutes) {
      setLayout(new BorderLayout());

      mTimeSp = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mTimeSp, Settings.getTimePattern()); 
      mTimeSp.setEditor(dateEditor);

      CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] {':'}, -1);
      
      add(mTimeSp, BorderLayout.EAST);
      setTime(minutes);
    }

    public void setTime(int minutes) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
      cal.set(Calendar.MINUTE, minutes % 60);
      mTimeSp.setValue(cal.getTime());
    }

    public int getTime() {

      Date time = (Date) mTimeSp.getValue();
      Calendar cal = Calendar.getInstance();
      cal.setTime(time);
      return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    @Override
    public void setEnabled(boolean val) {
      mTimeSp.setEnabled(val);
    }
  }

  private static class TimesListPanel extends JPanel {
    private ArrayList<Row> mRows;

    private JPanel mListPn;

    public TimesListPanel(int[] times) {
      mRows = new ArrayList<Row>();
      setLayout(new FormLayout("right:pref, fill:pref:grow", "pref, 3dlu, pref"));

      CellConstraints cc = new CellConstraints();

      mListPn = new JPanel();
      mListPn.setLayout(new BoxLayout(mListPn, BoxLayout.Y_AXIS));
      add(mListPn, cc.xy(1, 1));

      for (int i = 0; i < times.length; i++) {
        final Row row = new Row(times[i]);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        mRows.add(row);
        row.getRemoveButton().addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg) {
            mRows.remove(row);
            updateContent();
          }
        });
      }
      JButton newBtn = new JButton(mLocalizer.msg("new", "New"), TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
      JPanel southPn = new JPanel(new BorderLayout());
      southPn.add(newBtn, BorderLayout.WEST);

      add(southPn, cc.xyw(1, 3, 2));
      newBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {

          final Row row = new Row(0);
          mRows.add(row);

          row.getRemoveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg) {
              mRows.remove(row);
              updateContent();
            }
          });

          updateContent();
        }
      });

      updateContent();
    }

    private void updateContent() {
      mListPn.removeAll();
      for (int i = 0; i < mRows.size(); i++) {
        Row row = mRows.get(i);
        mListPn.add(row);
      }
      mListPn.updateUI();
    }

    public int[] getTimes() {
      ArrayList<Integer> list = new ArrayList<Integer>();

      for (int i = 0; i < mRows.size(); i++) {
        int value = (mRows.get(i)).getTime();

        if (!list.contains(value)) {
          list.add(value);
        }
      }

      int[] result = new int[list.size()];

      for (int i = 0; i < result.length; i++) {
        result[i] = (list.get(i)).intValue();
      }

      return result;
    }
  }

  private static class Row extends JPanel {

    private JButton mRemoveBtn;

    private TimePanel mTimePn;

    public Row(int time) {
      setLayout(new BorderLayout());
      mRemoveBtn = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
      mRemoveBtn.setToolTipText(Localizer.getLocalization(Localizer.I18N_DELETE));
      JPanel row = new JPanel(new TabLayout(2, 14, 0));

      row.add(mTimePn = new TimePanel(time));
      row.add(mRemoveBtn);

      add(row, BorderLayout.WEST);

    }

    public JButton getRemoveButton() {
      return mRemoveBtn;
    }

    public int getTime() {
      return mTimePn.getTime();
    }

  }
}

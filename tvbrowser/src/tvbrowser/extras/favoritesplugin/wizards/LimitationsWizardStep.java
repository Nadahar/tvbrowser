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

package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import util.ui.ChannelChooserDlg;
import util.ui.TimePeriodChooser;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Program;

public class LimitationsWizardStep extends AbstractWizardStep {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(LimitationsWizardStep.class);

  private JCheckBox mChannelCb;

  private JCheckBox mDayOfWeekCb;

  private JCheckBox mTimeCb;

  private JButton mChooseChannelsBtn;

  private JComboBox mDayOfWeekCombo;

  private TimePeriodChooser mTimePeriodChooser;

  private Program mProgram;

  private Channel[] mChannelArr;

  private WizardStep mCaller;

  private JPanel mContent;

  public LimitationsWizardStep(WizardStep caller, Program program) {
    mProgram = program;
    mCaller = caller;
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Limitations");
  }

  public JPanel createContent(WizardHandler handler) {

    if (mProgram != null) {
      mChannelArr = new Channel[]{ mProgram.getChannel()};
    }
    else {
      mChannelArr = new Channel[]{};
    }

    mDayOfWeekCombo = new JComboBox(new Object[] {
        LimitationConfiguration.DAYLIMIT_DAILY,
        LimitationConfiguration.DAYLIMIT_WEEKDAY,
        LimitationConfiguration.DAYLIMIT_WEEKEND,
        LimitationConfiguration.DAYLIMIT_MONDAY,
        LimitationConfiguration.DAYLIMIT_TUESDAY,
        LimitationConfiguration.DAYLIMIT_WEDNESDAY,
        LimitationConfiguration.DAYLIMIT_THURSDAY,
        LimitationConfiguration.DAYLIMIT_FRIDAY,
        LimitationConfiguration.DAYLIMIT_SATURDAY,
        LimitationConfiguration.DAYLIMIT_SUNDAY });
    mDayOfWeekCombo.setRenderer(new DayListCellRenderer());

    int lowBnd, upBnd;
    if (mProgram != null) {
      lowBnd = (mProgram.getHours() - 1) * 60;
      if (lowBnd < 0) {
        lowBnd = 0;
      }
      upBnd = lowBnd + 120;
      if (upBnd >= 24 * 60) {
        upBnd = 24 * 60 - 1;
      }
    } else {
      lowBnd = 0;
      upBnd = 24 * 60 - 1;
    }
    mTimePeriodChooser = new TimePeriodChooser(lowBnd, upBnd, TimePeriodChooser.ALIGN_RIGHT);

    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref, default:grow, pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    panelBuilder.setBorder(Borders.DLU4_BORDER);
    panelBuilder.add(new JLabel(mLocalizer.msg("mainQuestion", "Are there any limitations?")), cc.xy(1, 1));
    panelBuilder.add(mChannelCb = new JCheckBox(mLocalizer.msg("limitByChannel",
        "Certain channels only:")), cc.xy(1, 3));
    panelBuilder.add(mDayOfWeekCb = new JCheckBox(mLocalizer.msg("limitByDayOfWeek","Certain day of week only:")), cc.xy(1,5));
    panelBuilder.add(mTimeCb = new JCheckBox(mLocalizer.msg("limitByTime",
        "Certain start times only:")), cc.xy(1, 7));

    panelBuilder.add(mChooseChannelsBtn = new JButton(mLocalizer.msg("selectChannels","Select channels")), cc.xy(3,3));
    panelBuilder.add(mDayOfWeekCombo, cc.xy(3,5));
    panelBuilder.add(mTimePeriodChooser, cc.xy(3,7));

    updateControls();

    mChannelCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        updateControls();
      }
    });

    mDayOfWeekCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        updateControls();
      }
    });

    mTimeCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        updateControls();
      }
    });

    mChooseChannelsBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Window parent = UiUtilities.getBestDialogParent(mContent);
        ChannelChooserDlg dlg = new ChannelChooserDlg(parent, mChannelArr,
            null,
            ChannelChooserDlg.SELECTABLE_ITEM_LIST);
        UiUtilities.centerAndShow(dlg);
        Channel[] chArr = dlg.getChannels();
        if (chArr != null) {
          mChannelArr = dlg.getChannels();
          if (mChannelArr.length == 0) {
            mChannelCb.setSelected(false);
            updateControls();
          }
        }
      }
    });

    mContent = panelBuilder.getPanel();
    mContent.addFocusListener(new FocusAdapter() {

        public void focusGained(FocusEvent e) {
          mChannelCb.requestFocusInWindow();
        }
      });
    return mContent;

  }


  private void updateControls() {
    mChooseChannelsBtn.setEnabled(mChannelCb.isSelected());
    mDayOfWeekCombo.setEnabled(mDayOfWeekCb.isSelected());
    mTimePeriodChooser.setEnabled(mTimeCb.isSelected());

  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite)obj;
    if (mChannelCb.isSelected()) {
      fav.getLimitationConfiguration().setChannels(mChannelArr);
    }
    if (mTimeCb.isSelected()) {
      fav.getLimitationConfiguration().setTime(mTimePeriodChooser.getFromTime(), mTimePeriodChooser.getToTime());
    }
    if (mDayOfWeekCb.isSelected()) {
      int dayOfWeek = ((Integer)mDayOfWeekCombo.getSelectedItem()).intValue();
      fav.getLimitationConfiguration().setDayLimit(dayOfWeek);
      if (!mTimeCb.isSelected()) {
        fav.getLimitationConfiguration().setTime(0, 24*60-1);
      }
    }
    return obj;
  }

  public WizardStep next() {
    return new RenameWizardStep(this);
  }

  public WizardStep back() {
    return mCaller;
  }

  public boolean isValid() {
    return true;
  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_BACK, WizardStep.BUTTON_NEXT };
  }

}

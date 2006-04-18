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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;

import javax.swing.*;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.common.ReminderConfiguration;
import devplugin.Program;


public class ReminderWizardStep extends AbstractWizardStep {

  public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ReminderWizardStep.class);


  private JCheckBox mReminderCb;
  private JCheckBox mEmailCb;
  private JCheckBox mICQCb;
  private Program mProgram;
  private WizardStep mCaller;



  public ReminderWizardStep(WizardStep caller, Program prog) {
    mProgram = prog;
    mCaller = caller;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Reminder");
  }

  public JPanel createContent(WizardHandler handler) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(
                new FormLayout(
                    "pref",
                    "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));


    panelBuilder.setBorder(Borders.DLU4_BORDER);
    panelBuilder.add(new JLabel(mLocalizer.msg("mainQuestion","Wie wollen Sie auf diese Sendung hingewiesen werden?")), cc.xy(1,1));
    panelBuilder.add(mReminderCb = new JCheckBox(mLocalizer.msg("option.reminderwindow","Erinnerungsfenster (TV-Browser muß geöffnet sein).")), cc.xy(1,3));
    panelBuilder.add(mEmailCb = new JCheckBox("Ich möchte eine E-Mail erhalten."), cc.xy(1,5));
    panelBuilder.add(mICQCb = new JCheckBox("Ich möchte über ICQ erinnert werden."), cc.xy(1,7));

    mReminderCb.setSelected(true);
    return panelBuilder.getPanel();

  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite)obj;
    if (mReminderCb.isSelected()) {
      fav.getReminderConfiguration().setReminderServices(new String[]{ReminderConfiguration.REMINDER_DEFAULT});
    }
    return fav;
  }


  public WizardStep next() {
    return new LimitationsWizardStep(this, mProgram);
  }

  public WizardStep back() {
    return mCaller;
  }

  public boolean isValid() {
    return true;
  }

  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_BACK, WizardStep.BUTTON_NEXT};
  }
  
}

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

import devplugin.Program;

public class LimitationsWizardStep extends AbstractWizardStep {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LimitationsWizardStep.class);

  private JCheckBox mChannelCb;

  private JCheckBox mTimeCb;

  private Program mProgram;

  private WizardStep mCaller;

  public LimitationsWizardStep(WizardStep caller, Program program) {
    mProgram = program;
    mCaller = caller;
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Limitations");
  }

  public JPanel createContent(WizardHandler handler) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    panelBuilder.setBorder(Borders.DLU4_BORDER);
    panelBuilder.add(new JLabel(mLocalizer.msg("mainQuestion", "Gibt es weitere Einschränkungen?")), cc.xy(1, 1));
    panelBuilder.add(mChannelCb = new JCheckBox(mLocalizer.msg("limitByChannel",
        "Ich möchte die Sendung nur auf bestimmten Sendern sehen")), cc.xy(1, 3));
    panelBuilder.add(mTimeCb = new JCheckBox(mLocalizer.msg("limitByTime",
        "Ich möchte die Sendung nur zu bestimmten Zeiten sehen")), cc.xy(1, 5));

    return panelBuilder.getPanel();

  }

  public Object createDataObject(Object obj) {
    return obj;
  }

  public WizardStep next() {
    if (mChannelCb.isSelected()) {
      if (mTimeCb.isSelected()) {
        return new LimitChannelWizardStep(this, new LimitTimeWizardStep(this, mProgram), mProgram);
      } else {
        return new LimitChannelWizardStep(this, mProgram);
      }
    } else if (mTimeCb.isSelected()) {
      return new LimitTimeWizardStep(this, mProgram);
    }
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

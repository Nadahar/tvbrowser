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

import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import util.ui.TimePeriodChooser;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

public class LimitTimeWizardStep extends AbstractWizardStep {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LimitTimeWizardStep.class);

  private TimePeriodChooser mTimePeriodChooser;

  private Program mProgram;

  private WizardStep mCaller;

  public LimitTimeWizardStep(Program program) {
    this(null, program);
  }

  public LimitTimeWizardStep(WizardStep caller, Program program) {
    mProgram = program;
    mCaller = caller;
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Time");
  }

  public JPanel createContent(WizardHandler handler) {
    JPanel panel = new JPanel(new FormLayout("fill:pref:grow", "pref, 3dlu, pref"));
    panel.setBorder(Borders.DLU4_BORDER);
    
    CellConstraints cc = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("timeLabel", "Choose Time in which the program has to run:")), cc.xy(1,1));
    
    int lowBnd;
    int upBnd;

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
    mTimePeriodChooser = new TimePeriodChooser(lowBnd, upBnd, TimePeriodChooser.ALIGN_LEFT);

    panel.add(mTimePeriodChooser, cc.xy(1,3));
    
    return panel;
  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite) obj;
    fav.getLimitationConfiguration().setTime(mTimePeriodChooser.getFromTime(), mTimePeriodChooser.getToTime());
    return fav;
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

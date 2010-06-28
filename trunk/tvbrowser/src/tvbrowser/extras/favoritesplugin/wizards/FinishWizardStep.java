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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FinishWizardStep extends AbstractWizardStep {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(FinishWizardStep.class);

  private WizardStep mCaller;

  public FinishWizardStep(WizardStep caller) {
    mCaller = caller;
  }

  public JPanel createContent(WizardHandler handler) {
    JPanel pn = new JPanel(new FormLayout("fill:default:grow", "fill:pref:grow"));
    CellConstraints cc = new CellConstraints();
    pn.add(new JLabel(mLocalizer.msg("msg", "Die Lieblingssendung ist nun eingerichtet!"), SwingConstants.CENTER), cc.xy(1,1));
    handler.allowCancel(false);
    final WizardDlg dialog=handler.getDialog();
    pn.addFocusListener(new FocusAdapter() {

        public void focusGained(FocusEvent e) {
          dialog.focusFinish();
        }
      });
    return pn;
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Done");
  }

  public Object createDataObject(Object obj) {
    return obj;
  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_BACK, WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL};
  }

  public WizardStep next() {
    return null;
  }

  public WizardStep back() {
    return mCaller;
  }

  public boolean isValid() {
    return true;
  }

}

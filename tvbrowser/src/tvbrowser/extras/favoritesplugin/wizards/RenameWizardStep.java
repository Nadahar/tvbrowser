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
 *     $Date: 2006-04-08 20:05:42 +0200 (Sat, 08 Apr 2006) $
 *   $Author: darras $
 * $Revision: 2090 $
 */

package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tvbrowser.extras.favoritesplugin.core.Favorite;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RenameWizardStep extends AbstractWizardStep {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(RenameWizardStep.class);

  private JTextField mNameTf;

  private WizardStep mCaller;

  public RenameWizardStep(WizardStep caller) {
    super();
    mCaller = caller;
  }

  protected JPanel createContent(WizardHandler handler) {

    mNameTf = new JTextField(((Favorite)handler.getCurrentValue()).getName());


    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("5dlu, pref, 3dlu, default:grow",
        "pref"));
    panelBuilder.setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();
    panelBuilder.add(new JLabel(mLocalizer.msg("saveAs", "Save as:")), cc.xy(2,1));
    panelBuilder.add(mNameTf, cc.xy(4,1));

    JPanel panel = panelBuilder.getPanel();
    panel.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          mNameTf.requestFocusInWindow();
        }
      });
    return panel;
  }

 public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_BACK, WizardStep.BUTTON_NEXT};
  }

  public String getTitle() {
    return mLocalizer.msg("title","name");
  }

  public Object createDataObject(Object obj) {
    ((Favorite)obj).setName(mNameTf.getText());
    return obj;
  }

  public WizardStep next() {
    return new FinishWizardStep(this);
  }

  public WizardStep back() {
    return mCaller;
  }

  public boolean isValid() {
    String name = mNameTf.getText();
    return name != null && name.trim().length() > 0;
  }

}

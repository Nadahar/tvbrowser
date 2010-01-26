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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.Favorite;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

public class NotificationWizardStep extends AbstractWizardStep {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(NotificationWizardStep.class);

  private JCheckBox mReminderCb;
  private JCheckBox mCheckOnUpdateCb;
  private Program mProgram;
  private WizardStep mCaller;


  public NotificationWizardStep(WizardStep caller, Program prog) {
    mProgram = prog;
    mCaller = caller;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Notification");
  }

  public JPanel createContent(WizardHandler handler) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref",
                    "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    panelBuilder.setBorder(Borders.DLU4_BORDER);
    panelBuilder.add(new JLabel(mLocalizer.msg("mainQuestion","Wollen Sie automatisch auf diese Sendung hingewiesen werden?")), cc.xy(1,1));
    panelBuilder.add(mReminderCb = new JCheckBox(mLocalizer.msg("option.remind","Automatisch an diese Sendung erinnern.")), cc.xy(1,3));
    panelBuilder.add(mCheckOnUpdateCb = new JCheckBox(mLocalizer.msg("option.checkAfterUpdate","Sofort alarmieren, wenn die Sendung nach einer Aktualisierung gefunden wird.")), cc.xy(1,5));

    mReminderCb.setSelected(FavoritesPlugin.getInstance().isAutoSelectingReminder());
    JPanel result = panelBuilder.getPanel();
    result.addFocusListener(new FocusAdapter() {

      public void focusGained(FocusEvent e) {
        mReminderCb.requestFocusInWindow();
      }
    });
    return result;
  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite)obj;
    fav.setRemindAfterDownload(mCheckOnUpdateCb.isSelected());
    if (mReminderCb.isSelected()) {
      fav.getReminderConfiguration().setReminderServices(new String[]{ReminderConfiguration.REMINDER_DEFAULT});
    }
    else {
      fav.getReminderConfiguration().setReminderServices(new String[]{});
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

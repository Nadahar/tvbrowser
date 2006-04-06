package tvbrowser.extras.favoritesplugin.wizards;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.PanelBuilder;

import javax.swing.*;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import devplugin.Program;

public class NotificationWizardStep implements WizardStep {

  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(NotificationWizardStep.class);

  private JCheckBox mReminderCb;
  private JCheckBox mCheckOnUpdateCb;
  private Program mProgram;


  public String getTitle() {
    return mLocalizer.msg("title","Notification");
  }

  public NotificationWizardStep(Program prog) {
    mProgram = prog;
  }

  public JPanel getContent(WizardHandler handler) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref",
                    "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    panelBuilder.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    panelBuilder.add(new JLabel(mLocalizer.msg("mainQuestion","Wollen Sie automatisch auf diese Sendung hingewiesen werden?")), cc.xy(1,1));
    panelBuilder.add(mReminderCb = new JCheckBox(mLocalizer.msg("option.remind","Automatisch an diese Sendung erinnern.")), cc.xy(1,3));
    panelBuilder.add(mCheckOnUpdateCb = new JCheckBox(mLocalizer.msg("option.checkAfterUpdate","Sofort alarmieren, wenn die Sendung nach einer Aktualisierung gefunden wird.")), cc.xy(1,5));

    mReminderCb.setSelected(true);
    return panelBuilder.getPanel();

  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite)obj;
    fav.setRemindAfterDownload(mCheckOnUpdateCb.isSelected());
    if (mReminderCb.isSelected()) {
      fav.getReminderConfiguration().setReminderServices(new String[]{"reminder"});
    }
    return fav;
  }

  public WizardStep next() {

    if (mReminderCb.isSelected()) {
      return new ReminderWizardStep(mProgram);
    }
    return new LimitationsWizardStep(mProgram);
  }

  public boolean isValid() {
    return true;
  }

  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_NEXT};
  }
  

}

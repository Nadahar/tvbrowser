package tvbrowser.extras.favoritesplugin.wizards;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.PanelBuilder;

import javax.swing.*;

import devplugin.Program;

public class LimitationsWizardStep implements WizardStep {

  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(LimitationsWizardStep.class);

  private JCheckBox mChannelCb;
  private JCheckBox mTimeCb;
  private Program mProgram;

  public LimitationsWizardStep(Program program) {
    mProgram = program;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Limitations");
  }

  public JPanel getContent(WizardHandler handler) {
    CellConstraints cc = new CellConstraints();
        PanelBuilder panelBuilder = new PanelBuilder(
                new FormLayout(
                    "pref",
                    "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));


        panelBuilder.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panelBuilder.add(new JLabel(mLocalizer.msg("mainQuestion","Gibt es weitere Einschränkungen?")), cc.xy(1,1));
        panelBuilder.add(mChannelCb = new JCheckBox(mLocalizer.msg("limitByChannel","Ich möchte die Sendung nur auf bestimmten Sendern sehen")), cc.xy(1,3));
        panelBuilder.add(mTimeCb = new JCheckBox(mLocalizer.msg("limitByTime","Ich möchte die Sendung nur zu bestimmten Zeiten sehen")), cc.xy(1,5));

    return panelBuilder.getPanel();

  }

  public Object createDataObject(Object obj) {
    return obj;
  }

  public WizardStep next() {
    if (mChannelCb.isSelected()) {
      if (mTimeCb.isSelected()) {
        return new LimitChannelWizardStep(new LimitTimeWizardStep(mProgram), mProgram);
      }
      else {
        return new LimitChannelWizardStep(mProgram);
      }
    }
    else if (mTimeCb.isSelected()) {
      return new LimitTimeWizardStep(mProgram);
    }
    return new FinishWizardStep();
  }

  public boolean isValid() {
    return true;
  }

  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_NEXT};
  }
  

}

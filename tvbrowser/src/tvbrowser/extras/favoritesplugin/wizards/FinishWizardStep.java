package tvbrowser.extras.favoritesplugin.wizards;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FinishWizardStep extends AbstractWizardStep {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FinishWizardStep.class);

  private WizardStep mCaller;

  public FinishWizardStep(WizardStep caller) {
    mCaller = caller;
  }

  public JPanel createContent(WizardHandler handler) {
    JPanel pn = new JPanel(new FormLayout("fill:pref:grow", "fill:pref:grow"));
    CellConstraints cc = new CellConstraints();
    pn.add(new JLabel(mLocalizer.msg("msg", "Die Lieblingssendung ist nun eingerichtet!"), JLabel.CENTER), cc.xy(1,1));
    handler.allowCancel(false);
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

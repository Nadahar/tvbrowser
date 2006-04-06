package tvbrowser.extras.favoritesplugin.wizards;

import javax.swing.*;

public class FinishWizardStep implements WizardStep {

  public static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(FinishWizardStep.class);


  public JPanel getContent(WizardHandler handler) {


    JPanel pn = new JPanel();
    pn.add(new JLabel(mLocalizer.msg("msg","Die Lieblingssendung ist nun eingerichtet!")));
    handler.allowCancel(false);
    return pn;

  }

  public String getTitle() {
    return mLocalizer.msg("title","Done");
  }

  public Object createDataObject(Object obj) {
    return obj;
  }

  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE};
  }

  public WizardStep next() {
    return null;
  }

  public boolean isValid() {
    return true;
  }

}

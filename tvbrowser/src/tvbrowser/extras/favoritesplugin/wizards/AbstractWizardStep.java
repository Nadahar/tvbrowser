package tvbrowser.extras.favoritesplugin.wizards;

import javax.swing.*;

public abstract class AbstractWizardStep implements WizardStep {


  private JPanel mContent;

  protected abstract JPanel createContent(WizardHandler handler);

  public JPanel getContent(WizardHandler handler) {
    if (mContent == null) {
      mContent = createContent(handler);
    }
    return mContent;
  }

}

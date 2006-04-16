package tvbrowser.extras.favoritesplugin.wizards;


import javax.swing.*;

public interface WizardStep {

  public final int BUTTON_NEXT = 1;
  public final int BUTTON_DONE = 2;
  public final int BUTTON_BACK = 3;
  public final int BUTTON_CANCEL = 4;


  public int[] getButtons();

  public String getTitle();

  public JPanel getContent(WizardHandler handler);

  public Object createDataObject(Object obj);

  public WizardStep next();

  public WizardStep back();

  public boolean isValid();

}

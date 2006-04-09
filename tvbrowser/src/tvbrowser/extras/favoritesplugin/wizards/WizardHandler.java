package tvbrowser.extras.favoritesplugin.wizards;

import util.ui.UiUtilities;

import java.awt.*;


public class WizardHandler {

  private Container mParent;
  private WizardStep mStep;
  private WizardDlg mWizardDialog;

  public WizardHandler(Container parent, WizardStep initialStep) {
    mParent = parent;
    mStep = initialStep;
  }


  public Object show() {
    WizardStep currentStep = mStep;
    int result;
    Object obj = null;

    if (mParent instanceof Frame) {
      mWizardDialog = new WizardDlg((Frame)mParent, this, currentStep);
    }
    else {
      mWizardDialog = new WizardDlg((Dialog)mParent, this, currentStep);
    }
    UiUtilities.centerAndShow(mWizardDialog);
    result = mWizardDialog.getResult();
    if (result == WizardDlg.FINISH) {
      obj = mWizardDialog.getDataObject();
    }
    return obj;
  }


  
  public void allowNext(boolean allow) {
    mWizardDialog.allowNext(allow);
  }

  public void allowFinish(boolean allow) {
    mWizardDialog.allowFinish(allow);
  }

  public void allowCancel(boolean allow) {
    mWizardDialog.allowCancel(allow);
  }


  public void closeCurrentStep() {
    if (mWizardDialog != null) {
      mWizardDialog.close();
    }
  }


}

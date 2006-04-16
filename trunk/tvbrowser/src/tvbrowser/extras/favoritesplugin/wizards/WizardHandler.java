package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import util.ui.UiUtilities;

public class WizardHandler {

  private Component mParent;

  private WizardStep mStep;

  private WizardDlg mWizardDialog;

  public WizardHandler(Component parent, WizardStep initialStep) {
    mParent = parent;
    mStep = initialStep;
  }

  public Object show() {
    WizardStep currentStep = mStep;
    int result;
    Object obj = null;

    if (mParent instanceof Frame) {
      mWizardDialog = new WizardDlg((Frame) mParent, this, currentStep);
    } else {
      mWizardDialog = new WizardDlg((Dialog) mParent, this, currentStep);
    }
    UiUtilities.centerAndShow(mWizardDialog);
    result = mWizardDialog.getResult();
    if (result == WizardDlg.FINISH) {
      obj = mWizardDialog.getDataObject();
    }
    return obj;
  }

  public void allowNext(boolean allow) {
    if (mWizardDialog != null) {
      mWizardDialog.allowNext(allow);
    }
  }

  public void allowFinish(boolean allow) {
    if (mWizardDialog != null) {
      mWizardDialog.allowFinish(allow);
    }
  }

  public void allowCancel(boolean allow) {
    if (mWizardDialog != null) {
      mWizardDialog.allowCancel(allow);
    }
  }

  public void closeCurrentStep() {
    if (mWizardDialog != null) {
      mWizardDialog.close();
    }
  }

}

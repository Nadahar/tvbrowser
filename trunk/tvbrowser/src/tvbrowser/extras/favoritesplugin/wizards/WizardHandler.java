package tvbrowser.extras.favoritesplugin.wizards;

import util.ui.UiUtilities;

import java.awt.*;


public class WizardHandler {

  private Container mParent;
  private WizardStep mStep;
  private WizardDialog mCurrentDialog;

  public WizardHandler(Container parent, WizardStep initialStep) {
    mParent = parent;
    mStep = initialStep;
  }

  public Object show() {
    WizardStep currentStep = mStep;
    int result;
    Object obj = null;
    do {
      if (mParent instanceof Frame) {
        mCurrentDialog = new WizardDialog((Frame)mParent, this, currentStep);
      }
      else {
        mCurrentDialog = new WizardDialog((Dialog)mParent, this, currentStep);
      }
      UiUtilities.centerAndShow(mCurrentDialog);
      result = mCurrentDialog.getResult();

      if (result == WizardDialog.NEXT) {
        obj = currentStep.createDataObject(obj);
        currentStep = currentStep.next();
      }
      else if (result == WizardDialog.FINISH) {
        obj = currentStep.createDataObject(obj);
      }
    }while (result == WizardDialog.NEXT);

    if (result == WizardDialog.FINISH) {
      return obj;
    }
    else {
      return null;
    }
  }
  
  public void allowNext(boolean allow) {
    mCurrentDialog.allowNext(allow);
  }

  public void allowFinish(boolean allow) {
    mCurrentDialog.allowFinish(allow);
  }

  public void allowCancel(boolean allow) {
    mCurrentDialog.allowCancel(allow);
  }


  public void closeCurrentStep() {
    if (mCurrentDialog != null) {
      mCurrentDialog.close();
    }
  }


}

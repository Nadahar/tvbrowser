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

import java.awt.Window;

import util.ui.UiUtilities;

public class WizardHandler {

  private Window mParent;

  private WizardStep mStep;

  private WizardDlg mWizardDialog;
  
  private boolean mAllowNext;
  private boolean mAllowFinish;

  public WizardHandler(Window parent, WizardStep initialStep) {
    mParent = parent;
    mStep = initialStep;
    mAllowNext = true;
    mAllowFinish = true;
  }

  public Object show() {
    WizardStep currentStep = mStep;
    int result;
    Object obj = null;

    mWizardDialog = new WizardDlg(mParent, this, currentStep);
    
    changeDoneBtnText();
    allowNext(mAllowNext);
    allowFinish(mAllowFinish);
    
    UiUtilities.centerAndShow(mWizardDialog);
    result = mWizardDialog.getResult();
    if (result == WizardDlg.FINISH) {
      obj = mWizardDialog.getDataObject();
    }
    
    mWizardDialog.dispose();
    mWizardDialog = null;
    
    return obj;
  }

  public Object getCurrentValue() {
    if (mWizardDialog != null) {
      return mWizardDialog.getDataObject();
    }
    else {
      return null;
    }
  }

  public void allowNext(boolean allow) {
    mAllowNext = allow;
    
    if (mWizardDialog != null) {
      mWizardDialog.allowNext(allow);
    }
  }

  public void allowFinish(boolean allow) {
    mAllowFinish = allow;
    
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
  
  public void changeDoneBtnText() {
    if(mWizardDialog != null) {
      mWizardDialog.setDoneBtnText();
    }
  }
  
  public WizardDlg getDialog() {
	  return mWizardDialog;
  }
}

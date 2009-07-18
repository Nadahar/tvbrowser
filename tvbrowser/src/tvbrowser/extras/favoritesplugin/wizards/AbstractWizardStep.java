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
 *     $Date: 2006-04-08 20:05:42 +0200 (Sat, 08 Apr 2006) $
 *   $Author: darras $
 * $Revision: 2090 $
 */

package tvbrowser.extras.favoritesplugin.wizards;

import javax.swing.JPanel;

public abstract class AbstractWizardStep implements WizardStep {

  private JPanel mContent;

  protected abstract JPanel createContent(WizardHandler handler);

  public JPanel getContent(WizardHandler handler) {
    if (mContent == null) {
      mContent = createContent(handler);
    }
    return mContent;
  }
  
  public boolean isSingleStep() {
    return false;
  }
  
  public String getDoneBtnText() {
    return WizardDlg.mLocalizer.msg("done","Done");
  }

}

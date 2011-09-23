/*
* TV-Browser
* Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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

package tvbrowser.ui.settings.tablebackgroundstyles;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
/**
 * @author René Mach
 */
public class UiTimeBlockBackgroundStyle implements TableBackgroundStyle {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(UiTimeBlockBackgroundStyle.class);
  
  private JSpinner mTimeBlockSizeSp;
  private JCheckBox mTimeBlockShowWestChB;

  private JPanel mContent;

  public UiTimeBlockBackgroundStyle() {

  }

  public boolean hasContent() {
    return true;
  }

  public JPanel createSettingsContent() {
    mTimeBlockSizeSp = new JSpinner(new SpinnerNumberModel(Settings.propTimeBlockSize.getInt(), 1, 23, 1));
    mTimeBlockShowWestChB = new JCheckBox(TimeBlockBackgroundStyle.mLocalizer.msg("timeBlock.showWest", "Show left border"), Settings.propTimeBlockShowWest.getBoolean());
    
    mContent = new JPanel(new FormLayout("default,5dlu,default:grow","default,2dlu,default"));
    mContent.add(new JLabel(TimeBlockBackgroundStyle.mLocalizer.msg("timeBlock.blockSize", "Block size")), CC.xy(1,1));
    mContent.add(mTimeBlockSizeSp, CC.xy(3,1));
    mContent.add(mTimeBlockShowWestChB, CC.xyw(1,3,3));
    
    return mContent;
  }
  
  public void storeSettings() {
    if (mContent == null) {
      return;
    }
    
    Integer blockSize = (Integer) mTimeBlockSizeSp.getValue();
    Settings.propTimeBlockSize.setInt(blockSize.intValue());
    Settings.propTimeBlockShowWest.setBoolean(mTimeBlockShowWestChB.isSelected());
  }

  public String getName() {
    return mLocalizer.msg("style","Theme color time block");
  }


  public String toString() {
    return getName();
  }

  public String getSettingsString() {
    return "uiTimeBlock";
  }

}

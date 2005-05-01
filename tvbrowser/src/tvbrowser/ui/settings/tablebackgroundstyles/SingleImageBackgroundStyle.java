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

package tvbrowser.ui.settings.tablebackgroundstyles;

import util.ui.TabLayout;

import javax.swing.*;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.ProgramTableSettingsTab;


/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 30.04.2005
 * Time: 17:47:57
 */
public class SingleImageBackgroundStyle implements TableBackgroundStyle {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SingleImageBackgroundStyle.class);

  private JTextField mOneImageBackgroundTF;

  private JPanel mContent;


  public SingleImageBackgroundStyle() {

  }

  public boolean hasContent() {
    return true;
  }

  public JPanel createSettingsContent() {
    mContent = new JPanel(new TabLayout(3));

    mContent.add(new JLabel(mLocalizer.msg("oneImage.image", "Image")));
    mOneImageBackgroundTF = new JTextField(Settings.propOneImageBackground.getString(), 25);
    mContent.add(mOneImageBackgroundTF);
    mContent.add(ProgramTableSettingsTab.createBrowseButton(mContent, mOneImageBackgroundTF));


    return mContent;
  }

  public void storeSettings() {
    if (mContent != null) {
      Settings.propOneImageBackground.setString(mOneImageBackgroundTF.getText());
    }
  }

  public String getName() {
    return mLocalizer.msg("style","One image");
  }


  public String toString() {
    return getName();
  }

  public String getSettingsString() {
    return "oneImage";
  }

}

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

package tvbrowser.ui.settings.util;

import util.ui.AlphaColorChooser;
import util.ui.UiUtilities;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

import tvbrowser.ui.settings.ProgramTableSettingsTab;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.05.2005
 * Time: 14:22:05
 */
public
class ColorButton extends JButton {

  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ProgramTableSettingsTab.class);

  public ColorButton(final ColorLabel lb) {
    super(mLocalizer.msg("change","Change"));
    addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Color newColor = AlphaColorChooser.showDialog(UiUtilities.getBestDialogParent(getParent()),
                mLocalizer.msg("ChooseColor", "Please choose the Color"), lb.getColor());
        lb.setColor(newColor);

      }
    });
  }



}

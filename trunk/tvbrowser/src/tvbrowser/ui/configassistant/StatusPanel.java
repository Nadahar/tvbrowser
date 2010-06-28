/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.ui.configassistant;

import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The Step-List on top of the Wizard
 */
public class StatusPanel extends JPanel {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(StatusPanel.class);

  protected static final int NETWORK = 0;
  protected static final int CHANNELS = 1;
  protected static final int READY = 2;
  
  private Font mNormalFont;

  public StatusPanel(int selected) {
    setLayout(new FormLayout("10dlu, pref, pref:grow, pref, pref:grow, pref, pref:grow, pref, 20dlu", "3dlu, pref, 3dlu, 1px"));
    CellConstraints cc = new CellConstraints();
    
    JEditorPane textfield = new JEditorPane();
    
    setBackground(textfield.getBackground());
    setForeground(textfield.getForeground());
    setOpaque(true);
    setFont(textfield.getFont());

    mNormalFont = textfield.getFont();
    mNormalFont = mNormalFont.deriveFont(Font.BOLD, mNormalFont.getSize()+5);
    
    String text = "";
    if (selected == NETWORK) {
      text = mLocalizer.msg("network", "Network");
    }
    else if (selected == CHANNELS) {
      text = mLocalizer.msg("channel", "Choose channel");
    }
    else if (selected == READY) {
      text = mLocalizer.msg("done", "Done");
    }
    JLabel label = new JLabel();
    label.setText(mLocalizer.msg("title","Step {0} of {1}: {2}", selected + 1, READY + 1, text));
    label.setFont(mNormalFont);
    add(label, cc.xy(2,2));
    
    JPanel black = new JPanel();
    black.setBackground(textfield.getForeground());
    add(black, cc.xyw(1,4,9));
  }
}
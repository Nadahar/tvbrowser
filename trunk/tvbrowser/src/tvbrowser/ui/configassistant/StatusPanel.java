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

import util.ui.LineComponent;
import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The Step-List on top of the Wizard
 */
public class StatusPanel extends JPanel {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(StatusPanel.class);

  public static final int NETWORK = 0;
  public static final int CHANNELS = 1;
  public static final int PICTURES = 2;
  public static final int READY = 3;
  
  private Font mItalic;
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
    mNormalFont = mNormalFont.deriveFont(Font.BOLD, mNormalFont.getSize()+1);
    
    mItalic = mNormalFont.deriveFont(Font.BOLD, mNormalFont.getSize()+5);
    
    add(createStatusLabel("1. "+mLocalizer.msg("network", "Network"), selected == NETWORK), cc.xy(2,2));
    add(new LineComponent(textfield.getForeground()), cc.xy(3,2));
    add(createStatusLabel("2. "+mLocalizer.msg("channel", "Choose channel"), selected == CHANNELS), cc.xy(4,2));
    add(new LineComponent(textfield.getForeground()), cc.xy(5,2));
    add(createStatusLabel("3. "+Localizer.getLocalization(Localizer.I18N_PICTURES), selected == PICTURES), cc.xy(6,2));
    add(new LineComponent(textfield.getForeground()), cc.xy(7,2));
    add(createStatusLabel("3. "+mLocalizer.msg("done", "Done"), selected == READY), cc.xy(8,2));
    
    JPanel black = new JPanel();
    black.setBackground(textfield.getForeground());
    add(black, cc.xyw(1,4,9));
  }

  private JLabel createStatusLabel(String string, boolean selected) {
    JLabel label = new JLabel();
    label.setText((selected)?"<html><u>"+string+"</u></html>":string);
    label.setFont((selected)?mItalic:mNormalFont);
    return label;
  }

}
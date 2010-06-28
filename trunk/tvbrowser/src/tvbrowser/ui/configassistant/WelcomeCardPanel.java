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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Welcome-Card of the Wizard
 */
class WelcomeCardPanel extends AbstractCardPanel {
  private JPanel mContent;
  
  private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(WelcomeCardPanel.class);
 
  public WelcomeCardPanel(PrevNextButtons btns) {
    super(btns);
    mContent=new JPanel(new FormLayout("pref, 10dlu, fill:200dlu:grow", "fill:pref:grow, top:pref, fill:pref:grow"));
    
    JEditorPane textfield = new JEditorPane();
    
    JLabel icon = new JLabel(new ImageIcon("imgs/tvbrowser128.png"));
    icon.setOpaque(false);
    
    mContent.setBackground(textfield.getBackground());
    mContent.setForeground(textfield.getForeground());
    mContent.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

    CellConstraints cc = new CellConstraints();
    
    JPanel iconPanel = new JPanel(new BorderLayout());
    iconPanel.add(icon, BorderLayout.NORTH);
    iconPanel.setOpaque(false);
    
    mContent.add(iconPanel, cc.xywh(1, 2, 1, 2));
    mContent.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("welcome","Welcome",tvbrowser.TVBrowser.VERSION.toString().replaceAll("\\s", "&nbsp;")),textfield.getBackground()), cc.xy(3, 2));
  }
  
  public JPanel getPanel() {
    return mContent;
  }
}
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

import javax.swing.*;
import java.awt.Font;

class ProxyQuestionCardPanel extends AbstractCardPanel {
  
  private JPanel mContent;
  private JRadioButton mYesRadio, mNoRadio;
  private CardPanel mProxyPanel;
  
  private static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(ProxyQuestionCardPanel.class); 
 
  
  public ProxyQuestionCardPanel(PrevNextButtons btns, CardPanel proxyPanel) {
    super(btns);
    mProxyPanel=proxyPanel;
    proxyPanel.setPrev(this);
    mContent=new JPanel();
    mContent.setLayout(new BoxLayout(mContent,BoxLayout.Y_AXIS));
    
    JLabel area=new JLabel();
    area.setFont(new Font("SansSerif", Font.PLAIN, 12)); 
    area.setText(mLocalizer.msg("description","description"));
    
    mContent.add(area);
    
    
    
    mNoRadio=new JRadioButton(mLocalizer.msg("no","no"));
    mYesRadio=new JRadioButton(mLocalizer.msg("yes","yes"));
    
    mNoRadio.setSelected(true);
    
    ButtonGroup btnGroup=new ButtonGroup();
    btnGroup.add(mNoRadio);
    btnGroup.add(mYesRadio);
        
    mContent.add(new JLabel(mLocalizer.msg("configProxy","do you want to configure proxy?")));
    mContent.add(mNoRadio);
    mContent.add(mYesRadio);
    
  }
  
  public JPanel getPanel() {
    return mContent;
  }
  
  public CardPanel getNext() {
    if (mYesRadio.isSelected()) {
      return mProxyPanel;
    }
    return super.getNext();
  }
  
}

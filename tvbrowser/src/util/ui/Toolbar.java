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
 

package util.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


public class Toolbar extends JComponent implements ComponentListener, ActionListener {
  
  private JButton[] mButtons;  
  private JPanel mButtonPanel;
  private JButton mGroupBtn;
  private JPopupMenu popupMenu;
  private JPanel mGroupBtnPanel;
  private static final int GROUP_BUTTON_WIDTH = 24;
  
  public static final int TEXT=1;
  public static final int ICON=2;
  
  private int mButtonWidth;
  
  public Toolbar() {
    
    setLayout(new BorderLayout());
    mButtonPanel = new JPanel(new GridLayout(1,0));
    mButtonPanel.setFocusable(false);
    mGroupBtnPanel = new JPanel(new BorderLayout());
    
    mGroupBtn = new JButton(">>");
    mGroupBtn.setBorder(BorderFactory.createEmptyBorder());
    
    mGroupBtnPanel.add(mGroupBtn, BorderLayout.WEST);
    mGroupBtn.setPreferredSize(new Dimension(GROUP_BUTTON_WIDTH,0));
    
    popupMenu = new JPopupMenu();
    add(mButtonPanel, BorderLayout.WEST);
    add(mGroupBtnPanel, BorderLayout.CENTER);
    addComponentListener(this);   
    
    
    mGroupBtn.addActionListener(this);    
  
  }
  
  public void actionPerformed(ActionEvent event) {
    int x = this.getX() + mGroupBtnPanel.getX() + mGroupBtn.getWidth();
    int y = this.getY() + mGroupBtnPanel.getY() + mGroupBtn.getHeight()/2;
    if (event.getSource() == mGroupBtn) {
      popupMenu.show(this, x, y);
    }
  }
  
  public void setOpaque(boolean opaque) {
    super.setOpaque(opaque);
    mButtonPanel.setOpaque(opaque);
    mGroupBtn.setOpaque(opaque);
    mGroupBtnPanel.setOpaque(opaque);
  }
  
  public void setButtons(JButton[] buttons, int style) {
    mButtons = buttons;
    mButtonWidth=110;
    for (int i=0; i<mButtons.length; i++) {
      mButtons[i].setPreferredSize(new Dimension(mButtonWidth,0));
    }
    
    if ((style&TEXT) != TEXT) {  // hide button titles
      System.out.println("hide text");
      mButtonWidth=40;
      for (int i=0; i<mButtons.length; i++) {
        mButtons[i].setPreferredSize(new Dimension(mButtonWidth,0));
        String text = mButtons[i].getText();
        mButtons[i].setText(null);
        mButtons[i].setToolTipText(text);
      }
    }
    
    if ((style&ICON) != ICON) {  // hide button icon
      for (int i=0; i<mButtons.length; i++) {
        mButtons[i].setPreferredSize(new Dimension(mButtonWidth,0));
        mButtons[i].setIcon(null);
      }
    }
   
   
    updateToolbar();
  }
  
 
   
  public void updateToolbar() {
    mButtonPanel.removeAll();
    popupMenu.removeAll();
    if (mButtons == null || mButtons.length<1) {
      return;
    }
    int buttonWidth = mButtons[0].getWidth();
    buttonWidth = mButtonWidth;
    int visibleBtnsCount;
    if (mButtons.length * buttonWidth > getWidth()) {
      visibleBtnsCount = (getWidth()- GROUP_BUTTON_WIDTH) / buttonWidth;
			if (visibleBtnsCount < 0) {
				visibleBtnsCount=0;
			}
    }
    else {
      visibleBtnsCount = mButtons.length;
    }  
    
    
    for (int i=0; i<visibleBtnsCount; i++) {
      mButtonPanel.add(mButtons[i]);
    }
    for (int i=visibleBtnsCount; i<mButtons.length; i++) {
      JButton btn = mButtons[i];
      
      JMenuItem item = new JMenuItem(btn.getText());
      item.setIcon(btn.getIcon());
      popupMenu.add(item);
      ActionListener[] listeners = btn.getActionListeners();
      for (int inx=0; inx<listeners.length; inx++) {
        item.addActionListener(listeners[inx]);
      }
    }
    if (popupMenu.getComponentCount()>0) {
      add(mGroupBtnPanel, BorderLayout.CENTER);
    }
    else {
      remove(mGroupBtnPanel);
    }
    
    validate();
    
  }
  
  

	public void componentHidden(ComponentEvent arg0) {
	}

	public void componentMoved(ComponentEvent arg0) {
	}



	public void componentResized(ComponentEvent arg0) {
		updateToolbar();		
	}



	
	public void componentShown(ComponentEvent arg0) {
	}

	
	
  
}
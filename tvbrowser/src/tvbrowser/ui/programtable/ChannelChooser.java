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

package tvbrowser.ui.programtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.event.MouseInputListener;

import tvbrowser.core.ChannelList;
import tvbrowser.ui.finder.FinderItem;
import devplugin.Channel;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */


public class ChannelChooser extends JPanel implements MouseListener  {

	private JWindow win=null;
	private ScrollableTablePanel tablePanel;
	private JLabel label;
	private Frame parent;

	public ChannelChooser(Frame parent, ScrollableTablePanel tablePanel) {
	   label=new JLabel(new ImageIcon("imgs/down16.gif"));
		add(label);
		addMouseListener(this);
		this.tablePanel=tablePanel;
		this.parent=parent;
	}

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {
		if (win==null) {
			win=new ChannelChooserWindow(parent, tablePanel);
		}
		win.setLocation(getLocationOnScreen());
		win.setVisible(true);
	}
   
	public void mouseClicked(MouseEvent e) {}


}

class ChannelChooserWindow extends JWindow implements MouseInputListener {

	private ScrollableTablePanel tablePanel;
	private JPanel contentPane;
	private ChannelItem curSelectedItem;
	private int visibleRows=20;
	private int upperInx;
	private JLabel upLabel, downLabel;
	private Channel[] list;
	private ChannelItem[] channelItems;
    

	public ChannelChooserWindow(Frame parent, ScrollableTablePanel tablePanel) {
		super(parent);
		this.tablePanel=tablePanel;
        
		contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(BorderFactory.createLineBorder(Color.black));
		JPanel listPane=new JPanel(new GridLayout(0,1));
      
		upLabel=new JLabel(new ImageIcon("imgs/Up16.gif"));
		downLabel=new JLabel(new ImageIcon("imgs/Down16.gif"));
   
		list=ChannelList.getSubscribedChannels();
        
		upperInx=0;
        
		if (list.length<visibleRows) {
			visibleRows=list.length;
		}
        
		channelItems=new ChannelItem[visibleRows];
		listPane.add(upLabel);
		for (int i=0;i<visibleRows;i++) {
			channelItems[i]=new ChannelItem(this,list[i]);
			listPane.add(channelItems[i]);
		}
		listPane.add(downLabel);
		updateList();
        
		contentPane.add(listPane,BorderLayout.CENTER);
		       
		contentPane.addMouseListener(this);
		contentPane.addMouseMotionListener(this);
        
		this.pack();
      
	}
    
	private void updateList() {
        
		for (int i=0;i<visibleRows;i++) {
			channelItems[i].setChannel(list[i+upperInx]);           
		}
        
		upLabel.setEnabled(upperInx!=0);
		downLabel.setEnabled(upperInx<list.length-visibleRows);
        
	}
    
	private JLabel getItem(int x, int y) {
        
		Component comp=contentPane.getComponentAt(x,y);
		comp=comp.getComponentAt(x-comp.getX(),y-comp.getY());
        
		if (comp instanceof JLabel) {
			return (JLabel)comp;
		}
		return null;
        
	}

	public void setChannel(Channel channel) {
		tablePanel.scrollTo(channel);
	}

	public void mouseClicked(MouseEvent e) {
		JLabel item=getItem(e.getX(),e.getY());
		if (item==upLabel) {
			if (upLabel.isEnabled()) {
				upperInx--;
				updateList();
			}
		}
		else if (item==downLabel) {
			if (downLabel.isEnabled()) {
				upperInx++;
				updateList();
			}
		}else if (curSelectedItem!=null) {
			curSelectedItem.mouseClicked();
		}
	}
    
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		setVisible(false);
	}
	public void mouseDragged(MouseEvent e) {
	}
    
	public void mouseMoved(MouseEvent e) {
		JLabel item=getItem(e.getX(),e.getY());
		if (item==null) {
			if (curSelectedItem!=null) {
				curSelectedItem.mouseExited();
			}
			curSelectedItem=null;
		}
        
		else if (item instanceof ChannelItem) {
			ChannelItem chItem=(ChannelItem)item;
			if (chItem!=curSelectedItem) {
				if (curSelectedItem!=null) {
					curSelectedItem.mouseExited();
				}
				curSelectedItem=chItem;
				chItem.mouseEntered();
			}
		}
        
	}
    
    

}

class ChannelItem extends JLabel {
  
	private ChannelChooserWindow mParent;
	private Channel mChannel;
    
    
	public ChannelItem(ChannelChooserWindow parent, Channel channel) {
		super(channel.getName());
        
		mParent = parent;
		mChannel = channel;
        
		setOpaque(false);
	}
    
	public void setChannel(Channel ch) {
		mChannel=ch;
		setText(ch.getName());
	}

	public void mouseClicked() {
		mParent.setChannel(mChannel);
	}
   
   
	public void mouseExited() {
	   setOpaque(false);
	   updateUI();
	}

	public void mouseEntered() {
		setOpaque(true);
		setBackground(FinderItem.MARKED_BG_COLOR);
		updateUI();
	}
}

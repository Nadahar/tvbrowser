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

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import devplugin.Channel;
import tvbrowser.ui.finder.FinderItem;
import tvbrowser.ui.programtable.ScrollableTablePanel;
import tvbrowser.core.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ChannelChooser extends JPanel implements MouseListener {

    private JWindow win=null;
    private ScrollableTablePanel tablePanel;
    private JLabel label;

    public ChannelChooser(ScrollableTablePanel tablePanel) {
       label=new JLabel(new ImageIcon("imgs/down16.gif"));
        add(label);
        addMouseListener(this);
        this.tablePanel=tablePanel;
    }

    public void mousePressed(MouseEvent e) {
        win=new ChannelChooserWindow(tablePanel);
        win.setLocation(getLocationOnScreen());
        win.setVisible(true);
    }
    public void mouseReleased(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
}


class ChannelChooserWindow extends JWindow implements MouseListener {

    private ScrollableTablePanel tablePanel;

    public ChannelChooserWindow(ScrollableTablePanel tablePanel) {
        super();
        this.tablePanel=tablePanel;
        JPanel contentPane=(JPanel)getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel listPane=new JPanel(new GridLayout(0,1,5,0));
        Object[] list=ChannelList.getSubscribedChannels();

        for (int i=0;i<list.length;i++) {
          Channel channel = (Channel) list[i];
          listPane.add(new ChannelItem(this, channel));
        }

        contentPane.add(new JScrollPane(listPane),BorderLayout.CENTER);

        addMouseListener(this);
        setSize(100,200);
    }

    public void setChannel(Channel channel) {
        tablePanel.scrollTo(channel);
    }

    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
        setVisible(false);
    }

}

class ChannelItem extends JLabel implements MouseListener {
  
    private ChannelChooserWindow mParent;
    private Channel mChannel;
    
    
    public ChannelItem(ChannelChooserWindow parent, Channel channel) {
        super(channel.getName());
        
        mParent = parent;
        mChannel = channel;
        
        addMouseListener(this);
        setOpaque(false);
    }

    public void mouseClicked(MouseEvent evt) {
        mParent.setVisible(false);
        mParent.setChannel(mChannel);
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
       setOpaque(false);
       updateUI();
    }

    public void mouseEntered(MouseEvent e) {
      setOpaque(true);
      setBackground(FinderItem.MARKED_BG_COLOR);
      updateUI();
    }
}

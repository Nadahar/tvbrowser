/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.ui.channelpanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import tvbrowser.core.*;

public class ChannelPanel extends JPanel {

    private JScrollPane scrollPane=null;

    public ChannelPanel() {
        int n=ChannelList.getNumberOfSubscribedChannels();
        setLayout(new BorderLayout());

        JPanel content=new JPanel(new GridLayout(1,n));

        scrollPane=new JScrollPane(content);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane,BorderLayout.WEST);

        JLabel []labels=new JLabel[n];
        Enumeration enum=ChannelList.getChannels();
        int colWidth=Settings.getColumnWidth();

        while (enum.hasMoreElements()) {
            Channel ch=(Channel)enum.nextElement();
            // if (ch.isSubscribed()) {
            if (ChannelList.isSubscribedChannel(ch.getId())) {
            	int pos=ChannelList.getPos(ch.getId());
                labels[pos]=new JLabel(ch.getName());
                labels[pos].setOpaque(false);
                labels[pos].setHorizontalAlignment(JLabel.CENTER);
                labels[pos].setPreferredSize(new Dimension(colWidth,15));
            }

        }
        for (int i=0;i<n;i++) {
            if (labels[i]==null) {
                labels[i]=new JLabel("unknown");
            }
            content.add(labels[i]);
        }
    }


    public void scroll(int x) {
    	scrollPane.getViewport().setViewPosition(new Point(x,0));
    	
     
    }
    
   
    
   
}
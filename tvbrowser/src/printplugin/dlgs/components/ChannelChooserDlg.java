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

package printplugin.dlgs.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import util.ui.OrderChooser;

import devplugin.Channel;
import devplugin.Plugin;

public class ChannelChooserDlg extends JDialog {
  
  private Channel[] mChannelArr;
  private OrderChooser mChannelChooser;
  
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ChannelChooserDlg.class);

  
  public ChannelChooserDlg(Frame parent, Channel[] channelArr) {
    
    super(parent,true);
    setTitle(mLocalizer.msg("chooseChannels","choose channels"));
    
    if (channelArr == null) {
      mChannelArr = new Channel[]{};
    }
    else {
      mChannelArr = channelArr;
    }
    
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
    JPanel southPn = new JPanel(new BorderLayout());
    JPanel btnPn = new JPanel();
    
    JButton okBt = new JButton(mLocalizer.msg("ok","OK"));
    JButton cancelBt = new JButton(mLocalizer.msg("cancel","Cancel"));
    
    btnPn.add(okBt);
    btnPn.add(cancelBt);
    southPn.add(btnPn,BorderLayout.NORTH);
    
    JPanel centerPn = new JPanel(new BorderLayout());
    centerPn.add(mChannelChooser = new OrderChooser(mChannelArr, Plugin.getPluginManager().getSubscribedChannels()), BorderLayout.NORTH);
    JLabel lb = new JLabel("<html>" + mLocalizer.msg("infotext.1","Waehlen Sie jene Sender aus, deren Programm ausgedruckt werden soll.")+"</html>");
 
    centerPn.add(lb,BorderLayout.SOUTH);
    
    contentPane.add(centerPn,BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);
    
    
    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Object[] o = mChannelChooser.getOrder();
        mChannelArr = new Channel[o.length];
        for (int i=0;i<o.length;i++) {
          mChannelArr[i]=(Channel)o[i];
        }
        hide();
      }      
      });
      
    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mChannelArr = null;
        hide();
      }      
    });
    
    
    pack();
  }
  
  public Channel[] getChannels(Channel[] channels) {
    
    if (mChannelArr==null) {
      return channels;
    }
    return mChannelArr;
  }
  
}
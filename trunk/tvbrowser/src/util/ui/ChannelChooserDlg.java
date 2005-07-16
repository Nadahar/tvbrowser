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

package util.ui;

import devplugin.Channel;
import devplugin.Plugin;

import javax.swing.*;


import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * The ChannelChooserDlg class provides a Dialog for choosing channels. The user
 * can choose from all subscribed channels.
 */
public class ChannelChooserDlg extends JDialog {

  private Channel[] mResultChannelArr;
  private Channel[] mChannelArr;
  private OrderChooser mChannelChooser;


  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ChannelChooserDlg.class);

  /**
   *
   * @param parent
   * @param channelArr The initially selected channels
   * @param description A description text below the channel list.
   */
  public ChannelChooserDlg(Dialog parent, Channel[] channelArr, String description) {
    super(parent, true);
    init(channelArr, description);
  }

  /**
   *
   * @param parent
   * @param channelArr The initially selected channels
   * @param description A description text below the channel list.
   */
  public ChannelChooserDlg(Frame parent, Channel[] channelArr, String description) {
    super(parent,true);
    init(channelArr, description);
  }

  private void init(Channel[] channelArr, String description) {
    setTitle(mLocalizer.msg("chooseChannels","choose channels"));

    if (channelArr == null) {
      mChannelArr = new Channel[]{};
      mResultChannelArr = new Channel[]{};
    }
    else {
      mChannelArr = channelArr;
      mResultChannelArr = channelArr;
    }

    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JPanel southPn = new JPanel(new BorderLayout());
    JPanel btnPn = new JPanel();

    JButton okBt = new JButton(mLocalizer.msg("OK","OK"));
    JButton cancelBt = new JButton(mLocalizer.msg("Cancel","Cancel"));

    btnPn.add(okBt);
    btnPn.add(cancelBt);
    southPn.add(btnPn,BorderLayout.NORTH);

    JPanel centerPn = new JPanel(new BorderLayout());
    centerPn.add(mChannelChooser = new OrderChooser(mResultChannelArr, Plugin.getPluginManager().getSubscribedChannels()), BorderLayout.NORTH);

    if (description != null) {
      JLabel lb = new JLabel(description);
      centerPn.add(lb,BorderLayout.SOUTH);
    }

    contentPane.add(centerPn,BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);


    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Object[] o = mChannelChooser.getOrder();
        mResultChannelArr = new Channel[o.length];
        for (int i=0;i<o.length;i++) {
          mResultChannelArr[i]=(Channel)o[i];
        }
        hide();
      }
      });

    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mResultChannelArr = null;
        hide();
      }
    });

    pack();
  }

  /**
   *
   * @return an array of the selected channels. If the user cancelled the dialog,
   * the array from the constructor call is returned.
   */
  public Channel[] getChannels() {

    if (mResultChannelArr==null) {
      return mChannelArr;
    }
    return mResultChannelArr;
  }

}
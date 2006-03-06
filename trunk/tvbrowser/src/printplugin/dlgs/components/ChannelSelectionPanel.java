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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import util.ui.ChannelChooserDlg;
import devplugin.Channel;



/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 06.02.2005
 * Time: 21:11:24
 */
public class ChannelSelectionPanel extends JPanel {

    private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ChannelSelectionPanel.class);

  private JRadioButton mAllChannelsRb, mSelectedChannelsRb;
  private JButton mChangeSelectedChannelsBt;
  private Channel[] mChannels;

  public ChannelSelectionPanel(final Frame dlgParent, Channel[] channels) {
    mChannels = channels;

    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("channels","Kanaele")));

    JPanel allChannelsPn = new JPanel(new BorderLayout());
    allChannelsPn.add(mAllChannelsRb=new JRadioButton(mLocalizer.msg("all","Alle")));

    JPanel channelSelectionPn = new JPanel(new BorderLayout());
    channelSelectionPn.add(mSelectedChannelsRb=new JRadioButton(),BorderLayout.WEST);
    channelSelectionPn.add(mChangeSelectedChannelsBt=new JButton(mLocalizer.msg("change","Aendern")+"..."),BorderLayout.EAST);

    ButtonGroup group = new ButtonGroup();
    group.add(mAllChannelsRb);
    group.add(mSelectedChannelsRb);

    add(allChannelsPn);
    add(channelSelectionPn);

    updateSelectedChannelsPanel();

    mChangeSelectedChannelsBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        ChannelChooserDlg dlg = new ChannelChooserDlg(dlgParent, mChannels,"<html>" + mLocalizer.msg("infotext.1","Waehlen Sie jene Sender aus, deren Programm ausgedruckt werden soll.")+"</html>");
        util.ui.UiUtilities.centerAndShow(dlg);
        mChannels = dlg.getChannels();
        updateSelectedChannelsPanel();
      }
    });

    mAllChannelsRb.setSelected(true);

  }

  private void updateSelectedChannelsPanel() {
    String radioBtnText = mLocalizer.msg("selectedChannels","Ausgewaehlte");
    if (mChannels != null) {
      radioBtnText+=" ("+ mLocalizer.msg("selectedChannelsCnt","{0} channels selected",""+mChannels.length)+")";
    }
    mSelectedChannelsRb.setText(radioBtnText);
  }

  public Channel[] getChannels() {
    if (mAllChannelsRb.isSelected()) {
      return null;
    }
    return mChannels;
  }

  public void setChannels(Channel[] channels) {
    mChannels = channels;
    if (mChannels == null) {
      mAllChannelsRb.setSelected(true);
    }
    else {
      mSelectedChannelsRb.setSelected(true);
    }
    updateSelectedChannelsPanel();
  }

}

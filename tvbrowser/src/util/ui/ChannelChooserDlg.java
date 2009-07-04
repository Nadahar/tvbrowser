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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Channel;
import devplugin.Plugin;

/**
 * The ChannelChooserDlg class provides a Dialog for choosing channels. The user
 * can choose from all subscribed channels.
 */
public class ChannelChooserDlg extends JDialog implements WindowClosingIf {

  private Channel[] mResultChannelArr;
  private Channel[] mChannelArr;
  private OrderChooser mChannelOrderChooser;
  private SelectableItemList mChannelItemList;
  
  /**
   * If this Dialog should contain an OrderChooser use this type.
   */
  public static final int ORDER_CHOOSER = 0;
  
  /**
   * If this Dialog should contain a SelectableItemList use this type.
   */
  public static final int SELECTABLE_ITEM_LIST = 1;

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ChannelChooserDlg.class);

  /**
   * 
   * @param parent
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @since 3.0
   */
  public ChannelChooserDlg(Window parent, Channel[] channelArr,
      String description) {
    super(parent);
    setModal(true);
    init(channelArr, description, ORDER_CHOOSER);
  }

  /**
   * 
   * @param parent
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @deprecated since 3.0
   */
  public ChannelChooserDlg(Dialog parent, Channel[] channelArr, String description) {
    this((Window) parent, channelArr, description);
  }

  /**
   * 
   * @param parent
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @deprecated since 3.0
   */
  public ChannelChooserDlg(Frame parent, Channel[] channelArr, String description) {
    this((Window) parent, channelArr, description);
  }

  /**
   * 
   * @param parent
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @param type
   *          The type of this ChannelChooser
   * @since 3.0
   */
  public ChannelChooserDlg(Window parent, Channel[] channelArr,
      String description, int type) {
    super(parent);
    setModal(true);
    init(channelArr, description, type);
  }

  /**
   * 
   * @param parent
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @param type
   *          The type of this ChannelChooser
   * @deprecated since 3.0
   */
 public ChannelChooserDlg(Dialog parent, Channel[] channelArr, String description, int type) {
   this((Window) parent, channelArr, description, type);
 }

  /**
   * 
   * @param parent
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @param type
   *          The type of this ChannelChooser
   * @deprecated since 3.0
   */
 public ChannelChooserDlg(Frame parent, Channel[] channelArr, String description, int type) {
   this((Window) parent, channelArr, description, type);
 }

  private void init(Channel[] channelArr, String description, int type) {
    setTitle(mLocalizer.msg("chooseChannels","choose channels"));
    UiUtilities.registerForClosing(this);
    
    if (channelArr == null) {
      mChannelArr = new Channel[]{};
      mResultChannelArr = new Channel[]{};
    }
    else {
      mChannelArr = channelArr;
      mResultChannelArr = channelArr;
    }

    JPanel contentPane = (JPanel)getContentPane();
    FormLayout layout = new FormLayout("fill:pref:grow", "");
    contentPane.setLayout(layout);
    contentPane.setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();
    
    if(type == ORDER_CHOOSER) {
      mChannelOrderChooser = new OrderChooser(mResultChannelArr, Plugin.getPluginManager().getSubscribedChannels());
      mChannelItemList = null;
    }
    else {
      mChannelItemList = new SelectableItemList(mResultChannelArr, Plugin.getPluginManager().getSubscribedChannels());
      mChannelOrderChooser = null;
    }

    int pos = 1;
    layout.appendRow(RowSpec.decode("fill:default:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));
    
    if(mChannelOrderChooser != null) {
      contentPane.add(mChannelOrderChooser, cc.xy(1,pos));
    } else {
      contentPane.add(mChannelItemList, cc.xy(1,pos));
    }
      
    pos += 2;
    
    if (description != null) {
      JLabel lb = new JLabel(description);
      layout.appendRow(RowSpec.decode("pref"));
      layout.appendRow(RowSpec.decode("3dlu"));
      contentPane.add(lb, cc.xy(1,pos));
      pos += 2;
    }

    JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Object[] o = mChannelOrderChooser != null ? mChannelOrderChooser.getOrder() : mChannelItemList.getSelection();
        mResultChannelArr = new Channel[o.length];
        for (int i=0;i<o.length;i++) {
          mResultChannelArr[i]=(Channel)o[i];
        }
        setVisible(false);
      }
      });

    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mResultChannelArr = null;
        setVisible(false);
      }
    });

    ButtonBarBuilder2 builder = new ButtonBarBuilder2();
    builder.addGlue();
    builder.addButton(new JButton[] {okBt, cancelBt});
    
    layout.appendRow(RowSpec.decode("pref"));
    contentPane.add(builder.getPanel(), cc.xy(1,pos));
    
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

  public void close() {
    setVisible(false);
  }

}
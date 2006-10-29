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

package tvbrowser.ui.settings.channel;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * This Dialog enables the User to change every Setting in the Channel Apperance
 * 
 * @author bodum
 * @since 2.1
 */
public class ChannelConfigDlg extends JDialog implements ActionListener, WindowClosingIf {
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelConfigDlg.class);
  /** Current Channel */
  private Channel mChannel;
  /** Close/OK Buttons */
  private JButton mCloseBt, mOKBt;
  /** The Correction-Time*/
  private JComboBox mCorrectionCB;

  /** File for Icon */
  private File mIconFile;

  /** Button to Change FileName */
  private JButton mChangeIcon;

  /** use User-Icon */
  private JCheckBox mUseUserIcon;

  /** Channel-Name */
  private JTextField mChannelName;
  /** Channel-WebPage*/
  private JTextField mWebPage;
  
  /**
   * Create the Dialog 
   * @param parent Parent
   * @param channel Channel to show
   */
  public ChannelConfigDlg(JDialog parent, Channel channel) {
    super(parent, mLocalizer.msg("configChannel", "Configure Channel"), true);
    mChannel = channel;
    createDialog();
  }

  /**
   * Create the Dialog
   * @param parent Parent
   * @param channel Channel to show
   */
  public ChannelConfigDlg(JFrame parent, Channel channel) {
    super(parent,  mLocalizer.msg("configChannel", "Configure Channel"), true);
    mChannel = channel;
    createDialog();
  }

  /**
   * Create the GUI
   */
  private void createDialog() {
    JPanel panel = (JPanel) getContentPane();

    UiUtilities.registerForClosing(this);
    
    panel.setLayout(new FormLayout("default, 3dlu, fill:default:grow",
        "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu:grow, default"));

    CellConstraints cc = new CellConstraints();

    panel.setBorder(Borders.DLU4_BORDER);

    panel.add(new JLabel(mLocalizer.msg("channelName", "Channel Name") + ":"), cc.xy(1, 1));

    mChannelName = new JTextField(mChannel.getName());
    
    panel.add(mChannelName, cc.xy(3, 1));

    panel.add(new JLabel(mLocalizer.msg("channelLogo", "Channel Logo") + ":"), cc.xy(1, 3));    

    if (mChannel.getUserIconFileName() != null)
      mIconFile = new File(mChannel.getUserIconFileName());

    mChangeIcon = new JButton(createUserIcon());
    mChangeIcon.setEnabled(mChannel.isUsingUserIcon());

    mChangeIcon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        changeIcon();
      }
    });

    panel.add(mChangeIcon, cc.xy(3, 5));

    mUseUserIcon = new JCheckBox(mLocalizer.msg("useIcon", "Use own channel-icon"));
    mUseUserIcon.setSelected(mChannel.isUsingUserIcon());

    mUseUserIcon.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mChangeIcon.setEnabled(mUseUserIcon.isSelected());
      }
    });

    panel.add(mUseUserIcon, cc.xy(3, 3));

    panel.add(new JLabel(mLocalizer.msg("webAddress", "Web Address") + ":"), cc.xy(1, 7));

    mWebPage =new JTextField(mChannel.getWebpage()); 
    
    panel.add(mWebPage, cc.xy(3, 7));

    panel.add(new JLabel(mLocalizer.msg("time", "Time Correction") + ":"), cc.xy(1, 9));

    mCorrectionCB = new JComboBox(new String[] { "-1:00", "0:00", "+1:00" });
    mCorrectionCB.setSelectedIndex(mChannel.getDayLightSavingTimeCorrection() + 1);

    panel.add(mCorrectionCB, cc.xy(3, 9));

    JTextArea txt = UiUtilities.createHelpTextArea(mLocalizer.msg("DLSTNote", ""));
    // Hack because of growing JTextArea in FormLayout
    txt.setMinimumSize(new Dimension(200, 20));
    panel.add(txt, cc.xy(3, 11));

    ButtonBarBuilder builder = new ButtonBarBuilder();
    
    JButton defaultButton = new JButton(mLocalizer.msg("default", "Default"));
    
    defaultButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetToDefaults();
      }

    });
    
    builder.addGridded(defaultButton);
    builder.addRelatedGap();
    builder.addGlue();

    mOKBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOKBt.addActionListener(this);
    
    getRootPane().setDefaultButton(mOKBt);
    
    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCloseBt.addActionListener(this);

    builder.addGriddedButtons(new JButton[] { mOKBt, mCloseBt });

    panel.add(builder.getPanel(), cc.xyw(1, 13, 3));

    pack();

    if (getWidth() < 500) {
      setSize(500, getHeight());
    }
    if (getHeight() < 250) {
      setSize(getWidth(), 250);
    }

  }

  /**
   * Reset the Channel to the Default-Values
   */
  private void resetToDefaults() {
    mWebPage.setText(mChannel.getDefaultWebPage());
    mChannelName.setText(mChannel.getDefaultName());
    mUseUserIcon.setSelected(false);
    mCorrectionCB.setSelectedIndex(1);
  }

  /**
   * Create a User-Icon
   * @return Icon
   */
  private Icon createUserIcon() {
    Icon icon;
    if ((mIconFile != null) && (mIconFile.exists())) {
      Image img = ImageUtilities.createImage(mIconFile.getAbsolutePath());
      if (img != null) {
        icon = UiUtilities.createChannelIcon(new ImageIcon(img));
      } else {
        icon = UiUtilities.createChannelIcon(mChannel.getIcon());
      }
    } else {
      icon = UiUtilities.createChannelIcon(mChannel.getIcon());
    }

    return icon;
  }

  /**
   * Change the Icon
   */
  private void changeIcon() {
    JFileChooser fileChooser = new JFileChooser(mIconFile);
    String[] extArr = { ".jpg", ".jpeg", ".gif", ".png" };
    fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, png"));
    fileChooser.showOpenDialog(this);
    File selection = fileChooser.getSelectedFile();
    if (selection != null) {
      mIconFile = selection;
      mChangeIcon.setIcon(createUserIcon());
    }
  }

  /**
   * Center and Show the Dialog
   */
  public void centerAndShow() {
    UiUtilities.centerAndShow(this);
  }

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == mOKBt) {
      int correction = mCorrectionCB.getSelectedIndex() - 1;
      mChannel.setDayLightSavingTimeCorrection(correction);
      mChannel.useUserIcon(mUseUserIcon.isSelected());
      if (mIconFile != null) {
        mChannel.setUserIconFileName(mIconFile.getAbsolutePath());
      } else {
        mChannel.setUserIconFileName(null);
      }
      mChannel.setUserChannelName(mChannelName.getText());
      mChannel.setUserWebPage(mWebPage.getText());
      setVisible(false);
    } else if (o == mCloseBt) {
      setVisible(false);
    }

  }

  public void close() {
    setVisible(false);
  }

}
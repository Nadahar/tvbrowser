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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.ui.ColorButton;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * This Dialog enables the User to change Settings in more than one Channel.
 * 
 * @author bodum
 * @since 2.1
 */
public class MultiChannelConfigDlg extends JDialog implements ActionListener, WindowClosingIf{
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelConfigDlg.class);
  /** Current Channel */
  private Channel[] mChannel;
  /** Close/OK Buttons */
  private JButton mCloseBt, mOKBt;
  /** The Correction-Time*/
  private JComboBox<String> mCorrectionCB;
  
  /** Enabe user background color */
  private JCheckBox mUseUserBackground;
  
  /** User background color selection */
  private ColorButton mSelectBackgroundColor;
  
  private boolean mBackgroundWasSelected;

  /**
   * Create the Dialog
   * @param parent Parent
   * @param channel Channel to show
   */
  public MultiChannelConfigDlg(Window parent, Channel[] channel) {
    super(parent, mLocalizer.msg("configChannel", "Configure Channel"));
    setModalityType(ModalityType.DOCUMENT_MODAL);
    mChannel = channel;
    createDialog();
  }

  /**
   * Create the GUI
   */
  private void createDialog() {
    UiUtilities.registerForClosing(this);
    JPanel panel = (JPanel) getContentPane();

    panel.setLayout(new FormLayout("default, 3dlu, fill:default:grow",
        "default, 3dlu, default, 3dlu, default, 3dlu, fill:0dlu:grow, 3dlu, default, 3dlu"));

    CellConstraints cc = new CellConstraints();

    panel.setBorder(Borders.DLU4);
    
    // background color
    
    mUseUserBackground = new JCheckBox(ChannelConfigDlg.mLocalizer.msg("backgroundColorUse", "User defined background color"), false);
    mSelectBackgroundColor = new ColorButton(Color.white);
    mSelectBackgroundColor.setEnabled(mUseUserBackground.isSelected());
    
    mUseUserBackground.addItemListener(e -> {
      mBackgroundWasSelected = true;
      mSelectBackgroundColor.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
    });
    
    panel.add(mUseUserBackground, CC.xyw(1, 1, 3));
    panel.add(mSelectBackgroundColor, CC.xyw(1, 3, 3));

    panel.add(new JLabel(mLocalizer.msg("time", "Time zone correction")), cc.xy(1, 5));

    mCorrectionCB = new JComboBox<String>(new String[] { "-1:00", "-0:45", "-0:30", "-0:15", "0:00", "+0:15", "+0:30", "+0:45", "+1:00" });
    mCorrectionCB.setSelectedIndex(mChannel[0].getTimeZoneCorrectionMinutes() / 15 + 4);

    panel.add(mCorrectionCB, cc.xy(3, 5));

    JTextArea txt = UiUtilities.createHelpTextArea(mLocalizer.msg("DLSTNote", ""));
    panel.add(txt, cc.xyw(1, 7, 3));

    ButtonBarBuilder builder = new ButtonBarBuilder();
    
    JButton defaultButton = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    
    defaultButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetToDefaults();
      }

    });
    
    builder.addButton(defaultButton);
    builder.addRelatedGap();
    builder.addGlue();

    mOKBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOKBt.addActionListener(this);
    getRootPane().setDefaultButton(mOKBt);
    
    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCloseBt.addActionListener(this);

    builder.addButton(new JButton[] { mOKBt, mCloseBt });

    panel.add(builder.getPanel(), cc.xyw(1, 9, 3));
  }

  /**
   * Reset the Channel to the Default-Values
   */
  private void resetToDefaults() {
    mCorrectionCB.setSelectedIndex(mCorrectionCB.getItemCount() / 2);
  }

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == mOKBt) {
      int minutes = (mCorrectionCB.getSelectedIndex() - 4) * 15;
      for (Channel element : mChannel) {
        element.setTimeZoneCorrectionMinutes(minutes);
        
        if(mBackgroundWasSelected) {
          element.setUserBackgroundColor(mUseUserBackground.isSelected() ? mSelectBackgroundColor.getColor() : null);
        }
      }
      
      setVisible(false);
    } else if (o == mCloseBt) {
      setVisible(false);
    }

  }

  public void close() {
    setVisible(false);
  }

}
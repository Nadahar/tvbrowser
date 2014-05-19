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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.extras.reminderplugin;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.Program;

public class ReminderDialog extends JDialog implements WindowClosingIf {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ReminderDialog.class);

  private boolean mOkPressed=false;

  private JComboBox mList;

  private JCheckBox mRememberSettingsCb, mDontShowDialog;

  private JTextField mCommentField;
  
  private RemindValue[] mRemindValueArr;

  public ReminderDialog(Window parent, devplugin.Program program,
      final java.util.Properties settings) {
    super(parent);
    setModal(true);
    createGui(program, settings);
  }

  private void createGui(final devplugin.Program program, final java.util.Properties settings) {
    mRemindValueArr = ReminderPlugin.calculatePossibleReminders(program);

    setTitle(mLocalizer.msg("title", "New reminder"));

    UiUtilities.registerForClosing(this);

    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JPanel northPn = new JPanel();
    northPn.setLayout(new BoxLayout(northPn,BoxLayout.Y_AXIS));

    JLabel channelLabel=new JLabel(program.getChannel().getName());
    channelLabel.setIcon(UiUtilities.createChannelIcon(program.getChannel().getIcon()));
    channelLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
    channelLabel.setHorizontalTextPosition(SwingConstants.CENTER);

    JLabel titleLabel=new JLabel(program.getTitle());
    JPanel infoPanel=new JPanel(new GridLayout(2,1));

    Font font=titleLabel.getFont();

    titleLabel.setFont(new Font(font.getName(),Font.BOLD,font.getSize()+4));

    infoPanel.add(new JLabel(program.getDateString()));
    infoPanel.add(new JLabel(program.getTimeString()));

    JPanel headerPanel=new JPanel(new BorderLayout(20,0));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
    headerPanel.add(channelLabel,BorderLayout.WEST);
    headerPanel.add(titleLabel,BorderLayout.CENTER);
    headerPanel.add(infoPanel,BorderLayout.EAST);

    northPn.add(headerPanel);

    mList=new JComboBox(mRemindValueArr);

    String s=settings.getProperty("defaultReminderEntry");
    int reminderTime = 5;
    if (s!=null) {
      try {
        reminderTime = Integer.parseInt(s) + ReminderPlugin.getStartIndexForBeforeReminders(program);
      }catch(NumberFormatException e) {
        // ignore
      }
    }
    
    if (reminderTime>=0 && reminderTime<mRemindValueArr.length) {
      mList.setSelectedIndex(reminderTime);
    }
    else {
      mList.setSelectedIndex(mRemindValueArr.length - 1);
    }

    northPn.add(mList);
    northPn.add(Box.createRigidArea(new Dimension(0,3)));

    JPanel commentPanel = new JPanel();
    commentPanel.setLayout(new BorderLayout());
    commentPanel.add(new JLabel(mLocalizer.msg("comment", "Comment")), BorderLayout.NORTH);
    mCommentField = new JTextField();
    commentPanel.add(mCommentField, BorderLayout.CENTER);
    northPn.add(commentPanel);

    mRememberSettingsCb = new JCheckBox(mLocalizer.msg("rememberSetting", "Remember setting"));
    JPanel pn1 = new JPanel(new BorderLayout());
    pn1.add(mRememberSettingsCb, BorderLayout.NORTH);
    
    mList.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(mRememberSettingsCb.isSelected() && ((RemindValue)mList.getSelectedItem()).getMinutes() < 0) {
          mRememberSettingsCb.setSelected(false);
        }
        
        mRememberSettingsCb.setEnabled(((RemindValue)mList.getSelectedItem()).getMinutes() >= 0);
      }
    });

    mDontShowDialog = new JCheckBox(mLocalizer.msg("dontShow","Don't show this dialog anymore"));
    pn1.add(mDontShowDialog, BorderLayout.CENTER);

    pn1.add(new JLabel(mLocalizer.msg("howToChange","You can change the behavior under Settings -> Reminder")), BorderLayout.SOUTH);

    northPn.add(pn1);

    JPanel btnPn=new JPanel(new FlowLayout(FlowLayout.TRAILING));
    btnPn.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

    JButton okBtn=new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mOkPressed=true;
        if (mRememberSettingsCb.isSelected()) {
          settings.setProperty("defaultReminderEntry", Integer.toString(mList
              .getSelectedIndex() - ReminderPlugin.getStartIndexForBeforeReminders(program)));
        }
        settings.setProperty("showTimeSelectionDialog",String.valueOf(!mDontShowDialog.isSelected()));
        setVisible(false);
      }
    });
    btnPn.add(okBtn);
    getRootPane().setDefaultButton(okBtn);

    JButton cancelBtn=new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    btnPn.add(cancelBtn);

    contentPane.add(northPn,BorderLayout.NORTH);
    contentPane.add(btnPn,BorderLayout.SOUTH);
  }

  public int getReminderMinutes() {
    return ((RemindValue)mList.getSelectedItem()).getMinutes();
  }

  public ReminderContent getReminderContent() {
    return new ReminderContent(getReminderMinutes(), mCommentField.getText());
  }

  public boolean getOkPressed() {
    return mOkPressed;
  }


  public void close() {
    setVisible(false);
  }

}
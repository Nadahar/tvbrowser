/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package helper.newseditor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import tvbrowserdataservice.GroupNews;

public class GroupNewsEditor extends JDialog {
  private GroupNews mNews;
  
  private JTextField mTitleEn;
  private JTextField mTitleDe;
  
  private JTextArea mTextEn;
  private JTextArea mTextDe;
  
  private JCheckBox[] mRestrictChannelArr;
  
  private boolean mOkButtonPressed;
  
  private HashMap<JCheckBox, GroupNewsChannel> mRestrictedChannelMap;
  
  public GroupNewsEditor(String group, GroupNews news, Window parent, GroupNewsChannel[] groupChannels) {
    super(parent, "Edit GroupNews for Group: " + group, ModalityType.APPLICATION_MODAL);
    
    mRestrictedChannelMap = new HashMap<JCheckBox, GroupNewsChannel>(groupChannels.length);
    
    mNews = news;
    mOkButtonPressed = false;
    
    mTitleEn = new JTextField(mNews.getTitleEn());
    mTitleDe = new JTextField(mNews.getTitleDe());
    
    mTextEn = new JTextArea(mNews.getTextEn());
    mTextEn.setLineWrap(true);
    
    mTextDe = new JTextArea(mNews.getTextDe());
    mTextDe.setLineWrap(true);
    
    JPanel channelPanel = new JPanel();
    channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
    
    mRestrictChannelArr = new JCheckBox[groupChannels.length];
    
    for(int i = 0; i < mRestrictChannelArr.length; i++) {
      mRestrictChannelArr[i] = new JCheckBox(groupChannels[i].toString(), news.isChannelRestricted(groupChannels[i].getId()));
      channelPanel.add(mRestrictChannelArr[i]);
      mRestrictedChannelMap.put(mRestrictChannelArr[i], groupChannels[i]);
    }
    
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
    GridBagConstraints c = new GridBagConstraints();
    
    c.insets = new Insets(2, 2, 2, 2);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    c.gridwidth = 1;
    c.weighty = 0;
    
    mainPanel.add(new JLabel("English title:"), c);
    
    c.gridy = 0;
    c.gridx = 1;
    c.weightx = 1;
    
    mainPanel.add(mTitleEn, c);
    
    c.gridy = 1;
    c.gridx = 0;
    c.gridwidth = 2;
    c.weightx = 0;
    
    mainPanel.add(new JLabel("English news text (html format possible):"), c);
    
    c.gridy = 2;
    c.weighty = 0.4;
    c.weightx = 1;
    c.fill = GridBagConstraints.BOTH;
    
    mainPanel.add(new JScrollPane(mTextEn), c);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(10, 2, 10, 2);
    c.weightx = 0;
    c.weighty = 0;
    c.gridy = 3;
    
    mainPanel.add(new JSeparator(JSeparator.HORIZONTAL), c);
    
    c.insets = new Insets(2, 2, 2, 2);
    c.gridy = 4;
    c.gridwidth = 1;
    
    mainPanel.add(new JLabel("German title:"), c);
    
    c.gridx = 1;
    c.weightx = 1;
    
    mainPanel.add(mTitleDe, c);
        
    c.gridy = 5;
    c.gridx = 0;
    c.gridwidth = 2;
    c.weightx = 0;
    
    mainPanel.add(new JLabel("German news text (html format possible):"), c);
    
    c.gridy = 6;
    c.weighty = 0.4;
    c.weightx = 1;
    c.fill = GridBagConstraints.BOTH;
    
    mainPanel.add(new JScrollPane(mTextDe), c);
    
    c.gridy = 7;
    c.weighty = 0;
    c.weightx = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(10, 2, 5, 2);
    c.gridwidth = 5;
    
    mainPanel.add(new JSeparator(JSeparator.HORIZONTAL), c);
    
    c.gridy = 8;
    c.insets = new Insets(2, 2, 2, 2);
    
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    
    JButton save = new JButton("Save");
    save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        save();
      }
    });
        
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(cancel);
    buttonPanel.add(Box.createRigidArea(new Dimension(20,0)));
    buttonPanel.add(save);
    buttonPanel.add(Box.createHorizontalGlue());
    
    setContentPane(mainPanel);
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        dispose();
      }
    });
    
    mainPanel.add(buttonPanel, c);

    c.insets = new Insets(2, 10, 0, 2);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 3;
    c.gridy = 0;
    c.weightx = 1;
    c.weighty = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    
    mainPanel.add(new JLabel("Restrict news to channels:"), c);
    
    c.insets = new Insets(0, 10, 2, 2);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 3;
    c.gridy = 1;
    c.weightx = 0.5;
    c.weighty = 1;
    c.gridheight = 6;
    
    JScrollPane pane = new JScrollPane(channelPanel);
    pane.getVerticalScrollBar().setUnitIncrement(50);
    pane.getVerticalScrollBar().setBlockIncrement(150);
    
    mainPanel.add(pane, c);
    
    setSize(800, 600);
    
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    
    int x = d.width/2 - 400;
    int y = d.height/2 - 300;
    
    setLocation(x, y);
  }
  
  private void save() {
    mOkButtonPressed = true;
    
    mNews.setTitleDe(mTitleDe.getText().trim());
    mNews.setTitleEn(mTitleEn.getText().trim());
    
    mNews.setTextDe(mTextDe.getText().trim());
    mNews.setTextEn(mTextEn.getText().trim());
    
    ArrayList<String> restrictedChannels = new ArrayList<String>();
    
    for(JCheckBox restricted : mRestrictChannelArr) {
      if(restricted.isSelected()) {
        GroupNewsChannel ch = mRestrictedChannelMap.get(restricted);
        
        if(ch != null) {
          restrictedChannels.add(ch.getId());
        }
      }
    }
    
    mNews.setRestrictedChannelIds(restrictedChannels.toArray(new String[restrictedChannels.size()]));
    
    dispose();
  }
  
  public boolean wasOkButtonPressed() {
    return mOkButtonPressed;
  }
}

/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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


package tvbrowserdataservice;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import tvdataservice.SettingsPanel;
import tvbrowserdataservice.file.*;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ImageUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;




public class TvBrowserDataServiceSettingsPanel extends SettingsPanel implements ActionListener {

	private Properties mSettings;
  private JCheckBox[] mLevelCheckboxes;
  
  private JTextArea mGroupDescriptionTA;
  
  private JButton mAddBtn, mRemoveBtn, mResetBtn;
  
  private JList mGroupList;
  private DefaultListModel mGroupListModel;
  private ChannelGroup mGroup;
  
  private static SettingsPanel mInstance;
    
  /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(TvBrowserDataServiceSettingsPanel.class);
  
    
  protected TvBrowserDataServiceSettingsPanel(Properties settings) {
  
    mSettings=settings;
    setLayout(new BorderLayout());
  
    JTabbedPane tabbedPane = new JTabbedPane();
    
    
    /* level list pane */
    JPanel levelList=new JPanel();
    levelList.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    levelList.setLayout(new BoxLayout(levelList,BoxLayout.Y_AXIS));
    
    levelList.add(new JLabel(mLocalizer.msg("downloadLevel","Download this data")));
    
    TvDataLevel[] levelArr=DayProgramFile.LEVEL_ARR;
    
    String[] levelIds=settings.getProperty("level","").split(":::");
        
    mLevelCheckboxes=new JCheckBox[levelArr.length];
    for (int i=0;i<levelArr.length;i++) {
      mLevelCheckboxes[i]=new JCheckBox(levelArr[i].getDescription());
      levelList.add(mLevelCheckboxes[i]);
      if (levelArr[i].isRequired()) {
        mLevelCheckboxes[i].setSelected(true);
        mLevelCheckboxes[i].setEnabled(false);
      }
      else {
        for (int j=0;j<levelIds.length;j++) {
          if (levelIds[j].equals(levelArr[i].getId())) {
            mLevelCheckboxes[i].setSelected(true);
          }
        }
      }
    }
       
    /* group list pane */   
    
    JPanel groupListPanel=new JPanel(new BorderLayout(0,10));
    groupListPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
    JTextArea ta=new JTextArea(mLocalizer.msg("channelgroup.description","Use this "));
    ta.setWrapStyleWord(true);
    ta.setLineWrap(true);
    ta.setEditable(false);
    ta.setFocusable(false);
    ta.setOpaque(false);
    groupListPanel.add(ta,BorderLayout.NORTH);
   
   
    JPanel panel2=new JPanel(new BorderLayout(10,0));
      
    mGroupListModel=new DefaultListModel();    
        
    mGroupList=new JList(mGroupListModel);
    panel2.add(new JScrollPane(mGroupList),BorderLayout.CENTER);
        
    JPanel panel3=new JPanel(new BorderLayout());
    JPanel btnPn=new JPanel();
    btnPn.setLayout(new GridLayout(0,1,0,4));
            
    mAddBtn=new JButton(mLocalizer.msg("add","Add"), ImageUtilities.createImageIconFromJar("tvbrowserdataservice/Add24.gif", getClass()));
    mRemoveBtn=new JButton(mLocalizer.msg("remove","Remove"), ImageUtilities.createImageIconFromJar("tvbrowserdataservice/Remove24.gif", getClass()));
    mResetBtn=new JButton(mLocalizer.msg("reset","Reset"), ImageUtilities.createImageIconFromJar("tvbrowserdataservice/Refresh24.gif", getClass()));
            
    mAddBtn.setHorizontalAlignment(JButton.LEFT);
    mRemoveBtn.setHorizontalAlignment(JButton.LEFT);
    mResetBtn.setHorizontalAlignment(JButton.LEFT);
            
    btnPn.add(mAddBtn);
    btnPn.add(mRemoveBtn);
    
    mAddBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    mResetBtn.addActionListener(this);
            
    panel3.add(btnPn,BorderLayout.NORTH);
    panel3.add(mResetBtn,BorderLayout.SOUTH);
    panel2.add(panel3,BorderLayout.EAST); 
    
    JPanel groupInfoPanel=new JPanel(new BorderLayout(3,0));
    JPanel westPn=new JPanel(new BorderLayout());
    
    westPn.add(new JLabel(mLocalizer.msg("description","Description:")),BorderLayout.NORTH);
    groupInfoPanel.add(westPn,BorderLayout.WEST);
    mGroupDescriptionTA=new JTextArea();
    mGroupDescriptionTA.setWrapStyleWord(true);
    mGroupDescriptionTA.setLineWrap(true);
    mGroupDescriptionTA.setEditable(false);
    mGroupDescriptionTA.setFocusable(false);
    mGroupDescriptionTA.setOpaque(false);
    mGroupDescriptionTA.setPreferredSize(new Dimension(0,30));
    
    groupInfoPanel.add(mGroupDescriptionTA,BorderLayout.CENTER);
    
    groupListPanel.add(panel2,BorderLayout.CENTER);
    
    groupListPanel.add(groupInfoPanel,BorderLayout.SOUTH);
    
   
    tabbedPane.add(mLocalizer.msg("datalevel","data level"),levelList);
    tabbedPane.add(mLocalizer.msg("channelgroups","channel groups"),groupListPanel);
    
    add(tabbedPane,BorderLayout.CENTER);
    
    mGroupList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent arg0) {				
        mRemoveBtn.setEnabled(mGroupList.getSelectedIndex()>=0);
        ChannelGroup group=(ChannelGroup)mGroupList.getSelectedValue();
        if (group==null) {
          mGroupDescriptionTA.setText("");
        }
        else {
          mGroupDescriptionTA.setText(group.getDescription());
        } 
			}
    }
    );  
      
    mRemoveBtn.setEnabled(mGroupList.getSelectedIndex()>=0);
    fillGroupList(TvBrowserDataService.getInstance().getChannelGroups());
    
  }
  
  private void fillGroupList(devplugin.ChannelGroup[] groups) {
    mGroupListModel.removeAllElements();
    for (int i=0;i<groups.length;i++) {
      mGroupListModel.addElement(groups[i]);
    }    
  }
  
  public static SettingsPanel getInstance(Properties settings) {
    if (mInstance==null) {
      mInstance=new TvBrowserDataServiceSettingsPanel(settings);
    }
    return mInstance;
  }
  
  
  
	public void ok() {
    String setting="";
		for (int i=0;i<mLevelCheckboxes.length;i++) {
      if (mLevelCheckboxes[i].isSelected()) {
        setting+=":::"+DayProgramFile.LEVEL_ARR[i].getId();
      }
		}
    if (setting.length()>3) {
      setting=setting.substring(3);
    }
    mSettings.setProperty("level",setting);
    
    StringBuffer buf=new StringBuffer();
    
    Object[] groups=mGroupListModel.toArray();
    
    for (int i=0;i<groups.length-1;i++) {
      buf.append(((ChannelGroup)groups[i]).getId()).append(":");
    }
    if (groups.length>0) {
      buf.append(((ChannelGroup)groups[groups.length-1]).getId());
    }
    mSettings.setProperty("groupname",buf.toString());
    for (int i=0;i<groups.length;i++) {
      StringBuffer urlBuf=new StringBuffer();
      String[] mirrorArr=((ChannelGroup)groups[i]).getMirrorArr();
      for (int j=0;j<mirrorArr.length-1;j++) {
        urlBuf.append(mirrorArr[j]).append(";");
      }
      if (mirrorArr.length>0) {
        urlBuf.append(mirrorArr[mirrorArr.length-1]);
      }
      mSettings.setProperty("group_"+((ChannelGroup)groups[i]).getId(),urlBuf.toString());
    }
    
	}
  
  private ChannelGroup getChannelGroupByURL(String url, devplugin.ProgressMonitor monitor) throws TvBrowserException {
    int pos=url.lastIndexOf('/');
    String groupId=url.substring(pos+1,url.length());
   
    String groupUrl=url.substring(0,pos);
    
    ChannelGroup group=new ChannelGroup(TvBrowserDataService.getInstance(), groupId, new String[]{groupUrl});
    group.checkForAvailableChannels(monitor);
    return group;
  }

  private void addGroupUrl(final String url) {
    URL page=null;
		try {
			page = new URL(url);
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(this,mLocalizer.msg("invalidUrl","'{0}' is not a valid URL",url));
			return;
		}
    
    final ProgressWindow progressWindow=new ProgressWindow(this);
    mGroup=null;
    progressWindow.run(new Progress(){
      public void run() {
        try {
          mGroup=getChannelGroupByURL(url,progressWindow);
        }catch (TvBrowserException exc) {
          ErrorHandler.handle(exc);
        }
      }
    });
    if (mGroup!=null) {
      if (mGroupListModel.contains(mGroup)) {
        System.out.println("not new!");
      }
      else {
        mGroupListModel.addElement(mGroup);
        TvBrowserDataService.getInstance().addGroup(mGroup);
      }
    }          
    
  }
	
	public void actionPerformed(ActionEvent event) {
		
    Object source=event.getSource();
		if (source==mAddBtn) {
      String groupUrl = (String)JOptionPane.showInputDialog(
                          this,
                          mLocalizer.msg("enterGroupUrl","Please enter the URL of the new group"),
                          mLocalizer.msg("enterGroupDlgTitle","Add group"),
                          JOptionPane.PLAIN_MESSAGE,
                          null,
                          null,
                          "");
      if (groupUrl!=null && groupUrl.length()>0) {
        addGroupUrl(groupUrl);
      }
		}
    else if (source==mRemoveBtn) {
      ChannelGroup group=(ChannelGroup)mGroupList.getSelectedValue();
      Object[] options = {mLocalizer.msg("removeGroup","yes,remove"),mLocalizer.msg("keepGroup","Keep!")};
      int deleteGroup = JOptionPane.showOptionDialog(this,
           mLocalizer.msg("removeGroupQuestion","Do you want to remove group '{0}' ?",group.getName()),
           mLocalizer.msg("removeGroupDlgTitle","Remove group"),
           JOptionPane.YES_NO_OPTION,
           JOptionPane.QUESTION_MESSAGE,
           null,     
           options,  
           options[1]); 
    
      if (deleteGroup==JOptionPane.YES_OPTION) {
        mGroupListModel.removeElement(group);
        TvBrowserDataService.getInstance().removeGroup(group);
      }    
    }
    else if (source==mResetBtn){
      ChannelGroup[] groups=TvBrowserDataService.getInstance().getDefaultGroups();
      fillGroupList(groups);
      TvBrowserDataService.getInstance().setChannelGroups(groups);
    }
    
  }
  
  
}
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
 

package tvbrowser.ui.update;

import javax.swing.*;



import tvbrowser.core.PluginManager;


import java.awt.*;
import java.awt.event.*;



public class SelectPluginsDlg extends JDialog implements ActionListener {
	
	
	/** The localizer for this class. */
			  private static final util.ui.Localizer mLocalizer
				= util.ui.Localizer.getLocalizerFor(SelectPluginsDlg.class);
	
	
	private JButton mCancelBtn, mDownloadBtn;
	private JLabel mDeveloperVersionLb, mStableVersionLb;
	private JList mList;
	private UpdateItemPanel[] mPanelList;
	private Frame mParent;
	private int mResult=CANCEL;
	public static final int OK=0, CANCEL=1;
	
	public SelectPluginsDlg(Frame parent, UpdateItem[] list) {	
		super(parent,true);
		mParent=parent;
		setTitle(mLocalizer.msg("title","Download plugins"));
    
		JPanel contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout(0,10));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
		JPanel btnPanel=new JPanel();
		mCancelBtn=new JButton(mLocalizer.msg("cancel","Cancel"));
		mDownloadBtn=new JButton(mLocalizer.msg("download","Download"));
		mCancelBtn.addActionListener(this);
		mDownloadBtn.addActionListener(this);
		btnPanel.add(mCancelBtn);
		btnPanel.add(mDownloadBtn);
		
		
		JTextArea txtArea=new JTextArea(mLocalizer.msg("description.1","To download a plulgin check..."));

		txtArea.setWrapStyleWord(true);
		txtArea.setLineWrap(true);
		txtArea.setEditable(false);
		txtArea.setFocusable(false);
		txtArea.setOpaque(false);
		
		
		
		JPanel centerPanel=new JPanel(new BorderLayout());
		
		JPanel listPanel=new JPanel();
   		listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.Y_AXIS));
    	mPanelList=new UpdateItemPanel[list.length];
    	for (int i=0;i<list.length;i++) {
			mPanelList[i]=new UpdateItemPanel(list[i]);
			mPanelList[i].setEnabled(false);
			listPanel.add(mPanelList[i]);
		}
		centerPanel.add(listPanel,BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(centerPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(30);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(30);
		
		
		JPanel southPanel=new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.Y_AXIS));
		
		mDeveloperVersionLb=new JLabel();
		mStableVersionLb=new JLabel();
		mDeveloperVersionLb=new JLabel();
		
		JPanel tvbPanel=new JPanel();
		tvbPanel.setLayout(new BoxLayout(tvbPanel,BoxLayout.Y_AXIS));
		
		JPanel instVersPanel=new JPanel(new BorderLayout());
		instVersPanel.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
		instVersPanel.add(new JLabel(mLocalizer.msg("installedversion","Installed version:")),BorderLayout.WEST);
		instVersPanel.add(new JLabel(tvbrowser.TVBrowser.VERSION.toString()),BorderLayout.EAST);
		
		JPanel stableVersPanel=new JPanel(new BorderLayout());
		stableVersPanel.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
		stableVersPanel.add(new JLabel(mLocalizer.msg("lateststable","Latest stable version:")),BorderLayout.WEST);
		stableVersPanel.add(mStableVersionLb,BorderLayout.EAST);
		
		JPanel devVersPanel=new JPanel(new BorderLayout());
		devVersPanel.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
		devVersPanel.add(new JLabel(mLocalizer.msg("latestdeveloper","latest developer version:")),BorderLayout.WEST);
		devVersPanel.add(mDeveloperVersionLb,BorderLayout.EAST);
				
		tvbPanel.add(instVersPanel);
		tvbPanel.add(stableVersPanel);
		tvbPanel.add(devVersPanel);
		
		devplugin.Version latestStableV=null, latestDeveloperV=null;
		for (int i=0;i<list.length;i++) {
			if (list[i].getType()==UpdateItem.TVBROWSER) {
				VersionItem[] versions=list[i].getVersions();
				for (int j=0;j<versions.length;j++) {
					if (versions[j].isStable()) {
						if (latestStableV==null || versions[j].getVersion().compareTo(latestStableV)>0) {
							latestStableV=versions[j].getVersion();
						}
					}else{  // not stable
						if (latestDeveloperV==null || versions[j].getVersion().compareTo(latestDeveloperV)>0) {
							latestDeveloperV=versions[j].getVersion();
						}
					}
				}
			}
		}
		
		if (latestDeveloperV==null) {
			mDeveloperVersionLb.setText("-");
		}else {
			mDeveloperVersionLb.setText(latestDeveloperV.toString());
		}
		
		if (latestStableV==null) {
			mStableVersionLb.setText("-");
		}else {
			mStableVersionLb.setText(latestStableV.toString());
		}
		tvbPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("infotext.version","Information about new TV-Browser versions")));
		
		if (latestDeveloperV!=null || latestStableV!=null) {
			JTextArea area=new JTextArea(mLocalizer.msg("infotext.download","To download new versions of TV-Browser, please visit our website on http://tvbrowser.sourceforge.net"));
			area.setWrapStyleWord(true);
			area.setLineWrap(true);
			area.setEditable(false);
			area.setFocusable(false);
			area.setOpaque(false);
			area.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			tvbPanel.add(area);
		}
		
		southPanel.add(tvbPanel);
		southPanel.add(btnPanel);
		
		contentPane.add(txtArea,BorderLayout.NORTH);
		contentPane.add(scrollPane,BorderLayout.CENTER);
		contentPane.add(southPanel,BorderLayout.SOUTH);
		this.setSize(350,400);
			
	}
	
	public void actionPerformed(ActionEvent event) {
		if (event.getSource()==mCancelBtn) {
			hide();
		}
		else if (event.getSource()==mDownloadBtn) {
			
			int cnt=0;
			for (int i=0;i<mPanelList.length;i++) {
				mPanelList[i].storeSelection();	
				if (mPanelList[i].getUpdateItem().getSelectedVersion()!=null) {
					cnt++;
				}			
			}
			
			if (cnt==0) {
				JOptionPane.showMessageDialog(this,mLocalizer.msg("nopluginselected","No plugin selected for download"));
			}else{
				hide();
				mResult=OK;			
			}
		}
	}
	
	
	public int getResult() {
		return mResult;
	}
	
}



class UpdateItemPanel extends JPanel implements ActionListener {
	
	private JCheckBox mCheck;
	private JComboBox mBox;
	private JLabel mInstalledLb, mToDoLb;
	private UpdateItem mUpdateItem;
	private boolean mEnabled=true;
	
	/** The localizer for this class. */
				  private static final util.ui.Localizer mLocalizer
					= util.ui.Localizer.getLocalizerFor(UpdateItemPanel.class);
	
	
	private static Font normalFont=new Font("Dialog", Font.PLAIN, 12);
	
	public UpdateItemPanel(UpdateItem item) {
		super();	
		mUpdateItem=item;
		setBackground(Color.white);
		setOpaque(true);
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0,0,0,8));
		JPanel namePanel=new JPanel(new BorderLayout());
		namePanel.setOpaque(false);
		mCheck=new JCheckBox(item.getName());
		mCheck.addActionListener(this);
		mCheck.setOpaque(false);
		namePanel.add(mCheck);
		
		add(namePanel);		
		
		String version=null;
		if (item.getType()==UpdateItem.PLUGIN) {
			devplugin.Plugin plugin=PluginManager.getPluginByName(item.getName());
			if (plugin!=null) {
				version=plugin.getInfo().getVersion().toString();
			}
		}
		else if (item.getType()==UpdateItem.DATASERVICE) {
			tvdataservice.TvDataService service=tvbrowser.core.TvDataServiceManager.getInstance().getDataService(item.getName());
			if (service!=null) {
				version=service.getVersion().toString();	
			}
		}
		
		
		JPanel versionPanel=new JPanel(new BorderLayout());
		versionPanel.setOpaque(false);
		versionPanel.setBorder(BorderFactory.createEmptyBorder(0,40,0,0));
		add(versionPanel);
		
		mInstalledLb=new JLabel();
		mInstalledLb.setFont(normalFont);
		if (version!=null) {
			mInstalledLb.setText(mLocalizer.msg("installed","installed:")+version);			
		}else{
			mInstalledLb.setText(mLocalizer.msg("notinstalled","not installed"));
		}
		versionPanel.add(mInstalledLb,BorderLayout.WEST);
		
		JPanel updatePanel=new JPanel(new BorderLayout());
		updatePanel.setOpaque(false);
		updatePanel.setBorder(BorderFactory.createEmptyBorder(0,40,0,0));
			
		mToDoLb=new JLabel();
		mToDoLb.setFont(normalFont);
		if (version==null) {
			mToDoLb.setText(mLocalizer.msg("installversion","install version"));			
		}
		else {
			mToDoLb.setText(mLocalizer.msg("upgradeto","upgrade to"));			
		}
		
		updatePanel.add(mToDoLb,BorderLayout.WEST);
		
		mBox=new JComboBox(item.getVersions());
		mBox.setEnabled(false);
		updatePanel.add(mBox,BorderLayout.EAST);
		
		add(updatePanel);
		
	}
	
	
	
	public void setEnabled(boolean e) {
		mBox.setEnabled(e);
		mInstalledLb.setEnabled(e);
		mToDoLb.setEnabled(e);
		mEnabled=e;
	}
	
	public boolean isEnabled() {
		return mEnabled;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==mCheck) {
			setEnabled(mCheck.isSelected());
		}
	}
	
	
	public void storeSelection() {
		VersionItem vi;
		if (mEnabled) {
			vi=(VersionItem)mBox.getSelectedItem();
		}else{
			vi=null;
		}
		mUpdateItem.selectVersion(vi);
		
	}
	
	public UpdateItem getUpdateItem() {
		return mUpdateItem;
	}
	
}

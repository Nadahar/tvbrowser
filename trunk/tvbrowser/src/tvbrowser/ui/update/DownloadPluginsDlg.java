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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


public class DownloadPluginsDlg extends JDialog {
	
	/** The localizer for this class. */
	  private static final util.ui.Localizer mLocalizer
		= util.ui.Localizer.getLocalizerFor(DownloadPluginsDlg.class);
	
	private JTextArea mTextArea;
	private Frame mParent;
	private boolean mThreadIsRunning=false;
	private final JButton mCloseBtn;
	
	
	public DownloadPluginsDlg(Frame parent, UpdateItem[] items) {
		super(parent,true);
		String msg;
		msg = mLocalizer.msg("title","Download plugins");
		setTitle(msg);
		
		mParent=parent;
		JPanel contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout(0,7));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
		
		mTextArea=new JTextArea();
		mTextArea.setWrapStyleWord(true);
		mTextArea.setLineWrap(true);
		mTextArea.setEditable(false);
		JScrollPane scrollPane=new JScrollPane(mTextArea);
		contentPane.add(scrollPane,BorderLayout.CENTER);
		JPanel btnPanel=new JPanel();
		mCloseBtn=new JButton(mLocalizer.msg("close","close"));
		mCloseBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (mThreadIsRunning) {
					mCloseBtn.setText(mLocalizer.msg("close","close"));
					mThreadIsRunning=false;
				}else{
					hide();
				}
			}
		});
		
		btnPanel.add(mCloseBtn);
		contentPane.add(btnPanel,BorderLayout.SOUTH);
		
		setSize(400,200);
		startDownloadThread(items);
		util.ui.UiUtilities.centerAndShow(this);
		
	}
	
	
	public void startDownloadThread(final UpdateItem[]items) {
		Thread downloadingThread = new Thread() {
			public void run() {
				for (int i=0;i<items.length&&mThreadIsRunning;i++) {
					if (items[i].isSelected()) {
						mTextArea.append(mLocalizer.msg("downloading","Downloading file ({0})...",items[i].getName()));
						try {
							items[i].download();
							mTextArea.append("OK\n");
						}catch(IOException e) {
							mTextArea.append("\n   Error: "+e.toString()+"\n");
						}
					}
				}
				mThreadIsRunning=false;
				mCloseBtn.setText(mLocalizer.msg("close","close"));
				
			}
		};
		mThreadIsRunning=true;
		downloadingThread.start();			
	
	}
	
}

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

import util.exc.TvBrowserException;

import java.awt.*;
import java.awt.event.*;

public class SoftwareUpdateDlg extends JDialog implements ActionListener {
	
	/** The localizer for this class. */
	private static final util.ui.Localizer mLocalizer
	= util.ui.Localizer.getLocalizerFor(SoftwareUpdateDlg.class);
	
	
	private JButton mCloseBtn;
	private JList mList;
	private Frame mParent;
	
  private SoftwareUpdateItemListPanel mItemListPanel;
  
	
	public SoftwareUpdateDlg(Frame parent) {
		
		
		super(parent,true);
		mParent=parent;
		setTitle(mLocalizer.msg("title","Download plugins"));
		
		JPanel contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout(0,10));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
		JPanel btnPanel=new JPanel();
		mCloseBtn=new JButton(mLocalizer.msg("close","Close"));
		mCloseBtn.addActionListener(this);
		
    btnPanel.add(mCloseBtn);
		
    mItemListPanel=new SoftwareUpdateItemListPanel();
   
		contentPane.add(new JScrollPane(mItemListPanel),BorderLayout.CENTER);
		contentPane.add(btnPanel,BorderLayout.SOUTH);
		this.setSize(350,400);
		
	}
	
	
	public void actionPerformed(ActionEvent event) {
		hide();
	}
	
	public void setSoftwareUpdateItems(SoftwareUpdateItem[] items) {
		
    for (int i=0;i<items.length;i++) {
      mItemListPanel.addItem(items[i]);
    } 
	}
	
	
	
}


class SoftwareUpdateItemPanel extends JPanel {
  
  private JButton mDownloadBt;
  
  public SoftwareUpdateItemPanel(SoftwareUpdateItem item) {
    setLayout(new BorderLayout());
    JPanel content=new JPanel(new BorderLayout());
    devplugin.Version v=item.getVersion();
    add(new JLabel(item.getName()+" "+(v!=null?item.getVersion().toString():"")),BorderLayout.NORTH);
    JTextArea txtArea=new JTextArea(item.getDescription());

    txtArea.setWrapStyleWord(true);
    txtArea.setLineWrap(true);
    txtArea.setEditable(false);
    txtArea.setFocusable(false);
    txtArea.setOpaque(false);
    txtArea.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
    add(txtArea,BorderLayout.CENTER);
    
    JPanel downloadPn=new JPanel(new BorderLayout());
    
    mDownloadBt=new JButton("downlaod");
    
    downloadPn.add(mDownloadBt,BorderLayout.SOUTH);
    add(downloadPn,BorderLayout.EAST);
    
  }
  
  public JButton getDownloadButton() {
    return mDownloadBt;
  }

}

class SoftwareUpdateItemListPanel extends JPanel {
  
  JPanel mContent;
 
  public SoftwareUpdateItemListPanel() {
    setLayout(new BorderLayout());
    mContent=new JPanel();
    mContent.setLayout(new BoxLayout(mContent,BoxLayout.Y_AXIS));
    add(mContent,BorderLayout.NORTH);
  }
  
  public void addItem(final SoftwareUpdateItem item) {
    final SoftwareUpdateItemPanel panel=new SoftwareUpdateItemPanel(item);
    panel.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));
    mContent.add(panel);
    
    panel.getDownloadButton().addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        boolean success=false;
        try {
          success=item.download();
        }catch(TvBrowserException exc) {
          util.exc.ErrorHandler.handle(exc);
        }
        if (success) {
          mContent.remove(panel);
          updateUI();
        }        
      }
    });
    
  }
  
  
}
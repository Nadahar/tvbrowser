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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import util.exc.TvBrowserException;
import util.ui.BrowserLauncher;
import util.ui.html.ExtendedHTMLEditorKit;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class SoftwareUpdateDlg extends JDialog implements ActionListener, ListSelectionListener {
	
	/** The localizer for this class. */
	private static final util.ui.Localizer mLocalizer
	= util.ui.Localizer.getLocalizerFor(SoftwareUpdateDlg.class);
	
	
	private JButton mCloseBtn, mDownloadBtn;
  private JLabel mInfoLb;
	private JList mList;
	private Frame mParent;
	private JEditorPane mDescriptionPane;
	
	public SoftwareUpdateDlg(Frame parent) {
		
		
		super(parent,true);
		mParent=parent;
		setTitle(mLocalizer.msg("title","Download plugins"));
		
		JPanel contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout(0,10));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
		JPanel btnPanel=new JPanel(new BorderLayout(10,0));
		mCloseBtn=new JButton(mLocalizer.msg("close","Close"));
		mCloseBtn.addActionListener(this);
		
    mDownloadBtn = new JButton(mLocalizer.msg("download","Download selected items"));
    mDownloadBtn.addActionListener(this);
    mDownloadBtn.setEnabled(false);
    
    btnPanel.add(mDownloadBtn, BorderLayout.CENTER);
    btnPanel.add(mCloseBtn, BorderLayout.EAST);
    
		 
    mList = new JList();
    mList.setCellRenderer(new SoftwareUpdateItemRenderer());
    mList.addListSelectionListener(this);
		
    JPanel northPn = new JPanel();
    northPn.add(new JLabel(mLocalizer.msg("header","Hier k&ouml;nnen neue Plugins heruntergeladen und installiert werden.")));
    
    JPanel southPn = new JPanel(new BorderLayout());
    
    mDescriptionPane = new JEditorPane();
    
    mDescriptionPane.setEditorKit(new ExtendedHTMLEditorKit());
    mDescriptionPane.setEditable(false);
    
    mDescriptionPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            BrowserLauncher.openURL(url.toString());
          }
        }
      }
    });
    
    southPn.add(btnPanel, BorderLayout.SOUTH);
    
    JSplitPane splitPn = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(mList), new JScrollPane(mDescriptionPane));
    splitPn.setDividerLocation(150);
    
    contentPane.add(northPn, BorderLayout.NORTH);
    contentPane.add(splitPn, BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);
    
		this.setSize(450,400);
    
	}
	
	
  private void updateDescription(SoftwareUpdateItem item) {
    if (item == null) {
        mDescriptionPane.setText("");  
    }
    else {
      StringBuffer content = new StringBuffer();
      String author = item.getProperty("author");
      String website = item.getProperty("website");
      devplugin.Version version = item.getVersion();
      
      content.append("<html><head>"
          + "<style type=\"text/css\" media=\"screen\"><!--"
          + "body { font-family: Dialog; }"
          + "h1 { font-size: medium; }"
          + "td { text-align: left; font-style: bold; font-size: small;}"
          + "th { text-align: right; font-style: plain; font-size: small; }"
          + "--></style>" +
          "</head>")
        .append("<body><h1>")
        .append(item.getName())
        .append("</h1>")
        .append("<p>")
        .append(item.getProperty("description"))
        .append("</p><br><table>");
      
      if (author != null) {
        content.append("<tr><th>")
               .append(mLocalizer.msg("author","author"))
               .append(":</th><td>")
               .append(author)
               .append("</td></tr>");
      }
      if (version != null) {
        content.append("<tr><th>")
               .append(mLocalizer.msg("version","version"))
               .append("</th><td>")
               .append(version)
               .append("</td></tr>");
      }
      if (website != null) {
        content.append("<tr><th>")
               .append(mLocalizer.msg("website","website"))
               .append("</th><td><a href=\"")
               .append(website)
               .append("\">")
               .append(website)
               .append("</a></td></tr>");
      }  
      content.append("</table></body></html>");
      
      mDescriptionPane.setText(content.toString());
    }
  }
    
	public void actionPerformed(ActionEvent event) {
    if (event.getSource() == mCloseBtn) {
		  hide();
    }
    else if (event.getSource() == mDownloadBtn) {
      Object[] o = mList.getSelectedValues();
      int successfullyDownloadedItems = 0;
      for (int i = 0; i<o.length; i++) {
        SoftwareUpdateItem item = (SoftwareUpdateItem)o[i];
        try {
          item.download();
          successfullyDownloadedItems++;
        } catch (TvBrowserException e) {
           util.exc.ErrorHandler.handle(e); 
        }
      }
      if (successfullyDownloadedItems>0) {
        JOptionPane.showMessageDialog(null,mLocalizer.msg("restartprogram","please restart tvbrowser before..."));
        hide();
      }
    }
	}
	
	public void setSoftwareUpdateItems(SoftwareUpdateItem[] items) {		
    mList.setListData(items);
  }
	
	

  public void valueChanged(ListSelectionEvent event) {
    JList list = (JList) event.getSource();
    
    Object[] items = list.getSelectedValues();
    if (items.length == 1) {
      updateDescription((SoftwareUpdateItem)items[0]);
    }
    else {
      updateDescription(null);
    }
    
    if (items.length == 0) {
      mDownloadBtn.setEnabled(false);
    }
    else {
      mDownloadBtn.setEnabled(true);
    }
    
    
  }
  
  class SoftwareUpdateItemRenderer extends DefaultListCellRenderer {
      
    public Component getListCellRendererComponent(JList list, Object value,
              int index, boolean isSelected, boolean cellHasFocus) {  
       
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);

      if (value instanceof SoftwareUpdateItem) {
        SoftwareUpdateItem item = (SoftwareUpdateItem)value;      
        label.setText(item.getName());
      }
      return label;
        
    }
      
  }

}
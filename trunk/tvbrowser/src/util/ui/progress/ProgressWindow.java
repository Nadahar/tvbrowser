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

package util.ui.progress;
import javax.swing.*;
import util.ui.*;
import util.ui.progress.Progress;

import java.awt.*;

public class ProgressWindow implements devplugin.ProgressMonitor {

	private JLabel mLabel;
  private JDialog mDialog;
  
  public ProgressWindow(Component parent, String msg) {
    mDialog=UiUtilities.createDialog(parent,true);
   
    JPanel content=(JPanel)mDialog.getContentPane();
    content.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    content.setLayout(new BorderLayout());
    mLabel=new JLabel(msg);
    mLabel.setHorizontalAlignment(SwingConstants.CENTER);
    content.add(mLabel,BorderLayout.CENTER);
    mDialog.setSize(500,70);
    mDialog.setUndecorated(true);    
  }
  
  public ProgressWindow(Component parent) {
    this(parent,"");
  }
  
  
 
  public void run(final Progress progress) {   
    Thread thread=new Thread() {
      public void run() {
        progress.run();
        while (!mDialog.isVisible()) {};
        mDialog.hide();
      }
    };
    thread.start();
    UiUtilities.centerAndShow(mDialog);    
  }
  
 
  public void setMaximum(int maximum) {
    
  }

 
  public void setValue(int value) {
    
  }

  public void setMessage(String msg) {
    mLabel.setText(msg);
  }
  
  /*
  public ProgressWindow() {
    super("test");
    JPanel contentPane=(JPanel)getContentPane();
    mLabel=new JLabel();
    contentPane.add(mLabel);
    setSize(200,80);
    util.ui.UiUtilities.centerAndShow(this);
  }
  
	public ProgressWindow(Window parent) {
		super("test");
    JPanel contentPane=(JPanel)getContentPane();
    mLabel=new JLabel();
    contentPane.add(mLabel);
    setSize(200,80);
    util.ui.UiUtilities.centerAndShow(this);
    
	}
	
	public void setText(String txt) {
		mLabel.setText(txt);
	}
	*/
	
}
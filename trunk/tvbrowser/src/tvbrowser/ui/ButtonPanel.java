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
 */

package tvbrowser.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import tvbrowser.core.PluginManager;
import tvbrowser.core.Settings;

public class ButtonPanel extends JPanel {

  private JButton[] mTimeBtns, mPluginBtns;
  private JButton mUpdateBtn, mPrefBtn;
  
  
  
  public ButtonPanel() {
    
    setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    setOpaque(false);
  }
  
  
  
  public void setTimeButtons(JButton[] timeBtns) {
    mTimeBtns=timeBtns;
  }
  
  public void setUpdateButton(JButton updateBtn) {
    mUpdateBtn=updateBtn;
  }
  
  public void setPreferencesButton(JButton prefBtn) {
    mPrefBtn=prefBtn;
  }
  
  public void setPluginButtons() {
  	java.util.ArrayList pluginBtns=new java.util.ArrayList();
	String[] buttonPluginArr = Settings.getButtonPlugins();
		for (int i = 0; i < buttonPluginArr.length; i++) {
		  final devplugin.Plugin plugin = PluginManager.getPlugin(buttonPluginArr[i]);
		  if ((plugin != null) && PluginManager.isInstalled(plugin)) {
			Icon icon = plugin.getButtonIcon();
			JButton btn = new PictureButton(plugin.getButtonText(), icon);
			//add(btn);
			pluginBtns.add(btn);
			btn.addActionListener(new ActionListener(){
			  public void actionPerformed(ActionEvent event) {
				plugin.execute();
			  }
			});
		  }
		}
	Object[] objs=pluginBtns.toArray();
	mPluginBtns=new JButton[objs.length];
	for (int i=0;i<objs.length;i++) {
		mPluginBtns[i]=(JButton)objs[i];		
	}
  }
  
  public void update() {
    this.removeAll();
    //JButton btn;
    String msg;
    if (Settings.isTimeBtnVisible()) {
      for (int i=0;i<mTimeBtns.length;i++) {
        add(mTimeBtns[i]);
      }
      add(new JSeparator(JSeparator.VERTICAL));
    }
    
    if (Settings.isUpdateBtnVisible()) {
      add(mUpdateBtn);
    }
    
    if (Settings.isPreferencesBtnVisible()) {
      add(mPrefBtn);
    }
    
    for (int i=0;i<mPluginBtns.length;i++) {
    	add(mPluginBtns[i]);
    }
    
    /*
    String[] buttonPluginArr = Settings.getButtonPlugins();
    for (int i = 0; i < buttonPluginArr.length; i++) {
      final devplugin.Plugin plugin = PluginManager.getPlugin(buttonPluginArr[i]);
      if ((plugin != null) && PluginManager.isInstalled(plugin)) {
        Icon icon = plugin.getButtonIcon();
        btn = new PictureButton(plugin.getButtonText(), icon);
        add(btn);
        btn.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent event) {
            plugin.execute();
          }
        });
      }
    }*/
    this.updateUI();
  }
  
}
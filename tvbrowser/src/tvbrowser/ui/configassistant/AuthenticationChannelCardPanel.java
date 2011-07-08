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

package tvbrowser.ui.configassistant;

import java.awt.Font;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import tvbrowser.core.ChannelList;
import tvbrowser.core.tvdataservice.ChannelGroupManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.SettingsPanel;
import util.ui.ScrollableJPanel;
import util.ui.UiUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

class AuthenticationChannelCardPanel extends AbstractCardPanel {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(AuthenticationChannelCardPanel.class);

  private JPanel mContent;
  private ArrayList<SettingsPanel> mSettingsPanelList;
  private JScrollPane mScrollPane;

  public AuthenticationChannelCardPanel(PrevNextButtons btns) {
    super(btns);
    
    mSettingsPanelList = new ArrayList<SettingsPanel>(0);
    
    FormLayout layout = new FormLayout("5dlu,default:grow,5dlu");
    PanelBuilder pb = new PanelBuilder(layout,new ScrollableJPanel());
    
    TvDataServiceProxy[] tvDataServices = TvDataServiceProxyManager.getInstance().getDataServices();
    
    for(TvDataServiceProxy dataService : tvDataServices) {
      SettingsPanel panel = dataService.getAuthenticationPanel();
      
      if(panel != null) {
        mSettingsPanelList.add(panel);
        
        layout.appendRow(RowSpec.decode("10dlu"));
        layout.appendRow(RowSpec.decode("default"));
        
        JComponent x = pb.addSeparator(dataService.getInfo().getName(),CC.xyw(1,layout.getRowCount(),3));
        
        try {          
          Method getFont = x.getComponent(0).getClass().getMethod("getFont",new Class[0]);
          Method setFont = x.getComponent(0).getClass().getMethod("setFont",new Class[] {Font.class});
          
          Font f = (Font)getFont.invoke(x.getComponent(0),new Object[0]);
          f = f.deriveFont(Font.BOLD);
          setFont.invoke(x.getComponent(0), new Object[] {f});
        }catch(Exception e) {}
        
        layout.appendRow(RowSpec.decode("5dlu"));
        layout.appendRow(RowSpec.decode("fill:default:grow"));
        
        pb.add(panel,CC.xy(2,layout.getRowCount()));
      }
    }
    
    mContent = new JPanel(new FormLayout("15dlu,default:grow,15dlu","default,15dlu,default,5dlu,fill:0dlu:grow,5dlu,default,15dlu"));
    mContent.add(new StatusPanel(StatusPanel.CHANNELS), CC.xyw(1,1,3));
    mContent.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("info1","<div style=\"font-weight:bold;\">Some TV data sources need authentication. You need to enter the authentication data if you want to get TV data for the channels of the shown TV data sources.</div>")),CC.xy(2,3));
    
    mScrollPane = new JScrollPane(pb.getPanel());
    mScrollPane.setBorder(null);
    
    mContent.add(mScrollPane,CC.xy(2,5));
    
    mContent.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("info2","<div style=\"color:green;font-weight:bold;font-size:medium;\">If you don't need the channels of the listed TV data sources you can skip to the next step.</div>")),CC.xy(2,7));    
  }

  public JPanel getPanel() {
    return mContent;
  }

  public boolean onNext() {
    for(SettingsPanel panel : mSettingsPanelList) {
      panel.ok();
    }
    
    final ProgressWindow win=new ProgressWindow(MainFrame.getInstance());  
    
    win.run(new Progress(){
      public void run() {
        ChannelGroupManager.getInstance().checkForAvailableGroupsAndChannels(win);
      }
    });
    
    ChannelList.reload();
    ChannelList.initSubscribedChannels();
    
    return true;
  }

  public boolean onPrev() {
    for(SettingsPanel panel : mSettingsPanelList) {
      panel.ok();
    }
    
    return true;
  }

  public void onShow() {
    super.onShow();
    mScrollPane.getVerticalScrollBar().setValue(0);
  }
  
  public boolean isNeeded() {
    return !mSettingsPanelList.isEmpty();
  }

}

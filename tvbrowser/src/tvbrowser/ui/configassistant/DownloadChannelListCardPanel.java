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

import javax.swing.*;

import tvbrowser.core.ChannelList;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

import java.awt.event.*;
import java.awt.Font;

class DownloadChannelListCardPanel extends AbstractCardPanel implements ActionListener {

  private CardPanel mNext, mPrev;
  private JPanel mContent;
  private JButton mDownloadBtn;
  private JLabel mChannelCntLabel;
  private JLabel mStatusLabel;
  private boolean mDownloadStarted;
  
  private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(DownloadChannelListCardPanel.class); 
  

  public DownloadChannelListCardPanel(PrevNextButtons btns) {
    super(btns);
    mDownloadStarted=false;
    mContent=new JPanel();
    mContent.setLayout(new BoxLayout(mContent,BoxLayout.Y_AXIS));
    
    JLabel area=new JLabel();
    area.setFont(new Font("SansSerif", Font.PLAIN, 12)); 
   /* area.setText("<HTML>" +
              "<h2>Senderliste herunterladen</h2><br>" +
              "TV-Browser wird nun eine Liste der verfuegbaren Sender herunterladen.<br><br>" +
              "Stellen Sie sicher, dass eine Verbindung ins Internet besteht und klicken Sie auf " +
              "<i><u>Senderliste jetzt herunterladen</u></i>.<br><br>" +
              ""+ 
              "</HTML>");
    */
     area.setText(mLocalizer.msg("description","Download channel list..."));
     mContent.add(area);
     mDownloadBtn=new JButton(mLocalizer.msg("downloadChannelList","Download channellist now"));
     mDownloadBtn.addActionListener(this);
     mContent.add(mDownloadBtn); 
     
     int cnt=getChannelCount();
     mStatusLabel=new JLabel();
     mStatusLabel.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
     if (cnt>0) {
       mStatusLabel.setText(mLocalizer.msg("availableChannels","{0} channels available.",""+cnt));
     }
     
     mContent.add(mStatusLabel);
       
  }
 
  public JPanel getPanel() {
    return mContent;
  }
  
  private int getChannelCount() {
    tvdataservice.TvDataService services[]=TvDataServiceManager.getInstance().getDataServices();
    int channelCount=0;
    for (int i=0;i<services.length;i++) {
      devplugin.Channel channelList[]=services[i].getAvailableChannels();
      if (channelList!=null) {
        channelCount+=channelList.length;
      } 
    }
    return channelCount;
  }
  
  public void actionPerformed(ActionEvent event) {
    
    
    final ProgressWindow win=new ProgressWindow(MainFrame.getInstance());
        
    win.run(new Progress(){
      public void run() {
        mDownloadStarted=true;
        tvdataservice.TvDataService services[]=TvDataServiceManager.getInstance().getDataServices();
        int channelCount=0;
        for (int i=0;i<services.length;i++) {
          if (services[i].supportsDynamicChannelList()) {
            try {
              devplugin.Channel channelList[]=services[i].checkForAvailableChannels(win);
            }catch (TvBrowserException e) {
              ErrorHandler.handle(e);
            }
          }
        }
      }
    });
    
    int cnt=getChannelCount();
    if (cnt>0) {
      mStatusLabel.setText(mLocalizer.msg("availableChannels","({0}) channels available.",""+cnt));
    }
    else {      
      mStatusLabel.setText(mLocalizer.msg("noChannelAvailable","no channel available"));
    }
    ChannelList.create();
    mBtns.enableNextButton();
    
    
  }

  
  public void onShow() {
    super.onShow();
    if (!mDownloadStarted || getChannelCount()==0) {
      mBtns.disableNextButton();
    }
  }
}


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

import tvbrowser.core.TvDataServiceManager;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class ConfigAssistant extends JDialog implements ActionListener, PrevNextButtons {  
  
  private JButton mNextBt, mBackBt, mCancelBt;
  private CardPanel mCurCardPanel, mFinishedPanel;
  private JPanel mCardPn;
  
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(ConfigAssistant.class); 
  
 
  public ConfigAssistant(JFrame parent) {
    super(parent, true);
    setTitle(mLocalizer.msg("title","Setup assistant"));
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    
    JPanel centerPanel=new JPanel(new BorderLayout());
    
    mCardPn=new JPanel(new CardLayout());
    mCardPn.setBorder(BorderFactory.createEmptyBorder(3,100,10,20));
      
    JPanel btnPanel=new JPanel(new BorderLayout());
    JPanel panel1=new JPanel();
    
    mBackBt=new JButton("<< "+mLocalizer.msg("back","back"));
    mNextBt=new JButton(mLocalizer.msg("next","next")+" >>");
    mCancelBt=new JButton(mLocalizer.msg("cancel","cancel"));
    
    mBackBt.setEnabled(false);
    
    mBackBt.addActionListener(this);
    mNextBt.addActionListener(this);
    mCancelBt.addActionListener(this);
    
    CardPanel welcomePanel=new WelcomeCardPanel(this);
    CardPanel proxyPanel=new ProxyCardPanel(this);
    CardPanel proxyQuestionPanel=new ProxyQuestionCardPanel(this,proxyPanel);
    CardPanel tvDataCardPanel = new TvDataCardPanel(this);
    CardPanel subscribeChannelPanel=new SubscribeChannelCardPanel(this);
    CardPanel downloadChannelListPanel=new DownloadChannelListCardPanel(this);
    mFinishedPanel=new FinishCardPanel(this);
    
    mCardPn.add(welcomePanel.getPanel(),welcomePanel.toString());
    mCardPn.add(proxyQuestionPanel.getPanel(),proxyQuestionPanel.toString());
    mCardPn.add(proxyPanel.getPanel(),proxyPanel.toString());
    mCardPn.add(tvDataCardPanel.getPanel(),tvDataCardPanel.toString());
    mCardPn.add(mFinishedPanel.getPanel(),mFinishedPanel.toString());
    mCardPn.add(subscribeChannelPanel.getPanel(),subscribeChannelPanel.toString());
   
    
    boolean dynamicChannelList=isDynamicChannelListSupported();
    
    welcomePanel.setNext(proxyQuestionPanel);
    if (dynamicChannelList) {
      mCardPn.add(downloadChannelListPanel.getPanel(),downloadChannelListPanel.toString());
   
      proxyQuestionPanel.setNext(tvDataCardPanel);
      proxyPanel.setNext(tvDataCardPanel);
      tvDataCardPanel.setNext(downloadChannelListPanel);
      tvDataCardPanel.setPrev(proxyQuestionPanel);
      downloadChannelListPanel.setNext(subscribeChannelPanel);
      subscribeChannelPanel.setNext(mFinishedPanel);
    }
    else {
      proxyQuestionPanel.setNext(tvDataCardPanel);
      proxyPanel.setNext(tvDataCardPanel);
      subscribeChannelPanel.setNext(mFinishedPanel);
      tvDataCardPanel.setPrev(proxyQuestionPanel);
    }
    
     
        
    mCurCardPanel=welcomePanel;
    
    panel1.add(mBackBt);
    panel1.add(mNextBt);
    
    JPanel panel2=new JPanel();
    panel2.add(mCancelBt);
    
    centerPanel.add(mCardPn,BorderLayout.CENTER);
    
    btnPanel.add(panel1,BorderLayout.CENTER);
    btnPanel.add(panel2,BorderLayout.EAST);
    contentPane.add(btnPanel,BorderLayout.SOUTH);
    contentPane.add(centerPanel,BorderLayout.CENTER);
    
    setSize(530,420);
   
  }
  
  private boolean isDynamicChannelListSupported() {
    tvdataservice.TvDataService services[]=TvDataServiceManager.getInstance().getDataServices();
    for (int i=0;i<services.length;i++) {
      if (services[i].supportsDynamicChannelList()) return true;       
    }
    return false;    
  }
  
  public void actionPerformed(ActionEvent e) {
    Object o=e.getSource();
    if (o==mBackBt) {
      if (!mCurCardPanel.onPrev()) return;
      mCurCardPanel=mCurCardPanel.getPrev();
      CardLayout cl=(CardLayout)mCardPn.getLayout();
      mCurCardPanel.onShow();
      cl.show(mCardPn,mCurCardPanel.toString());
    }
    else if (o==mNextBt) {
      if (!mCurCardPanel.onNext()) return;
      mCurCardPanel=mCurCardPanel.getNext();
      CardLayout cl=(CardLayout)mCardPn.getLayout();
      mCurCardPanel.onShow();
      
      cl.show(mCardPn,mCurCardPanel.toString());
      
      if (mCurCardPanel==mFinishedPanel) {
        mCancelBt.setText(mLocalizer.msg("finish","Finish"));
        mNextBt.setEnabled(false);
        mBackBt.setEnabled(false);
      }
      
    }
    else if (o==mCancelBt) {
      if (mCurCardPanel==mFinishedPanel) {
        tvbrowser.core.Settings.propShowAssistant.setBoolean(false);
        hide();
      }
      else {
        Object[] possibleValues = {
              mLocalizer.msg("option.1","configure later"),
              mLocalizer.msg("option.2","quit"),
              mLocalizer.msg("option.3","cancel")
        }; 
        Object selectedValue = JOptionPane.showInputDialog(null, 
          mLocalizer.msg("cancelDlg","message"),
          mLocalizer.msg("cancelDlg.title","title"), 
        JOptionPane.INFORMATION_MESSAGE, null, 
        possibleValues, possibleValues[2]);       
        
        if (selectedValue==possibleValues[0]) {
         tvbrowser.core.Settings.propShowAssistant.setBoolean(true); 
         hide();
        }
        else if (selectedValue==possibleValues[1]) {
          tvbrowser.core.Settings.propShowAssistant.setBoolean(false);
          hide();
        }
      }
      
    }
    
  }
  
  public void enablePrevButton() {
    mBackBt.setEnabled(true);
  }
   public void enableNextButton() {
     mNextBt.setEnabled(true);
   }
   public void disablePrevButton() {
     mBackBt.setEnabled(false);
   }
   public void disableNextButton() {
     mNextBt.setEnabled(false);
   }
 

  
}
/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.ui.mainframe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import tvbrowser.core.Settings;

import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;


/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class UpdateDlg extends JDialog implements ActionListener {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(UpdateDlg.class);
  
  public static final int CANCEL=-1, GETALL=99;

  public static final String[] PERIOD_MSG_ARR = {
    mLocalizer.msg("period.0", "Today"),
    mLocalizer.msg("period.1", "Up to tomorrow"),
    mLocalizer.msg("period.2", "Next 2 days"),
    mLocalizer.msg("period.3", "Next 3 days"),
    mLocalizer.msg("period.4", "Next 4 days"),
    mLocalizer.msg("period.5", "Next 5 days"),
    mLocalizer.msg("period.6", "Next 6 days"),
    mLocalizer.msg("period.7", "Next week"),
    mLocalizer.msg("period.1000", "Get all")
  };
  
  private JButton cancelBtn, updateBtn;
  private int result=0;
  private JComboBox comboBox;
  private JCheckBox checkBox;
  private TvDataServiceCheckBox[] mDataServiceCbArr;
  private TvDataServiceProxy[] mSelectedTvDataServiceArr;

  
  
  public UpdateDlg(JFrame parent, boolean modal) {
    super(parent,modal);
    
    String msg;
    
    result=CANCEL;
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    this.setTitle(mLocalizer.msg("dlgTitle", "TV data update"));
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

    updateBtn=new JButton(mLocalizer.msg("updateNow", "Update now"));
    updateBtn.addActionListener(this);
    buttonPanel.add(updateBtn);
    getRootPane().setDefaultButton(updateBtn);

    cancelBtn=new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancelBtn.addActionListener(this);
    buttonPanel.add(cancelBtn);

    contentPane.add(buttonPanel,BorderLayout.SOUTH);

    JPanel northPanel=new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));

    JPanel panel1=new JPanel(new BorderLayout(7,0));
    msg = mLocalizer.msg("period", "Update program for");
    panel1.add(new JLabel(msg), BorderLayout.WEST);
    comboBox = new JComboBox(PERIOD_MSG_ARR);
    panel1.add(comboBox,BorderLayout.EAST);
    northPanel.add(panel1);
    
    
    TvDataServiceProxy[] serviceArr = TvDataServiceProxyManager.getInstance().getDataServices();
    if (serviceArr.length>1) {
      panel1.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
      JPanel dataServicePanel = new JPanel();
      dataServicePanel.setLayout(new BoxLayout(dataServicePanel, BoxLayout.Y_AXIS));
      dataServicePanel.setBorder(BorderFactory.createTitledBorder("Diese Datenquellen verwenden:"));
      mDataServiceCbArr = new TvDataServiceCheckBox[serviceArr.length];
      
      String[] checkedServiceNames = Settings.propDataServicesForUpdate.getStringArray();
      
      for (int i=0; i<serviceArr.length; i++) {
        mDataServiceCbArr[i] = new TvDataServiceCheckBox(serviceArr[i]);
        mDataServiceCbArr[i].setSelected(tvDataServiceIsChecked(serviceArr[i], checkedServiceNames));
        dataServicePanel.add(mDataServiceCbArr[i]);        
      }
      JPanel p = new JPanel(new BorderLayout());
      p.add(dataServicePanel,BorderLayout.CENTER);
      northPanel.add(p);
    }
    
    msg = mLocalizer.msg("rememberSettings", "Remember settings");
    checkBox=new JCheckBox(msg);
    JPanel panel2=new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
    
    try {
      int inx = Settings.propDownloadPeriod.getInt();
      if (inx==GETALL) {
        inx=comboBox.getItemCount()-1;
      }
    	comboBox.setSelectedIndex(inx);
     }catch(IllegalArgumentException e) {
     	comboBox.setSelectedIndex(0);
    }
    panel2.add(checkBox,BorderLayout.WEST);

    northPanel.add(panel2);

    contentPane.add(northPanel,BorderLayout.NORTH);
  }


  private boolean tvDataServiceIsChecked(TvDataServiceProxy service, String[] serviceNames) {
    if (serviceNames == null) {
      return true;
    }
    for (int i=0; i<serviceNames.length; i++) {
      if (service.getClass().getName().equals(serviceNames[i])) {
        return true;
      }
    }
    return false;
  }

  public int getResult() { return result; }

  public TvDataServiceProxy[] getSelectedTvDataServices() {
    if (mSelectedTvDataServiceArr == null) {
      mSelectedTvDataServiceArr = TvDataServiceProxyManager.getInstance().getDataServices();
    }
    
    return mSelectedTvDataServiceArr;
  }
  
  public void actionPerformed(ActionEvent event) {
    Object source=event.getSource();
    if (source==cancelBtn) {
      result=CANCEL;
      setVisible(false);
    }
    else if (source==updateBtn) {
      result=comboBox.getSelectedIndex();
      if (result==comboBox.getItemCount()-1) {
      	result=GETALL;
      }
      
      if (mDataServiceCbArr == null) {  // there is only one tvdataservice available
        mSelectedTvDataServiceArr = TvDataServiceProxyManager.getInstance().getDataServices();
      }
      else {
        ArrayList dataServiceList = new ArrayList();   
        for (int i=0; i<mDataServiceCbArr.length; i++) {
          if (mDataServiceCbArr[i].isSelected()) {
            dataServiceList.add(mDataServiceCbArr[i].getTvDataService());
          }
        }
        mSelectedTvDataServiceArr = new TvDataServiceProxy[dataServiceList.size()];
        dataServiceList.toArray(mSelectedTvDataServiceArr);
      }
      
      if (checkBox.isSelected()) {
        Settings.propDownloadPeriod.setInt(result);
        
        String[] dataServiceArr = new String[mSelectedTvDataServiceArr.length];
        for (int i=0; i<dataServiceArr.length; i++) {
          dataServiceArr[i] = mSelectedTvDataServiceArr[i].getClass().getName();        
        }
        Settings.propDataServicesForUpdate.setStringArray(dataServiceArr);        
      }
      setVisible(false);
    }
  }

}

class TvDataServiceCheckBox extends JCheckBox {
 
  private TvDataServiceProxy mService;
  
  public TvDataServiceCheckBox(TvDataServiceProxy service) {
    super(service.getInfo().getName());
    mService = service; 
  }
  
  public TvDataServiceProxy getTvDataService() {
    return mService;
  }
}

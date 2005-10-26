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


  private JButton mCancelBtn, mUpdateBtn;
  private int mResult =0;
  private JComboBox mComboBox;
  private JCheckBox mCheckBox;
  private TvDataServiceCheckBox[] mDataServiceCbArr;
  private TvDataServiceProxy[] mSelectedTvDataServiceArr;



  public UpdateDlg(JFrame parent, boolean modal) {
    super(parent,modal);

    String msg;

    mResult =CANCEL;

    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    this.setTitle(mLocalizer.msg("dlgTitle", "TV data update"));
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

    mUpdateBtn =new JButton(mLocalizer.msg("updateNow", "Update now"));
    mUpdateBtn.addActionListener(this);
    buttonPanel.add(mUpdateBtn);
    getRootPane().setDefaultButton(mUpdateBtn);

    mCancelBtn =new JButton(mLocalizer.msg("cancel", "Cancel"));
    mCancelBtn.addActionListener(this);
    buttonPanel.add(mCancelBtn);

    contentPane.add(buttonPanel,BorderLayout.SOUTH);

    JPanel northPanel=new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));

    JPanel panel1=new JPanel(new BorderLayout(7,0));
    msg = mLocalizer.msg("period", "Update program for");
    panel1.add(new JLabel(msg), BorderLayout.WEST);
    mComboBox = new JComboBox(PeriodItem.PERIOD_ARR);
    panel1.add(mComboBox,BorderLayout.EAST);
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
    mCheckBox =new JCheckBox(msg);
    JPanel panel2=new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

    int period = Settings.propDownloadPeriod.getInt();
    PeriodItem pi = new PeriodItem(period);
    mComboBox.setSelectedItem(pi);

    panel2.add(mCheckBox,BorderLayout.WEST);

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

  public int getResult() {
    return mResult;
  }

  public TvDataServiceProxy[] getSelectedTvDataServices() {
    if (mSelectedTvDataServiceArr == null) {
      mSelectedTvDataServiceArr = TvDataServiceProxyManager.getInstance().getDataServices();
    }

    return mSelectedTvDataServiceArr;
  }

  public void actionPerformed(ActionEvent event) {
    Object source=event.getSource();
    if (source==mCancelBtn) {
      mResult =CANCEL;
      setVisible(false);
    }
    else if (source==mUpdateBtn) {
      PeriodItem pi = (PeriodItem)mComboBox.getSelectedItem();
      mResult = pi.getDays();

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

      if (mCheckBox.isSelected()) {
        Settings.propDownloadPeriod.setInt(mResult);

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

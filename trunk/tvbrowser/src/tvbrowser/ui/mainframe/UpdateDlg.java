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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class UpdateDlg extends JDialog implements ActionListener, WindowClosingIf {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(UpdateDlg.class);

  public static final int CANCEL = -1, GETALL = 28;

  private JButton mCancelBtn, mUpdateBtn;
  private int mResult = 0;
  private JComboBox mComboBox;
  private TvDataServiceCheckBox[] mDataServiceCbArr;
  private TvDataServiceProxy[] mSelectedTvDataServiceArr;
  
  private JCheckBox mAutoUpdate;  
  
  private JRadioButton mStartUpdate;
  private JRadioButton mRecurrentUpdate;

  public UpdateDlg(JFrame parent, boolean modal) {
    super(parent, modal);

    UiUtilities.registerForClosing(this);
    
    String msg;

    mResult = CANCEL;

    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    this.setTitle(mLocalizer.msg("dlgTitle", "TV data update"));
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    mUpdateBtn = new JButton(mLocalizer.msg("updateNow", "Update now"));
    mUpdateBtn.addActionListener(this);
    buttonPanel.add(mUpdateBtn);
    getRootPane().setDefaultButton(mUpdateBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);
    buttonPanel.add(mCancelBtn);

    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

    JPanel panel1 = new JPanel(new BorderLayout(7, 0));
    msg = mLocalizer.msg("period", "Update program for");
    panel1.add(new JLabel(msg), BorderLayout.WEST);
    mComboBox = new JComboBox(PeriodItem.PERIOD_ARR);
    panel1.add(mComboBox, BorderLayout.EAST);
    northPanel.add(panel1);

    TvDataServiceProxy[] serviceArr = getActiveDataServices();
    
    if (serviceArr.length > 1) {
      panel1.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
      JPanel dataServicePanel = new JPanel();
      dataServicePanel.setLayout(new BoxLayout(dataServicePanel,
          BoxLayout.Y_AXIS));
      dataServicePanel.setBorder(BorderFactory.createTitledBorder(mLocalizer
          .msg("useDataSources", "Use these data sources:")));
      mDataServiceCbArr = new TvDataServiceCheckBox[serviceArr.length];

      String[] checkedServiceNames = Settings.propDataServicesForUpdate
          .getStringArray();

      for (int i = 0; i < serviceArr.length; i++) {
        mDataServiceCbArr[i] = new TvDataServiceCheckBox(serviceArr[i]);
        mDataServiceCbArr[i].setSelected(tvDataServiceIsChecked(serviceArr[i],
            checkedServiceNames));
        dataServicePanel.add(mDataServiceCbArr[i]);
      }
      JPanel p = new JPanel(new BorderLayout());
      p.add(dataServicePanel, BorderLayout.CENTER);
      northPanel.add(p);
    }

    int period = Settings.propDownloadPeriod.getInt();
    PeriodItem pi = new PeriodItem(period);
    mComboBox.setSelectedItem(pi);
    
    if (Settings.propAutoDownloadType.getString().equals("never")) {
      JPanel p = new JPanel(new BorderLayout());
      p.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
      northPanel.add(p);
      p = new JPanel(new BorderLayout());
      p.setBorder(BorderFactory.createTitledBorder(mLocalizer
          .msg("autoUpdateTitle", "Automatic update")));
      
      JPanel boxPanel = new JPanel(new FormLayout("10dlu,pref:grow","default,2dlu,default,default"));
      CellConstraints cc = new CellConstraints();
      
      mAutoUpdate = new JCheckBox(mLocalizer.msg("autoUpdateMessage", "Update data automatically"));
      
      mStartUpdate = new JRadioButton(mLocalizer.msg("onStartUp", "Only on TV-Browser startup"), true);
      mRecurrentUpdate = new JRadioButton(mLocalizer.msg("recurrent", "Recurrent"));
      
      mStartUpdate.setEnabled(false);
      mRecurrentUpdate.setEnabled(false);
      
      boxPanel.add(mAutoUpdate, cc.xyw(1,1,2));
      boxPanel.add(mStartUpdate, cc.xy(2,3));
      boxPanel.add(mRecurrentUpdate, cc.xy(2,4));
      
      p.add(boxPanel, BorderLayout.CENTER);
      
      ButtonGroup bg = new ButtonGroup();
      
      bg.add(mStartUpdate);
      bg.add(mRecurrentUpdate);
      
      mAutoUpdate.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mRecurrentUpdate.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          mStartUpdate.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        }
      });
      
      northPanel.add(p);
    }

    contentPane.add(northPanel, BorderLayout.NORTH);
  }

  /**
   * @return all TvDataServices that have subscribed Channels
   */
  private TvDataServiceProxy[] getActiveDataServices() {
    ArrayList<TvDataServiceProxy> services = new ArrayList<TvDataServiceProxy>();
    
    for (Channel channel : ChannelList.getSubscribedChannels()) {
      if (!services.contains(channel.getDataServiceProxy())) {
        services.add(channel.getDataServiceProxy());
      }
    }
    
    return services.toArray(new TvDataServiceProxy[services.size()]);
  }

  private boolean tvDataServiceIsChecked(TvDataServiceProxy service,
      String[] serviceNames) {
    if (serviceNames == null) {
      return true;
    }
    for (int i = 0; i < serviceNames.length; i++) {
      if (service.getId().compareTo(serviceNames[i]) == 0) {
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
      mSelectedTvDataServiceArr = getActiveDataServices();
    }

    return mSelectedTvDataServiceArr;
  }

  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == mCancelBtn) {
      mResult = CANCEL;
      setVisible(false);
    } else if (source == mUpdateBtn) {
      PeriodItem pi = (PeriodItem) mComboBox.getSelectedItem();
      mResult = pi.getDays();

      if (mDataServiceCbArr == null) { // there is only one tvdataservice
                                        // available
        mSelectedTvDataServiceArr = getActiveDataServices();
      } else {
        ArrayList<TvDataServiceProxy> dataServiceList = new ArrayList<TvDataServiceProxy>();
        for (int i = 0; i < mDataServiceCbArr.length; i++) {
          if (mDataServiceCbArr[i].isSelected()) {
            dataServiceList.add(mDataServiceCbArr[i].getTvDataService());
          }
        }
        mSelectedTvDataServiceArr = new TvDataServiceProxy[dataServiceList
            .size()];
        dataServiceList.toArray(mSelectedTvDataServiceArr);
      }

      Settings.propDownloadPeriod.setInt(mResult);

      String[] dataServiceArr = new String[mSelectedTvDataServiceArr.length];
      for (int i = 0; i < dataServiceArr.length; i++) {
        dataServiceArr[i] = mSelectedTvDataServiceArr[i].getId();
      }
      Settings.propDataServicesForUpdate.setStringArray(dataServiceArr);
      
      if (mStartUpdate != null) {
        if (mAutoUpdate.isSelected()) {
          Settings.propAutoDownloadType.setString("daily");
          Settings.propAutoDownloadPeriod.setInt(mResult);
          Settings.propAutoDataDownloadEnabled.setBoolean(mRecurrentUpdate.isSelected());
        }
      }
      setVisible(false);
    }
  }

  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }

  public void setNumberOfDays(int numberOfDays) {
    for (PeriodItem item : PeriodItem.PERIOD_ARR) {
      if (item.getDays() >= numberOfDays) {
        mComboBox.setSelectedItem(item);
        return;
      }
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

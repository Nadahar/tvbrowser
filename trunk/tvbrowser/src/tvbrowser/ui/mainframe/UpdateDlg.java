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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Channel;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class UpdateDlg extends JDialog implements ActionListener, WindowClosingIf {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(UpdateDlg.class);

  protected static final int CANCEL = -1, GETALL = 28;

  private JButton mCancelBtn, mUpdateBtn;
  private int mResult = 0;
  private JComboBox mComboBox;
  private TvDataServiceCheckBox[] mDataServiceCbArr;
  private TvDataServiceProxy[] mSelectedTvDataServiceArr;

  private JCheckBox mAutoUpdate;

  private JRadioButton mStartUpdate;
  private JRadioButton mRecurrentUpdate;

  public UpdateDlg(JFrame parent, boolean modal, final String reason) {
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

    // first show reason of update
    if (reason != null && !reason.isEmpty()) {
      String question = mLocalizer.msg("question", "Do you want to update now?");
      JLabel lbReason = new JLabel("<html>" + reason + "<br>" + question + "</html>");
      JPanel panelReason = new JPanel(new BorderLayout(7, 0));
      panelReason.add(lbReason, BorderLayout.WEST);
      northPanel.add(panelReason);
      northPanel.add(new JLabel(" "));
    }

    // then time selection
    PanelBuilder panel1 = new PanelBuilder(new FormLayout("10dlu,default,5dlu:grow","default,5dlu,default"));
    panel1.addSeparator(mLocalizer.msg("period", "Update program for"), CC.xyw(1,1,3));
    
    mComboBox = new JComboBox(PeriodItem.getPeriodItems());
    
    panel1.add(mComboBox, CC.xy(2,3));
    
    northPanel.add(panel1.getPanel());

    // channel selection
    TvDataServiceProxy[] serviceArr = getActiveDataServices();
    if (serviceArr.length > 1) {
      FormLayout layout = new FormLayout("default");
      JPanel dataServicePanel = new JPanel(layout);
      
      dataServicePanel.setLayout(new BoxLayout(dataServicePanel,
          BoxLayout.Y_AXIS));
      mDataServiceCbArr = new TvDataServiceCheckBox[serviceArr.length];

      String[] checkedServiceNames = Settings.propDataServicesForUpdate
          .getStringArray();

      boolean expand = false;
      for (int i = 0; i < serviceArr.length; i++) {
        mDataServiceCbArr[i] = new TvDataServiceCheckBox(serviceArr[i]);
        boolean isSelected = tvDataServiceIsChecked(serviceArr[i],
            checkedServiceNames);
        mDataServiceCbArr[i].setSelected(isSelected);
        if (!isSelected) {
          expand = true;
        }
        layout.appendRow(RowSpec.decode("default"));
        dataServicePanel.add(mDataServiceCbArr[i], CC.xy(1,layout.getRowCount()));
      }
      
      PanelBuilder ds = new PanelBuilder(new FormLayout("10dlu,default:grow,5dlu,default","10dlu,default,5dlu,default"));
      ds.add(dataServicePanel, CC.xyw(2,4,3));
      
      ds.addSeparator(mLocalizer.msg("dataSources", "Data sources"), CC.xyw(1,2,2));
      
      dataServicePanel.setVisible(expand);
      
      PanelButton open = new PanelButton(dataServicePanel,this);
      
      ds.add(open, CC.xy(4,2));
            
      northPanel.add(ds.getPanel());
    }

    int period = Settings.propDownloadPeriod.getInt();
    PeriodItem pi = new PeriodItem(period);
    mComboBox.setSelectedItem(pi);

    PanelBuilder pb = new PanelBuilder(new FormLayout("10dlu,default:grow,5dlu,default","10dlu,default,5dlu,default"));
    
    pb.addSeparator(mLocalizer.msg("autoUpdateTitle", "Automatic update"), CC.xyw(1,2,2));

    final JPanel boxPanel = new JPanel(new FormLayout("10dlu,default:grow","default,2dlu,default,default"));
    CellConstraints cc = new CellConstraints();
    

    mAutoUpdate = new JCheckBox(mLocalizer.msg("autoUpdateMessage", "Update data automatically"), !Settings.propAutoDownloadType.getString().equals("never"));
    boxPanel.setVisible(!mAutoUpdate.isSelected());
    
    mStartUpdate = new JRadioButton(mLocalizer.msg("onStartUp", "Only on TV-Browser startup"), false);
    mRecurrentUpdate = new JRadioButton(mLocalizer.msg("recurrent", "Recurrent"), true);

    mStartUpdate.setEnabled(mAutoUpdate.isSelected());
    mRecurrentUpdate.setEnabled(mAutoUpdate.isSelected());

    boxPanel.add(mAutoUpdate, cc.xyw(1,1,2));
    boxPanel.add(mStartUpdate, cc.xy(2,3));
    boxPanel.add(mRecurrentUpdate, cc.xy(2,4));

    pb.add(boxPanel, CC.xyw(2,4,2));

    ButtonGroup bg = new ButtonGroup();

    bg.add(mStartUpdate);
    bg.add(mRecurrentUpdate);
    
    PanelButton open = new PanelButton(boxPanel,this);
    
    pb.add(open, CC.xy(4,2));

    mAutoUpdate.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mRecurrentUpdate.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        mStartUpdate.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });

    northPanel.add(pb.getPanel());

    contentPane.add(northPanel, BorderLayout.NORTH);
    mUpdateBtn.requestFocusInWindow();
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
    for (String serviceName : serviceNames) {
      if (service.getId().compareTo(serviceName) == 0) {
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
        for (TvDataServiceCheckBox element : mDataServiceCbArr) {
          if (element.isSelected()) {
            dataServiceList.add(element.getTvDataService());
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

      //if (mStartUpdate != null) {
        if (mAutoUpdate.isSelected()) {
          Settings.propAutoDownloadType.setString("daily");
          Settings.propAutoDownloadPeriod.setInt(mResult);
        }
        else {
          Settings.propAutoDownloadType.setString("never");
        }
        
        Settings.propAutoDataDownloadEnabled.setBoolean(mAutoUpdate.isSelected() && mRecurrentUpdate.isSelected());
        
        
      //}
      setVisible(false);
    }
  }

  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }

  public void setNumberOfDays(int numberOfDays) {
    for (PeriodItem item : PeriodItem.getPeriodItems()) {
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

class PanelButton extends JButton { 
  public PanelButton(final JPanel panel, final JDialog dialog) {
    super(panel.isVisible() ? "<<" : ">>");
    setContentAreaFilled(false);
    setBorder(BorderFactory.createEtchedBorder());
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        panel.setVisible(!panel.isVisible());
        if(panel.isVisible()) {
          setText("<<");
        }
        else {
          setText(">>");
        }
        dialog.pack();
      }
    });
    
    addMouseListener(new MouseAdapter() {
      private Thread mWaiting;
      
      public void mousePressed(MouseEvent e) {
        if(mWaiting != null && mWaiting.isAlive()) {
          mWaiting.interrupt();
        }
      }
      
      public void mouseExited(MouseEvent e) {
        if(mWaiting != null && mWaiting.isAlive()) {
          mWaiting.interrupt();
        }
      }
      
      public void mouseEntered(MouseEvent e) {
        if(mWaiting == null || !mWaiting.isAlive()) {
          mWaiting = new Thread() {
            public void run() {
              try {
                Thread.sleep(750);
                
                panel.setVisible(!panel.isVisible());
                if(panel.isVisible()) {
                  setText("<<");
                }
                else {
                  setText(">>");
                }
                dialog.pack();
              } catch (InterruptedException e) {
                // ignore
              }
            }
          };
          mWaiting.start();
        }
      }
    });
  }
}

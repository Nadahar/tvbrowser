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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DummyChannel;
import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

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
  private JComboBox<PeriodItem> mManuelDownloadPeriodSelection;
  private JComboBox<PeriodItem> mAutoDownloadPeriodSelection;
  private TvDataServiceCheckBox[] mDataServiceCbArr;
  private TvDataServiceProxy[] mSelectedTvDataServiceArr;

  private JCheckBox mAutoUpdate;
  private JCheckBox mSaveAsDefaultPeriod;
  private JCheckBox mSaveAsDefaultDataservices;

  private JRadioButton mStartUpdate;
  private JRadioButton mRecurrentUpdate;

  public UpdateDlg(JFrame parent, boolean modal, final String reason) {
    super(parent, modal);

    UiUtilities.registerForClosing(this);

    mResult = CANCEL;

    final JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    this.setTitle(mLocalizer.msg("dlgTitle", "TV data update"));
    
    final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    mUpdateBtn = new JButton(mLocalizer.msg("updateNow", "Update now"));
    mUpdateBtn.addActionListener(this);
    buttonPanel.add(mUpdateBtn);
    getRootPane().setDefaultButton(mUpdateBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);
    buttonPanel.add(mCancelBtn);

    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    final JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

    // first show reason of update
    if (reason != null && !reason.isEmpty()) {
      final String question = mLocalizer.msg("question", "Do you want to update now?");
      final JLabel lbReason = new JLabel("<html>" + reason + "<br>" + question + "</html>");
      final JPanel panelReason = new JPanel(new BorderLayout(7, 0));
      
      panelReason.add(lbReason, BorderLayout.WEST);
      northPanel.add(panelReason);
      northPanel.add(new JLabel(" "));
    }

    // then time selection
    final PanelBuilder panel1 = new PanelBuilder(new FormLayout("10dlu,default,5dlu:grow,5dlu","default,5dlu,default,default"));
    panel1.addSeparator(mLocalizer.msg("period", "Update program for"), CC.xyw(1,1,4));
    
    mManuelDownloadPeriodSelection = new JComboBox<>(PeriodItem.getPeriodItems());
    mSaveAsDefaultPeriod = new JCheckBox(mLocalizer.msg("saveDefault", "Save as default"), Settings.propSaveDefaultDataUpdateValuesDefault.getBoolean());
    
    panel1.add(mManuelDownloadPeriodSelection, CC.xyw(2,3,2));
    panel1.add(mSaveAsDefaultPeriod, CC.xyw(2,4,2));
    
    northPanel.add(panel1.getPanel());

    // channel selection
    final TvDataServiceProxy[] serviceArr = getActiveDataServices();
    
    if (serviceArr.length > 1) {
      final JPanel dataServicePanel = new JPanel();
      
      dataServicePanel.setLayout(new BoxLayout(dataServicePanel,
          BoxLayout.Y_AXIS));
      mDataServiceCbArr = new TvDataServiceCheckBox[serviceArr.length];

      final String[] checkedServiceNames = Settings.propDataServicesForUpdate
          .getStringArray();

      boolean expand = false;
      
      for (int i = 0; i < serviceArr.length; i++) {
        mDataServiceCbArr[i] = new TvDataServiceCheckBox(serviceArr[i]);
        
        final boolean isSelected = tvDataServiceIsChecked(serviceArr[i], checkedServiceNames);
        mDataServiceCbArr[i].setSelected(isSelected);
        
        if (!isSelected) {
          expand = true;
        }
        
        dataServicePanel.add(mDataServiceCbArr[i]);
      }
      
      mSaveAsDefaultDataservices = new JCheckBox(mLocalizer.msg("saveDefault", "Save as default"), Settings.propSaveDefaultDataUpdateValuesDefault.getBoolean());
      
      dataServicePanel.add(Box.createRigidArea(new Dimension(0,Sizes.dialogUnitXAsPixel(5, dataServicePanel))));
      dataServicePanel.add(mSaveAsDefaultDataservices);
      
      final PanelBuilder ds = new PanelBuilder(new FormLayout("10dlu,default:grow,5dlu,default","10dlu,default,5dlu,default"));
      ds.add(dataServicePanel, CC.xyw(2,4,3));
      
      ds.addSeparator(mLocalizer.msg("dataSources", "Data sources"), CC.xyw(1,2,2));
      
      dataServicePanel.setVisible(expand);
      
      PanelButton open = new PanelButton(dataServicePanel,this);
      
      ds.add(open, CC.xy(4,2));
            
      northPanel.add(ds.getPanel());
    }

    int period = Settings.propDownloadPeriod.getInt();
    
    PeriodItem pi = new PeriodItem(period);
    mManuelDownloadPeriodSelection.setSelectedItem(pi);

    final PanelBuilder pb = new PanelBuilder(new FormLayout("10dlu,default:grow,5dlu,default","10dlu,default,5dlu,default"));
    
    pb.addSeparator(mLocalizer.msg("autoUpdateTitle", "Automatic update"), CC.xyw(1,2,2));

    final JPanel boxPanel = new JPanel(new FormLayout("10dlu,0dlu,default:grow","default,2dlu,default,default,4dlu,default,3dlu,default"));
    
    mAutoUpdate = new JCheckBox(mLocalizer.msg("autoUpdateMessage", "Update data automatically"), !Settings.propAutoDownloadType.getString().equals("never"));
    boxPanel.setVisible(!mAutoUpdate.isSelected());
    
    mStartUpdate = new JRadioButton(mLocalizer.msg("onStartUp", "Only on TV-Browser startup"), !Settings.propAutoDataDownloadEnabled.getBoolean() && mAutoUpdate.isSelected());
    mRecurrentUpdate = new JRadioButton(mLocalizer.msg("recurrent", "Recurrent"), Settings.propAutoDataDownloadEnabled.getBoolean());
    
    mAutoDownloadPeriodSelection = new JComboBox<>(PeriodItem.getPeriodItems());
    
    period = Settings.propAutoDownloadPeriod.getInt();
    pi = new PeriodItem(period);
    mAutoDownloadPeriodSelection.setSelectedItem(pi);
    
    final JLabel label = new JLabel(mLocalizer.msg("period", "Update program for")+":");
    
    boxPanel.add(mAutoUpdate, CC.xyw(1,1,3));
    boxPanel.add(mStartUpdate, CC.xyw(2,3,2));
    boxPanel.add(mRecurrentUpdate, CC.xyw(2,4,2));
    boxPanel.add(label, CC.xyw(2, 6, 2));
    boxPanel.add(mAutoDownloadPeriodSelection, CC.xy(3, 8));
    
    mRecurrentUpdate.setEnabled(mAutoUpdate.isSelected());
    mStartUpdate.setEnabled(mAutoUpdate.isSelected());
    label.setEnabled(mAutoUpdate.isSelected());
    mAutoDownloadPeriodSelection.setEnabled(mAutoUpdate.isSelected());

    pb.add(boxPanel, CC.xyw(2,4,2));

    final ButtonGroup bg = new ButtonGroup();

    bg.add(mStartUpdate);
    bg.add(mRecurrentUpdate);
    
    final PanelButton open = new PanelButton(boxPanel,this);
    
    pb.add(open, CC.xy(4,2));

    mAutoUpdate.addItemListener(e -> {
      mRecurrentUpdate.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      mStartUpdate.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      label.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      mAutoDownloadPeriodSelection.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
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
      if (!(channel instanceof DummyChannel) && channel.getDataServiceProxy() != null && !services.contains(channel.getDataServiceProxy())) {
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
      PeriodItem pi = (PeriodItem) mManuelDownloadPeriodSelection.getSelectedItem();
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
      
      if(mSaveAsDefaultPeriod.isSelected()) {
        Settings.propDownloadPeriod.setInt(mResult);
      }
      
      if(mSaveAsDefaultDataservices == null || mSaveAsDefaultDataservices.isSelected()) {
        final String[] dataServiceArr = new String[mSelectedTvDataServiceArr.length];
        
        for (int i = 0; i < dataServiceArr.length; i++) {
          dataServiceArr[i] = mSelectedTvDataServiceArr[i].getId();
        }
        
        Settings.propDataServicesForUpdate.setStringArray(dataServiceArr);
      }
      
      if (mAutoUpdate.isSelected()) {
        if(Settings.propAutoDownloadType.getString().equals("never")) {
          Settings.propAutoDownloadType.setString(Settings.propAutoDownloadType.getDefault());
        }
        
        Settings.propAutoDownloadPeriod.setInt(((PeriodItem)mAutoDownloadPeriodSelection.getSelectedItem()).getDays());
      }
      else {
        Settings.propAutoDownloadType.setString("never");
      }
      
      Settings.propAutoDataDownloadEnabled.setBoolean(mAutoUpdate.isSelected() && mRecurrentUpdate.isSelected());
        
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
        mManuelDownloadPeriodSelection.setSelectedItem(item);
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
    addActionListener(e -> {
      panel.setVisible(!panel.isVisible());
      if(panel.isVisible()) {
        setText("<<");
      }
      else {
        setText(">>");
      }
      dialog.pack();
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

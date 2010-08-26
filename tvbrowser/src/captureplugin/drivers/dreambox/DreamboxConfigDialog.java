/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.ProgramReceiveTargetSelectionPanel;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import captureplugin.CapturePlugin;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;
import captureplugin.utils.ConfigTableModel;
import captureplugin.utils.ExternalChannelIf;
import captureplugin.utils.ExternalChannelTableCellEditor;
import captureplugin.utils.ExternalChannelTableCellRenderer;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Plugin;

/**
 * The configuration dialog for the dreambox
 */
public class DreamboxConfigDialog extends JDialog implements WindowClosingIf {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DreamboxConfigDialog.class);

    /** Configuration */
    private DreamboxConfig mConfig;
    /** Device */
    private DreamboxDevice mDevice;
    /** Was ok pressed ? */
    private boolean mOkPressed;
    /** The software version on the box */    
    private JComboBox mSoftwareSelection;
    /** IP-Address of the dreambox */
    private JTextField mDreamboxAddress;
    /** Device Name of the dreambox */
    private JTextField mDeviceName;
    /** Table with channel mappings */
    private JTable mTable;

    private SpinnerNumberModel mBeforeModel;
    private SpinnerNumberModel mAfterModel;
    private SpinnerNumberModel mTimeoutModel;

    private JComboBox mTimezone;
    private JTextField mUserName;
    private JPasswordField mPasswordField;
    
    private JTextField mMediaplayer;

    private JButton mRefreshButton;
    
    private ProgramReceiveTargetSelectionPanel mProgramReceiveTargetSelection;

  /**
   * Create the Dialog
   * 
   * @param parent
   *          Parent-Frame
   * @param device
   *          Device to configure
   * @param config
   *          Config for the Device
   */
    public DreamboxConfigDialog(Window parent, DreamboxDevice device,
      DreamboxConfig config) {
    super(parent);
    setModal(true);
        mConfig = config.clone();
        mDevice = device;
        createGui();
    }

    /**
     * Create the GUI
     */
    private void createGui() {
        setTitle(mLocalizer.msg("title", "Configure Dreambox"));

        UiUtilities.registerForClosing(this);

        EnhancedPanelBuilder basicPanel = new EnhancedPanelBuilder("2dlu, pref, 3dlu, fill:min:grow, 3dlu, pref, 3dlu, pref");
        basicPanel.setBorder(Borders.DLU4_BORDER);

        CellConstraints cc = new CellConstraints();

        basicPanel.addParagraph(mLocalizer.msg("misc", "Miscellaneous"));

        basicPanel.addRow();
        basicPanel.add(new JLabel(mLocalizer.msg("name","Name:")), cc.xy(2, basicPanel.getRow()));
        mDeviceName = new JTextField(mDevice.getName());
        basicPanel.add(mDeviceName, cc.xy(4, basicPanel.getRow()));

        basicPanel.addRow();
        basicPanel.add(new JLabel(mLocalizer.msg("softwareVersion","SoftwareVersion:")), cc.xy(2,basicPanel.getRow()));
        
        String[] values = {mLocalizer.msg("lowerVersion","lower than 1.6 (< 1.6)"),
                           mLocalizer.msg("higherVersion","at least 1.6 (>= 1.6)")};
        
        mSoftwareSelection = new JComboBox(values);
        mSoftwareSelection.setSelectedIndex(mConfig.getIsVersionAtLeast_1_6() ? 1 : 0);
        
        basicPanel.add(mSoftwareSelection, cc.xy(4, basicPanel.getRow()));
        
        basicPanel.addRow();
        basicPanel.add(new JLabel(mLocalizer.msg("ipaddress", "IP address")), cc.xy(2, basicPanel.getRow()));
        mDreamboxAddress = new JTextField(mConfig.getDreamboxAddress());
        basicPanel.add(mDreamboxAddress, cc.xy(4, basicPanel.getRow()));

        JButton help = new JButton(CapturePlugin.getInstance().createImageIcon("apps", "help-browser", 16));
        help.setToolTipText(Localizer.getLocalization(Localizer.I18N_HELP));
        help.setOpaque(false);
        help.setBorder(Borders.EMPTY_BORDER);
        basicPanel.add(help, cc.xy(8, basicPanel.getRow()));

        ButtonBarBuilder2 refresh = new ButtonBarBuilder2();

        refresh.addGlue();

        mRefreshButton = new JButton(mLocalizer.msg("refresh", "Refresh channellist"));
        mRefreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshChannelList();
            }
        });
        mRefreshButton.setIcon(TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));
        mRefreshButton.setEnabled(mConfig.hasValidAddress());

        mDreamboxAddress.getDocument().addDocumentListener(new DocumentListener() {
          
          @Override
          public void removeUpdate(DocumentEvent e) {
            check(e);
          }
          
          @Override
          public void insertUpdate(DocumentEvent e) {
            check(e);
          }
          
          @Override
          public void changedUpdate(DocumentEvent e) {
            check(e);
          }

          private void check(DocumentEvent e) {
            mRefreshButton.setEnabled(!mDreamboxAddress.getText().trim().isEmpty());
          }
        });

        refresh.addButton(new JButton[]{mRefreshButton});

        basicPanel.addRow();
        basicPanel.add(refresh.getPanel(), cc.xy(4, basicPanel.getRow()));

        basicPanel.addRow();
        basicPanel.add(new JLabel(mLocalizer.msg("preTime", "Time before in minutes:")), cc.xy(2,basicPanel.getRow()));

        mBeforeModel = new SpinnerNumberModel(mConfig.getPreTime(), 0, 60, 1);
        JSpinner beforeSpinner = new JSpinner(mBeforeModel);
        basicPanel.add(beforeSpinner, cc.xy(4, basicPanel.getRow()));

        basicPanel.addRow();
        basicPanel.add(new JLabel(mLocalizer.msg("afterTime", "Time after in minutes:")), cc.xy(2, basicPanel.getRow()));

        mAfterModel = new SpinnerNumberModel(mConfig.getAfterTime(), 0, 60, 1);
        JSpinner afterSpinner = new JSpinner(mAfterModel);
        basicPanel.add(afterSpinner, cc.xy(4, basicPanel.getRow()));

        basicPanel.addParagraph(mLocalizer.msg("channel", "Channel assignment"));

        mTable = new JTable(new ConfigTableModel(mConfig, mLocalizer.msg("dreambox", "Dreambox Channel")));
        mTable.getTableHeader().setReorderingAllowed(false);
        mTable.getColumnModel().getColumn(0).setCellRenderer(new util.ui.ChannelTableCellRenderer());
        mTable.getColumnModel().getColumn(1).setCellRenderer(new ExternalChannelTableCellRenderer());
        mTable.getColumnModel().getColumn(1).setCellEditor(new ExternalChannelTableCellEditor(mConfig));

        basicPanel.addGrowingRow();
        basicPanel.add(new JScrollPane(mTable), cc.xyw(2, basicPanel.getRow(), basicPanel.getColumnCount() - 1));

        ButtonBarBuilder2 builder = new ButtonBarBuilder2();

        builder.addGlue();

        JButton attach = new JButton(mLocalizer.msg("attach", "Attach"));
        attach.setToolTipText(mLocalizer.msg("attachHelp", "Attach channels"));
        attach.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                attachChannels();
            }
        });

        builder.addButton(attach);

        basicPanel.addRow();
        basicPanel.add(builder.getPanel(), cc.xyw(2,basicPanel.getRow(), basicPanel.getColumnCount() - 1));

        final EnhancedPanelBuilder extendedPanel = new EnhancedPanelBuilder("2dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu");
        extendedPanel.setBorder(Borders.DLU4_BORDER);

        extendedPanel.addParagraph(mLocalizer.msg("misc", "Miscellanious"));

        extendedPanel.addRow();
        extendedPanel.add(new JLabel(mLocalizer.msg("Timeout", "Timeout for connections in ms:")), cc.xy(2, extendedPanel.getRow()));

        mTimeoutModel = new SpinnerNumberModel(mConfig.getTimeout(), 0, 100000, 10);
        JSpinner timeoutSpinner = new JSpinner(mTimeoutModel);
        extendedPanel.add(timeoutSpinner, cc.xyw(4, extendedPanel.getRow(), 3));

        extendedPanel.addParagraph(mLocalizer.msg("timeZoneSeparator","Time zone"));
        
        extendedPanel.addRow();
        extendedPanel.add(new JLabel(mLocalizer.msg("timeZone", "Time zone:")), cc.xy(2, extendedPanel.getRow()));

        String[] zoneIds = new String[0];
        try {
          zoneIds = TimeZone.getAvailableIDs();
        } catch (Exception e) {
          e.printStackTrace();
        }
        Arrays.sort(zoneIds);
        mTimezone = new JComboBox(zoneIds);

        String zone = mConfig.getTimeZoneAsString();
        for (int i = 0; i < zoneIds.length; i++) {
          if (zoneIds[i].equals(zone)) {
            mTimezone.setSelectedIndex(i);
            break;
          }
        }

        extendedPanel.add(mTimezone, cc.xyw(4, extendedPanel.getRow(), 3));

        extendedPanel.addParagraph(mLocalizer.msg("security", "Security"));

        extendedPanel.addRow();
        extendedPanel.add(new JLabel(mLocalizer.msg("userName", "User name :")), cc.xy(2, extendedPanel.getRow()));
        mUserName = new JTextField(mConfig.getUserName());
        extendedPanel.add(mUserName, cc.xyw(4, extendedPanel.getRow(),3));

        extendedPanel.addRow();
        extendedPanel.add(new JLabel(mLocalizer.msg("password", "Password :")), cc.xy(2, extendedPanel.getRow()));
        mPasswordField = new JPasswordField(mConfig.getPassword());
        extendedPanel.add(mPasswordField, cc.xyw(4, extendedPanel.getRow(), 3));
        
        extendedPanel.addParagraph(mLocalizer.msg("streaming", "Streaming"));
        
        extendedPanel.addRow();
        extendedPanel.add(new JLabel(mLocalizer.msg("mediaplayer", "Mediaplayer :")), cc.xy(2, extendedPanel.getRow()));
        mMediaplayer = new JTextField(mConfig.getMediaplayer());
        extendedPanel.add(mMediaplayer, cc.xy(4, extendedPanel.getRow()));
        
        JButton select = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
        select.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser mediaplayerChooser = new JFileChooser();
                int returnVal = mediaplayerChooser.showOpenDialog(extendedPanel.getPanel());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    mMediaplayer.setText(mediaplayerChooser.getSelectedFile().getAbsolutePath());
               }
            }
        });
        extendedPanel.add(select, cc.xy(6, extendedPanel.getRow()));
        
        mProgramReceiveTargetSelection = new ProgramReceiveTargetSelectionPanel(UiUtilities.getLastModalChildOf(CapturePlugin.getInstance().getSuperFrame()),
            mConfig.getProgramReceiveTargets(),null,CapturePlugin.getInstance(),true,mLocalizer.msg("sendToTitle","Send scheduled programs to:"));
        
        extendedPanel.addRow();
        extendedPanel.addRow();
        extendedPanel.add(mProgramReceiveTargetSelection, cc.xyw(1,extendedPanel.getRow(),7));

        builder = new ButtonBarBuilder2();

        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        builder.addGlue();
        builder.addButton(new JButton[]{ok, cancel});

        getRootPane().setDefaultButton(ok);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add(mLocalizer.msg("basicTitle", "Basic settings"), basicPanel.getPanel());
        tabs.add(mLocalizer.msg("extendedTitle", "Extended settings"), extendedPanel.getPanel());

        JPanel content = (JPanel) getContentPane();
        content.setBorder(Borders.DLU4_BORDER);
        content.setLayout(new FormLayout("fill:pref:grow", "fill:pref:grow, 3dlu, pref"));
        content.add(tabs, cc.xy(1,1));
        content.add(builder.getPanel(), cc.xy(1,3));

        pack();
    }

    /**
     * Try to attach internal channels with dreambox channels
     */
    private void attachChannels() {
        Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
        ExternalChannelIf[] dchannels = mConfig.getExternalChannels();

        for (Channel channel:channels) {
            if (mConfig.getExternalChannel(channel) == null) {

                String name = normalizeName(channel.getName());

                for (ExternalChannelIf dch:dchannels) {
                    if (normalizeName(dch.getName()).equals(name)) {
                        mConfig.setExternalChannel(channel,  dch);
                    }
                }

            }

        }

        mTable.repaint();
    }

    /**
     * Normalizes a channel name. Lower case and no spaces.
     * @param name channel name
     * @return normalized channel name
     */
    private String normalizeName(String name) {
        return name.toLowerCase().replaceAll("\\W", "");
    }

    /**
     * Refresh all Channels and update the table
     */
    private void refreshChannelList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        mConfig.setDreamboxAddress(mDreamboxAddress.getText());

                        DreamboxConnector connect = new DreamboxConnector(mConfig);

                        try {
                          if (connect.testDreamboxVersion()) {
                            Collection<DreamboxChannel> channels = null;

                            try {
                                channels = connect.getChannels();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (channels == null) {
                                JOptionPane.showMessageDialog(DreamboxConfigDialog.this, mLocalizer.msg("errorText", "Sorry, could not load channellist from Dreambox."),
                                        mLocalizer.msg("errorTitle", "Error"), JOptionPane.ERROR_MESSAGE);
                            } else {
                                mConfig.setDreamboxChannels(channels.toArray(new DreamboxChannel[channels.size()]));
                                JOptionPane.showMessageDialog(DreamboxConfigDialog.this, mLocalizer.msg("okText", "Channellist updated."),
                                        mLocalizer.msg("okTitle", "Updated"), JOptionPane.INFORMATION_MESSAGE);
                            }
                            mTable.repaint();
                          } else {
                            JOptionPane.showMessageDialog(DreamboxConfigDialog.this, mLocalizer.msg("wrongVersion", "Wrong Version of Dreambox-WebInterface. Please update!"),
                                        mLocalizer.msg("errorTitle", "Error"), JOptionPane.INFORMATION_MESSAGE);
                          }
                        } catch (IOException e) {
                          JOptionPane.showMessageDialog(DreamboxConfigDialog.this, mLocalizer.msg("errorText", "Sorry, could not load channellist from Dreambox."),
                                  mLocalizer.msg("errorTitle", "Error"), JOptionPane.ERROR_MESSAGE);
                          e.printStackTrace();
                        }

                    }
                }).start();

            }
        });
    }

    /**
     * OK was pressed, config gets saved
     */
    private void okPressed() {
        mOkPressed = true;

        if (mTable.isEditing()) {
            TableCellEditor editor = mTable.getCellEditor();
            if (editor != null) {
              editor.stopCellEditing();
            }
        }

        mConfig.setAfterTime(mAfterModel.getNumber().intValue());
        mConfig.setBeforeTime(mBeforeModel.getNumber().intValue());
        mConfig.setTimeout(mTimeoutModel.getNumber().intValue());
        
        mConfig.setDreamboxAddress(mDreamboxAddress.getText());

        mConfig.setTimeZone(((String) mTimezone.getSelectedItem()));

        mConfig.setUserName(mUserName.getText());
        mConfig.setPassword(mPasswordField.getPassword());
        
        mConfig.setMediaplayer(mMediaplayer.getText());
        mConfig.setProgramReceiveTargets(mProgramReceiveTargetSelection.getCurrentSelection());
        
        mConfig.setIsVersionAtLeast_1_6(mSoftwareSelection.getSelectedIndex() == 1);
        
        setVisible(false);
    }

    /**
     * @return true, if ok was pressed
     */
    public boolean wasOkPressed() {
        return mOkPressed;
    }

    /**
     * @return current configuration
     */
    public DreamboxConfig getConfig() {
        return mConfig;
    }

    /**
     * Close the Dialog
     */
    public void close() {
        setVisible(false);
    }

    /**
     * @return Name of the Device
     */
    public String getDeviceName() {
        return mDeviceName.getText();
    }
}

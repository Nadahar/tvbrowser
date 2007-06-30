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
package captureplugin.drivers.dreambox.configdialog;

import captureplugin.CapturePlugin;
import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.drivers.dreambox.DreamboxDevice;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import util.ui.ChannelTableCellRenderer;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JPasswordField;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.TimeZone;
import java.util.Arrays;
import java.io.IOException;

import devplugin.Channel;
import tvbrowser.core.Settings;

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
    /** IP-Address of the dreambox */
    private JTextField mDreamboxAddress;
    /** Device Name of the dreambox */
    private JTextField mDeviceName;
    /** Table with channel mappings */
    private JTable mTable;

    private SpinnerNumberModel mBeforeModel;
    private SpinnerNumberModel mAfterModel;

    private JComboBox mTimezone;
    private JTextField mUserName;
    private JPasswordField mPasswordField;

    /**
     * Create the Dialog
     * @param frame Parent-Frame
     * @param device Device to configure
     * @param config Config for the Device
     */
    public DreamboxConfigDialog(JFrame frame, DreamboxDevice device, DreamboxConfig config) {
        super(frame, true);
        mConfig = config.clone();
        mDevice = device;
        createGui();
    }

    /**
     * Create the Dialog
     * @param frame Parent-Frame
     * @param device Device to configure
     * @param config Config for the Device
     */
    public DreamboxConfigDialog(JDialog frame, DreamboxDevice device, DreamboxConfig config) {
        super(frame, true);
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

        JPanel basicPanel = new JPanel();
        basicPanel.setBorder(Borders.DLU4_BORDER);

        basicPanel.setLayout(new FormLayout("5dlu, fill:min:grow",
                "pref, 3dlu, pref, 3dlu, pref, 3dlu, fill:min:grow, 3dlu, pref"));

        CellConstraints cc = new CellConstraints();

        basicPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("misc", "Miscellaneous")), cc.xyw(1, 1, 2));

        JPanel miscPanel = new JPanel(new FormLayout("right:pref, 3dlu, fill:min:grow, 3dlu, pref, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));

        miscPanel.add(new JLabel(mLocalizer.msg("name","Name:")), cc.xy(1, 1));
        mDeviceName = new JTextField(mDevice.getName());
        miscPanel.add(mDeviceName, cc.xy(3, 1));

        miscPanel.add(new JLabel(mLocalizer.msg("ipaddress", "IP address")), cc.xy(1, 3));
        mDreamboxAddress = new JTextField(mConfig.getDreamboxAddress());
        miscPanel.add(mDreamboxAddress, cc.xy(3, 3));

        JButton help = new JButton(CapturePlugin.getInstance().createImageIcon("apps", "help-browser", 16));
        help.setToolTipText(mLocalizer.msg("help", "Help"));
        help.setOpaque(false);
        help.setBorder(Borders.EMPTY_BORDER);
        miscPanel.add(help, cc.xy(7, 3));

        basicPanel.add(miscPanel, cc.xy(2, 3));

        ButtonBarBuilder refresh = new ButtonBarBuilder();

        refresh.addGlue();

        JButton refreshButton = new JButton(mLocalizer.msg("refresh", "Refresh channellist"));
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshChannelList();
            }
        });
        refreshButton.setIcon(CapturePlugin.getInstance().createImageIcon("apps", "view-refresh", 16));

        refresh.addGriddedButtons(new JButton[]{refreshButton});

        miscPanel.add(refresh.getPanel(), cc.xy(3, 5));

        miscPanel.add(new JLabel(mLocalizer.msg("preTime", "Time before in minutes:")), cc.xy(1,7));

        mBeforeModel = new SpinnerNumberModel(mConfig.getPreTime(), 0, 60, 1);
        JSpinner beforeSpinner = new JSpinner(mBeforeModel);
        miscPanel.add(beforeSpinner, cc.xy(3,7));

        miscPanel.add(new JLabel(mLocalizer.msg("afterTime", "Time after in minutes:")), cc.xy(1,9));

        mAfterModel = new SpinnerNumberModel(mConfig.getAfterTime(), 0, 60, 1);
        JSpinner afterSpinner = new JSpinner(mAfterModel);
        miscPanel.add(afterSpinner, cc.xy(3,9));

        basicPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("channel", "Channel assignment")), cc.xyw(1, 5, 2));

        mTable = new JTable(new ConfigTableModel(mConfig));
        mTable.getTableHeader().setReorderingAllowed(false);
        mTable.getColumnModel().getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
        mTable.getColumnModel().getColumn(1).setCellRenderer(new DreamboxChannelRenderer());
        mTable.getColumnModel().getColumn(1).setCellEditor(new DreamboxChannelEditor(mConfig));

        basicPanel.add(new JScrollPane(mTable), cc.xy(2, 7));

        ButtonBarBuilder builder = new ButtonBarBuilder();

        builder.addGlue();

        JButton attach = new JButton(mLocalizer.msg("attach", "Attach"));
        attach.setToolTipText(mLocalizer.msg("attachHelp", "Attach channels"));
        attach.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                attachChannels();
            }
        });

        builder.addGridded(attach);

        basicPanel.add(builder.getPanel(), cc.xyw(1,9,2));

        JPanel extendedPanel = new JPanel();
        extendedPanel.setBorder(Borders.DLU4_BORDER);
        extendedPanel.setLayout(new FormLayout("5dlu, right:pref, 3dlu, fill:pref:grow, 5dlu", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));

        extendedPanel.add(DefaultComponentFactory.getInstance().createSeparator("Timezone"), cc.xyw(1,1,5));
        extendedPanel.add(new JLabel(mLocalizer.msg("timeZone", "TimeZone :")), cc.xy(2,3));

        String[] zoneIds = TimeZone.getAvailableIDs();
        Arrays.sort(zoneIds);
        mTimezone = new JComboBox(zoneIds);

        String zone = mConfig.getTimeZoneAsString();
        for (int i = 0; i < zoneIds.length; i++) {
          if (zoneIds[i].equals(zone)) {
            mTimezone.setSelectedIndex(i);
            break;
          }
        }

        extendedPanel.add(mTimezone, cc.xy(4,3));

        extendedPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("security", "Security")), cc.xyw(1,5,5));

        extendedPanel.add(new JLabel(mLocalizer.msg("userName", "Username :")), cc.xy(2,7));
        mUserName = new JTextField(mConfig.getUserName());
        extendedPanel.add(mUserName, cc.xy(4,7));

        extendedPanel.add(new JLabel(mLocalizer.msg("password", "Password :")), cc.xy(2,9));
        mPasswordField = new JPasswordField(mConfig.getPassword());
        extendedPanel.add(mPasswordField, cc.xy(4,9));

        builder = new ButtonBarBuilder();

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
        builder.addGriddedButtons(new JButton[]{ok, cancel});

        getRootPane().setDefaultButton(ok);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add(mLocalizer.msg("basicTitle", "Basic settings"), basicPanel);
        tabs.add(mLocalizer.msg("extendedTitle", "Extended settings"), extendedPanel);

        JPanel content = (JPanel) getContentPane();
        content.setBorder(Borders.DLU4_BORDER);
        content.setLayout(new FormLayout("fill:pref:grow", "fill:pref:grow, 3dlu, pref"));
        content.add(tabs, cc.xy(1,1));
        content.add(builder.getPanel(), cc.xy(1,3));

        setSize(Sizes.dialogUnitXAsPixel(240, this), Sizes.dialogUnitYAsPixel(340, this));
    }

    /**
     * Try to attach internal channels with dreambox channels
     */
    private void attachChannels() {
        Channel[] channels = CapturePlugin.getPluginManager().getSubscribedChannels();
        DreamboxChannel[] dchannels = mConfig.getDreamboxChannels();

        for (Channel channel:channels) {
            if (mConfig.getDreamboxChannel(channel) == null) {

                String name = normalizeName(channel.getName());

                for (DreamboxChannel dch:dchannels) {
                    if (normalizeName(dch.getName()).equals(name)) {
                        mConfig.setDreamboxChannel(channel,  dch);
                    }
                }

            }

        }

        mTable.updateUI();
    }

    /**
     * Normalizes a channel name. Lowercase and no spaces.
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
                                mConfig.setDreamboxChannels(channels.toArray(new DreamboxChannel[0]));
                                JOptionPane.showMessageDialog(DreamboxConfigDialog.this, mLocalizer.msg("okText", "Channellist updated."),
                                        mLocalizer.msg("okTitle", "Updated"), JOptionPane.INFORMATION_MESSAGE);
                            }
                            mTable.updateUI();
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
            if (editor != null)
                editor.stopCellEditing();
        }

        mConfig.setAfterTime(mAfterModel.getNumber().intValue());
        mConfig.setBeforeTime(mBeforeModel.getNumber().intValue());

        mConfig.setDreamboxAddress(mDreamboxAddress.getText());

        mConfig.setTimeZone(((String) mTimezone.getSelectedItem()));

        mConfig.setUserName(mUserName.getText());
        mConfig.setPassword(mPasswordField.getPassword());

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

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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

import util.ui.Localizer;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import captureplugin.drivers.DeviceIf;
import devplugin.ActionMenu;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * Plugin to send the TV-Data to an external program
 *
 * @author Andreas Hessel, Bodo Tasche
 */
public class CapturePlugin extends devplugin.Plugin {
  private static final Version mVersion = new Version(3,00);

    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePlugin.class);

    /**
     * mData that stores the Settings
     */
    private CapturePluginData mConfig = new CapturePluginData();

    /**
     * Current Marked Programs
     */
    private Vector<Program> mMarkedPrograms = new Vector<Program>();

    /**
     * The Singleton
     */
    private static CapturePlugin mInstance = null;

    private boolean mAllowedToShowDialog = false;
    private boolean mNeedsUpdate = false;

    private Properties mSettings;

    /**
     * Root-Node for the Program-Tree
     */
    private PluginTreeNode mRootNode = new PluginTreeNode(this, false);
    private static final String RECORD = "##record";
    private static final String REMOVE = "##remove";

    private PluginInfo mPluginInfo;

    /**
     * Creates the Plugin
     */
    public CapturePlugin() {
        mInstance = this;
    }

    /**
     * Returns this Instance
     *
     * @return Instance
     */
    public static CapturePlugin getInstance() {
        return mInstance;
    }

    /**
     * Called by the host-application during start-up. Implement this method to
     * load any objects from the file system.
     *
     * @see #writeData(ObjectOutputStream)
     */
    public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mConfig = new CapturePluginData();
        mConfig.readData(in, this);
        updateMarkedPrograms();
    }

    /**
     * Counterpart to loadData. Called when the application shuts down.
     * Implement this method to store any objects to the file system.
     *
     * @see #readData(ObjectInputStream)
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        mConfig.writeData(out);
    }

    /**
     * Called by the host-application during start-up. Implements this method to
     * load your plugins settings from the file system.
     */
    public void loadSettings(Properties settings) {
      if(settings == null) {
        mSettings = new Properties();
      } else {
        mSettings = settings;
      }
    }

    /**
     * Called by the host-application during shut-down. Implements this method
     * to store your plugins settings to the file system.
     */
    public Properties storeSettings() {
        return mSettings;
    }

    public static Version getVersion() {
      return mVersion;
    }

    /**
     * Implement this function to provide information about your plugin.
     */
    public PluginInfo getInfo() {
      if(mPluginInfo == null) {
        String name = mLocalizer.msg("CapturePlugin", "Capture Plugin");
        String desc = mLocalizer.msg("Desc", "Starts a external Program with configurable Parameters");
        String author = "Bodo Tasche, Andreas Hessel";

        mPluginInfo = new PluginInfo(CapturePlugin.class, name, desc, author);
      }
      return mPluginInfo;
    }

    /**
     * Returns a new SettingsTab object, which is added to the settings-window.
     */
    public SettingsTab getSettingsTab() {
        return new CapturePluginSettingsTab((JFrame) getParentFrame(), this);
    }

    public ActionMenu getContextMenuActions(final Program program) {

        final DeviceIf[] devices = mConfig.getDeviceArray();

        final Window parent = UiUtilities.getLastModalChildOf(getParentFrame());

        String menuText = mLocalizer.msg("record", "record Program");
        ImageIcon menuIcon = createImageIcon("mimetypes", "video-x-generic", 16);

        ArrayList<ActionMenu> actionList = new ArrayList<ActionMenu>();

        for (final DeviceIf dev : devices) {
            ArrayList<AbstractAction> commandList = new ArrayList<AbstractAction>();

            if (dev.isAbleToAddAndRemovePrograms()) {
                final Program test = dev.getProgramForProgramInList(program);

                if (test != null) {
                    AbstractAction caction = new AbstractAction() {
                        public void actionPerformed(ActionEvent evt) {
                            dev.remove(parent, test);
                            updateMarkedPrograms();
                        }
                    };
                    caction.putValue(Action.NAME, Localizer.getLocalization(Localizer.I18N_DELETE));
                    commandList.add(caction);
                } else {
                    AbstractAction caction = new AbstractAction() {
                        public void actionPerformed(ActionEvent evt) {
                            if(dev.add(parent, program)) {
                              dev.sendProgramsToReceiveTargets(new Program[] {program});
                            }
                            updateMarkedPrograms();
                        }
                    };
                    caction.putValue(Action.NAME, mLocalizer.msg("record", "record"));
                    commandList.add(caction);
                }

            }

            String[] commands = dev.getAdditionalCommands();

            if (commands != null) {
              for (int y = 0; y < commands.length; y++) {

                  final int num = y;

                  AbstractAction caction = new AbstractAction() {

                      public void actionPerformed(ActionEvent evt) {
                          dev.executeAdditionalCommand(parent, num, program);
                      }
                  };
                  caction.putValue(Action.NAME, commands[y]);
                  commandList.add(caction);
              }
            }

            if (!commandList.isEmpty()) {
              actionList.add(new ActionMenu(dev.getName(), commandList.toArray(new Action[commandList.size()])));
            }
        }

        if (actionList.size() == 1) {
            ActionMenu menu = actionList.get(0);

            if (menu.getSubItems().length == 0) {
                return null;
            }

            if (menu.getSubItems().length == 1) {
                Action action = menu.getSubItems()[0].getAction();
                action.putValue(Action.SMALL_ICON, menuIcon);
                return new ActionMenu(action);
            } else {
                return new ActionMenu(menu.getTitle(), menuIcon, menu.getSubItems());
            }

        }

        ActionMenu[] actions = new ActionMenu[actionList.size()];
        actionList.toArray(actions);

        if (actions.length == 0) {
            return null;
        }

        return new ActionMenu(menuText, menuIcon, actions);
    }

    /**
     * Updates the marked Programs.
     */
    protected void updateMarkedPrograms() {
        Vector<Program> list = getMarkedByDevices();

        for (Program aList : list) {

            if (mMarkedPrograms.contains(aList)) {
                mMarkedPrograms.remove(aList);
            }

            aList.mark(this);
        }

        for (Program mMarkedProgram : mMarkedPrograms) {
            mMarkedProgram.unmark(this);
        }

        mMarkedPrograms = list;

        updateTreeNode();
        super.saveMe();
    }

    /**
     * This Function Iterates over all Devices and collects the list of Programs
     * to mark...
     *
     * @return List with all Programs to mark
     */
    private Vector<Program> getMarkedByDevices() {
        Vector<Program> v = new Vector<Program>();

        for (Object o : mConfig.getDevices()) {
            DeviceIf device = (DeviceIf) o;

            Program[] programs = device.getProgramList();

            if (programs != null) {
                for (Program program : programs) {
                    if (!v.contains(program)) {
                        v.add(program);
                    }
                }
            }
        }

        return v;
    }

    public ActionMenu getButtonAction() {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showDialog();
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("CapturePlugin", "Capture Plugin"));
        action.putValue(Action.SMALL_ICON, createImageIcon("mimetypes", "video-x-generic", 16));
        action.putValue(BIG_ICON, createImageIcon("mimetypes", "video-x-generic", 22));

        return new ActionMenu(action);
    }

    /**
     * This method is invoked by the host-application if the user has chosen
     * your plugin from the menu.
     */
    protected void showDialog() {
        CapturePluginDialog dialog = new CapturePluginDialog(getParentFrame(), mConfig);

        layoutWindow("captureDlg", dialog, new Dimension(500,450));

        if (mConfig.getDevices().isEmpty()) {
            dialog.show(CapturePluginPanel.TAB_DEVICELIST);
        } else {
            dialog.show(CapturePluginPanel.TAB_PROGRAMLIST);
        }

        updateMarkedPrograms();
    }

    public ThemeIcon getMarkIconFromTheme() {
        return new ThemeIcon("mimetypes", "video-x-generic", 16);
    }

    /**
     * Sets the CaputePluginData
     *
     * @param data CapturePluginData
     */
    public void setCapturePluginData(CapturePluginData data) {
        mConfig = data;
    }

    /**
     * Returns the CapturePluginData
     *
     * @return The CapaturePluginData
     */
    public CapturePluginData getCapturePluginData() {
        return mConfig;
    }

    /**
     * Update the TreeNode
     */
    private void updateTreeNode() {
        mRootNode.removeAllChildren();
        mRootNode.removeAllActions();

        for (Object o : mConfig.getDevices()) {
            final DeviceIf device = (DeviceIf) o;

            PluginTreeNode node;
            if (mConfig.getDevices().size() > 1) {
              node = new PluginTreeNode(device.getName());
              mRootNode.add(node);
            }
            else {
              node = mRootNode;
            }

            if(device.isAbleToAddAndRemovePrograms()) {
              node.getMutableTreeNode().setProgramReceiveTarget(new ProgramReceiveTarget(this, device.getName() + " - " + mLocalizer.msg("record", "record"), device.getId() + RECORD));
            }

            Program[] programs = device.getProgramList();

            if (programs != null) {
              for (Program program : programs) {
                  node.addProgram(program);
              }
            }

            node.addAction(new AbstractAction(mLocalizer.msg("configure", "Configure '{0}'", device.getName())) {
              @Override
              public void actionPerformed(ActionEvent e) {
                device.configDevice(UiUtilities.getBestDialogParent(getParentFrame()));
                updateTreeNode();
              }
            });
        }

        mRootNode.update();
    }

    /**
     * Get the Root-Node.
     * The CapturePlugin handles all Programs for itself. Some
     * Devices can remove Programs externaly
     */
    public PluginTreeNode getRootNode() {
        return mRootNode;
    }

    public boolean canUseProgramTree() {
        return true;
    }

    @Override
    public boolean canReceiveProgramsWithTarget() {
        return getProgramReceiveTargets().length >0;
    }

    @Override
    public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
        if (receiveTarget == null || receiveTarget.getTargetId() == null ||
            receiveTarget.getTargetId().indexOf('#') == -1) {
          return false;
        }

        String id = receiveTarget.getTargetId();
        String deviceid = StringUtils.substringBefore(id,"#");
    String command = id.substring(id.indexOf('#'));

        for (DeviceIf device : mConfig.getDevices()) {
            if (device.getId().equals(deviceid)) {
                if (command.equals(REMOVE)) {
                    for (Program program:programArr) {
                      if(device.isInList(program)) {
                        device.remove(getParentFrame(), program);
                      }
                    }
                    updateMarkedPrograms();
                    return true;
                } else if (command.equals(RECORD)) {
                    ArrayList<Program> successfullPrograms = new ArrayList<Program>(programArr.length);

                    for (Program program:programArr) {
                        if(device.add(getParentFrame(), program)) {
                          successfullPrograms.add(program);
                        }
                    }
                    device.sendProgramsToReceiveTargets(successfullPrograms.toArray(new Program[successfullPrograms.size()]));
                    updateMarkedPrograms();
                    return true;
                }

                if (command.startsWith("#_")) {
                    command = command.substring(2);

                    String[] cmdstr = device.getAdditionalCommands();

                    for (int i = 0;i < cmdstr.length;i++) {
                        if (cmdstr[i].equals(command)) {
                            for (Program program:programArr) {
                                device.executeAdditionalCommand(getParentFrame(), i, program);
                            }
                            return true;
                        }
                    }

                }
            }
        }

        return false;
    }

    @Override
    public ProgramReceiveTarget[] getProgramReceiveTargets() {
        ArrayList<ProgramReceiveTarget> targets = new ArrayList<ProgramReceiveTarget>();

        for (DeviceIf device : mConfig.getDevices()) {
            if (device.isAbleToAddAndRemovePrograms()) {
                targets.add(new ProgramReceiveTarget(this, device.getName() + " - " + mLocalizer.msg("record", "record"), device.getId() + RECORD));
                targets.add(new ProgramReceiveTarget(this, device.getName() + " - " + mLocalizer.msg("remove", "remove"), device.getId() + REMOVE));
            }

            for (String command : device.getAdditionalCommands()) {
                targets.add(new ProgramReceiveTarget(this, device.getName() + " - " + command, device.getId() + "#_" + command));
            }
        }

        return targets.toArray(new ProgramReceiveTarget[targets.size()]);
    }

    public void handleTvBrowserStartFinished() {
      mAllowedToShowDialog = true;

      if(mNeedsUpdate) {
        handleTvDataUpdateFinished();
      }
    }

    /**
     * Check the programs after data update.
     */
    public void handleTvDataUpdateFinished() {
      mNeedsUpdate = true;

      if(mAllowedToShowDialog) {
        mNeedsUpdate = false;

        final DeviceIf[] devices = mConfig.getDeviceArray();

        final DefaultTableModel model = new DefaultTableModel() {
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

        model.setColumnCount(5);
        model.setColumnIdentifiers(new String[] {mLocalizer.msg("device","Device"),Localizer.getLocalization(Localizer.I18N_CHANNEL),mLocalizer.msg("date","Date"),ProgramFieldType.START_TIME_TYPE.getLocalizedName(),ProgramFieldType.TITLE_TYPE.getLocalizedName()});

        UIThreadRunner.invokeLater(new Runnable() {

          @Override
          public void run() {
            JTable table = new JTable(model);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setResizingAllowed(false);
            table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
              public Component getTableCellRendererComponent(JTable renderTable, Object value,
                  boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(renderTable,value,isSelected,hasFocus,row,column);

                if(value instanceof DeviceIf) {
                  if(((DeviceIf)value).getDeleteRemovedProgramsAutomatically() && !isSelected) {
                    c.setForeground(Color.red);
                  }
                }

                return c;
              }
            });

            int[] columnWidth = new int[5];

            for(int i = 0; i < columnWidth.length; i++) {
              columnWidth[i] =  UiUtilities.getStringWidth(table.getFont(),model.getColumnName(i)) + 10;
            }

            for (DeviceIf device : devices) {
              Program[] deleted = device.checkProgramsAfterDataUpdateAndGetDeleted();

              if(deleted != null && deleted.length > 0) {
                for(Program p : deleted) {
                  if(device.getDeleteRemovedProgramsAutomatically() && !p.isExpired() && !p.isOnAir()) {
                    device.remove(UiUtilities.getLastModalChildOf(getParentFrame()), p);
                  } else {
                    device.removeProgramWithoutExecution(p);
                  }

                  if(!p.isExpired()) {
                    Object[] o = new Object[] {device,p.getChannel().getName(),p.getDateString(),p.getTimeString(),p.getTitle()};

                    for(int i = 0; i < columnWidth.length; i++) {
                      columnWidth[i] = Math.max(columnWidth[i],UiUtilities.getStringWidth(table.getFont(),o[i].toString())+10);
                    }

                    model.addRow(o);
                  }
                }
              }

              device.getProgramList();
            }

            if(model.getRowCount() > 0) {
              int sum = 0;

              for(int i = 0; i < columnWidth.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);

                if(i < columnWidth.length-1) {
                  table.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i]);
                }

                sum += columnWidth[i];
              }

              JScrollPane scrollPane = new JScrollPane(table);
              scrollPane.setPreferredSize(new Dimension(450,250));

              if(sum > 500) {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                scrollPane.getViewport().setPreferredSize(new Dimension(sum,scrollPane.getViewport().getPreferredSize().height));
              }

              JButton export = new JButton(mLocalizer.msg("exportList","Export list"));
              export.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  JFileChooser chooser = new JFileChooser();
                  chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                  chooser.setFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                      return f.isDirectory() || f.toString().toLowerCase().endsWith(".txt");
                    }

                    public String getDescription() {
                      return "*.txt";
                    }
                  });

                  chooser.setSelectedFile(new File("RemovedPrograms.txt"));
                  if (chooser.showSaveDialog(UiUtilities.getLastModalChildOf(getParentFrame())) == JFileChooser.APPROVE_OPTION) {
                    if(chooser.getSelectedFile() != null) {
                      String file = chooser.getSelectedFile().getAbsolutePath();

                      if (!file.toLowerCase().endsWith(".txt")
                        && file.indexOf('.') == -1) {
                        file = file + ".txt";
                      }

                      if (file.indexOf('.') != -1) {
                        try {
                          RandomAccessFile write = new RandomAccessFile(file,"rw");
                          write.setLength(0);

                          String eolStyle = File.separator.equals("/") ? "\n" : "\r\n";

                          for(int i = 0; i < model.getRowCount(); i++) {
                            StringBuilder line = new StringBuilder();

                            for(int j = 0; j < model.getColumnCount(); j++) {
                              line.append(model.getValueAt(i, j)).append(' ');
                            }

                            line.append(eolStyle);

                            write.writeBytes(line.toString());
                          }

                          write.close();
                        }catch(Exception ee) {}
                      }
                    }
                  }
                }
              });

              Object[] message = {mLocalizer.msg("deletedText","The data was changed and the following programs were deleted:"),scrollPane,export};

              JOptionPane pane = new JOptionPane();
              pane.setMessage(message);
              pane.setMessageType(JOptionPane.PLAIN_MESSAGE);

              final JDialog d = pane.createDialog(UiUtilities.getLastModalChildOf(getParentFrame()), mLocalizer.msg("CapturePlugin","CapturePlugin") + " - " + mLocalizer.msg("deletedTitle","Deleted programs"));
              d.setResizable(true);
              d.setModal(false);

              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  d.setVisible(true);
                }
              });
            }
          }
        });
      }
    }

    /**
     * @return The parent frame.
     */
    public Frame getSuperFrame() {
      return getParentFrame();
    }

    public int getMarkPriorityForProgram(Program p) {
      return mConfig.getMarkPriority();
    }
}
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package programlistplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.ArrayUtils;

import util.program.ProgramUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

/**
 * Shows all important programs in a time sorted list.
 *
 * @author René Mach
 * @since 2.7
 */
public class ProgramListPlugin extends Plugin {
  /**
   * maximum number of programs to be shown in the list. If the filter has more results, only the first results are shown.
   */
  private static final int MAX_PROGRAMS_SHOWN = 5000;

  protected static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListPlugin.class);

  private static Version mVersion = new Version(3, 0);

  private JDialog mDialog;
  private JComboBox mBox;
  private ProgramFilter mFilter;
  private ProgramListSettings mSettings;
  private ArrayList<Program> mPrograms = new ArrayList<Program>();
  private DefaultListModel mModel;
  private ProgramList mList;
  private ProgramPanelSettings mProgramPanelSettings;
  private JCheckBox mShowDescription;
  private JComboBox mFilterBox;

  private ProgramFilter mReceiveFilter;

  private static ProgramListPlugin mInstance;

  private JButton mSendBtn;

  /**
   * Creates an instance of this class.
   */
  public ProgramListPlugin() {
    mInstance = this;
  }

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(ProgramListPlugin.class, mLocalizer.msg("name", "Program list"), mLocalizer.msg(
        "description", "Shows programs in a list."), "René Mach");
  }

  /**
   * Gets the instance of this plugin.
   *
   * @return The instance of this plugin.
   */
  public static ProgramListPlugin getInstance() {
    if (mInstance == null) {
      new ProgramListPlugin();
    }

    return mInstance;
  }

  public void loadSettings(final Properties properties) {
    mSettings = new ProgramListSettings(properties);
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        mReceiveFilter = null;
        showProgramList();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("name", "Important programs"));
    action.putValue(Action.SMALL_ICON, createImageIcon("emblems", "emblem-important", 16));
    action.putValue(Plugin.BIG_ICON, createImageIcon("emblems", "emblem-important", 22));

    return new ActionMenu(action);
  }

  private void fillFilterBox() {
    // initialize filter as allFilter because we may no longer find the filter
    // with the stored name
    if (mFilter == null) {
      mFilter = Plugin.getPluginManager().getFilterManager().getAllFilter();
    }
    ArrayList<ProgramFilter> filters = new ArrayList<ProgramFilter>();
    for (ProgramFilter filter : Plugin.getPluginManager().getFilterManager().getAvailableFilters()) {
      filters.add(filter);
    }
    if (mReceiveFilter != null) {
      filters.add(mReceiveFilter);
    }

    for (ProgramFilter filter : filters) {
      boolean found = false;

      for (int i = 0; i < mFilterBox.getItemCount(); i++) {
        if (filter != null && filter.equals(mFilterBox.getItemAt(i))) {
          found = true;
          break;
        }
      }

      if (!found) {
        mFilterBox.addItem(filter);

        if ((mReceiveFilter == null && filter.getName().equals(mSettings.getFilterName()))
            || (mReceiveFilter != null && filter.getName().equals(mReceiveFilter.getName()))) {
          mFilter = filter;
          mFilterBox.setSelectedItem(filter);
        }
      }
    }

    for (int i = mFilterBox.getItemCount() - 1; i >= 0; i--) {
      boolean found = false;

      for (ProgramFilter filter : filters) {
        if (filter.equals(mFilterBox.getItemAt(i))) {
          found = true;
          break;
        }
      }

      if (!found) {
        mFilterBox.removeItemAt(i);
      }
    }

  }

  private void fillProgramList() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          mModel.clear();
          mPrograms.clear();

          boolean showExpired = false;

          Channel[] channels = mBox.getSelectedItem() instanceof String ? Plugin.getPluginManager()
              .getSubscribedChannels() : new Channel[] { (Channel) mBox.getSelectedItem() };

          Date date = channels.length > 1
              && mFilter.equals(Plugin.getPluginManager().getFilterManager().getAllFilter()) ? Plugin
              .getPluginManager().getCurrentDate() : Date.getCurrentDate();

          int startTime = Plugin.getPluginManager().getTvBrowserSettings().getProgramTableStartOfDay();
          int endTime = Plugin.getPluginManager().getTvBrowserSettings().getProgramTableEndOfDay();

          int maxDays = channels.length > 1
              && mFilter.equals(Plugin.getPluginManager().getFilterManager().getAllFilter()) ? 2 : 28;
          for (int d = 0; d < maxDays; d++) {
            if (Plugin.getPluginManager().isDataAvailable(date)) {
              for (Channel channel : channels) {
                for (Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, channel); it.hasNext();) {
                  Program program = it.next();
                  if ((showExpired || !program.isExpired()) && mFilter.accept(program)) {
                    if (mFilter.equals(Plugin.getPluginManager().getFilterManager().getAllFilter())) {
                      if ((d == 0 && program.getStartTime() >= startTime)
                          || (d == 1 && program.getStartTime() <= endTime)) {
                        mPrograms.add(program);
                      }
                    } else {
                      mPrograms.add(program);
                    }
                  }
                }
              }
            }
            date = date.addDays(1);
          }

          if (channels.length > 1) {
            Collections.sort(mPrograms, ProgramUtilities.getProgramComparator());
          }

          int index = -1;

          for (Program program : mPrograms) {
            if (mModel.size() < MAX_PROGRAMS_SHOWN) {
              mModel.addElement(program);

              if (!program.isExpired() && index == -1) {
                index = mModel.getSize() - 1;
              }
            }
          }
          int forceScrollingIndex = mModel.size() - 1;
          if (forceScrollingIndex > 1000) {
            forceScrollingIndex = 1000;
          }
          mList.ensureIndexIsVisible(forceScrollingIndex);
          mList.ensureIndexIsVisible(index);
          mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void onDeactivation() {
    if (mDialog != null) {
      mDialog.setVisible(false);
    }
  }

  private static class ChannelListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (c instanceof JLabel && value instanceof Channel) {
        ((JLabel) c).setIcon(UiUtilities.createChannelIcon(((Channel) value).getIcon()));
      }

      return c;
    }
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this, mLocalizer.msg("programTarget", "Program list"),
        "program list") };
  }

  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    final List<Program> programs = Arrays.asList(programArr);
    mReceiveFilter = new ProgramFilter() {
      @Override
      public boolean accept(Program prog) {
        return programs.contains(prog);
      }

      @Override
      public String getName() {
        return mLocalizer.msg("filterName", "Received programs");
      }

      @Override
      public String toString() {
        return mLocalizer.msg("filterName", "Received programs");
      }
    };
    showProgramList();
    return true;
  }

  private void showProgramList() {
    showProgramList(null);
  }

  private void showProgramList(final Channel selectedChannel) {
    try {
      if (mDialog == null) {
        mDialog = new JDialog(getParentFrame());
        mDialog.setTitle(mLocalizer.msg("name", "Important programs"));
        mDialog.getContentPane().setLayout(new BorderLayout(0, 10));
        ((JPanel) mDialog.getContentPane()).setBorder(Borders.DIALOG_BORDER);

        UiUtilities.registerForClosing(new WindowClosingIf() {

          public void close() {
            closeDialog();
          }

          public JRootPane getRootPane() {
            return mDialog.getRootPane();
          }
        });

        mModel = new DefaultListModel();
        boolean showDescription = mSettings.getShowDescription();
        mProgramPanelSettings = new ProgramPanelSettings(new PluginPictureSettings(
            PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), !showDescription, ProgramPanelSettings.X_AXIS);
        mList = new ProgramList(mModel, mProgramPanelSettings);

        mList.addMouseListeners(null);

        Channel[] subscribedChannels = Plugin.getPluginManager().getSubscribedChannels();
        mBox = new JComboBox(subscribedChannels);
        mBox.insertItemAt(mLocalizer.msg("allChannels", "All channels"), 0);
        mBox.setRenderer(new ChannelListCellRenderer());
        mBox.setSelectedIndex(mSettings.getIndex());
        if (selectedChannel != null) {
          mBox.setSelectedItem(selectedChannel);
        }

        mFilterBox = new JComboBox();

        if (mSettings.getFilterName().isEmpty()) {
          mSettings.setFilterName(Plugin.getPluginManager().getFilterManager().getAllFilter().getName());
        }

        fillFilterBox();

        mFilterBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            mFilter = (ProgramFilter) mFilterBox.getSelectedItem();
            if (mFilter != mReceiveFilter) {
              mSettings.setFilterName(mFilter.getName());
            }
            mBox.getItemListeners()[0].itemStateChanged(null);
          }
        });

        mBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            fillProgramList();
            mSettings.setIndex(mBox.getSelectedIndex());
          }
        });

        CellConstraints cc = new CellConstraints();

        JPanel panel = new JPanel(new FormLayout("1dlu,default,3dlu,default:grow", "pref,2dlu,pref,2dlu"));
        panel.add(new JLabel(Localizer.getLocalization(Localizer.I18N_CHANNELS) + ":"), cc.xy(2, 1));
        panel.add(mBox, cc.xy(4, 1));
        panel.add(new JLabel(mLocalizer.msg("filter", "Filter:")), cc.xy(2, 3));
        panel.add(mFilterBox, cc.xy(4, 3));

        mSendBtn = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
        mSendBtn.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
        mSendBtn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Program[] programs = mList.getSelectedPrograms();
            if (programs == null || programs.length == 0) {
              programs = mPrograms.toArray(new Program[mPrograms.size()]);
            }
            if (programs != null && programs.length > 0) {
              SendToPluginDialog sendDialog = new SendToPluginDialog(ProgramListPlugin.getInstance(), (Window) mDialog,
                  programs);
              sendDialog.setVisible(true);
            }
          }
        });

        mShowDescription = new JCheckBox(mLocalizer.msg("showProgramDescription", "Show program description"),
            showDescription);
        mShowDescription.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            int topRow = mList.getFirstVisibleIndex();
            mProgramPanelSettings.setShowOnlyDateAndTitle(e.getStateChange() == ItemEvent.DESELECTED);
            mSettings.setShowDescription(e.getStateChange() == ItemEvent.SELECTED);
            mList.updateUI();
            if (topRow != -1) {
              mList.ensureIndexIsVisible(topRow);
            }
          }
        });

        JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
        close.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            closeDialog();
          }
        });

        JPanel southPanel = new JPanel(new FormLayout("default,5dlu,default,0dlu:grow,default", "default"));
        southPanel.add(mSendBtn, cc.xy(1, 1));
        southPanel.add(mShowDescription, cc.xy(3, 1));
        southPanel.add(close, cc.xy(5, 1));

        mDialog.getContentPane().add(panel, BorderLayout.NORTH);
        mDialog.getContentPane().add(new JScrollPane(mList), BorderLayout.CENTER);
        mDialog.getContentPane().add(southPanel, BorderLayout.SOUTH);

        layoutWindow("programListWindow", mDialog, new Dimension(500, 500));

        mDialog.setVisible(true);

        mBox.getItemListeners()[0].itemStateChanged(null);
      } else {
        if (!mDialog.isVisible()) {
          fillFilterBox();
        }

        mDialog.setVisible(!mDialog.isVisible());

        if (mDialog.isVisible()) {
          fillProgramList();
        } else {
          saveMe();
        }
      }
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  private void closeDialog() {
    mDialog.setVisible(false);
    saveMe();
  }

  @Override
  public ActionMenu getContextMenuActions(final Channel channel) {
    return new ActionMenu(new AbstractAction(mLocalizer.msg("showChannel", "Show programs of channel in program list")) {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        showProgramList(channel);
      }
    });
  }
}

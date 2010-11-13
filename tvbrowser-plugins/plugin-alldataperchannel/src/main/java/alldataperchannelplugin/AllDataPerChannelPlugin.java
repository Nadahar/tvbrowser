package alldataperchannelplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.Version;

/**
 * Shows all data for a channel in a program list.
 *
 * @author René Mach
 * @deprecated in favor of the program list plugin
 */
public class AllDataPerChannelPlugin extends Plugin {
  private Localizer mLocalizer = Localizer.getLocalizerFor(AllDataPerChannelPlugin.class);

  private JDialog mDialog;
  private JComboBox mBox;
  private ProgramFilter mFilter;
  private Properties mSettings;
  private DefaultListModel mModel;
  private ProgramList mList;

  public static Version getVersion() {
    return new Version(0, 12, 0, true);
  }

  public PluginInfo getInfo() {
    return new PluginInfo(AllDataPerChannelPlugin.class, mLocalizer.msg("name", "All programs per channel"),
        mLocalizer.msg("description", "Shows all available programs of a channel in a list."), "René Mach", "GPL");
  }

  public void loadSettings(Properties settings) {
    if (settings == null) {
      mSettings = new Properties();
    } else {
      mSettings = settings;
    }
  }

  public Properties storeSettings() {
    return mSettings;
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (mDialog == null) {
          mDialog = new JDialog(getParentFrame());
          mDialog.setTitle(mLocalizer.msg("name", "All programs per channel"));
          mDialog.getContentPane().setLayout(new BorderLayout());

          mModel = new DefaultListModel();
          mList = new ProgramList(mModel);

          mList.addMouseListeners(null);
          mBox = new JComboBox(getPluginManager().getSubscribedChannels());
          mBox.setRenderer(new ChannelListCellRenderer());

          final JComboBox mFilterBox = new JComboBox(getPluginManager().getFilterManager().getAvailableFilters());

          if (mSettings.getProperty("filter") == null) {
            mFilter = getPluginManager().getFilterManager().getAllFilter();
            mFilterBox.setSelectedItem(mFilter);
          } else {
            for (int i = 0; i < mFilterBox.getItemCount(); i++) {
              if (((ProgramFilter) mFilterBox.getItemAt(i)).getName().equals(mSettings.getProperty("filter"))) {
                mFilter = (ProgramFilter) mFilterBox.getItemAt(i);
                mFilterBox.setSelectedItem(mFilterBox.getItemAt(i));
                break;
              }
            }
          }

          mFilterBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              mFilter = (ProgramFilter) mFilterBox.getSelectedItem();
              mSettings.setProperty("filter", mFilter.getName());
              mBox.getItemListeners()[0].itemStateChanged(null);
            }
          });

          mBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              showOnTimeProgram();
            }
          });

          CellConstraints cc = new CellConstraints();

          JPanel panel = new JPanel(new FormLayout("1dlu,default,3dlu,default:grow", "pref,2dlu,pref,2dlu"));
          panel.add(new JLabel(mLocalizer.msg("channel", "Channel:")), cc.xy(2, 1));
          panel.add(mBox, cc.xy(4, 1));
          panel.add(new JLabel(mLocalizer.msg("filter", "Filter:")), cc.xy(2, 3));
          panel.add(mFilterBox, cc.xy(4, 3));

          mDialog.getContentPane().add(panel, BorderLayout.NORTH);
          mDialog.getContentPane().add(new JScrollPane(mList), BorderLayout.CENTER);

          mDialog.setSize(500, 500);
          mDialog.setLocationRelativeTo(getParentFrame());
          mDialog.setVisible(true);

          mBox.getItemListeners()[0].itemStateChanged(null);
        } else {
          mDialog.setVisible(!mDialog.isVisible());

          if (mDialog.isVisible()) {
            showOnTimeProgram();
          }
        }
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("name", "All programs per channel"));
    action.putValue(Action.SMALL_ICON, createImageIcon("apps", "help-browser", 16));
    action.putValue(Plugin.BIG_ICON, createImageIcon("apps", "help-browser", 22));

    return new ActionMenu(action);
  }

  private void showOnTimeProgram() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Channel ch = (Channel) mBox.getSelectedItem();
        mModel.clear();

        Date date = Date.getCurrentDate();
        Iterator<Program> chDayProg = getPluginManager().getChannelDayProgram(date, ch);

        int index = -1;
        int emptyDays = 0;
        while (chDayProg != null && emptyDays < 7) {
          while (chDayProg.hasNext()) {
            Program p = chDayProg.next();

            if (mFilter.accept(p)) {
              mModel.addElement(p);
            }

            if (!p.isExpired() && index == -1) {
              index = mModel.getSize() - 1;
            }
          }

          date = date.addDays(1);
          chDayProg = getPluginManager().getChannelDayProgram(date, ch);
          if (chDayProg == null || !chDayProg.hasNext()) {
            emptyDays++;
          } else {
            emptyDays = 0;
          }
        }
        int forceScrollingIndex = mModel.size() - 1;
        if (forceScrollingIndex > 1000) {
          forceScrollingIndex = 1000;
        }
        mList.ensureIndexIsVisible(forceScrollingIndex);
        mList.ensureIndexIsVisible(index);
        mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }

  private class ChannelListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (c instanceof JLabel && value instanceof Channel) {
        ((JLabel) c).setIcon(UiUtilities.createChannelIcon(((Channel) value).getIcon()));
      }

      return c;
    }
  }
}

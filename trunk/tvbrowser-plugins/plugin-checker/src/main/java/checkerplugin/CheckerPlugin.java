/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package checkerplugin;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import checkerplugin.check.AbstractCheck;
import checkerplugin.check.ArticleCheck;
import checkerplugin.check.DescriptionRepetitionCheck;
import checkerplugin.check.DurationCheck;
import checkerplugin.check.EllipsisCheck;
import checkerplugin.check.EmptyLinesCheck;
import checkerplugin.check.FormatCheck;
import checkerplugin.check.GenreCategoryCheck;
import checkerplugin.check.LiveCheck;
import checkerplugin.check.ModerationCheck;
import checkerplugin.check.NetPlayTimeCheck;
import checkerplugin.check.NewCheck;
import checkerplugin.check.PersonsCheck;
import checkerplugin.check.PrintableCharactersCheck;
import checkerplugin.check.RepetitionCheck;
import checkerplugin.check.SeriesEpisodeCheck;
import checkerplugin.check.SeriesNumberCheck;
import checkerplugin.check.ShortDescriptionLengthCheck;
import checkerplugin.check.TextFieldFormatCheck;
import checkerplugin.check.TitleFormatCheck;
import checkerplugin.check.UnknownProgramCheck;
import checkerplugin.check.UrlCheck;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author bananeweizen
 * 
 */
public class CheckerPlugin extends Plugin {

  private static final Version mVersion = new Version(0, 3, false);

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(CheckerPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  private PluginInfo mPluginInfo;

  private ImageIcon mInfoIcon = createImageIcon("status", "dialog-information", 16);
  private ImageIcon mWarnIcon = createImageIcon("status", "dialog-warning", 16);

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private CheckerSettings mSettings;

  private static final AbstractCheck[] ALL_CHECKS = new AbstractCheck[] { new ArticleCheck(),
      new DescriptionRepetitionCheck(), new DurationCheck(), new EllipsisCheck(), new EmptyLinesCheck(),
      new FormatCheck(), new GenreCategoryCheck(), new LiveCheck(), new ModerationCheck(), new NetPlayTimeCheck(),
      new NewCheck(), new PersonsCheck(), new PrintableCharactersCheck(), new RepetitionCheck(), new SeriesEpisodeCheck(), new SeriesNumberCheck(),
      new ShortDescriptionLengthCheck(), new TextFieldFormatCheck(), new TitleFormatCheck(), new UnknownProgramCheck(), new UrlCheck() };

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Checker");
      final String desc = mLocalizer.msg("description", "Checks program data for bugs.");
      mPluginInfo = new PluginInfo(CheckerPlugin.class, name, desc, "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  @Override
  public Icon[] getMarkIconsForProgram(final Program program) {
    if (program == null || getPluginManager().getExampleProgram().equals(program)) {
      return new Icon[] { mWarnIcon };
    }
    if (!isSupportedChannel(program.getChannel())) {
      return null;
    }

    ArrayList<Issue> issues = getIssues(program);
    if (issues.isEmpty()) {
      return null;
    }
    for (Issue issue : issues) {
      if (issue.getSeverity() == Issue.SEVERITY_ERROR) {
        return new Icon[] { mWarnIcon };
      }
    }
    return new Icon[] { mInfoIcon };
  }

  private boolean isSupportedChannel(final Channel channel) {
    return channel.getDataServiceProxy().getInfo().getDescription().contains("TV-Browser");
  }

  private ArrayList<Issue> getIssues(final Program program) {
    final ArrayList<Issue> results = new ArrayList<Issue>();

    for (AbstractCheck check : ALL_CHECKS) {
      check.check(program, results);
    }

    return results;
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    if (getPluginManager().getExampleProgram().equals(program)) {
      return new ActionMenu(new AbstractAction("Checker") {

        @Override
        public void actionPerformed(final ActionEvent e) {
          // do nothing, example program
        }
      });
    }
    if (!isSupportedChannel(program.getChannel())) {
      return null;
    }
    final ArrayList<Issue> issues = getIssues(program);
    final ArrayList<Object> actionList = new ArrayList<Object>();
    if (issues.size() > 0) {
      for (Issue result : issues) {
        actionList.add(new AbstractAction(result.getMessage()) {
          @Override
          public void actionPerformed(final ActionEvent e) {
            // do nothing
          }
        });
      }
    }
    if (actionList.size() > 0) {
      actionList.add(ContextMenuSeparatorAction.getInstance());
    }
    final ArrayList<Action> fieldActions = new ArrayList<Action>();
    final Iterator<ProgramFieldType> it = program.getFieldIterator();
    while (it.hasNext()) {
      final ProgramFieldType field = it.next();
      fieldActions.add(new AbstractAction(field.getLocalizedName()) {
        public void actionPerformed(final ActionEvent e) {
          String content = "";
          final int format = field.getFormat();
          if (format == ProgramFieldType.TEXT_FORMAT) {
            content = program.getTextField(field);
          } else if (format == ProgramFieldType.INT_FORMAT) {
            content = program.getIntFieldAsString(field);
          } else if (format == ProgramFieldType.TIME_FORMAT) {
            content = program.getTimeFieldAsString(field);
          } else if (format == ProgramFieldType.BINARY_FORMAT) {
            content = Arrays.toString(program.getBinaryField(field));
          }
          JOptionPane.showMessageDialog(null, content);
        }
      });
    }
    Collections.sort(fieldActions, new Comparator<Action>() {

      @Override
      public int compare(Action o1, Action o2) {
        return ((String) o1.getValue(Action.NAME)).compareTo((String) o2.getValue(Action.NAME));
      }
    });
    actionList.add(new ActionMenu(new ContextMenuAction(mLocalizer.msg("showField", "Show field")), fieldActions
        .toArray(new Action[fieldActions.size()])));
    if (actionList.size() > 0) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("contextMenu", "Checker")), actionList
          .toArray(new Object[actionList.size()]));
    }
    return null;
  }

  @Override
  public void handleTvBrowserStartFinished() {
    if (mSettings.getAutostart()) {
      runAllChecks();
    }
  }

  private void runAllChecks() {
    Cursor cursor = getParentFrame().getCursor();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        getParentFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
    });
    try {
      mRootNode.clear();
      final HashMap<String, PluginTreeNode> nodes = new HashMap<String, PluginTreeNode>();
      final Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
      Date currentDate = Date.getCurrentDate();
      for (Channel channel : channels) {
        if (!isSupportedChannel(channel)) {
          continue;
        }
        Date date = currentDate;
        for (int days = 0; days < 30; days++) {
          final Iterator<Program> iter = Plugin.getPluginManager().getChannelDayProgram(date, channel);
          if (iter != null) {
            while (iter.hasNext()) {
              final Program program = iter.next();
              final ArrayList<Issue> issues = getIssues(program);
              if (!issues.isEmpty()) {
                program.mark(this);
                for (Issue issue : issues) {
                  String message = issue.getMessage();
                  PluginTreeNode node = nodes.get(message);
                  if (node == null) {
                    node = new PluginTreeNode(issue.getMessage());
                    nodes.put(issue.getMessage(), node);
                    if (issue.getSeverity() == Issue.SEVERITY_ERROR) {
                      node.getMutableTreeNode().setIcon(mWarnIcon);
                    } else {
                      node.getMutableTreeNode().setIcon(mInfoIcon);
                    }
                  }
                  node.addProgram(program);
                }
              }
            }
          }
          date = date.addDays(1);
        }
      }
      // sort nodes and add them to the root
      final Collection<PluginTreeNode> values = nodes.values();
      final PluginTreeNode[] nodeArray = new PluginTreeNode[values.size()];
      values.toArray(nodeArray);
      Arrays.sort(nodeArray);
      for (PluginTreeNode node : nodeArray) {
        mRootNode.add(node);
      }
      mRootNode.update();
    } finally {
      final Cursor finalCursor = cursor;
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          getParentFrame().setCursor(finalCursor);
        }
      });
    }
  }

  @Override
  public int getMarkPriorityForProgram(final Program p) {
    return Program.MAX_MARK_PRIORITY;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  @Override
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction(mLocalizer.msg("action.check", "Start check"), mWarnIcon) {

      @Override
      public void actionPerformed(ActionEvent e) {
        runAllChecks();
      }
    };
    action.putValue(Plugin.BIG_ICON, createImageIcon("status", "dialog-warning", 22));
    return new ActionMenu(action);
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new CheckerSettingsTab(mSettings);
  }

  @Override
  public void loadSettings(Properties properties) {
    mSettings = new CheckerSettings(properties);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }
}

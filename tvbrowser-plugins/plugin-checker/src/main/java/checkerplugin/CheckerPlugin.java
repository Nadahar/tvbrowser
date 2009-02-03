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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.Version;

public class CheckerPlugin extends Plugin {

  private static final Version mVersion = new Version(0, 1, false);

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(CheckerPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  private PluginInfo mPluginInfo;

  private ImageIcon mWarnIcon;
  
  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private static String HTML_PATTERN = Pattern.quote("&") + "\\w+"
      + Pattern.quote(";");

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Checker");
      final String desc = mLocalizer.msg("description",
          "Checks program data for bugs.");
      final String author = "Michael Keppler";

      mPluginInfo = new PluginInfo(CheckerPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  @Override
  public Icon[] getMarkIconsForProgram(Program program) {
    if (program == null
        || getPluginManager().getExampleProgram().equals(program)) {
      return new Icon[] { mWarnIcon };
    }
    if (!isSupportedChannel(program.getChannel())) {
      return null;
    }
    ArrayList<String> results = getIssues(program);
    if (!results.isEmpty()) {
      return new Icon[] { mWarnIcon };
    }
    return null;
  }

  private boolean isSupportedChannel(Channel channel) {
    return channel.getDataServiceProxy().getInfo()
        .getDescription().contains("TV-Browser");
  }

  private ArrayList<String> getIssues(Program program) {
    ArrayList<String> results = new ArrayList<String>();
    checkCategories(program, results);
    checkShortDescription(program, results);
    checkDuration(program, results);
    checkTextFields(program, results);
    return results;
  }

  private void checkTextFields(Program program, ArrayList<String> results) {
    Iterator<ProgramFieldType> it = ProgramFieldType.getTypeIterator();
    while (it.hasNext()) {
      ProgramFieldType fieldType = it.next();
      if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        String content = program.getTextField(fieldType);
        if (content != null) {
          if (content.trim().length() < content.length()) {
            if (content.trim().length() == 0) {
              results.add(mLocalizer.msg("issue.whitespaceOnly",
                  "Text field {0} contains only whitespace.", fieldType
                      .getLocalizedName()));
            } else {
            results.add(mLocalizer.msg("issue.trim",
                "Text field {0} has blanks at beginning or end.", fieldType
                    .getLocalizedName()));
            }
          }
          if (content.matches(HTML_PATTERN)) {
            results.add(mLocalizer.msg("issue.entity",
                "Text field {0} contains HTML entity.", fieldType
                    .getLocalizedName()));
          }
        }
      }
    }
    final String title = program.getTitle();
    if (title.indexOf('\n') >= 0) {
      results.add(mLocalizer.msg("issue.linebreak",
          "Title contains line break."));
    }
    else {
      for (int i = 0; i < title.length(); i++) {
        if (Character.isWhitespace(title.charAt(i)) && (title.charAt(i) != ' ')) {
          results.add(mLocalizer.msg("issue.whitespace",
              "Title contains white space which is no space character."));
        }
      }
    }
  }

  private void checkDuration(Program program, ArrayList<String> results) {
    final int length = program.getLength();
    if (length == -1) {
      results.add(mLocalizer.msg("issue.unknownDuration",
          "Duration of program is unknown."));
    }
    if (length == 0) {
      results.add(mLocalizer.msg("issue.zeroDuration",
          "Duration of program is zero."));
    }
  }

  private void checkShortDescription(Program program, ArrayList<String> results) {
    String desc = program.getShortInfo();
    final int maxChars = 200;
    if (desc != null && desc.length() > maxChars) {
      results.add(mLocalizer.msg("issue.shortDescription",
          "Short description containes more than {0} characters", maxChars));
    }
  }

  private void checkCategories(Program program, ArrayList<String> results) {
    String genre = program.getTextField(ProgramFieldType.GENRE_TYPE);
    if (genre != null && !genre.isEmpty() && program.getInfo() == 0) {
      results.add(mLocalizer.msg("issue.missingCategory",
          "Category info missing for genre {0}", genre));
    }
  }

  @Override
  public void onActivation() {
    mWarnIcon = Plugin.getPluginManager().getIconFromTheme(this, "status",
        "dialog-warning", 16);
  }

  @Override
  public ActionMenu getContextMenuActions(Program program) {
    if (getPluginManager().getExampleProgram().equals(program)) {
      return new ActionMenu(new AbstractAction("Checker") {

        @Override
        public void actionPerformed(ActionEvent e) {
          // do nothing, example program
        }
      });
    }
    if (!isSupportedChannel(program.getChannel())) {
      return null;
    }
    ArrayList<String> results = getIssues(program);
    if (results.size() > 0) {
      Action[] problems = new Action[results.size()];
      for (int i = 0; i < problems.length; i++) {
        problems[i] = new AbstractAction(results.get(i)) {
          @Override
          public void actionPerformed(ActionEvent e) {
            // do nothing
          }
        };
      }
      return new ActionMenu(new ContextMenuAction("Checker"), problems);
    }
    return null;
  }

  @Override
  public void handleTvBrowserStartFinished() {
    HashMap<String, PluginTreeNode> nodes = new HashMap<String, PluginTreeNode>();
    Channel[] channels = devplugin.Plugin.getPluginManager()
        .getSubscribedChannels();
    for (Channel channel : channels) {
      if (!isSupportedChannel(channel)) {
        continue;
      }
      Date date = Date.getCurrentDate();
      for (int days = 0; days < 30; days++) {
        Iterator<Program> iter = Plugin.getPluginManager()
            .getChannelDayProgram(date, channel);
        if (iter != null) {
          while (iter.hasNext()) {
            Program program = iter.next();
            ArrayList<String> issues = getIssues(program);
            if (!issues.isEmpty()) {
              program.mark(this);
              for (String issue : issues) {
                PluginTreeNode node = nodes.get(issue);
                if (node == null) {
                  node = new PluginTreeNode(issue);
                  mRootNode.add(node);
                  nodes.put(issue, node);
                }
                node.addProgram(program);
              }
            }
          }
        }
        date = date.addDays(1);
      }
    }
    mRootNode.update();
  }

  private boolean hasIssues(Program program) {
    return getIssues(program).size() > 0;
  }

  @Override
  public int getMarkPriorityForProgram(Program p) {
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
  
  

}

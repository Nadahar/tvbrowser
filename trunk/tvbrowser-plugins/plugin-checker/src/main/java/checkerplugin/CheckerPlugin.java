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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import util.program.ProgramUtilities;
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
import devplugin.ProgramInfoHelper;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public class CheckerPlugin extends Plugin {

  private static final Version mVersion = new Version(0, 3, false);

  static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(CheckerPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  private PluginInfo mPluginInfo;

  private ImageIcon mWarnIcon = createImageIcon("status", "dialog-warning", 16);

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private CheckerSettings mSettings;

  private static Pattern HTML_PATTERN = Pattern.compile(Pattern.quote("&")
      + "\\w+" + Pattern.quote(";"));

  private static Pattern MISSING_WHITE_PATTERN = Pattern
      .compile("\\s\\p{Lower}+\\p{Upper}");

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Checker");
      final String desc = mLocalizer.msg("description",
          "Checks program data for bugs.");
      mPluginInfo = new PluginInfo(CheckerPlugin.class, name, desc,
          "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  @Override
  public Icon[] getMarkIconsForProgram(final Program program) {
    if (program == null
        || getPluginManager().getExampleProgram().equals(program)) {
      return new Icon[] { mWarnIcon };
    }
    if (!isSupportedChannel(program.getChannel())) {
      return null;
    }
    final ArrayList<String> results = getIssues(program);
    if (!results.isEmpty()) {
      return new Icon[] { mWarnIcon };
    }
    return null;
  }

  private boolean isSupportedChannel(final Channel channel) {
    return channel.getDataServiceProxy().getInfo().getDescription().contains(
        "TV-Browser");
  }

  private ArrayList<String> getIssues(final Program program) {
    final ArrayList<String> results = new ArrayList<String>();
    checkCategories(program, results);
    checkShortDescription(program, results);
    checkDuration(program, results);
    checkTextFields(program, results);
    checkURL(program, results);
    checkTime(program, results);
    checkSeriesByEpisode(program, results);
    checkSeriesNumbers(program, results);
    checkActors(program, results);
    checkPersonFields(program, results);
    return results;
  }

  private void checkPersonFields(final Program program, final ArrayList<String> results) {
    checkPersonFields(ProgramFieldType.ACTOR_LIST_TYPE, program, results);
    checkPersonFields(ProgramFieldType.DIRECTOR_TYPE, program, results);
    checkPersonFields(ProgramFieldType.CAMERA_TYPE, program, results);
    checkPersonFields(ProgramFieldType.CUTTER_TYPE, program, results);
    checkPersonFields(ProgramFieldType.MODERATION_TYPE, program, results);
    checkPersonFields(ProgramFieldType.ADDITIONAL_PERSONS_TYPE, program, results);
  }

  private void checkPersonFields(final ProgramFieldType fieldType, final Program program, final ArrayList<String> results) {
    String field = program.getTextField(fieldType);
    if (field != null) {
      // TODO use ProgramUtilities instead
      //String[] persons = ProgramUtilities.splitPersons(field);
      String[] persons = splitPersons(field);
      if (persons != null) {
        for (String person : persons) {
          if (person.isEmpty()) {
            results.add(mLocalizer.msg("issue.name.empty", "Person name is empty in {0}", fieldType.getLocalizedName()));
          } else {
            char firstChar = person.charAt(0);
            if (! (Character.isLetter(firstChar) || firstChar == '\'')) {
              results.add(mLocalizer.msg("issue.name.starts", "Person name starts with non whitespace in {0}", fieldType.getLocalizedName()));
            }
          }
        }
        HashSet set = new HashSet(Arrays.asList(persons));
        if (set.size() != persons.length) {
          results.add(mLocalizer.msg("issue.name.duplicate", "Person name duplicate in {0}", fieldType.getLocalizedName()));
        }
      }
    }
  }

  private void checkActors(final Program program, final ArrayList<String> results) {
    String[] actors = ProgramUtilities.getActorNames(program);
    if (actors == null) {
      return;
    }
    for (String actor : actors) {
      if (actor.isEmpty()) {
        results.add(mLocalizer.msg("issue.actor", "Unknown actor"));
      }
    }
  }

  private void checkSeriesNumbers(final Program program,
      final ArrayList<String> results) {
    final int episodeNumber = program
        .getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
    if (episodeNumber != -1 && episodeNumber < 1) {
      results.add(mLocalizer.msg("issue.episodeLess",
          "Episode number is less than 1."));
    }
    final int total = program
        .getIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE);
    if (total != -1) {
      if (total < 1) {
        results.add(mLocalizer.msg("issue.totalLess",
            "Total episode is less than 1."));
      }
      if (episodeNumber != -1 && episodeNumber > total) {
        results.add(mLocalizer.msg("issue.episodeToLarge",
            "Episode number is larger than total episode count."));
      }
    }
  }

  private void checkSeriesByEpisode(final Program program,
      final ArrayList<String> results) {
    if (!ProgramInfoHelper.bitSet(program.getInfo(),
        Program.INFO_CATEGORIE_SERIES)) {
      final String episode = program
          .getTextField(ProgramFieldType.EPISODE_TYPE);
      if (episode != null) {
        results.add(mLocalizer.msg("issue.seriesEpisode",
            "Episode title is set, but category series is not set."));
      }
      final String original = program
          .getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE);
      if (original != null) {
        results.add(mLocalizer.msg("issue.seriesOriginal",
            "Original episode title is set, but category series is not set."));
      }
      final int episodeNumber = program
          .getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
      if (episodeNumber != -1) {
        results.add(mLocalizer.msg("issue.seriesNumber",
            "Episode number is set, but category series is not set."));
      }
      final int total = program
          .getIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE);
      if (total != -1) {
        results.add(mLocalizer.msg("issue.seriesTotal",
            "Total episode number is set, but category series is not set."));
      }
    }
  }

  private void checkTime(final Program program, final ArrayList<String> results) {
    final int netTime = program
        .getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
    if (netTime != -1) {
      final int duration = program.getLength();
      if (netTime > duration) {
        if (duration > 0) {
          results.add(mLocalizer.msg("issue.netTime",
              "Net playing time is longer than duration ({0} min.)", netTime
                  - duration));
        } else {
          results.add(mLocalizer.msg("issue.netTimeAvailable",
              "Net play time is set, but duration missing."));
        }
      }
    }
  }

  private void checkURL(final Program program, final ArrayList<String> results) {
    final String url = program.getTextField(ProgramFieldType.URL_TYPE);
    if (url != null) {
      if (url.isEmpty()) {
        results.add("URL length is zero.");
      } else {
        if (!Character.isLetterOrDigit(url.charAt(0))) {
          results.add(mLocalizer.msg("issue.urlFormat",
              "URL not correctly formatted."));
        }
      }
    }
  }

  private void checkTextFields(final Program program,
      final ArrayList<String> results) {
    final Iterator<ProgramFieldType> it = ProgramFieldType.getTypeIterator();
    while (it.hasNext()) {
      final ProgramFieldType fieldType = it.next();
      if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        final String content = program.getTextField(fieldType);
        if (content != null) {
          if (content.trim().length() < content.length()) {
            if (content.trim().length() == 0) {
              results.add(mLocalizer.msg("issue.whitespaceOnly",
                  "Text field {0} contains only whitespace.", fieldType
                      .getLocalizedName()));
            } else {
              results.add(mLocalizer.msg("issue.trim",
                  "Text field {0} has whitespace at beginning or end.",
                  fieldType.getLocalizedName()));
            }
          }
          Matcher matcher = HTML_PATTERN.matcher(content);
          if (matcher.find()) {
            results.add(mLocalizer.msg("issue.entity",
                "Text field {0} contains HTML entity: {1}", fieldType
                    .getLocalizedName(), matcher.group()));
          }
          if (fieldType != ProgramFieldType.ACTOR_LIST_TYPE
              && fieldType != ProgramFieldType.DIRECTOR_TYPE
              && fieldType != ProgramFieldType.CAMERA_TYPE
              && fieldType != ProgramFieldType.CUTTER_TYPE
              && fieldType != ProgramFieldType.MODERATION_TYPE
              && fieldType != ProgramFieldType.ADDITIONAL_PERSONS_TYPE) {
            if (MISSING_WHITE_PATTERN.matcher(content).find()) {
              results.add(mLocalizer.msg("issue.missingWhitespace",
                  "Text field {0} probably misses whitespace.", fieldType
                      .getLocalizedName()));
            }
          }
          int closing = 0;
          int opening = 0;
          for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '(') {
              opening++;
            }
            else if (content.charAt(i) == ')') {
              closing++;
            }
          }
          if (closing != opening) {
            results.add(mLocalizer.msg("issue.braces", "Opening and closing braces doen't match in {0}.", fieldType.getLocalizedName()));
          }
        }
      }
    }
    final String title = program.getTitle();
    if (title.indexOf('\n') >= 0) {
      results.add(mLocalizer.msg("issue.linebreak",
          "Title contains line break."));
    } else {
      for (int i = 0; i < title.length(); i++) {
        if (Character.isWhitespace(title.charAt(i)) && (title.charAt(i) != ' ')) {
          results.add(mLocalizer.msg("issue.whitespace",
              "Title contains white space which is no space character."));
        }
      }
    }
    if (title.length() >= 2) {
      char lastChar = title.charAt(title.length() - 1);
      if (title.charAt(title.length() - 2) == ' ' && !Character.isLetterOrDigit(lastChar) && lastChar != '?' && lastChar != '!') {
        results.add(mLocalizer.msg("issue.titleEnd",
          "Title seems to contain confusing end character."));
      }
    }
  }

  private void checkDuration(final Program program,
      final ArrayList<String> results) {
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

  private void checkShortDescription(final Program program,
      final ArrayList<String> results) {
    final String desc = program.getShortInfo();
    final int maxChars = 200;
    if (desc != null && desc.length() > maxChars) {
      results.add(mLocalizer.msg("issue.shortDescription",
          "Short description containes more than {0} characters", maxChars));
    }
  }

  private void checkCategories(final Program program,
      final ArrayList<String> results) {
    final String genre = program.getTextField(ProgramFieldType.GENRE_TYPE);
    if (genre != null && !genre.isEmpty() && program.getInfo() == 0) {
      results.add(mLocalizer.msg("issue.missingCategory",
          "Category info missing for genre {0}", genre));
    }
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
    final ArrayList<String> results = getIssues(program);
    final ArrayList<Object> actionList = new ArrayList<Object>();
    if (results.size() > 0) {
      for (String result : results) {
        actionList.add(new AbstractAction(result) {
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
        return ((String)o1.getValue(Action.NAME)).compareTo((String) o2.getValue(Action.NAME));
      }
    });
    actionList.add(new ActionMenu(new ContextMenuAction(mLocalizer.msg(
        "showField", "Show field")), fieldActions
        .toArray(new Action[fieldActions.size()])));
    if (actionList.size() > 0) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("contextMenu",
          "Checker")), actionList.toArray(new Object[actionList.size()]));
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
      final HashMap<String, PluginTreeNode> nodes = new HashMap<String, PluginTreeNode>();
      final Channel[] channels = devplugin.Plugin.getPluginManager()
          .getSubscribedChannels();
      for (Channel channel : channels) {
        if (!isSupportedChannel(channel)) {
          continue;
        }
        Date date = Date.getCurrentDate();
        for (int days = 0; days < 30; days++) {
          final Iterator<Program> iter = Plugin.getPluginManager()
              .getChannelDayProgram(date, channel);
          if (iter != null) {
            while (iter.hasNext()) {
              final Program program = iter.next();
              final ArrayList<String> issues = getIssues(program);
              if (!issues.isEmpty()) {
                program.mark(this);
                for (String issue : issues) {
                  PluginTreeNode node = nodes.get(issue);
                  if (node == null) {
                    node = new PluginTreeNode(issue);
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
    AbstractAction action = new AbstractAction(mLocalizer.msg("action.check",
        "Start check"), mWarnIcon) {

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

  /**
   * extract a list of person names out of the given string
   * 
   * @param field
   * @return list of person names
   */
  private static String[] splitPersons(final String field) {
    if (field == null) {
      return new String[0];
    }
    String[] items;
    if (field.contains("\n")) {
      items = field.split("\n|( und )");
    }
    else if (field.contains(",")) {
      items = field.split(",|( und )");
    }
    else if (field.contains(" und ")) {
      items = field.split(" und ");
    }
    else {
      items = new String[1];
      items[0] = field;
    }
    for (int i = 0; i < items.length; i++) {
      items[i] = items[i].trim();
      if (items[i].endsWith(",") || items[i].endsWith(".")) {
        items[i] = items[i].substring(0, items[i].length() - 1);
      }
    }
    return items;
  }
}

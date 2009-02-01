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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
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
    if (program.getChannel().getDataServiceProxy().getInfo().getAuthor()
        .contains(
        "TV-Browser")) {
      ArrayList<String> results = runAllChecks(program);
      if (!results.isEmpty()) {
        return new Icon[] { mWarnIcon };
      }
    }
    return null;
  }

  private ArrayList<String> runAllChecks(Program program) {
    ArrayList<String> results = new ArrayList<String>();
    checkCategories(program, results);
    return results;
  }

  private void checkCategories(Program program, ArrayList<String> results) {
    String genre = program.getTextField(ProgramFieldType.GENRE_TYPE);
    if (genre != null && !genre.isEmpty()) {
      results.add(mLocalizer.msg("missingCategory",
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
    ArrayList<String> results = runAllChecks(program);
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

}

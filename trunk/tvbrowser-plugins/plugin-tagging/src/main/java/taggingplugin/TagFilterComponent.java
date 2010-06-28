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
package taggingplugin;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.ui.LineNumberHeader;
import util.ui.UiUtilities;
import devplugin.PluginsFilterComponent;
import devplugin.Program;

/**
 * public class so the filter can be constructed from core by reflection
 * @author bananeweizen
 *
 */
public class TagFilterComponent extends PluginsFilterComponent {

  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TagFilterComponent.class);

  private JTextArea mTextInput = null;

  private JPanel mSettingsPanel = null;

  private ArrayList<String> mTags = new ArrayList<String>();

  public int getVersion() {
    return 1;
  }

  public boolean accept(final Program program) {
    ArrayList<String> programTags = TaggingPlugin.getInstance().getProgramTags(program);
    if (programTags == null || programTags.isEmpty()) {
      return false;
    }
    for (String tag : programTags) {
      if (mTags.contains(tag)) {
        return true;
      }
    }
    return false;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    int count = in.readInt();
    mTags = new ArrayList<String>(count);
    for (int i = 0; i < count; i++) {
      mTags.add(in.readUTF());
    }
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mTags.size());
    for (String tag : mTags) {
      out.writeUTF(tag);
    }
  }

  public JPanel getSettingsPanel() {
    mSettingsPanel = new JPanel(new BorderLayout());

    mSettingsPanel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("description",
        "Each line is a tag. A program is shown if it has at least one of these tags.")), BorderLayout.NORTH);

    String newLine = System.getProperty("line.separator");
    StringBuilder builder = new StringBuilder();
    for (String tag : mTags) {
      builder.append(tag).append(newLine);
    }
    mTextInput = new JTextArea(builder.toString());
    JScrollPane scrollPane = new JScrollPane(mTextInput);
    LineNumberHeader header = new LineNumberHeader(mTextInput);
    scrollPane.setRowHeaderView(header);

    mSettingsPanel.add(scrollPane, BorderLayout.CENTER);

    return mSettingsPanel;
  }

  public void saveSettings() {
    // split into non empty lines
    String[] lines = mTextInput.getText().split("[\\r\\n]+");
    Arrays.sort(lines);
    mTags = new ArrayList<String>(Arrays.asList(lines));
  }

  @Override
  public String getUserPresentableClassName() {
    return mLocalizer.msg("name", "Tags");
  }

}
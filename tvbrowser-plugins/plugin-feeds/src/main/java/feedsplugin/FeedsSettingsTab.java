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
package feedsplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

public class FeedsSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FeedsSettingsTab.class);

  private DefaultListModel mListModel;
  private JList mFeeds;
  private JButton mAdd;
  private JButton mRemove;

  private FeedsPluginSettings mSettings;

  public FeedsSettingsTab(final FeedsPluginSettings settings) {
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ", fill:default:grow");
    final CellConstraints cc = new CellConstraints();

    mListModel = new DefaultListModel();
    for (String feed : mSettings.getFeeds()) {
      mListModel.addElement(feed);
    }
    mFeeds = new JList(mListModel);
    mFeeds.setSelectedIndex(0);
    mFeeds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mFeeds.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent e) {
        listSelectionChanged();
      }
    });

    panelBuilder.addGrowingRow();
    panelBuilder.add(new JScrollPane(mFeeds), cc.xyw(2, panelBuilder.getRow(), panelBuilder.getColumnCount() - 1));

    mAdd = new JButton(mLocalizer.msg("add", "Add feed"));
    mAdd.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent e) {
        String genre = JOptionPane.showInputDialog(mLocalizer.msg("addMessage", "Add feed URL"), "");
        if (genre != null) {
          genre = genre.trim();
          if (genre.length() > 0) {
            mListModel.addElement(genre);
          }
        }
      }});

    mRemove = new JButton(mLocalizer.msg("remove", "Remove feed"));
    mRemove.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent e) {
        final int index = mFeeds.getSelectedIndex();
        if (index >= 0) {
          mListModel.remove(index);
        }
      }});

    panelBuilder.addRow();
    ButtonBarBuilder2 buttonBar = new ButtonBarBuilder2();
    buttonBar.addButton(new JButton[]{mAdd, mRemove});
    panelBuilder.add(buttonBar.getPanel(), cc.xyw(2, panelBuilder.getRow(), panelBuilder.getColumnCount() - 1));

    mFeeds.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent e) {
        mRemove.setEnabled(mFeeds.getSelectedIndex() >= 0);
      }});

    // force update of enabled states
    listSelectionChanged();

    return panelBuilder.getPanel();
  }

  private void listSelectionChanged() {
    final boolean selected = (mFeeds.getSelectedIndex() > -1);
    mRemove.setEnabled(selected);
  }

  public Icon getIcon() {
    return FeedsPlugin.getPluginIcon();
  }

  public String getTitle() {
    return null;
  }

  public void saveSettings() {
    ArrayList<String> feeds = new ArrayList<String>(mListModel.getSize());
    for (int i = 0; i < mListModel.getSize(); i++) {
      feeds.add((String) mListModel.get(i));
    }
    mSettings.setFeeds(feeds);
  }

}

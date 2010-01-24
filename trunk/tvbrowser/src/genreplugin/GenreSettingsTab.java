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
package genreplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

/**
 * @author bananeweizen
 *
 */
class GenreSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GenreSettingsTab.class);
  private GenrePlugin mPlugin;
  private DefaultListModel mListModel;
  private JList mFilteredGenres;
  private GenreSettings mSettings;
  private JSpinner mSpinner;
  private JButton mAddFilter;
  private JButton mRemoveFilter;
  private JCheckBox mUnifyBraces;

  GenreSettingsTab(final GenrePlugin plugin,
      final ArrayList<String> hiddenGenres, final GenreSettings settings) {
    mPlugin = plugin;
    mListModel = new DefaultListModel();
    Collections.sort(hiddenGenres);
    for (String genre: hiddenGenres) {
      mListModel.addElement(genre);
    }
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ","
        + FormFactory.PREF_COLSPEC.encode() + "," + FormFactory.RELATED_GAP_COLSPEC.encode() + ","
        + FormFactory.PREF_COLSPEC.encode() + ", fill:default:grow");
    final CellConstraints cc = new CellConstraints();

    final JLabel label = new JLabel(mLocalizer
        .msg("daysToShow", "Days to show"));

    panelBuilder.addRow();
    panelBuilder.add(label, cc.xy(2, panelBuilder.getRow()));
    
    final SpinnerNumberModel model = new SpinnerNumberModel(7, 1, 28, 1);
    mSpinner = new JSpinner(model);
    mSpinner.setValue(mSettings.getDays());
    panelBuilder.add(mSpinner, cc.xy(4, panelBuilder.getRow()));

    mUnifyBraces = new JCheckBox(mLocalizer.msg("unifyBracedGenres", "Unify genres with sub genres in braces"), mSettings.getUnifyBraceGenres());
    panelBuilder.addRow();
    panelBuilder.add(mUnifyBraces, cc.xy(2, panelBuilder.getRow()));

    panelBuilder.addParagraph(mLocalizer.msg("filteredGenres", "Filtered genres"));

    mFilteredGenres = new JList(mListModel);
    mFilteredGenres.setSelectedIndex(0);
    mFilteredGenres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mFilteredGenres.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent e) {
        listSelectionChanged();
      }
    });

    panelBuilder.addGrowingRow();
    panelBuilder.add(new JScrollPane(mFilteredGenres), cc.xyw(2, panelBuilder.getRow(), panelBuilder.getColumnCount() - 1));
    
    mAddFilter = new JButton(mLocalizer.msg("addFilterBtn", "Add filter"));
    mAddFilter.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent e) {
        String genre = JOptionPane.showInputDialog(mLocalizer.msg("addFilterMessage", "Add genre to be filtered"), "");
        if (genre != null) {
          genre = genre.trim();
          if (genre.length() > 0) {
            mListModel.addElement(genre);
          }
        }
      }});
    
    mRemoveFilter = new JButton(mLocalizer.msg("removeFilterBtn", "Remove filter"));
    mRemoveFilter.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent e) {
        final int index = mFilteredGenres.getSelectedIndex();
        if (index >= 0) {
          mListModel.remove(index);
        }
      }});
    
    panelBuilder.addRow();
    ButtonBarBuilder2 buttonBar = new ButtonBarBuilder2();
    buttonBar.addButton(new JButton[]{mAddFilter, mRemoveFilter});
    panelBuilder.add(buttonBar.getPanel(), cc.xyw(2, panelBuilder.getRow(), panelBuilder.getColumnCount() - 1));

    mFilteredGenres.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent e) {
        mRemoveFilter.setEnabled(mFilteredGenres.getSelectedIndex() >= 0);
      }});
    
    // force update of enabled states
    listSelectionChanged();
    
    return panelBuilder.getPanel();
  }

  public Icon getIcon() {
    return GenrePlugin.getInstance().createImageIcon("apps", "system-file-manager", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Genres");
  }

  public void saveSettings() {
    mSettings.setDays((Integer) mSpinner.getValue());
    mSettings.setHiddenGenres(mListModel.toArray());
    mSettings.setUnifyBraceGenres(mUnifyBraces.isSelected());
    mPlugin.getFilterFromSettings();
    mPlugin.updateRootNode();
  }

  private void listSelectionChanged() {
    final boolean selected = (mFilteredGenres.getSelectedIndex() > -1);
    mRemoveFilter.setEnabled(selected);
  }
}

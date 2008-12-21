/*
 * GenrePlugin Copyright Michael Keppler
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
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

import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class GenreSettingsTab implements SettingsTab, IGenreSettings {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GenreSettingsTab.class);
  private GenrePlugin mPlugin;
  private DefaultListModel mListModel;
  private JList mFilteredGenres;
  private Properties mSettings;
  private JSpinner mSpinner;
  private JButton mAddFilter;
  private JButton mRemoveFilter;

  public GenreSettingsTab(GenrePlugin plugin, ArrayList<String> hiddenGenres, Properties settings) {
    mPlugin = plugin;
    mListModel = new DefaultListModel();
    Collections.sort(hiddenGenres);
    for (String genre: hiddenGenres) {
      mListModel.addElement(genre);
    }
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("5dlu, pref, 3dlu, pref, fill:default:grow",
        "5dlu, pref, 5dlu, pref, 2dlu, fill:default:grow, 3dlu, pref, 3dlu"));
    CellConstraints cc = new CellConstraints();

    JLabel label = new JLabel(mLocalizer.msg("daysToShow", "Days to show"));
    panelBuilder.add(label, cc.xy(2, 2));
    
    SpinnerNumberModel model = new SpinnerNumberModel(7, 1, 28, 1);
    mSpinner = new JSpinner(model);
    mSpinner.setValue(Integer.valueOf(mSettings.getProperty(SETTINGS_DAYS, "7")));
    panelBuilder.add(mSpinner, cc.xy(4, 2));

    panelBuilder.addSeparator(mLocalizer.msg("filteredGenres", "Filtered genres"), cc.xyw(1, 4, 5));

    mFilteredGenres = new JList(mListModel);
    mFilteredGenres.setSelectedIndex(0);
    mFilteredGenres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mFilteredGenres.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        listSelectionChanged();
      }
    });

    
    JPanel listPanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 5, 5);

    listPanel.add(new JScrollPane(mFilteredGenres), c);
    
    panelBuilder.add(listPanel, cc.xyw(2,6,4));
    
    mAddFilter = new JButton(mLocalizer.msg("addFilterBtn", "Add filter"));
    panelBuilder.add(mAddFilter, cc.xy(2, 8));
    mAddFilter.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        String genre = JOptionPane.showInputDialog(mLocalizer.msg("addFilterMessage", "Add genre to be filtered"), "");
        if (genre != null) {
          genre = genre.trim();
          if (genre.length() > 0) {
            mListModel.addElement(genre);
          }
        }
      }});
    
    mRemoveFilter = new JButton(mLocalizer.msg("removeFilterBtn", "Remove filter"));
    panelBuilder.add(mRemoveFilter, cc.xy(4, 8));
    mRemoveFilter.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int index = mFilteredGenres.getSelectedIndex();
        if (index >= 0) {
          mListModel.remove(index);
        }
      }});
    
    mFilteredGenres.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
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
    mSettings.setProperty(SETTINGS_DAYS, mSpinner.getValue().toString());
    mPlugin.saveSettings(mListModel.toArray());
    mPlugin.getFilterFromSettings();
    mPlugin.updateRootNode();
  }

  private void listSelectionChanged() {
    boolean selected = (mFilteredGenres.getSelectedIndex() > -1);
    mRemoveFilter.setEnabled(selected);
  }
}

/*
 * GenrePlugin by Michael Keppler
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
 * VCS information:
 *     $Date: 2007-09-15 19:13:12 +0200 (Sa, 15 Sep 2007) $
 *   $Author: bananeweizen $
 * $Revision: 1 $
 */
package genreplugin;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.ui.Localizer;
import devplugin.SettingsTab;

public class GenreSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GenreSettingsTab.class);
  private GenrePlugin mPlugin;
  private DefaultListModel mListModel;
  private JList mFilteredGenres;

  public GenreSettingsTab(GenrePlugin plugin, ArrayList<String> hiddenGenres) {
    mPlugin = plugin;
    mListModel = new DefaultListModel();
    Collections.sort(hiddenGenres);
    for (String genre: hiddenGenres) {
      mListModel.addElement(genre);
    }
  }

  public JPanel createSettingsPanel() {
    final JPanel configPanel = new JPanel();

    FormLayout layout = new FormLayout("5dlu, pref, 3dlu, pref, 5dlu",
        "5dlu, fill:default:grow, 3dlu, pref, 3dlu");
    configPanel.setLayout(layout);

    mFilteredGenres = new JList(mListModel);
    JPanel listPanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 5, 5);

    listPanel.add(new JScrollPane(mFilteredGenres), c);
    
    CellConstraints cc = new CellConstraints();
    configPanel.add(listPanel, cc.xyw(2,2,3));
    
    JButton addFilter = new JButton(mLocalizer.msg("addFilterBtn", "Add filter"));
    configPanel.add(addFilter, cc.xy(2, 4));
    addFilter.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        String genre = JOptionPane.showInputDialog(mLocalizer.msg("addFilterMessage", "Add genre to be filtered"), "");
        if (genre != null) {
          genre = genre.trim();
          if (genre.length() > 0) {
            mListModel.addElement(genre);
          }
        }
      }});
    
    final JButton removeFilter = new JButton(mLocalizer.msg("removeFilterBtn", "Remove filter"));
    configPanel.add(removeFilter, cc.xy(4, 4));
    removeFilter.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int index = mFilteredGenres.getSelectedIndex();
        if (index >= 0) {
          mListModel.remove(index);
        }
      }});
    
    mFilteredGenres.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        removeFilter.setEnabled(mFilteredGenres.getSelectedIndex() >= 0);
      }});

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(configPanel, BorderLayout.NORTH);
    return panel;
  }

  public Icon getIcon() {
    return GenrePlugin.getInstance().createImageIcon("apps", "system-file-manager", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Genres");
  }

  public void saveSettings() {
    mPlugin.saveSettings(mListModel.toArray());
    mPlugin.updateRootNode();
  }

}

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
package filterviewplugin;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import util.io.IOUtilities;
import util.ui.EnhancedPanelBuilder;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

class FilterViewSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterViewSettingsTab.class);
  private static final int MAX_ICON_WIDTH = 32;
  private static final int MAX_ICON_HEIGHT = 32;
  private FilterViewSettings mSettings;
  private SelectableItemList mFilterList;
  private JSpinner mSpinner;
  protected HashMap<String, String> mIcons = new HashMap<String, String>();
  private JButton mFilterButton;
  private JButton mRemoveButton;

  FilterViewSettingsTab(final FilterViewSettings settings) {
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ','
        + FormFactory.PREF_COLSPEC.encode() + ',' + FormFactory.RELATED_GAP_COLSPEC.encode() + ','
        + FormFactory.PREF_COLSPEC.encode() + ", fill:default:grow");
    final CellConstraints cc = new CellConstraints();

    final JLabel label = new JLabel(mLocalizer.msg("daysToShow", "Days to show"));

    panelBuilder.addRow();
    panelBuilder.add(label, cc.xy(2, panelBuilder.getRow()));

    final SpinnerNumberModel model = new SpinnerNumberModel(3, 1, 7, 1);
    mSpinner = new JSpinner(model);
    mSpinner.setValue(mSettings.getDays());
    panelBuilder.add(mSpinner, cc.xy(4, panelBuilder.getRow()));

    panelBuilder.addParagraph(mLocalizer.msg("filters", "Filters to show"));

    mFilterList = new SelectableItemList(mSettings.getActiveFilterNames(), FilterViewSettings.getAvailableFilterNames());
    mIcons.clear();
    for (String filterName : FilterViewSettings.getAvailableFilterNames()) {
      mIcons.put(filterName, mSettings.getFilterIconName(mSettings.getFilter(filterName)));
    }
    mFilterList.addCenterRendererComponent(String.class, new SelectableItemRendererCenterComponentIf() {
      private DefaultListCellRenderer mRenderer = new DefaultListCellRenderer();

      public void calculateSize(JList list, int index, JPanel contentPane) {
      }

      public JPanel createCenterPanel(JList list, Object value, int index, boolean isSelected, boolean isEnabled,
          JScrollPane parentScrollPane, int leftColumnWidth) {
        DefaultListCellRenderer label = (DefaultListCellRenderer) mRenderer.getListCellRendererComponent(list, value,
            index, isSelected, false);
        String filterName = value.toString();
        String iconFileName = mIcons.get(filterName);
        Icon icon = null;
        if (!StringUtils.isEmpty(iconFileName)) {
          try {
            icon = FilterViewPlugin.getInstance().getIcon(FilterViewSettings.getIconDirectoryName() + File.separatorChar + iconFileName);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          label.setIcon(icon);
        }
        String text = filterName;
        if (icon == null) {
          text += " (" + mLocalizer.msg("noIcon", "no icon") + ')';
        }
        label.setText(text);
        label.setHorizontalAlignment(SwingConstants.LEADING);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout());
        if (isSelected && isEnabled) {
          panel.setOpaque(true);
          panel.setForeground(list.getSelectionForeground());
          panel.setBackground(list.getSelectionBackground());
        } else {
          panel.setOpaque(false);
          panel.setForeground(list.getForeground());
          panel.setBackground(list.getBackground());
        }
        panel.add(label, BorderLayout.WEST);
        return panel;
      }
    });

    panelBuilder.addGrowingRow();
    panelBuilder.add(mFilterList, cc.xyw(2, panelBuilder.getRow(), panelBuilder.getColumnCount() - 1));

    panelBuilder.addRow();
    mFilterButton = new JButton(mLocalizer.msg("changeIcon", "Change icon"));
    mFilterButton.setEnabled(false);
    panelBuilder.add(mFilterButton, cc.xyw(2, panelBuilder.getRow(), 1));

    mRemoveButton = new JButton(mLocalizer.msg("deleteIcon", "Remove icon"));
    mRemoveButton.setEnabled(false);
    panelBuilder.add(mRemoveButton, cc.xyw(4, panelBuilder.getRow(), 1));

    mFilterButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        SelectableItem item = (SelectableItem) mFilterList.getSelectedValue();
        String filterName = (String) item.getItem();
        chooseIcon(filterName);
      }
    });

    mFilterList.addListSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent e) {
        mFilterButton.setEnabled(mFilterList.getSelectedValue() != null);
        mRemoveButton.setEnabled(mFilterButton.isEnabled());
      }
    });

    mRemoveButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        SelectableItem item = (SelectableItem) mFilterList.getSelectedValue();
        String filterName = (String) item.getItem();
        mIcons.put(filterName, "");
        mFilterList.updateUI();
      }
    });

    return panelBuilder.getPanel();
  }

  public Icon getIcon() {
    return FilterViewPlugin.getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Filter view");
  }

  public void saveSettings() {
    mSettings.setActiveFilterNames(mFilterList.getSelection());
    mSettings.setDays((Integer) mSpinner.getValue());
    for (String filterName : FilterViewSettings.getAvailableFilterNames()) {
      mSettings.setFilterIconName(mSettings.getFilter(filterName), mIcons.get(filterName));
    }
    FilterViewPlugin.getInstance().updateRootNode();
  }

  private void chooseIcon(final String filterName) {
    String iconPath = mIcons.get(filterName);

    JFileChooser chooser = new JFileChooser(iconPath == null ? new File("") : (new File(iconPath)).getParentFile());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    String msg = mLocalizer.msg("iconFiles", "Icon Files ({0})", "*.png,*.jpg, *.gif");
    String[] extArr = { ".png", ".jpg", ".gif" };

    chooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
    chooser.setDialogTitle(mLocalizer.msg("chooseIcon", "Choose icon for '{0}'", filterName));

    Window w = UiUtilities.getLastModalChildOf(FilterViewPlugin.getInstance().getSuperFrame());

    if (chooser.showDialog(w, Localizer.getLocalization(Localizer.I18N_SELECT)) == JFileChooser.APPROVE_OPTION) {
      if (chooser.getSelectedFile() != null) {
        File dir = new File(FilterViewSettings.getIconDirectoryName());

        if (!dir.isDirectory()) {
          dir.mkdir();
        }

        String ext = chooser.getSelectedFile().getName();
        ext = ext.substring(ext.lastIndexOf('.'));

        Icon icon = FilterViewPlugin.getInstance().getIcon(chooser.getSelectedFile().getAbsolutePath());

        if (icon.getIconWidth() > MAX_ICON_WIDTH || icon.getIconHeight() > MAX_ICON_HEIGHT ) {
          JOptionPane.showMessageDialog(w, mLocalizer.msg("iconSize", "The icon size must be at most {0}x{1}.", MAX_ICON_WIDTH, MAX_ICON_HEIGHT));
          return;
        }

        try {
          IOUtilities.copy(chooser.getSelectedFile(), new File(dir, filterName + ext));
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        mIcons.put(filterName, filterName + ext);
        mFilterList.updateUI();
      }
    }
  }

}

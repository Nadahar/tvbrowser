/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.filter.dlgs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.filtercomponents.AgeLimitFilterComponent;
import tvbrowser.core.filters.filtercomponents.BeanShellFilterComponent;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.filters.filtercomponents.DayFilterComponent;
import tvbrowser.core.filters.filtercomponents.FavoritesFilterComponent;
import tvbrowser.core.filters.filtercomponents.KeywordFilterComponent;
import tvbrowser.core.filters.filtercomponents.MassFilterComponent;
import tvbrowser.core.filters.filtercomponents.PluginFilterComponent;
import tvbrowser.core.filters.filtercomponents.PluginIconFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramInfoFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramLengthFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramMarkingPriorityFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramRunningFilterComponent;
import tvbrowser.core.filters.filtercomponents.ReminderFilterComponent;
import tvbrowser.core.filters.filtercomponents.TimeFilterComponent;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.DefaultComponentFactory;

import devplugin.PluginAccess;
import devplugin.PluginsFilterComponent;

public class EditFilterComponentDlg extends JDialog implements ActionListener, DocumentListener, WindowClosingIf {

  private static final String REGEX_INVALID_CHARACTERS = "[\\p{Punct}\\s&&[^_]]";

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(EditFilterComponentDlg.class);

  private tvbrowser.core.filters.FilterComponent mSelectedFilterComponent;

  private JComboBox mRuleCb;

  private JPanel mCenterPanel, mRulePanel = null, mContentPane;

  private JButton mOkBtn, mCancelBtn;

  private JTextField mDescTF, mNameTF;

  public EditFilterComponentDlg(JFrame parent) {
    this(parent, null);
  }

  public EditFilterComponentDlg(JFrame parent, FilterComponent comp) {
    this(parent, comp, null);
  }

  public EditFilterComponentDlg(JFrame parent, FilterComponent comp, Class<? extends FilterComponent> filterComponentClass) {
    super(parent, true);
    
    UiUtilities.registerForClosing(this);

    mContentPane = (JPanel) getContentPane();
    mContentPane.setLayout(new BorderLayout(7, 7));
    mContentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setTitle(mLocalizer.msg("title", "Edit filter component"));

    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

    JPanel namePanel = new JPanel(new BorderLayout());
    namePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
    JPanel descPanel = new JPanel(new BorderLayout());
    descPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));

    JPanel typePanel = new JPanel(new BorderLayout());

    namePanel.add(new JLabel(mLocalizer.msg("componentName", "Component name:")), BorderLayout.WEST);
    mNameTF = new JTextField(new PlainDocument() {

      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        str = str.replaceAll(REGEX_INVALID_CHARACTERS, "_");
        super.insertString(offset, str, a);
      }
    }, "", 20);
    mNameTF.getDocument().addDocumentListener(this);

    namePanel.add(mNameTF, BorderLayout.EAST);
    mDescTF = new JTextField(20);
    descPanel.add(new JLabel(mLocalizer.msg("componentDescription", "Description:")), BorderLayout.WEST);
    descPanel.add(mDescTF, BorderLayout.EAST);
    typePanel.add(new JLabel(mLocalizer.msg("componentType", "Type:")), BorderLayout.WEST);

    mRuleCb = new JComboBox();
    mRuleCb.addActionListener(this);
    mRuleCb.addItem(mLocalizer.msg("hint", "must choose one"));
    
    // The TreeSet sorts the Entries
    TreeSet<FilterComponent> set = new TreeSet<FilterComponent>(new FilterComponent.TypeComparator());
    
    if (filterComponentClass == null) {
      set.add(new AgeLimitFilterComponent());
      set.add(new BeanShellFilterComponent());
      set.add(new ChannelFilterComponent());
      set.add(new DayFilterComponent());
      set.add(new FavoritesFilterComponent());
      set.add(new KeywordFilterComponent());
      set.add(new MassFilterComponent());
      set.add(new PluginFilterComponent());
      set.add(new PluginIconFilterComponent());
      set.add(new ProgramInfoFilterComponent());
      set.add(new ProgramLengthFilterComponent());
      set.add(new ProgramMarkingPriorityFilterComponent());
      set.add(new ProgramRunningFilterComponent());
      set.add(new ReminderFilterComponent());
      set.add(new TimeFilterComponent());
  
      PluginAccess[] plugins = PluginManagerImpl.getInstance().getActivatedPlugins();
      
      for(PluginAccess plugin : plugins) {
        Class<? extends PluginsFilterComponent>[] clazzes = plugin.getAvailableFilterComponentClasses();
        
        if(clazzes != null)
          for(Class<? extends PluginsFilterComponent> clazz : clazzes) {
            try {
              set.add(clazz.newInstance());
            } catch (InstantiationException e) {
              // TODO Automatisch erstellter Catch-Block
              e.printStackTrace();
            } catch (IllegalAccessException e) {
              // TODO Automatisch erstellter Catch-Block
              e.printStackTrace();
            }
          }
      }
    }
    else {
      try {
        set.add(filterComponentClass.newInstance());
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    Iterator<FilterComponent> it = set.iterator();
    
    while (it.hasNext()) {
      mRuleCb.addItem(it.next());
    }
    
    typePanel.add(mRuleCb, BorderLayout.EAST);

    northPanel.add(namePanel);
    northPanel.add(descPanel);
    northPanel.add(typePanel);

    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOkBtn.addActionListener(this);
    buttonPn.add(mOkBtn);

    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);
    buttonPn.add(mCancelBtn);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("componentSettings", "Component settings:")), BorderLayout.NORTH);    

    mCenterPanel = new JPanel(new BorderLayout());
    panel.add(mCenterPanel, BorderLayout.CENTER);

    mContentPane.add(northPanel, BorderLayout.NORTH);
    mContentPane.add(buttonPn, BorderLayout.SOUTH);
    mContentPane.add(panel, BorderLayout.CENTER);

    if (comp != null) {
      this.setFilterComponent(comp);
    }
    else if (mRuleCb.getItemCount() == 2) {
      mRuleCb.setSelectedIndex(1);
    }

    updateOkBtn();

    setSize(500, 500);
    UiUtilities.centerAndShow(this);

  }

  private void setFilterComponent(FilterComponent comp) {
    for (int i = 1; // index 0 does not contain a FilterComponent object
    i < mRuleCb.getItemCount(); i++) {
      FilterComponent c = (FilterComponent) mRuleCb.getItemAt(i);
      if (c.toString().equals(comp.toString())) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) mRuleCb.getModel();
        model.removeElementAt(i);
        model.insertElementAt(comp, i);
        mRuleCb.setSelectedIndex(i);
        mNameTF.setText(comp.getName());
        mDescTF.setText(comp.getDescription());
        break;
      }
    }

  }

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == mRuleCb) {
      if (mRulePanel != null) {
        mCenterPanel.remove(mRulePanel);
      }
      Object item = mRuleCb.getSelectedItem();
      if (item instanceof FilterComponent) {
        FilterComponent fItem = (FilterComponent) item;
        mRulePanel = fItem.getSettingsPanel();
        mRulePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mCenterPanel.add(mRulePanel, BorderLayout.CENTER);
      }
      mContentPane.updateUI();
      updateOkBtn();

    } else if (o == mOkBtn) {

      String compName = mNameTF.getText();

      if (FilterComponentList.getInstance().exists(compName)) {

        JOptionPane.showMessageDialog(this, "Component '" + compName + "' already exists");
      } else {

        FilterComponent c = (FilterComponent) mRuleCb.getSelectedItem();
        c.saveSettings();
        mSelectedFilterComponent = c;
        mSelectedFilterComponent.setName(compName);
        mSelectedFilterComponent.setDescription(mDescTF.getText());
        setVisible(false);
      }
    } else if (o == mCancelBtn) {
      close();
    }

  }

  public FilterComponent getFilterComponent() {
    return mSelectedFilterComponent;
  }

  private void updateOkBtn() {
    if (mOkBtn != null) {
      Pattern p = Pattern.compile(REGEX_INVALID_CHARACTERS);
      Matcher m = p.matcher(mNameTF.getText());
      mOkBtn.setEnabled(mNameTF.getText().length() > 0 && !m.find() && mRuleCb.getSelectedItem() instanceof FilterComponent);
    }
  }

  public void changedUpdate(DocumentEvent e) {
    updateOkBtn();
  }

  public void insertUpdate(DocumentEvent e) {
    updateOkBtn();
  }

  public void removeUpdate(DocumentEvent e) {
    updateOkBtn();
  }

  public void close() {
    mSelectedFilterComponent = null;
    setVisible(false);
  }

}
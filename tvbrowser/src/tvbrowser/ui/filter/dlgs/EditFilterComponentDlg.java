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
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.filtercomponents.AgeLimitFilterComponent;
import tvbrowser.core.filters.filtercomponents.BeanShellFilterComponent;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.filters.filtercomponents.DateFilterComponent;
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
import tvbrowser.core.filters.filtercomponents.ProgramTypeFilterComponent;
import tvbrowser.core.filters.filtercomponents.ReminderFilterComponent;
import tvbrowser.core.filters.filtercomponents.SingleTitleFilterComponent;
import tvbrowser.core.filters.filtercomponents.TimeFilterComponent;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

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

  public EditFilterComponentDlg(JDialog parent) {
    this(parent, null);
  }

  public EditFilterComponentDlg(JDialog parent, FilterComponent comp) {
    this(parent, comp, null);
  }

  public EditFilterComponentDlg(JDialog parent, FilterComponent comp, Class<? extends FilterComponent> filterComponentClass) {
    super(parent, true);
    init(parent,comp,filterComponentClass);
  }

  
  public EditFilterComponentDlg(JFrame parent) {
    this(parent, null);
  }

  public EditFilterComponentDlg(JFrame parent, FilterComponent comp) {
    this(parent, comp, null);
  }

  public EditFilterComponentDlg(JFrame parent, FilterComponent comp, Class<? extends FilterComponent> filterComponentClass) {
    super(parent, true);
    init(parent,comp,filterComponentClass);
  }
  
  private void init(Window parent, FilterComponent comp, Class<? extends FilterComponent> filterComponentClass) {
    UiUtilities.registerForClosing(this);
    setTitle(mLocalizer.msg("title", "Edit filter component"));
    mContentPane = (JPanel)getContentPane();
    
    mNameTF = new JTextField(new PlainDocument() {
      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        str = str.replaceAll(REGEX_INVALID_CHARACTERS, "_");
        super.insertString(offset, str, a);
      }
    }, "", 20);
    mNameTF.getDocument().addDocumentListener(this);
    
    mDescTF = new JTextField(20);
    
    mRuleCb = new JComboBox();
    mRuleCb.addActionListener(this);
    mRuleCb.addItem(mLocalizer.msg("hint", "must choose one"));
    
    mCenterPanel = new JPanel(new BorderLayout());
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,5dlu,default:grow,5dlu,",
        "default,5dlu,default,2dlu,default,2dlu,default,10dlu,default,5dlu,fill:200dlu:grow,5dlu,default,5dlu,default"),mContentPane);
    pb.setDefaultDialogBorder();
    
    pb.addSeparator(mLocalizer.msg("component", "Component"), CC.xyw(1,1,5));
    pb.addLabel(mLocalizer.msg("name","Name:"), CC.xy(2,3));
    pb.add(mNameTF, CC.xy(4,3));
    pb.addLabel(mLocalizer.msg("description", "Description:"), CC.xy(2,5));
    pb.add(mDescTF, CC.xy(4,5));
    pb.addLabel(mLocalizer.msg("type", "Type:"), CC.xy(2,7));
    pb.add(mRuleCb, CC.xy(4,7));
    pb.addSeparator(mLocalizer.msg("componentSettings", "Component settings:"), CC.xyw(1,9,5));
    pb.add(mCenterPanel, CC.xyw(2,11,3));
    pb.add(new JSeparator(JSeparator.HORIZONTAL), CC.xyw(1,13,5));

    // The TreeSet sorts the Entries
    TreeSet<FilterComponent> set = new TreeSet<FilterComponent>(new FilterComponent.TypeComparator());

    if (filterComponentClass == null) {
      set.add(new AgeLimitFilterComponent());
      set.add(new BeanShellFilterComponent());
      set.add(new ChannelFilterComponent());
      set.add(new DateFilterComponent());
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
      set.add(new ProgramTypeFilterComponent());
      set.add(new ReminderFilterComponent());
      set.add(new SingleTitleFilterComponent());
      set.add(new TimeFilterComponent());

      PluginAccess[] plugins = PluginManagerImpl.getInstance().getActivatedPlugins();

      for(PluginAccess plugin : plugins) {
        Class<? extends PluginsFilterComponent>[] clazzes = plugin.getAvailableFilterComponentClasses();

        if(clazzes != null) {
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

    ButtonBarBuilder2 bottomBar = Utilities.createFilterButtonBar();

    mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOkBtn.addActionListener(this);

    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);
    bottomBar.addButton(new JButton[] {mOkBtn, mCancelBtn});

    if (comp != null) {
      this.setFilterComponent(comp);
    }
    else if (mRuleCb.getItemCount() == 2) {
      mRuleCb.setSelectedIndex(1);
    }

    pb.add(bottomBar.getPanel(), CC.xyw(1,15,5));
    
    updateOkBtn();
    Settings.layoutWindow("editFilterComponentDlg", this, new Dimension(500,550));
    setLocationRelativeTo(parent);
    setVisible(true);
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
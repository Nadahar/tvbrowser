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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ParserException;
import tvbrowser.core.filters.UserFilter;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.DefaultComponentFactory;

public class EditFilterDlg extends JDialog implements ActionListener, DocumentListener, CaretListener, WindowClosingIf {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(EditFilterDlg.class);

  private static final util.ui.Localizer mFilterLocalizer = util.ui.Localizer.getLocalizerFor(UserFilter.class);

  private JButton mNewBtn, mEditBtn, mRemoveBtn, mOkBtn, mCancelBtn;

  private JFrame mParent;

  private JTable mRuleTableBox;

  private JTextField mFilterNameTF, mFilterRuleTF;

  private FilterTableModel mComponentTableModel;

  private UserFilter mFilter = null;

  private JLabel mFilterRuleErrorLb, mColLb;

  private String mFilterName = null;

  private FilterList mFilterList;

  public EditFilterDlg(JFrame parent, FilterList filterList, UserFilter filter) {

    super(parent, true);

    UiUtilities.registerForClosing(this);

    mFilterList = filterList;
    mParent = parent;
    mFilter = filter;

    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout(7, 7));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    if (filter == null) {
      setTitle(mLocalizer.msg("titleNew", "Create filter"));
    } else {
      setTitle(mLocalizer.msg("titleEdit", "Edit filter {0}", filter.toString()));
      mFilterName = filter.toString();
    }

    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

    mFilterNameTF = new JTextField(new PlainDocument() {

      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        str = str.replaceAll("[\\p{Punct}&&[^_]]", "_");
        super.insertString(offset, str, a);
      }
    }, "", 30);


    mFilterNameTF.getDocument().addDocumentListener(this);
    JPanel panel = new JPanel(new BorderLayout(7, 7));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
    panel.add(new JLabel(mLocalizer.msg("filterName", "Filter name:")), BorderLayout.WEST);
    JPanel panel1 = new JPanel(new BorderLayout());
    panel1.add(mFilterNameTF, BorderLayout.WEST);
    panel.add(panel1, BorderLayout.CENTER);
    northPanel.add(panel);

    mFilterRuleTF = new JTextField();
    mFilterRuleTF.getDocument().addDocumentListener(this);
    mFilterRuleTF.addCaretListener(this);
    panel = new JPanel(new BorderLayout(7, 7));
    panel1 = new JPanel(new BorderLayout());
    panel.add(new JLabel(mLocalizer.msg("ruleString", "Filter rule:")), BorderLayout.WEST);
    JLabel exampleLb = new JLabel(mLocalizer.msg("ruleExample",
        "example: component1 or (component2 and not component3)"));
    Font f = exampleLb.getFont();
    exampleLb.setFont(new Font(f.getName(), Font.ITALIC | Font.PLAIN, f.getSize()));
    panel1.add(exampleLb, BorderLayout.WEST);
    panel.add(panel1, BorderLayout.CENTER);
    northPanel.add(panel);
    northPanel.add(mFilterRuleTF);
    mFilterRuleErrorLb = new JLabel();
    mFilterRuleErrorLb.setForeground(Color.red);
    panel = new JPanel(new BorderLayout(7, 7));
    panel.add(mFilterRuleErrorLb, BorderLayout.WEST);
    mColLb = new JLabel("0");
    panel.add(mColLb, BorderLayout.EAST);
    northPanel.add(panel);

    JPanel filterComponentsPanel = new JPanel(new BorderLayout(7, 7));
    filterComponentsPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("componentsTitle",
        "Available filter components:")), BorderLayout.NORTH);
    JPanel btnPanel = new JPanel(new BorderLayout());
    panel1 = new JPanel(new GridLayout(0, 1, 0, 7));

    mNewBtn = new JButton(mLocalizer.msg("newButton", "new"));
    mEditBtn = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT));
    mRemoveBtn = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE));

    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);

    panel1.add(mNewBtn);
    panel1.add(mEditBtn);
    panel1.add(mRemoveBtn);

    btnPanel.add(panel1, BorderLayout.NORTH);

    mComponentTableModel = new FilterTableModel();

    mRuleTableBox = new JTable(mComponentTableModel);
    mRuleTableBox.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateBtns();
      }
    });

    mRuleTableBox.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
          int row = mRuleTableBox.rowAtPoint(e.getPoint());

          if(mRuleTableBox.getSelectedRow() == row && mEditBtn.isEnabled())
            actionPerformed(new ActionEvent(mEditBtn,ActionEvent.ACTION_PERFORMED, mEditBtn.getActionCommand()));
        }
      }
    });

    mRuleTableBox.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    mRuleTableBox.setShowGrid(false);
    mRuleTableBox.setShowVerticalLines(true);
    mRuleTableBox.getColumnModel().getColumn(0).setPreferredWidth(125);
    mRuleTableBox.getColumnModel().getColumn(1).setPreferredWidth(320);
//    mRuleTableBox.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // Dispatchs the KeyEvent to the RootPane for Closing the Dialog.
    // Needed for Java 1.4.
    mRuleTableBox.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
          mRuleTableBox.getRootPane().dispatchEvent(e);
      }
    });


    JPanel ruleListBoxPanel = new JPanel(new BorderLayout());
    ruleListBoxPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
    ruleListBoxPanel.add(new JScrollPane(mRuleTableBox), BorderLayout.CENTER);

    filterComponentsPanel.add(btnPanel, BorderLayout.EAST);
    filterComponentsPanel.add(ruleListBoxPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    buttonPanel.add(mOkBtn);
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);
    buttonPanel.add(mCancelBtn);

    contentPane.add(northPanel, BorderLayout.NORTH);
    contentPane.add(filterComponentsPanel, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    if (mFilter != null) {
      mFilterNameTF.setText(mFilter.toString());
      mFilterRuleTF.setText(mFilter.getRule());
    }

    FilterComponent[] fc = FilterComponentList.getInstance().getAvailableFilterComponents();

    Arrays.sort(fc, new FilterComponent.NameComparator());

    for (int i = 0; i < fc.length; i++) {
      mComponentTableModel.addElement(fc[i]);
    }

    updateBtns();

    Settings.layoutWindow("editFilterDlg",this,new Dimension(600,300));
    setVisible(true);
  }

  private void updateBtns() {
    if (mRuleTableBox == null) {
      return;
    }
    Object item = mRuleTableBox.getSelectedRows();
    final boolean enabled = item != null;
    mEditBtn.setEnabled(enabled);
    mRemoveBtn.setEnabled(enabled);

    boolean validRule = true;
    try {
      UserFilter.testTokenTree(mFilterRuleTF.getText());
      mFilterRuleErrorLb.setText("");
    } catch (ParserException e) {
      mFilterRuleErrorLb.setText(e.getMessage());
      validRule = false;
    }

    mOkBtn.setEnabled(StringUtils.isNotBlank(mFilterNameTF.getText()) && mComponentTableModel.getRowCount() > 0 && validRule);
  }

  public void actionPerformed(ActionEvent e) {

    Object o = e.getSource();
    if (o == mNewBtn) {
      EditFilterComponentDlg dlg = new EditFilterComponentDlg(mParent);
      FilterComponent rule = dlg.getFilterComponent();
      if (rule != null) {
        mComponentTableModel.addElement(rule);
        tvbrowser.core.filters.FilterComponentList.getInstance().add(rule);
        String text = mFilterRuleTF.getText();
        if (text.length() > 0) {
          text += " " + mFilterLocalizer.msg("or", "or") + " ";
        }
        text += rule.getName();
        mFilterRuleTF.setText(text);

      }
    } else if (o == mEditBtn) {
      int inx = mRuleTableBox.getSelectedRow();

      if(inx == -1)
        return;

      FilterComponent rule = mComponentTableModel.getElement(inx);
      FilterComponentList.getInstance().remove(rule.getName());
      mComponentTableModel.removeElement(rule);
      EditFilterComponentDlg dlg = new EditFilterComponentDlg(mParent, rule);
      FilterComponent newRule = dlg.getFilterComponent();
      if (newRule == null) {
        newRule = rule;
      }
      FilterComponentList.getInstance().add(newRule);
      mComponentTableModel.addElement(newRule);
      // mRuleListBox.repaint();
      updateBtns();

    } else if (o == mRuleTableBox) {
      updateBtns();
    } else if (o == mRemoveBtn) {
      boolean allowRemove = true;
      UserFilter[] userFilterArr = mFilterList.getUserFilterArr();
      FilterComponent fc = mComponentTableModel.getElement(mRuleTableBox.getSelectedRow());

      // Create the Filter based on the new Rule and check if the FC exists
      // there
      UserFilter testFilter = new UserFilter("test");

      try {
        testFilter.setRule(mFilterRuleTF.getText());

        if (testFilter.containsRuleComponent(fc.getName())) {
          allowRemove = false;
          JOptionPane.showMessageDialog(this, mLocalizer.msg("usedByAnotherFilter",
              "This filter component is used by filter '{0}'\nRemove the filter first.", mFilterNameTF.getText()));
        }
      } catch (Exception ex) {
        // Filter creation failed, assume the old one is correct
        if ((mFilter != null) && (mFilter.containsRuleComponent(fc.getName()))) {
          allowRemove = false;
          JOptionPane.showMessageDialog(this, mLocalizer.msg("usedByAnotherFilter",
              "This filter component is used by filter '{0}'\nRemove the filter first.", mFilterNameTF.getText()));
        }
      }

      for (int i = 0; i < userFilterArr.length && allowRemove; i++) {
        if ((userFilterArr[i] != mFilter) && userFilterArr[i].containsRuleComponent(fc.getName())) {
          allowRemove = false;
          JOptionPane.showMessageDialog(this, mLocalizer.msg("usedByAnotherFilter",
              "This filter component is used by filter '{0}'\nRemove the filter first.", userFilterArr[i].toString()));
        }
      }
      if (allowRemove) {
        FilterComponentList.getInstance().remove(fc.getName());
        mComponentTableModel.removeElement(mRuleTableBox.getSelectedRow());
        updateBtns();
      }

    } else if (o == mOkBtn) {
      String filterName = mFilterNameTF.getText();
      if (!filterName.equalsIgnoreCase(mFilterName) && mFilterList.containsFilter(filterName)) {
        JOptionPane
            .showMessageDialog(this, mLocalizer.msg("alreadyExists", "Filter '{0}' already exists.", filterName));
      } else {
        if (mFilter == null) {
          mFilter = new UserFilter(mFilterNameTF.getText());
        } else {
          mFilter.setName(mFilterNameTF.getText());
        }

        try {
          mFilter.setRule(mFilterRuleTF.getText());
          FilterComponentList.getInstance().store();
          setVisible(false);
        } catch (ParserException exc) {
          JOptionPane.showMessageDialog(this, mLocalizer.msg("invalidRule", "Invalid rule: ") + exc.getMessage());
        }
      }

    } else if (o == mCancelBtn) {
      setVisible(false);
    }

  }

  public UserFilter getUserFilter() {
    return mFilter;
  }

  public void changedUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void insertUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void removeUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void caretUpdate(javax.swing.event.CaretEvent e) {
    mColLb.setText("pos: " + mFilterRuleTF.getCaretPosition());
  }

  private static class FilterTableModel extends AbstractTableModel {

    private Vector<FilterComponent> dataVector;

    public FilterTableModel() {
      dataVector = new Vector<FilterComponent>();
    }

    public int getRowCount() {
      return dataVector.size();
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int columnIndex) {
      if (columnIndex == 0) {
        return mLocalizer.msg("filtername", "Filtername");
      } else if (columnIndex == 1) {
        return mLocalizer.msg("description", "Description");
      }
      return "?";
    }

    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      Object row = dataVector.get(rowIndex);
      if (row instanceof FilterComponent) {
        FilterComponent comp = (FilterComponent) row;

        if (columnIndex == 0) {
          return comp.getName();
        } else if (columnIndex == 1) {
          return comp.getDescription();
        }

      }
      return "?";
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    public void addElement(FilterComponent fComp) {
      dataVector.add(fComp);
      fireTableRowsInserted(getRowCount(), getRowCount());
    }

    public FilterComponent getElement(int row) {
      if (row < 0 || row >= getRowCount()) {
        return null;
      }
      return dataVector.get(row);
    }

    public void removeElement(int row) {
      if (row < 0 || row >= getRowCount()) {
        return;
      }
      dataVector.remove(row);
      fireTableRowsDeleted(row, row);
    }

    public void removeElement(FilterComponent fComp) {
      if (fComp == null) {
        return;
      }
      if (dataVector.remove(fComp)) {
        fireTableDataChanged();
      }
    }

  }

  public void close() {
    setVisible(false);
  }

}
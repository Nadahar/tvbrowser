/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package searchplugin;


import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;

import devplugin.*;

/**
 *
 * @author  Til Schneider, www.murfman.de
 */
public class SearchDialog extends JDialog {
  
  private static final Border DIALOG_BORDER
    = BorderFactory.createEmptyBorder(5, 5, 0, 5);
  
  private static final String[] TIME_STRING_ARR = new String[] {
    "n‰chsten 14 Tagen", "Alle Daten", "Heute", "Morgen", "Eine Woche",
    "3 Wochen", "letzte Woche"
  };

  private static final int[] TIME_VALUE_ARR = new int[] {
    14, 1000, 0, 1, 7, 21, -7
  };
  
  private JTextField mPatternTF;
  private JComboBox mTimeCB;
  private JRadioButton mMatchExactlyRB, mMatchSubstringRB, mRegexRB;
  private JCheckBox mCaseSensitiveChB;
  private JCheckBox mSearchTitleChB, mSearchInTextChB;
  private JButton mSearchBt, mCloseBt;



  /**
   * Creates a new instance of SearchDialog.
   */
  public SearchDialog(Frame parent) {
    super(parent);
    
    setTitle("Sendungen suchen");
    
    JPanel p1;
    
    JPanel main = new JPanel(new TabLayout(1));
    main.setBorder(DIALOG_BORDER);
    setContentPane(main);
    
    // pattern
    p1 = new JPanel(new TabLayout(2));
    main.add(p1);
    
    p1.add(new JLabel("Suchbegriff"));
    p1.add(new JLabel("Zeitraum"));

    mPatternTF = new JTextField(20);
    p1.add(mPatternTF);
    mTimeCB = new JComboBox(TIME_STRING_ARR);
    p1.add(mTimeCB);

    // search in
    p1 = new JPanel(new TabLayout(1));
    p1.setBorder(BorderFactory.createTitledBorder("Suchen in"));
    main.add(p1);

    mSearchTitleChB = new JCheckBox("Titel");
    mSearchTitleChB.setSelected(true);
    p1.add(mSearchTitleChB);

    mSearchInTextChB = new JCheckBox("Info-Text");
    p1.add(mSearchInTextChB);
    
    // options
    p1 = new JPanel(new TabLayout(1));
    p1.setBorder(BorderFactory.createTitledBorder("Optionen"));
    main.add(p1);
    
    ButtonGroup bg = new ButtonGroup();
    mMatchExactlyRB = new JRadioButton("Genaue ‹bereinstimmung");
    bg.add(mMatchExactlyRB);
    p1.add(mMatchExactlyRB);
    
    mMatchSubstringRB = new JRadioButton("Suchbegriff ist ein Stichwort");
    mMatchSubstringRB.setSelected(true);
    bg.add(mMatchSubstringRB);
    p1.add(mMatchSubstringRB);
    
    mRegexRB = new JRadioButton("Suchbegriff ist Regul‰rer Ausdruck");
    bg.add(mRegexRB);
    p1.add(mRegexRB);
    
    mCaseSensitiveChB = new JCheckBox("Groﬂ-/Kleinschreibung beachten");
    p1.add(mCaseSensitiveChB);
        
    // the buttons
    JPanel buttonPn = new JPanel();
    main.add(buttonPn);
    
    mSearchBt = new JButton("Suchen");
    mSearchBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        search();
      }
    });
    buttonPn.add(mSearchBt);
    getRootPane().setDefaultButton(mSearchBt);

    mCloseBt = new JButton("Abbrechen");
    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(mCloseBt);
    
    pack();
  }

  
  
  private void search() {
    String regex;
    if (mMatchExactlyRB.isSelected()) {
      regex = "\\Q" + mPatternTF.getText() + "\\E";
    }
    else if (mMatchSubstringRB.isSelected()) {
      regex = ".*\\Q" + mPatternTF.getText() + "\\E.*";
    }
    else {
      regex = mPatternTF.getText();
    }
    
    boolean inTitle = mSearchTitleChB.isSelected();
    boolean inText = mSearchInTextChB.isSelected();
    boolean caseSensitive = mCaseSensitiveChB.isSelected();
    Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
    devplugin.Date startDate = new devplugin.Date();
    int nrDays = TIME_VALUE_ARR[mTimeCB.getSelectedIndex()];
    
    try {
      Program[] programArr = SearchPlugin.search(regex, inTitle, inText,
        caseSensitive, channels, startDate, nrDays);

      if (programArr.length == 0) {
        String msg = "Keine Sendungen mit '" + mPatternTF.getText() + "' gefunden!";
        JOptionPane.showMessageDialog(this, msg);
      } else {
        String title = "Sendungen mit '" + mPatternTF.getText() + "'";
        showHitsDialog(programArr, title);
      }
    }
    catch (java.util.regex.PatternSyntaxException exc) {
      String msg = "Fehler in regul‰rem Ausdruck!\n\n" + exc.getMessage();
      JOptionPane.showMessageDialog(this, msg);
    }
  }
  
  
  
  private void showHitsDialog(Program[] programArr, String title) {
    final JDialog dlg = new JDialog(this, title, true);
    
    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(DIALOG_BORDER);
    dlg.setContentPane(main);
    
    JComponent[] programPanelArr = new JComponent[programArr.length];
    for (int i = 0; i < programArr.length; i++) {
      programPanelArr[i] = Plugin.getPluginManager().createProgramPanel(programArr[i]);
    }
    JList list = new JList(programPanelArr);
    list.setCellRenderer(new ComponentCellRenderer());
    main.add(new JScrollPane(list), BorderLayout.CENTER);
    
    JPanel buttonPn = new JPanel();
    main.add(buttonPn, BorderLayout.SOUTH);
    
    JButton closeBt = new JButton("Schlieﬂen");
    closeBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dlg.dispose();
      }
    });
    buttonPn.add(closeBt);
    
    dlg.setSize(400, 400);
    dlg.show();
  }
  
  
  // inner class ComponentCellRenderer
  
  
  class ComponentCellRenderer extends DefaultListCellRenderer {
    
    /**
     * Return a component that has been configured to display the specified
     * value. That component's <code>paint</code> method is then called to
     * "render" the cell.  If it is necessary to compute the dimensions
     * of a list because the list cells do not have a fixed size, this method
     * is called to generate a component on which <code>getPreferredSize</code>
     * can be invoked.
     *
     * @param list The JList we're painting.
     * @param value The value returned by list.getModel().getElementAt(index).
     * @param index The cells index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     *
     * @see JList
     * @see ListSelectionModel
     * @see ListModel
     */
    public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus)
    {
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
        index, isSelected, cellHasFocus);
      
      if (value instanceof JComponent) {
        JComponent comp = (JComponent) value;
        comp.setBackground(label.getBackground());
        comp.setForeground(label.getForeground());
        comp.setEnabled(label.isEnabled());
        comp.setBorder(label.getBorder());
        comp.setOpaque(true);
        
        return comp;
      }
      
      return label;
    }
    
  }
  
}

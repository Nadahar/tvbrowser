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

import util.exc.*;
import util.ui.TabLayout;
import util.ui.UiUtilities;

import devplugin.*;

/**
 * A dialog for searching programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchDialog extends JDialog {

  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchDialog.class);
  
  /** The messages for the time combo box. */  
  private static final String[] TIME_STRING_ARR = new String[] {
    mLocalizer.msg("search.14", "Nächste 14 Tagen"),
    mLocalizer.msg("search.1000", "Alle Daten"),
    mLocalizer.msg("search.0", "Heute"),
    mLocalizer.msg("search.1", "Morgen"),
    mLocalizer.msg("search.7", "Eine Woche"),
    mLocalizer.msg("search.21", "3 Wochen"),
    mLocalizer.msg("search.-7", "Letzte Woche")
  };

  /** The values for the time combo box. */  
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
   *
   * @param parent The dialog's parent.
   */
  public SearchDialog(Frame parent) {
    super(parent);
    
    String msg;
    JPanel p1;
    
    msg = mLocalizer.msg("dlgTitle", "Search programs");
    setTitle(msg);
    
    JPanel main = new JPanel(new TabLayout(1));
    main.setBorder(UiUtilities.DIALOG_BORDER);
    setContentPane(main);
    
    // pattern
    p1 = new JPanel(new TabLayout(2));
    main.add(p1);
    
    msg = mLocalizer.msg("searchTerm", "Search term");
    p1.add(new JLabel(msg));
    msg = mLocalizer.msg("period", "Period");
    p1.add(new JLabel(msg));

    mPatternTF = new JTextField(20);
    p1.add(mPatternTF);
    mTimeCB = new JComboBox(TIME_STRING_ARR);
    p1.add(mTimeCB);

    // search in
    p1 = new JPanel(new TabLayout(1));
    msg = mLocalizer.msg("searchIn", "Search in");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);

    msg = mLocalizer.msg("title", "Title");
    mSearchTitleChB = new JCheckBox(msg);
    mSearchTitleChB.setSelected(true);
    p1.add(mSearchTitleChB);

    msg = mLocalizer.msg("infoText", "Information text");
    mSearchInTextChB = new JCheckBox(msg);
    p1.add(mSearchInTextChB);
    
    // options
    p1 = new JPanel(new TabLayout(1));
    msg = mLocalizer.msg("options", "Options");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);
    
    ButtonGroup bg = new ButtonGroup();
    msg = mLocalizer.msg("matchExactly", "Match exactly");
    mMatchExactlyRB = new JRadioButton(msg);
    bg.add(mMatchExactlyRB);
    p1.add(mMatchExactlyRB);
    
    msg = mLocalizer.msg("matchSubstring", "Term is a keyword");
    mMatchSubstringRB = new JRadioButton(msg);
    mMatchSubstringRB.setSelected(true);
    bg.add(mMatchSubstringRB);
    p1.add(mMatchSubstringRB);
    
    msg = mLocalizer.msg("matchRegex", "Term is a regular expression");
    mRegexRB = new JRadioButton(msg);
    bg.add(mRegexRB);
    p1.add(mRegexRB);
    
    msg = mLocalizer.msg("caseSensitive", "Case sensitive");
    mCaseSensitiveChB = new JCheckBox(msg);
    p1.add(mCaseSensitiveChB);
        
    // the buttons
    JPanel buttonPn = new JPanel();
    main.add(buttonPn);
    
    msg = mLocalizer.msg("search", "Search");
    mSearchBt = new JButton(msg);
    mSearchBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        search();
      }
    });
    buttonPn.add(mSearchBt);
    getRootPane().setDefaultButton(mSearchBt);

    msg = mLocalizer.msg("cancel", "Cancel");
    mCloseBt = new JButton(msg);
    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(mCloseBt);
    
    pack();
  }

  
  
  /**
   * Starts the search.
   */  
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
      Program[] programArr = SearchPlugin.getPluginManager().search(regex,
        inTitle, inText, caseSensitive, channels, startDate, nrDays);

      if (programArr.length == 0) {
        String msg = mLocalizer.msg("nothingFound",
          "No programs found with '{0}'!", mPatternTF.getText());
        JOptionPane.showMessageDialog(this, msg);
      } else {
        String title = mLocalizer.msg("hitsTitle",
          "Sendungen mit '{0}'",  mPatternTF.getText());
        showHitsDialog(programArr, title);
      }
    }
    catch (TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
  }
  
  
  
  /**
   * Shows a dialog containing the hits of the search.
   *
   * @param programArr The hits.
   * @param title The dialog's title.
   */  
  private void showHitsDialog(Program[] programArr, String title) {
    final JDialog dlg = new JDialog(this, title, true);
    
    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    dlg.setContentPane(main);
    
    JComponent[] programPanelArr = new JComponent[programArr.length];
    for (int i = 0; i < programArr.length; i++) {
      JPanel p1 = new JPanel(new BorderLayout());
      JComponent progPn = Plugin.getPluginManager().createProgramPanel(programArr[i]);
      p1.add(progPn, BorderLayout.CENTER);
      String msg = programArr[i].getDate() + " - " + programArr[i].getChannel().getName();
      p1.add(new JLabel(msg), BorderLayout.NORTH);
      
      programPanelArr[i] = p1;
    }
    JList list = new JList(programPanelArr);
    list.setCellRenderer(new ComponentCellRenderer());
    main.add(new JScrollPane(list), BorderLayout.CENTER);
    
    JPanel buttonPn = new JPanel();
    main.add(buttonPn, BorderLayout.SOUTH);
    
    JButton closeBt = new JButton(mLocalizer.msg("close", "Close"));
    closeBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dlg.dispose();
      }
    });
    buttonPn.add(closeBt);
    
    dlg.setSize(400, 400);
    UiUtilities.centerAndShow(dlg);
  }
  
  
  // inner class ComponentCellRenderer
  
  
  /**
   * A list cell renderer that renders JComponents.
   */  
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

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

package searchplugin;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.*;


import util.exc.*;
import util.ui.*;

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
    mLocalizer.msg("search.14", "Naechste 14 Tagen"),
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
  
  //private JTextField mPatternTF;
  private JComboBox mPatternCB;
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
    super(parent,true);
    
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

    //mPatternTF = new JTextField(20);
    mPatternCB=new JComboBox(SearchPlugin.searchHistory.toArray());
    mPatternCB.setEditable(true);
    mPatternCB.setMaximumRowCount(3);
    mPatternCB.setSelectedIndex(-1);
    
    //mPatternCB.setSelectedIndex(0);
    //p1.add(mPatternTF);
    p1.add(mPatternCB);
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
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
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
    
	mPatternCB.addItemListener(new ItemListener() {
			public void itemStateChanged (ItemEvent e) {
				if (e.getStateChange()!=ItemEvent.SELECTED) return;
				int inx=mPatternCB.getSelectedIndex();
				if (inx<0) return;
				SearchSettings item=(SearchSettings)mPatternCB.getSelectedItem();
				
				mSearchInTextChB.setSelected(item.getSearchInInfoText());
				mSearchTitleChB.setSelected(item.getSearchInTitle());
				mCaseSensitiveChB.setSelected(item.getCaseSensitive());
				int option=item.getOption();
				if (option==SearchSettings.EXACTLY) {
					mMatchExactlyRB.setSelected(true);
				}else if (option==SearchSettings.KEYWORD) {
					mMatchSubstringRB.setSelected(true);
				}else if (option==SearchSettings.REGULAR_EXPRESSION) {
					mRegexRB.setSelected(true);
				}
    		
			}
		});
    
    
    pack();
  }
  
  
  
  /**
   * Sets the text of the pattern text field.
   *
   * @param text The new pattern text.
   */
  public void setPatternText(String text) {
    ComboBoxEditor editor=mPatternCB.getEditor();
    JTextField tf=(JTextField)editor.getEditorComponent();
    tf.setText(text);
    mSearchTitleChB.setSelected(true);
    mSearchInTextChB.setSelected(false);
    mMatchExactlyRB.setSelected(true);
    mCaseSensitiveChB.setSelected(true);
  }

  
  
  /**
   * Starts the search.
   */  
  private void search() {
    String regex;
    String searchTxt;
	SearchSettings item;
	
	java.util.ArrayList list=SearchPlugin.searchHistory;
	
	int sel=mPatternCB.getSelectedIndex();
    if (sel==-1) {
		searchTxt=(String)mPatternCB.getSelectedItem();
		item=new SearchSettings(searchTxt);
		if (list.size()>0) {
			list.add(list.get(list.size()-1));
		}
		
		item.setCaseSensitive(mCaseSensitiveChB.isSelected());
		int opt=0;
		if (mMatchExactlyRB.isSelected()) opt=SearchSettings.EXACTLY;
		else if (mMatchSubstringRB.isSelected()) opt=SearchSettings.KEYWORD;
		else if (mRegexRB.isSelected()) opt=SearchSettings.REGULAR_EXPRESSION;
		item.setOption(opt);
		item.setSearchInInfoText(mSearchInTextChB.isSelected());
		item.setSearchInTitle(mSearchTitleChB.isSelected());
		
				
    }
    else {
    	item=(SearchSettings)mPatternCB.getSelectedItem();
    	searchTxt=item.toString();
    }
    
    
    if (sel==-1) {
    	sel=list.size()-1;
    }
	for (int i=sel;i>0;i--) {
    	list.set(i,list.get(i-1));
    }
    if (list.size()>SearchPlugin.MAXHISTORYLENGTH) {
    	list.remove(list.size()-1);
    }
    if (list.size()>0) {
    	list.set(0,item);
    }else{
    	list.add(item); 
    }
	mPatternCB.removeAllItems();
	java.util.Iterator iterator=list.iterator();
	while (iterator.hasNext()) {
		mPatternCB.addItem(iterator.next());
	}
    
    if (mMatchExactlyRB.isSelected()) {
    	
      //regex = "\\Q" + mPatternTF.getText() + "\\E";
	  regex = "\\Q" + searchTxt + "\\E";
    }
    else if (mMatchSubstringRB.isSelected()) {
		//regex = ".*\\Q" + mPatternTF.getText() + "\\E.*";
      regex = ".*\\Q" + searchTxt + "\\E.*";
    }
    else {
      //regex = mPatternTF.getText();
	  regex = searchTxt;
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
        //  "No programs found with '{0}'!", mPatternTF.getText());
		"No programs found with '{0}'!", searchTxt);
        JOptionPane.showMessageDialog(this, msg);
      } else {
        String title = mLocalizer.msg("hitsTitle",
        //  "Sendungen mit '{0}'",  mPatternTF.getText());
		"Sendungen mit '{0}'", searchTxt);
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
  private void showHitsDialog(final Program[] programArr, String title) {
    final JDialog dlg = new JDialog(this, title, true);
    
    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    dlg.setContentPane(main);
    
    final JList list = new JList(programArr);
    list.setCellRenderer(new ProgramListCellRenderer());
    main.add(new JScrollPane(list), BorderLayout.CENTER);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);
    
    JButton closeBt = new JButton(mLocalizer.msg("close", "Close"));
    closeBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dlg.dispose();
      }
    });
    dlg.getRootPane().setDefaultButton(closeBt);
    buttonPn.add(closeBt);
    
    
    
    list.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
    		if (SwingUtilities.isRightMouseButton(e)) {
				int inx=list.locationToIndex(e.getPoint());
				JPopupMenu menu=devplugin.Plugin.getPluginManager().createPluginContextMenu(programArr[inx],SearchPlugin.getInstance());
				
				menu.show(list, e.getX() - 15, e.getY() - 15);
			}
    	}
    	
    });
    
	dlg.setSize(400, 400);
	UiUtilities.centerAndShow(dlg);
    
  }
  
}

/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser
 * (darras@users.sourceforge.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * CVS information: $RCSfile$ $Source:
 * /cvsroot/tvbrowser/tvbrowser/src/tvbrowser/core/filters/filtercomponents/TimeFilterComponent.java,v $
 * $Date$ $Author$ $Revision$
 */
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tvbrowser.core.filters.FilterComponent;
import util.exc.TvBrowserException;
import util.ui.LineNumberHeader;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;
import devplugin.Program;
import devplugin.ProgramSearcher;

/**
 * This FilterComponent allows Filtering of large Search-Array
 * 
 * @author bodum
 */
public class MassFilterComponent implements FilterComponent {

  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MassFilterComponent.class);

  private String mName, mDescription, mText;

  private SearchFormSettings mSearchFormSettings, mNewSearchFormSettings;

  private ProgramSearcher[] mSearcher;
  
  private JTextArea mTextInput;
  
  private JPanel mSettingsPanel;

  /**
   * Create the Filter
   */
  public MassFilterComponent() {
    this("", "");
  }

  /**
   * Create the Filter
   * 
   * @param name 
   * @param description 
   */
  public MassFilterComponent(String name, String description) {
    mName = name;
    mDescription = description;
    mText = "";
    mSearchFormSettings = new SearchFormSettings("");
    generateSearcher();
  }

  /**
   * Generates the Search-Array
   */
  private void generateSearcher() {
    ArrayList array = new ArrayList();
    String[] keys = mText.split("\\n");
    
    for (int i=0;i<keys.length;i++) {
      try {
        array.add(mSearchFormSettings.createSearcher(keys[i]));
      } catch (TvBrowserException ex) {
        ex.printStackTrace();
      }
    }
    
    mSearcher = (ProgramSearcher[]) array.toArray(new ProgramSearcher[0]);
  }

  public int getVersion() {
    return 1;
  }

  public boolean accept(Program program) {
    
    if (mSearcher != null) {
      
      for (int i=0;i<mSearcher.length;i++) {
        if (mSearcher[i].matches(program, mSearchFormSettings.getFieldTypes())) {
          return true;
        }
      }
      
    }
    
    return false;
  }

  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    mText = (String) in.readObject();
    mSearchFormSettings = new SearchFormSettings(in);
    generateSearcher();
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeObject(mText);
    mSearchFormSettings.writeData(out);
  }

  public JPanel getPanel() {
    mNewSearchFormSettings = mSearchFormSettings;
    
    mSettingsPanel = new JPanel(new BorderLayout());

    mSettingsPanel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("desc", "help-text")), BorderLayout.NORTH);

    mTextInput = new JTextArea(mText);
    JScrollPane scrollPane = new JScrollPane(mTextInput);
    LineNumberHeader header = new LineNumberHeader(mTextInput);
    scrollPane.setRowHeaderView(header);

    mSettingsPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton config = new JButton("Config");

    config.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        showConfigDialog();
      }
      
    });
    
    buttonPanel.add(config);

    mSettingsPanel.add(buttonPanel, BorderLayout.SOUTH);

    return mSettingsPanel;
  }

  /**
   * Show the Config-Dialog and update the SeachFormSettings
   */
  private void showConfigDialog() {
    Window w = UiUtilities.getBestDialogParent(mSettingsPanel);
    
    MassFilterSettingsDialog dialog;
    
    if (w instanceof JFrame) {
      dialog = new MassFilterSettingsDialog((JFrame) w, mNewSearchFormSettings);
    } else {
      dialog = new MassFilterSettingsDialog((JDialog) w, mNewSearchFormSettings);
    }
    dialog.setVisible(true);
    
    mNewSearchFormSettings = dialog.getSearchFormSettings();
  }
  
  public void ok() {
    mText = mTextInput.getText();
    mSearchFormSettings = mNewSearchFormSettings;
    generateSearcher();
  }

  public String toString() {
    return mLocalizer.msg("name", "Mass-Filter");
  }

  public String getName() {
    return mName;
  }

  public String getDescription() {
    return mDescription;
  }

  public void setName(String name) {
    mName = name;
  }

  public void setDescription(String desc) {
    mDescription = desc;
  }

}
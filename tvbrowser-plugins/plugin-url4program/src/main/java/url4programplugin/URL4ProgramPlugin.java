/*
 * Copyright René Mach
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
 *
 * SVN information:
 *       $Id: URL4ProgramPlugin.java 5833 2009-07-27 19:50:15Z ds10 $
 *     $Date: 2009-07-27 21:50:15 +0200 (Mo, 27 Jul 2009) $
 *   $Author: ds10 $
 * $Revision: 5833 $
 */
package url4programplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.Version;

/**
 * A Plugin for setting URLs for programs.
 *
 * @author René Mach
 *
 */
public class URL4ProgramPlugin extends Plugin {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(URL4ProgramPlugin.class);
  private static final Version VERSION = new Version(0, 13, 1, true);

  private Hashtable<String,UrlListEntry> mProgram2Url = new Hashtable<String,UrlListEntry>();
  private JDialog mDialog;
  private Properties mProperties;
  private JTable mUrlProgramTable;

  public static Version getVersion() {
    return VERSION;
  }

  /** @return The Plugin Info. */
  public PluginInfo getInfo() {
    return (new PluginInfo(URL4ProgramPlugin.class,"URL4ProgramPlugin", mLocalizer.msg("description","Set an URL for a program title"), "Ren\u00e9 Mach", "GPL"));
  }

  public void loadSettings(Properties prop) {
    if(prop == null) {
      mProperties = new Properties();
    } else {
      mProperties = prop;
    }
  }

  public Properties storeSettings() {
    return mProperties;
  }
  
  private static class ContextEntry implements Comparable<ContextEntry> {
    String mUrl;
    boolean mShortLink;
    
    public ContextEntry(String url, boolean shortLink) {
      mUrl = url;
      mShortLink = shortLink;
    }

    public int compareTo(ContextEntry o) {
      return mUrl.compareToIgnoreCase(o.mUrl);
    }
  }

  /**
   * @return The ActionMenu for this Plugin.
   */
  public ActionMenu getContextMenuActions(final Program p) {
    ContextMenuAction menu = null;

    if(!mProgram2Url.isEmpty()) {
      String title = p.getTitle();
      ArrayList<String> urlList = new ArrayList<String>();
      ArrayList<ContextEntry> entryList = new ArrayList<ContextEntry>();
      UrlListEntry entry = mProgram2Url.get(title);
      
      int entryMatchLength = 0;
      
      if(entry != null) {
        for(String url : entry.getUrls()) {
          urlList.add(url);
          entryList.add(new ContextEntry(url, entry.isShortLinkEntry()));
        }
        
        entryMatchLength = Integer.MAX_VALUE;
      }
      
      Enumeration<UrlListEntry> elements = mProgram2Url.elements();
      
      while(elements.hasMoreElements()) {
        UrlListEntry test = elements.nextElement();
        
        if(test.matches(p)) {
          if(test.getProgramTitle().trim().length() > entryMatchLength) {
            entry = test;
            entryMatchLength = test.getProgramTitle().trim().length();
          }
          
          for(String urlTest : test.getUrls()) {
            if(!urlList.contains(urlTest)) {
              urlList.add(urlTest);
              entryList.add(new ContextEntry(urlTest, test.isShortLinkEntry()));
            }
          }
        }
      }
      
      if(!entryList.isEmpty()) {
        menu = new ContextMenuAction("URL4ProgramPlugin",createImageIcon("apps","internet-web-browser",16));

        Action[] allActions = createOpenActionMenu(entryList);

        allActions[allActions.length-2] = ContextMenuSeparatorAction.getInstance();
        allActions[allActions.length-1] = createAddMenu(p,entry);

        return new ActionMenu(menu,allActions);
      } else {
        menu = createAddMenu(p,entry);
      }
    } else {
      menu = createAddMenu(p,null);
    }

    return new ActionMenu(menu);
  }

  private ContextMenuAction createAddMenu(final Program p, final UrlListEntry entry) {
    ContextMenuAction menu = new ContextMenuAction();
    menu.setText(mLocalizer.msg("add", "Add internet page for this program"));
    menu.putValue(Action.ACTION_COMMAND_KEY, menu.getValue(Action.NAME));
    menu.setSmallIcon(createImageIcon("apps","internet-web-browser",16));

    menu.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
        try {
        
        String titleHelp = mLocalizer.msg("column1", "Program title")+":";
        
        JTextField title = new JTextField(p.getTitle());
        JCheckBox useRegularExpression = new JCheckBox(mLocalizer.msg("titleRegular", "Title is regular expression"));
        String websiteHelp = mLocalizer.msg("column2", "Internet page(s)")+":";
        JTextField input = new JTextField();
        JCheckBox shortLink = new JCheckBox(mLocalizer.msg("fullLink","Show full link in context menu"),(entry == null ? true : !entry.isShortLinkEntry()));
        shortLink.setToolTipText(mLocalizer.msg("fullLinkTooltip","<html>If this is <b>not</b> selected only 'Open website' is shown in context menu.</html>"));

        if(entry != null) {
          if(entry.isFilterType()) {
            titleHelp = mLocalizer.msg("filter", "Program filter") + ":";
            useRegularExpression.setVisible(false);
          }
          
          title.setText(entry.getProgramTitle());
          title.setEnabled(false);
          useRegularExpression.setSelected(entry.isRegExType());
          useRegularExpression.setEnabled(false);
        }
        
        Object[] message = {mLocalizer.msg("addText","Adding of the internet page for the program:\n") + p.getTitle(), titleHelp, title, useRegularExpression, websiteHelp, input, shortLink};

        int i = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(getParentFrame()),
            message,mLocalizer.msg("addTitle","Add internet page for program"),JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);

        if(i == JOptionPane.OK_OPTION && input.getText().length() > 0) {
          if(entry == null) {
            mProgram2Url.put(title.getText(), new UrlListEntry((useRegularExpression.isSelected() ? UrlListEntry.REGEX_TYPE : UrlListEntry.TITLE_TYPE),p.getTitle(),new String[] {input.getText()},!shortLink.isSelected()));
          }
          else {
            entry.addUrl(input.getText(),!shortLink.isSelected());
          }
        }
        
      }catch(Throwable t) {t.printStackTrace();}
      }
    });

    return menu;
  }

  private Action[] createOpenActionMenu(ArrayList<ContextEntry> entryList) {
    Collections.sort(entryList);
    
    Action[] actions = new Action[entryList.size()+2];
    
    for(int i = 0; i < entryList.size(); i++) {
      ContextEntry entry = entryList.get(i);
      String text = entry.mUrl;

      if(entry.mShortLink) {
        text = text.substring(text.indexOf(".")+1);
        int index = text.indexOf("/");

        if(index != -1) {
          text = text.substring(0,index);
        }
      }

      ContextMenuAction action = new ContextMenuAction(mLocalizer.msg("open", "Open {0}", "'" + text + "'"),createImageIcon("apps","internet-web-browser",16));
      action.putValue(Action.ACTION_COMMAND_KEY, entry.mUrl);
      action.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Launch.openURL(e.getActionCommand());
        }
      });
      
      actions[i] = action;
    }

    /*ContextMenuAction menu = new ContextMenuAction();
    menu.setText(mLocalizer.msg("open", "Open {0}", showLink ? "'" + url + "'" : mLocalizer.msg("website","Website")));
    menu.putValue(Action.ACTION_COMMAND_KEY, menu.getValue(Action.NAME));
    menu.setSmallIcon(createImageIcon("apps","internet-web-browser",16));

    menu.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Launch.openURL(url);
      }
    });

    return menu;*/

    return actions;
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        showProgramURLTable();
      }
    };
    // Name of the buttons in the menu and the icon bar
    action.putValue(Action.NAME, "URL4ProgramPlugin");
    // small icon
    action.putValue(Action.SMALL_ICON, createImageIcon("apps","internet-web-browser",16));
    // big icon
    action.putValue(BIG_ICON, createImageIcon("apps","internet-web-browser",22));
    return new ActionMenu(action);
  }

  private void showProgramURLTable() {
    Window w = UiUtilities.getBestDialogParent(getParentFrame());

    if(w instanceof JFrame) {
      mDialog = new JDialog((JFrame)w, mLocalizer.msg("title","Edit programs"), true);
    } else {
      mDialog = new JDialog((JDialog)w, mLocalizer.msg("title","Edit programs"), true);
    }

    mDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        closeDialog();
      }
      public JRootPane getRootPane() {
        return mDialog.getRootPane();
      }
    });

    mDialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeDialog();
      }
    });

    JPanel p = (JPanel)mDialog.getContentPane();
    p.setLayout(new BorderLayout(0,5));
    p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    JButton ok = new JButton("OK");
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeDialog();
      }
    });

    final JButton delete = new JButton(createImageIcon("actions","edit-delete",16));
    delete.setToolTipText(mLocalizer.msg("tooltip","Delete selected entries"));
    delete.setEnabled(false);

    String[] keys = mProgram2Url.keySet().toArray(new String[mProgram2Url.size()]);

    Arrays.sort(keys, new Comparator<String>() {
      public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
      }
    });
    
    String[] head = {mLocalizer.msg("column1","Program title")+"/"+mLocalizer.msg("filter", "Program filter"),
                     mLocalizer.msg("column1a","RegEx"),
                     mLocalizer.msg("column2","Internet page"),
                     mLocalizer.msg("column3","Complete link")};

    ArrayList<UrlListEntry> entryList = new ArrayList<UrlListEntry>();
    
    for(UrlListEntry entry : mProgram2Url.values()) {
      entryList.add(entry);
    }
    
    Collections.sort(entryList);
    
    mUrlProgramTable = new JTable(new ProgramUrlTableModel(entryList, head)) {
      @Override
      public TableCellEditor getCellEditor(int row, int column) {
        UrlListEntry entry = ((ProgramUrlTableModel)getModel()).getEntryAtRow(row);
        
        if(column == 0 && entry.isFilterType()) {
          ProgramFilter[] filters = getPluginManager().getFilterManager().getAvailableFilters();
          
          JComboBox comboBox = new JComboBox();
          
          for(ProgramFilter filter : filters) {
            comboBox.addItem(filter.getName());
          }
          
          DefaultCellEditor edit = new DefaultCellEditor(comboBox);
          edit.setClickCountToStart(2);
          
          return edit;
        }
        else {
          return super.getCellEditor(row, column);
        }
      }
    };
    mUrlProgramTable.getColumnModel().getColumn(0).setCellRenderer(new UrlTableRenderer());
    mUrlProgramTable.getColumnModel().getColumn(1).setCellRenderer(new UrlTableRenderer());
    mUrlProgramTable.getColumnModel().getColumn(1).setMaxWidth(mUrlProgramTable.getColumnModel().getColumn(1).getPreferredWidth());    
    mUrlProgramTable.getColumnModel().getColumn(3).setCellRenderer(new UrlTableRenderer());
    mUrlProgramTable.getColumnModel().getColumn(3).setPreferredWidth(UiUtilities.getStringWidth(mUrlProgramTable.getFont(), mLocalizer.msg("full","Show complete link")) + 30);
    mUrlProgramTable.getColumnModel().getColumn(3).setMaxWidth(mUrlProgramTable.getColumnModel().getColumn(3).getPreferredWidth());
    
    mUrlProgramTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          ((JComponent) e.getComponent()).getRootPane().dispatchEvent(e);
        }
      }
    });

    mUrlProgramTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          int row = mUrlProgramTable.getSelectedRow();
          if (row != -1) {
            UrlListEntry entry = ((ProgramUrlTableModel)mUrlProgramTable.getModel()).getEntryAtRow(row);
            
            delete.setEnabled(true);

            int column = mUrlProgramTable.columnAtPoint(e.getPoint());

            if((column == 1 && !entry.isFilterType()) || column == 3) {
              Boolean oldValue = (Boolean)mUrlProgramTable.getValueAt(row, column);

              mUrlProgramTable.setValueAt(!oldValue.booleanValue(),row,column);
              mUrlProgramTable.setRowSelectionInterval(row, row);
            }
          } else {
            delete.setEnabled(false);
          }
        }
      }
    });

    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DefaultTableModel model = (DefaultTableModel) mUrlProgramTable.getModel();

        int[] rows = mUrlProgramTable.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
          model.removeRow(rows[i]);
          delete.setEnabled(false);
        }
      }
    });
    
    JButton add = new JButton(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    add.setToolTipText(mLocalizer.msg("tooltipAdd", "Add new entry"));
    add.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(getParentFrame()), mLocalizer.msg("addProgramFilter","Add entry with program filter?"), mLocalizer.msg("addProgramFilterTitle","Type selection"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          ((ProgramUrlTableModel)mUrlProgramTable.getModel()).addEntry(new UrlListEntry(UrlListEntry.FILTER_TYPE, getPluginManager().getFilterManager().getAllFilter().getName(), new String[] {"www.tvbrowser.org"}, false));
        }
        else {
          ((ProgramUrlTableModel)mUrlProgramTable.getModel()).addEntry(new UrlListEntry(UrlListEntry.TITLE_TYPE, "DUMMY", new String[] {"www.tvbrowser.org"}, false));
        }
      }
    });

    JScrollPane pane = new JScrollPane(mUrlProgramTable);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(delete);
    buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));
    buttonPanel.add(add);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(ok);

    p.add(buttonPanel, BorderLayout.SOUTH);
    p.add(pane, BorderLayout.CENTER);
    
    layoutWindow("programURLTableDialog", mDialog, new Dimension(600,300));
    
    mDialog.setVisible(true);

    if (mUrlProgramTable.isEditing()) {
      mUrlProgramTable.getCellEditor().stopCellEditing();
    }

    mProperties.setProperty("x", String.valueOf(mDialog.getX()));
    mProperties.setProperty("y", String.valueOf(mDialog.getY()));
    mProperties.setProperty("width", String.valueOf(mDialog.getWidth()));
    mProperties.setProperty("height", String.valueOf(mDialog.getHeight()));

    mProgram2Url.clear();
    
    for(UrlListEntry entry : entryList) {
      String key = entry.getProgramTitle();
      
      if(entry.isFilterType()) {
        key = "FILTER###" + key;
      }
      
      mProgram2Url.put(key,entry);
    }
  }

  private void closeDialog() {
    mDialog.dispose();
  }

  private static class ProgramUrlTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 1L;
    private ArrayList<UrlListEntry> mEntryList;

    /**
     * @param entries The entries of the table.
     * @param header The title of the columns.
     */
    ProgramUrlTableModel(ArrayList<UrlListEntry> entries, Object[] header) {
      super(header,entries.size());
      mEntryList = entries;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      if (columnIndex == 0 || columnIndex == 2) {
        return true;
      }

      return false;
    }
    
    @Override
    public Object getValueAt(int row, int column) {
      if(row < mEntryList.size()) {
        UrlListEntry entry = mEntryList.get(row);
        
        switch (column) {
          case 0: return entry.getProgramTitle();
          case 1: return entry.isRegExType();
          case 2: return getStringForArray(entry.getUrls());
          case 3: return entry.isShortLinkEntry();
        }      
      }
      
      return null;
    }
    
    @Override
    public void setValueAt(Object aValue, int row, int column) {
      if(row < mEntryList.size()) {
        UrlListEntry entry = mEntryList.get(row);
        
        switch (column) {
          case 0: entry.setTitle((String)aValue);break;
          case 1: entry.setType(((Boolean)aValue) ? UrlListEntry.REGEX_TYPE : UrlListEntry.TITLE_TYPE);break;
          case 2: entry.setUrlList(((String)aValue).split(";"));break;
          case 3: entry.setShortLink((Boolean)aValue);break;
        }
        
        fireTableDataChanged();
      }
    }
    
    public void addEntry(UrlListEntry entry) {
      mEntryList.add(entry);
      super.addRow(new Object[]{entry.getProgramTitle(),entry.isRegExType(),getStringForArray(entry.getUrls()),entry.isShortLinkEntry()});
    }
    
    public UrlListEntry getEntryAtRow(int row) {
      if(row < mEntryList.size()) {
        return mEntryList.get(row);
      }
      
      return null;
    }
    
    @Override
    public void removeRow(int row) {
      if(row < mEntryList.size()) {
        mEntryList.remove(row);
        super.removeRow(row);
      }
    }
    
/*    public ArrayList<UrlListEntry> getEntryList() {
      return mEntryList;
    }*/
    
    private String getStringForArray(String[] values) {
      StringBuilder builder = new StringBuilder();
      
      if(values.length > 0) {
        builder.append(values[0]);
      }
      
      for(int i = 1; i < values.length; i++) {
        builder.append(";").append(values[i]);
      }
      
      return builder.toString();
    }
  }
  
  public void readData(ObjectInputStream in) throws IOException,
  ClassNotFoundException {
    int version = in.readInt();

    if(version <= 2) {
      Hashtable<String,Object> tempTable = (Hashtable<String,Object>)in.readObject();

      Enumeration<String> e = tempTable.keys();

      while(e.hasMoreElements()) {
        Object key = e.nextElement();
        String value = null;
        boolean shortLink = false;

        if(version == 1) {
           value = (String)tempTable.get(key);
        }
        else {
           Object[] o = (Object[])tempTable.get(key);
           value = (String)o[0];
           shortLink = !(Boolean)o[1];
        }

        mProgram2Url.put((String)key, new UrlListEntry(UrlListEntry.TITLE_TYPE,(String)key, new String[] {value}, shortLink));
      }
    }
    else {
      int n = in.readInt();

      for(int i = 0; i < n; i++) {
        UrlListEntry entry = new UrlListEntry(in,version);
        mProgram2Url.put(entry.getProgramTitle(),entry);
      }
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(5); // version

    out.writeInt(mProgram2Url.size());

    Collection<UrlListEntry> listEntries = mProgram2Url.values();

    for(UrlListEntry entry : listEntries) {
      entry.writeData(out);
    }
  }

  private static class UrlTableRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)  {
      UrlListEntry entry = ((ProgramUrlTableModel)table.getModel()).getEntryAtRow(row);
      
      if(column == 0) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if(entry.isFilterType() && (c instanceof JLabel)) {
          ((JLabel)c).setText(mLocalizer.msg("filterShort", "FILTER:")+" " + entry.getProgramTitle());
        }
        
        return c;
      }
      else {
        JCheckBox box = null;
        
        if(column == 1) {
          box = new JCheckBox("",((Boolean)value).booleanValue());
          box.setEnabled(!entry.isFilterType());
          box.setVisible(!entry.isFilterType());
        }
        else {
          box = new JCheckBox(mLocalizer.msg("full","Show complete link"),((Boolean)value).booleanValue());
        }
        
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
  
        JPanel center = new JPanel(new FormLayout("default:grow,default,default:grow","default"));
        
        if(isSelected) {
          center.setBackground(table.getSelectionBackground());
          center.setForeground(table.getSelectionForeground());
        }
        
        center.add(box, CC.xy(2,1));
        center.setOpaque(isSelected);
        
        return center;
      }
    }
  }
}

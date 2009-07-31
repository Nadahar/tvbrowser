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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * A Plugin for setting URLs for programs.
 * 
 * @author René Mach
 *
 */
public class URL4ProgramPlugin extends Plugin {
  
  private static Localizer mLocalizer = Localizer.getLocalizerFor(URL4ProgramPlugin.class);
  
  private Hashtable<String,UrlListEntry> mProgram2Url = new Hashtable<String,UrlListEntry>();
  private JDialog mDialog;
  private Properties mProperties;
  private JTable mUrlProgramTable;
  
  public static Version getVersion() {
    return new Version(0, 10, true);
  }
  
  /** @return The Plugin Info. */
  public PluginInfo getInfo() {
    return (new PluginInfo(URL4ProgramPlugin.class,"URL4ProgramPlugin", mLocalizer.msg("description","Set an URL for a program title"), "René Mach", "GPL"));
  }
  
  public void loadSettings(Properties prop) {
    if(prop == null)
      mProperties = new Properties();
    else
      mProperties = prop;
  }
  
  public Properties storeSettings() {
    return mProperties;
  }
  
  /**
   * @return The ActionMenu for this Plugin.
   */
  public ActionMenu getContextMenuActions(final Program p) {    
    ContextMenuAction menu = null;
    
    if(!mProgram2Url.isEmpty()) {
      UrlListEntry entry = (UrlListEntry)mProgram2Url.get(p.getTitle());
      
      if(entry != null) {        
        String[] url = entry.getUrls();
        
        if(url.length > 0) {
          menu = new ContextMenuAction("URL4ProgramPlugin",createImageIcon("apps","internet-web-browser",16));
          
          Action[] urlActions = createOpenActionMenu(entry.getUrls(),entry.isShortLinkEntry());
          
          Action[] allActions = new Action[urlActions.length + 2];
          
          System.arraycopy(urlActions,0,allActions,0,urlActions.length);
          
          allActions[allActions.length-2] = ContextMenuSeparatorAction.getInstance();
          allActions[allActions.length-1] = createAddMenu(p,entry);
                 
          return new ActionMenu(menu,allActions);
        }
        else
          menu = createAddMenu(p,entry);
      }
      else
        menu = createAddMenu(p,entry);
    }
    else
      menu = createAddMenu(p,null);
    
    return new ActionMenu(menu);
  }
  
  private ContextMenuAction createAddMenu(final Program p, final UrlListEntry entry) {
    ContextMenuAction menu = new ContextMenuAction();
    menu.setText(mLocalizer.msg("add", "Add internet page for this program"));
    menu.putValue(Action.ACTION_COMMAND_KEY, menu.getValue(Action.NAME));
    menu.setSmallIcon(createImageIcon("apps","internet-web-browser",16));
    
    menu.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTextField input = new JTextField();
        JCheckBox shortLink = new JCheckBox(mLocalizer.msg("fullLink","Show full link in context menu"),(entry == null ? true : !entry.isShortLinkEntry()));
        shortLink.setToolTipText(mLocalizer.msg("fullLinkTooltip","<html>If this is <b>not</b> selected only 'Open website' is shown in context menu.</html>"));
 
        Object[] message = {mLocalizer.msg("addText","Adding of the internet page for the program:\n") + p.getTitle(), input, shortLink};
        
        int i = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(getParentFrame()),
            message,mLocalizer.msg("addTitle","Add internet page for program"),JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
        
        if(i == JOptionPane.OK_OPTION && input.getText().length() > 0) {
          if(entry == null) {
            mProgram2Url.put(p.getTitle(), new UrlListEntry(p.getTitle(),new String[] {input.getText()},!shortLink.isSelected()));
          }
          else {
            entry.addUrl(input.getText(),!shortLink.isSelected());
          }
        }
      }
    });
    
    return menu;
  }
  
  private Action[] createOpenActionMenu(String[] urls, boolean shortLink) {
    ContextMenuAction[] actions = new ContextMenuAction[urls.length];
    
    for(int i = 0; i < urls.length; i++) {
      String text = urls[i];
      
      if(shortLink) {
        text = text.substring(text.indexOf(".")+1);
        int index = text.indexOf("/");
        
        if(index != -1) {
          text = text.substring(0,index);
        }
      }
      
      actions[i] = new ContextMenuAction(mLocalizer.msg("open", "Open {0}", "'" + text + "'"),createImageIcon("apps","internet-web-browser",16));
      actions[i].putValue(Action.ACTION_COMMAND_KEY, urls[i]);      
      actions[i].setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Launch.openURL(e.getActionCommand());
        }
      });
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
    
    if(w instanceof JFrame)
      mDialog = new JDialog((JFrame)w, mLocalizer.msg("title","Edit programs"), true);
    else
      mDialog = new JDialog((JDialog)w, mLocalizer.msg("title","Edit programs"), true);
    
    mDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    
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
    
    String[] keys = (String[])mProgram2Url.keySet().toArray(new String[mProgram2Url.size()]);
    
    Arrays.sort(keys, new Comparator<String>() {
      public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
      }
    });
    
    Object[][] tableEntries = new Object[keys.length][3];
    String[] head = {mLocalizer.msg("column1","Program title"),
                     mLocalizer.msg("column2","Internet page"),
                     mLocalizer.msg("column3","Complete link")};
    
    for(int i = 0; i < keys.length; i++) {
      tableEntries[i][0] = keys[i];
      
      UrlListEntry entry = mProgram2Url.get(keys[i]);
      
      String[] urls = entry.getUrls();
      StringBuilder value = new StringBuilder(urls[0]);
      
      for(int j = 1; j < urls.length; j++) {
        value.append(";").append(urls[j]);
      }
      
      tableEntries[i][1] = value.toString();
      tableEntries[i][2] = !entry.isShortLinkEntry();
    }   
    
    mUrlProgramTable = new JTable(new ProgramUrlTableModel(tableEntries, head));
    mUrlProgramTable.getColumnModel().getColumn(2).setCellRenderer(new UrlTableRenderer());
    mUrlProgramTable.getColumnModel().getColumn(2).setPreferredWidth(UiUtilities.getStringWidth(mUrlProgramTable.getFont(), mLocalizer.msg("full","Show complete link")) + 30);
    mUrlProgramTable.getColumnModel().getColumn(2).setMaxWidth(mUrlProgramTable.getColumnModel().getColumn(2).getPreferredWidth());
    
    mUrlProgramTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          ((JComponent) e.getComponent()).getRootPane().dispatchEvent(e);
      }
    });
    
    mUrlProgramTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          int i = mUrlProgramTable.getSelectedRow();
          if (i != -1) {
            delete.setEnabled(true);
            
            int column = mUrlProgramTable.columnAtPoint(e.getPoint());
            
            if(column == 2) {
              Boolean oldValue = (Boolean)mUrlProgramTable.getValueAt(i, column);
              
              mUrlProgramTable.setValueAt(new Boolean(!oldValue.booleanValue()),i,column);
            }
          }
          else
            delete.setEnabled(false);
          
          
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
    
    JScrollPane pane = new JScrollPane(mUrlProgramTable);
    
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(ok, BorderLayout.EAST);
    buttonPanel.add(delete, BorderLayout.WEST);
    
    p.add(buttonPanel, BorderLayout.SOUTH);
    p.add(pane, BorderLayout.CENTER);    
    
    if(mProperties.containsKey("width") && mProperties.containsKey("height"))
      mDialog.setSize(Integer.parseInt(mProperties.getProperty("width")),
          Integer.parseInt(mProperties.getProperty("height")));
    else
      mDialog.setSize(500,300);

    if(mProperties.containsKey("x") && mProperties.containsKey("y"))
      mDialog.setLocation(Integer.parseInt(mProperties.getProperty("x")),
          Integer.parseInt(mProperties.getProperty("y")));
    else
      mDialog.setLocationRelativeTo(w);
    
    mDialog.setVisible(true);
    
    if (mUrlProgramTable.isEditing())
      mUrlProgramTable.getCellEditor().stopCellEditing();
    
    mProperties.setProperty("x", String.valueOf(mDialog.getX()));
    mProperties.setProperty("y", String.valueOf(mDialog.getY()));
    mProperties.setProperty("width", String.valueOf(mDialog.getWidth()));
    mProperties.setProperty("height", String.valueOf(mDialog.getHeight()));
    
    mProgram2Url.clear();
    
    for(int i = 0; i < mUrlProgramTable.getRowCount(); i++) {
      String key = (String)mUrlProgramTable.getValueAt(i, 0);
      String value = (String)mUrlProgramTable.getValueAt(i, 1);
      Boolean b = !(Boolean)mUrlProgramTable.getValueAt(i, 2);
      
      if(value != null && value.length() > 0)
        mProgram2Url.put(key,new UrlListEntry(key,value.split(";"),b));
    }
  }
  
  private void closeDialog() {
    mDialog.dispose();
  }
  
  class ProgramUrlTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * @param entries The entries of the table.
     * @param header The title of the columns.
     */
    public ProgramUrlTableModel(Object[][] entries, Object[] header) {
      super(entries,header);
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      if (columnIndex == 1) {
        return true;
      }

      return false;
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
          
        mProgram2Url.put((String)key, new UrlListEntry((String)key, new String[] {value}, shortLink));
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
    out.writeInt(3); // version
    
    out.writeInt(mProgram2Url.size());
    
    Collection<UrlListEntry> listEntries = mProgram2Url.values();
    
    for(UrlListEntry entry : listEntries) {
      entry.writeData(out);
    }
  }
  
  private class UrlTableRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)  {      
      
      JCheckBox box = new JCheckBox(mLocalizer.msg("full","Show complete link"),((Boolean)value).booleanValue());
      box.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));      
      
      box.setOpaque(isSelected);
      
      if(isSelected) {
        box.setBackground(table.getSelectionBackground());
        box.setForeground(table.getSelectionForeground());
      }
      
      return box;
    }
  }
}

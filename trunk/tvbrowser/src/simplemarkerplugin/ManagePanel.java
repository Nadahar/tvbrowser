/*
 * SimpleMarkerPlugin by René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import devplugin.Plugin;
import devplugin.Program;
import java.awt.Window;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3 to only mark
 * programs and add them to the Plugin tree.
 * 
 * (Formerly known as Just_Mark ;-))
 * 
 * License: GNU General Public License (GPL)
 * 
 * @author René Mach
 */
public class ManagePanel {
  private static final long serialVersionUID = 1L;

  private JList mMarkListsList;
  
  private ProgramList mProgramsList;

  private DefaultListModel mProgramListModel;

  private JScrollPane mMarkListsScrolPane, mProgramsScrollPane;

  private MarkListsVector mMarkListVector;

  private JButton mSend, mDelete, mSettings, mClose, mUndo;
  
  private JRadioButton mShowPrograms, mShowTitles;
  
  private JDialog mParent;
  
  private Program[] mDeletedPrograms;
  private MarkList mLastDeletingList;
  
  private JSplitPane mSplitPane;

  /**
   * Creates an instance of this panel.
   * 
   * @param parent The parent dialog.
   * @param markListVector The MarkListsVector.
   */
  public ManagePanel(JDialog parent, MarkListsVector markListVector) {
    mParent = parent;
    mMarkListVector = markListVector;

    mShowPrograms = new JRadioButton(SimpleMarkerPlugin.getLocalizer().msg("list.programs","List programs"),true);
    mShowTitles = new JRadioButton(SimpleMarkerPlugin.getLocalizer().msg("list.titles","List for program titles"));
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(mShowPrograms);
    bg.add(mShowTitles);
    
    mProgramListModel = new DefaultListModel();
    
    mMarkListsList = new JList(mMarkListVector/*.getMarkListNames()*/);
    mMarkListsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mMarkListsList.setCellRenderer(new MarkListCellRenderer());
    
    mProgramsList = new ProgramList(mProgramListModel,new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false, ProgramPanelSettings.X_AXIS));
    mProgramsList.addMouseListeners(null);

    mMarkListsScrolPane = new JScrollPane(mMarkListsList);
    mProgramsScrollPane = new JScrollPane(mProgramsList);
    mMarkListsScrolPane.setBorder(null);
    mProgramsScrollPane.setBorder(null);

    JPanel panel = (JPanel)mParent.getContentPane();
    panel.setBorder(Borders.createEmptyBorder("6dlu,6dlu,5dlu,6dlu"));
    
    FormLayout layout = new FormLayout("default:grow",
    "pref,pref,4dlu,fill:default:grow,4dlu,pref");
    
    panel.setLayout(layout);

    CellConstraints cc = new CellConstraints();

    panel.add(mShowPrograms, cc.xy(1,1));
    panel.add(mShowTitles, cc.xy(1,2));
    
    if(markListVector.size() > 1) {
      mSplitPane = new JSplitPane();
      mSplitPane.setLeftComponent(mMarkListsScrolPane);
      mSplitPane.setRightComponent(mProgramsScrollPane);
      
      mSplitPane.setContinuousLayout(true);
    
      panel.add(mSplitPane, cc.xy(1,4));
    
      int pos = SimpleMarkerPlugin.getInstance().getSettings()
          .getSplitPosition();
    
      if (pos >= 0) {
        mSplitPane.setDividerLocation(pos);
      }
    }
    else {
      JPanel innerPanel = new JPanel(new FormLayout("fill:default:grow","fill:default:grow"));
      mProgramsScrollPane.setBorder(new JScrollPane().getBorder());
      
      innerPanel.add(mProgramsScrollPane, cc.xy(1,1));
      
      panel.add(innerPanel, cc.xy(1,4));
    }

    panel.add(getButtonPanel(cc), cc.xy(1,6));

    mShowPrograms.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrograms(true);
        mDelete.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.deletePrograms","Delete selected progams"));
      }
    });
    
    mShowTitles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrograms(true);
        mDelete.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.deleteTitles","Delete programs with selected titles/all programs"));
      }
    });
    
    mSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        send();
      }
    });

    mDelete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        delete();
      }
    });
    
    mClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeDialog();
      }
    });
    
    mUndo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undo();
      }
    });
    
    mSettings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeDialog();
        Plugin.getPluginManager()
            .showSettings(SimpleMarkerPlugin.getInstance());
      }
    });

    mMarkListsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          selectPrograms(true);
        }
      }
    });
    
    mProgramsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent arg0) {
        mDelete.setEnabled(true);
      }
    });
    
    mMarkListsList.setSelectedIndex(0);
    selectPrograms(true);
    
    mParent.getRootPane().setDefaultButton(mClose);
    
    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        closeDialog();
      }

      public JRootPane getRootPane() {
        return mParent.getRootPane();
      }
    });
    
    mParent.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeDialog();
      }
    });
  }
  
  private void closeDialog() {
    mParent.dispose();
    
    if(mSplitPane != null) {
      SimpleMarkerPlugin.getInstance().getSettings().setSplitPosition(
          mSplitPane.getDividerLocation());
    }
    
    SimpleMarkerPlugin.getInstance().resetManagePanel();
  }
  
  private void send() {
    Program[] programs = null;
    
    MarkList list = (MarkList) mMarkListsList.getSelectedValue();
    
    if(mProgramsList.getSelectedValue() != null && mProgramsList.getSelectedValue() instanceof Program) {
      programs = mProgramsList.getSelectedPrograms();
    } else if(mProgramsList.getSelectedValue() != null) {
      Object[] values = mProgramsList.getSelectedValues();
      ArrayList<Program> progList = new ArrayList<Program>();
      
      for(Object o : values) {
        Program[] progs = list.getProgramsWithTitle((String)o);
        
        for(Program p : progs) {
          progList.add(p);
        }
      }
      
      programs = progList.toArray(new Program[progList.size()]);
    }
    else if(mProgramsList.getSelectedValue() == null) {
      ArrayList<Program> expiredProgs = new ArrayList<Program>();
      ArrayList<Program> notExpiredProgs = new ArrayList<Program>();
      
      for(int i = 0; i < mProgramListModel.size(); i++) {
        Program prog = (Program)mProgramListModel.get(i);
        
        if(prog.isExpired()) {
          expiredProgs.add(prog);
        }
        else {
          notExpiredProgs.add(prog);
        }
      }
      
      if(notExpiredProgs.isEmpty()) {
        programs = expiredProgs.toArray(new Program[expiredProgs.size()]);
      }
      else {
        programs = notExpiredProgs.toArray(new Program[notExpiredProgs.size()]);
      }
    }

    if(programs != null) {
      SendToPluginDialog send = new SendToPluginDialog(SimpleMarkerPlugin
          .getInstance(), list.getReceiveTarget(), (Window)mParent, programs);

      send.setVisible(true);
    }
    
  }

  protected void selectPrograms(boolean scroll) {
    mProgramListModel.clear();

    MarkList list = (MarkList)mMarkListsList.getSelectedValue();
    int index = -1;
    
    if (mShowPrograms.isSelected()) {
      for (int i = 0; i < list.size(); i++) {
        Program prog = list.get(i);
        
        if(prog != null) {
          mProgramListModel.addElement(prog);
          
          if(!list.get(i).isExpired() && index == -1) {
            index = i;
          }
        }
      }
    } else {
      Hashtable<String, LinkedList<Program>> table = list.getSortedPrograms();
      Enumeration<String> keys = table.keys();

      while (keys.hasMoreElements()) {
        mProgramListModel.addElement(keys.nextElement());
      }
    }
    
    final int scrollIndex = index;
    
    if(scroll) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mProgramsScrollPane.getVerticalScrollBar().setValue(0);
          mProgramsScrollPane.getHorizontalScrollBar().setValue(0);
          
          if(scrollIndex != -1) {
            Rectangle cellBounds = mProgramsList.getCellBounds(scrollIndex,scrollIndex);
            cellBounds.setLocation(cellBounds.x, cellBounds.y + mProgramsScrollPane.getHeight() - cellBounds.height);
            
            mProgramsList.scrollRectToVisible(cellBounds);
          }
        }
      });
    }
    
    mSend.setEnabled(mProgramListModel.size() > 0);
    mDelete.setEnabled(mProgramListModel.size() > 0);
    mUndo.setEnabled(mLastDeletingList != null && mDeletedPrograms != null && mDeletedPrograms.length > 0);
    
    if(mMarkListsList != null && mParent.isVisible()) {
      mMarkListsList.repaint();
    }
  }

  private void delete() {
    if (mProgramsList.getSelectedIndex() == -1) {
      mProgramsList.setSelectionInterval(0, mProgramListModel.getSize()-1);
    }

    mLastDeletingList =(MarkList) mMarkListsList.getSelectedValue();

    if (mShowPrograms.isSelected()) {
      mDeletedPrograms = (mProgramsList).getSelectedPrograms();

      for (Program p : mDeletedPrograms) {
        mLastDeletingList.remove(p);
      }

    } else {
      Object[] programs = mProgramsList.getSelectedValues();

      ArrayList<Program> deletedPrograms = new ArrayList<Program>();
      
      for (Object o : programs) {
        Program[] deletedArr = mLastDeletingList.removeProgramsWithTitle((String)o);
        
        for(Program deleted: deletedArr) {
          deletedPrograms.add(deleted);
        }
      }
      
      mDeletedPrograms = deletedPrograms.toArray(new Program[deletedPrograms.size()]);
    }

    SimpleMarkerPlugin.getInstance().revalidate(mDeletedPrograms);
    mLastDeletingList.updateNode();
    SimpleMarkerPlugin.getInstance().refreshManagePanel(false);
  }
  
  private void undo() {
    if(mLastDeletingList != null && mDeletedPrograms != null && mDeletedPrograms.length > 0) {
      for(Program p : mDeletedPrograms) {
        mLastDeletingList.addElement(p);
      }
    }
    
    SimpleMarkerPlugin.getInstance().revalidate(mDeletedPrograms);
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mLastDeletingList.updateNode();
      }
    });

    mLastDeletingList = null;
    mDeletedPrograms = null;
    
    SimpleMarkerPlugin.getInstance().refreshManagePanel(true);
  }

  private JPanel getButtonPanel(CellConstraints cc) {
    JPanel p = new JPanel(new FormLayout("pref,5dlu,pref,5dlu,pref,5dlu,pref",
        "pref"));

    mSettings = new JButton(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    mSend = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
    mDelete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mUndo = new JButton(SimpleMarkerPlugin.getInstance().createImageIcon(
        "actions", "edit-undo", 16));
    
    mSettings.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.settings","Open settings"));
    mSend.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.send","Send  programs to other plugins"));
    mDelete.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.deletePrograms","Delete all/selected progams"));
    mUndo.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.undo","Undo"));
    mUndo.setEnabled(false);
    
    p.add(mSettings, cc.xy(1, 1));
    p.add(mSend, cc.xy(3, 1));
    p.add(mDelete, cc.xy(5, 1));
    p.add(mUndo, cc.xy(7, 1));
        
    mClose = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
      
    JPanel buttons = new JPanel(new BorderLayout());
    
    buttons.add(p, BorderLayout.WEST);
    buttons.add(mClose, BorderLayout.EAST);
    
    return buttons;
  }
  
  private static class MarkListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      simplemarkerplugin.ManagePanel.MarkListCellRenderer c = (simplemarkerplugin.ManagePanel.MarkListCellRenderer) super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
      
      if(!((MarkList)value).isEmpty()) {
        c.setText(c.getText() + " [" + ((MarkList)value).size() + "]");
      }
      
      return c;
    }
  }
}

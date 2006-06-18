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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: ds10 $
 * $Revision: 2537 $
 */
package simplemarkerplugin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.Program;

import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

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

  private JButton mSend, mDelete, mSettings, mClose;
  
  private JRadioButton mShowPrograms, mShowTitles;
  
  private JDialog mParent;

  /**
   * Creates an instance of this panel.
   * 
   * @param parent The parent dialog.
   * @param markListVector The MarkListsVector.
   */
  public ManagePanel(JDialog parent, MarkListsVector markListVector) {
    mParent = parent;
    mMarkListVector = markListVector;

    mShowPrograms = new JRadioButton(SimpleMarkerPlugin.mLocalizer.msg("list.programs","List programs"),true);
    mShowTitles = new JRadioButton(SimpleMarkerPlugin.mLocalizer.msg("list.titles","List for program titles"));
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(mShowPrograms);
    bg.add(mShowTitles);
    
    mProgramListModel = new DefaultListModel();
    
    mMarkListsList = new JList(mMarkListVector.getMarkListNames());
    mMarkListsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mProgramsList = new ProgramList(mProgramListModel);
    mProgramsList.addMouseListeners(null);    

    mMarkListsScrolPane = new JScrollPane(mMarkListsList);
    mProgramsScrollPane = new JScrollPane(mProgramsList);

    JPanel panel = (JPanel)mParent.getContentPane();    
    panel.setBorder(Borders.createEmptyBorder("6dlu,6dlu,5dlu,6dlu"));
    
    panel.setLayout(new FormLayout("max(90dlu;default),4dlu,fill:default:grow",
        "pref,pref,4dlu,fill:default:grow,4dlu,pref"));    

    CellConstraints cc = new CellConstraints();
    
    panel.add(mShowPrograms, cc.xy(1,1));
    panel.add(mShowTitles, cc.xy(1,2));
    panel.add(mMarkListsScrolPane, cc.xy(1,4));
    panel.add(mProgramsScrollPane, cc.xy(3,4));
    panel.add(getButtonPanel(cc), cc.xyw(1,6,3));

    mShowPrograms.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrograms(true);        
        mDelete.setToolTipText(SimpleMarkerPlugin.mLocalizer.msg("tooltip.deletePrograms","Delete selected progams"));
      }
    });
    
    mShowTitles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrograms(true);
        mDelete.setToolTipText(SimpleMarkerPlugin.mLocalizer.msg("tooltip.deleteTitles","Delete programs with selected titles"));
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
        
        if(mShowPrograms.isSelected())
          mSend.setEnabled(true);
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
    SimpleMarkerPlugin.getInstance().resetManagePanel();
  }
  
  private void send() {
    Program[] programs = ((ProgramList) mProgramsList).getSelectedPrograms();

    if (programs == null || programs.length == 0) {
      String value = (String) mMarkListsList.getSelectedValue();
      MarkList list = mMarkListVector.getListForName(value);
      programs = list.toArray(new Program[list.size()]);
    }

    SendToPluginDialog send = new SendToPluginDialog(SimpleMarkerPlugin
        .getInstance(), mParent, programs);

    send.setVisible(true);
  }

  protected void selectPrograms(boolean scroll) {
    mProgramListModel.clear();
    
    String value = (String) mMarkListsList.getSelectedValue();

    MarkList list = mMarkListVector.getListForName(value);

    if (mShowPrograms.isSelected()) {
      for (Program p : list)
        mProgramListModel.addElement(p);
    } else {
      Hashtable table = list.getSortedPrograms();
      Enumeration keys = table.keys();

      while (keys.hasMoreElements())
        mProgramListModel.addElement(keys.nextElement());
    }

    if(scroll)
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mProgramsScrollPane.getVerticalScrollBar().setValue(0);
          mProgramsScrollPane.getHorizontalScrollBar().setValue(0);
        }
      });
    
    mDelete.setEnabled(false);
    
    if(!mShowPrograms.isSelected() || mProgramListModel.size() < 1)
      mSend.setEnabled(false);
    else
      mSend.setEnabled(true);
  }

  private void delete() {
    if (mProgramsList.getSelectedIndex() == -1)
      return;

    MarkList list = mMarkListVector.getListForName((String) mMarkListsList
        .getSelectedValue());

    if (mShowPrograms.isSelected()) {
      Program[] programs = ((ProgramList) mProgramsList).getSelectedPrograms();

      for (Program p : programs)
        list.remove(p);

      SimpleMarkerPlugin.getInstance().revalidate(programs);
    } else {
      Object[] programs = mProgramsList.getSelectedValues();

      for (Object o : programs)
        list.removeProgramsWithTitle((String)o);
    }

    list.updateNode();
    SimpleMarkerPlugin.getInstance().refreshManagePanel();
  }

  private JPanel getButtonPanel(CellConstraints cc) {
    JPanel p = new JPanel(new FormLayout("pref,5dlu,pref,5dlu,pref", "pref"));;

    mSettings = new JButton(SimpleMarkerPlugin.getInstance().createImageIcon("categories",
        "preferences-desktop", 16));    
    mSend = new JButton(SimpleMarkerPlugin.getInstance().createImageIcon(
        "actions", "edit-copy", 16));
    mDelete = new JButton(SimpleMarkerPlugin.getInstance().createImageIcon(
        "actions", "edit-delete", 16));
    
    mSettings.setToolTipText(SimpleMarkerPlugin.mLocalizer.msg("tooltip.settings","Open settings"));
    mSend.setToolTipText(SimpleMarkerPlugin.mLocalizer.msg("tooltip.send","Send  programs to other plugins"));
    mDelete.setToolTipText(SimpleMarkerPlugin.mLocalizer.msg("tooltip.deletePrograms","Delete selected progams"));
    
    p.add(mSettings, cc.xy(1, 1));
    p.add(mSend, cc.xy(3, 1));
    p.add(mDelete, cc.xy(5, 1));
        
    mClose = new JButton(SimpleMarkerPlugin.mLocalizer.msg("close","Close"));    
      
    JPanel buttons = new JPanel(new BorderLayout());
    
    buttons.add(p, BorderLayout.WEST);
    buttons.add(mClose, BorderLayout.EAST);
    
    return buttons;
  }
}

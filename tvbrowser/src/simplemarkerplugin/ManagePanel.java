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
 *   $Author: darras $
 * $Revision: 2466 $
 */
package simplemarkerplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

import util.ui.ProgramList;
import util.ui.SendToPluginDialog;

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
public class ManagePanel extends JPanel {
  private static final long serialVersionUID = 1L;

  /** Type to use for managing programs in the list */
  public final static int MANAGE_PROGRAMS = 0;

  /** Type to use for managing program titles in the list */
  public final static int MANAGE_PROGRAM_TITLES = 1;

  private int mType;

  private JList mMarkListsList, mProgramsList;

  private DefaultListModel mProgramListModel;

  private JScrollPane mMarkListsScrolPane, mProgramsScrollPane;

  private MarkListsVector mMarkListVector;

  private JButton mSend, mDelete;

  private JDialog mParent;

  /**
   * Creates an instance of this panel.
   * 
   * @param parent The parent dialog.
   * @param markListVector The MarkListsVector.
   * @param type The type of this panel.
   */
  public ManagePanel(JDialog parent, MarkListsVector markListVector, int type) {
    mParent = parent;
    mMarkListVector = markListVector;
    mType = type;

    mMarkListsList = new JList(mMarkListVector.getMarkListNames());
    mMarkListsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mProgramsList = createProgramList();

    mMarkListsScrolPane = new JScrollPane(mMarkListsList);
    mProgramsScrollPane = new JScrollPane(mProgramsList);

    setLayout(new FormLayout("max(90dlu;default),4dlu,fill:default:grow",
        "fill:default:grow,4dlu,pref"));
    setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    add(mMarkListsScrolPane, cc.xywh(1, 1, 1, 3));
    add(mProgramsScrollPane, cc.xy(3, 1));
    add(getButtonPanel(cc), cc.xy(3, 3));

    if (mSend != null)
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
        
        if(mSend != null)
          mSend.setEnabled(true);
      }
    });
    
    mMarkListsList.setSelectedIndex(0);
    selectPrograms(true);
  }

  private void send() {
    if (mProgramsList.getSelectedIndex() == -1)
      return;

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

    if (mType == MANAGE_PROGRAMS) {
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
    
    if(mSend != null)
      mSend.setEnabled(false);
  }

  private void delete() {
    if (mProgramsList.getSelectedIndex() == -1)
      return;

    MarkList list = mMarkListVector.getListForName((String) mMarkListsList
        .getSelectedValue());

    if (mType == MANAGE_PROGRAMS) {
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
    SimpleMarkerPlugin.getInstance().refreshPanels();
  }

  private JList createProgramList() {
    mProgramListModel = new DefaultListModel();

    if (mType == MANAGE_PROGRAMS) {
      ProgramList list = new ProgramList(mProgramListModel);
      list.addMouseListeners(null);
      return list;
    } else
      return new JList(mProgramListModel);
  }

  private JPanel getButtonPanel(CellConstraints cc) {
    JPanel p = new JPanel(new FormLayout("pref:grow,pref,5dlu,pref", "pref"));;

    if (mType == MANAGE_PROGRAMS) {
      mSend = new JButton(SimpleMarkerPlugin.getInstance().createImageIcon(
          "actions", "edit-copy", 16));
      mDelete = new JButton(SimpleMarkerPlugin.getInstance().createImageIcon(
          "actions", "edit-delete", 16));
      
      p.add(mSend, cc.xy(2, 1));
      p.add(mDelete, cc.xy(4, 1));
    } else {
      mDelete = new JButton(SimpleMarkerPlugin.mLocalizer.msg("unmark",
          "Just unmark"));
      mDelete.setIcon(SimpleMarkerPlugin.getInstance().createImageIcon(
          "actions", "edit-delete", 16));

      p.add(mDelete, cc.xyw(1, 1, 4));
    }
    
    return p;
  }

}

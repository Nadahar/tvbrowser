/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package util.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.Settings;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Plugin;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

/**
 * The PluginChooserDlg class provides a Dialog for choosing plugins. The user
 * can choose from all Plugins that are able to receive Programs.
 */
public class PluginChooserDlg extends JDialog implements WindowClosingIf {

  private static final long serialVersionUID = 1L;
  private ProgramReceiveIf[] mResultPluginArr;
  private ProgramReceiveIf[] mPluginArr;
  private Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> mReceiveTargetTable;
  private SelectableItemList mPluginItemList;
  private JPanel mTargetPanel;
  private ProgramReceiveTarget[] mCurrentTargets;
  private boolean mOkWasPressed;
  
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(PluginChooserDlg.class);

  /**
   *
   * @param parent
   * @param pluginArr The initially selected ProgramReceiveIfs.
   * @param description A description text below the ProgramReceiveIf list.
   * @param caller The caller ProgramReceiveIf.
   */
  public PluginChooserDlg(Dialog parent, ProgramReceiveIf[] pluginArr, String description, ProgramReceiveIf caller) {
    super(parent,true);
    init(pluginArr, description, caller,null,null);
  }
  
  /**
  *
  * @param parent
  * @param pluginArr The initially selected ProgramReceiveIfs.
  * @param description A description text below the ProgramReceiveIf list.
  * @param caller The caller ProgramReceiveIf.
  */
 public PluginChooserDlg(Dialog parent, ProgramReceiveTarget[] pluginArr, String description, ProgramReceiveIf caller) {
   super(parent,true);
   
   Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> table = createReceiveTable(pluginArr);
   
   init(table.keySet().toArray(new ProgramReceiveIf[table.keySet().size()]), description, caller, table, null);
 }
 
   /**
   *
   * @param parent
   * @param pluginArr The initially selected ProgramReceiveIfs.
   * @param description A description text below the ProgramReceiveIf list.
   * @param caller The caller ProgramReceiveIf.
   * @param disabledTargets Targets that cannot be selected/deselected
   * @since 2.7.2
   */
  public PluginChooserDlg(Dialog parent, ProgramReceiveTarget[] pluginArr, String description, ProgramReceiveIf caller, ProgramReceiveTarget[] disabledTargets) {
    super(parent,true);
    
    Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> table = createReceiveTable(pluginArr);
    
    init(table.keySet().toArray(new ProgramReceiveIf[table.keySet().size()]), description, caller, table, disabledTargets);
  }

  /**
   *
   * @param parent
   * @param pluginArr The initially selected ProgramReceiveIfs.
   * @param description A description text below the ProgramReceiveIf list.
   * @param caller The caller ProgramReceiveIf.
   */
  public PluginChooserDlg(Frame parent, ProgramReceiveIf[] pluginArr, String description, ProgramReceiveIf caller) {
    super(parent,true);
    init(pluginArr, description, caller,null,null);
  }
  
  public PluginChooserDlg(Frame parent, ProgramReceiveTarget[] targets, String description, ProgramReceiveIf caller) {
    super(parent,true);

    Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> table = createReceiveTable(targets);
    
    init(table.keySet().toArray(new ProgramReceiveIf[table.keySet().size()]), description, caller, table, null);
  }
  
  /**
  *
  * @param parent
  * @param targets The initially selected ProgramReceiveTargets.
  * @param description A description text below the ProgramReceiveIf list.
  * @param caller The caller ProgramReceiveIf.
  * @param disabledTargets Targets that cannot be selected/deselected
  * @since 2.7.2
  */
  public PluginChooserDlg(Frame parent, ProgramReceiveTarget[] targets, String description, ProgramReceiveIf caller, ProgramReceiveTarget[] disabledTargets) {
    super(parent,true);

    Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> table = createReceiveTable(targets);
    
    init(table.keySet().toArray(new ProgramReceiveIf[table.keySet().size()]), description, caller, table, disabledTargets);
  }
  
  private Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> createReceiveTable(ProgramReceiveTarget[] targets) {
    Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> table = new Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>>();
    
    if(targets != null) {
      for(ProgramReceiveTarget target : targets) {
        if(target != null && target.getReceifeIfForIdOfTarget() != null) {
          ArrayList<ProgramReceiveTarget> receiveTargetList = table.get(target.getReceifeIfForIdOfTarget());
          
          if(receiveTargetList != null) {
            receiveTargetList.add(target);
          }
          else {
            receiveTargetList = new ArrayList<ProgramReceiveTarget>();
            receiveTargetList.add(target);
            
            table.put(target.getReceifeIfForIdOfTarget(),receiveTargetList);
          }
        }
      }
    }
    
    return table;
  }
  
  private void init(ProgramReceiveIf[] pluginArr, String description, ProgramReceiveIf caller, Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> targetTable, final ProgramReceiveTarget[] disabledReceiveTargets) {
    mOkWasPressed = false;
    setTitle(mLocalizer.msg("title","Choose Plugins"));
    UiUtilities.registerForClosing(this);
    
    if (pluginArr == null) {
      mPluginArr = new ProgramReceiveIf[]{};
      mResultPluginArr = new ProgramReceiveIf[]{};
      mReceiveTargetTable = new Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>>();
    }
    else {
      mPluginArr = pluginArr;
      mResultPluginArr = pluginArr;
      mReceiveTargetTable = targetTable;
    }
    

    JPanel contentPane = (JPanel)getContentPane();
    FormLayout layout = new FormLayout("fill:pref:grow", "");
    contentPane.setLayout(layout);
    contentPane.setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();
    
    ProgramReceiveIf[] tempProgramReceiveIf = Plugin.getPluginManager().getReceiveIfs(caller,null);
        
    ArrayList<ProgramReceiveIf> disabledList = new ArrayList<ProgramReceiveIf>(disabledReceiveTargets != null ? disabledReceiveTargets.length : 0);

    if(disabledReceiveTargets != null) {
      for(ProgramReceiveTarget target : disabledReceiveTargets) {
        disabledList.add(target.getReceifeIfForIdOfTarget());
      }
    }
    
    if(caller != null) {
      ArrayList<ProgramReceiveIf> list = new ArrayList<ProgramReceiveIf>();
    
      for(ProgramReceiveIf tempIf : tempProgramReceiveIf) {
        if(tempIf.getId().compareTo(caller.getId()) != 0) {
          list.add(tempIf);
        }
      }
            
      mPluginItemList = new SelectableItemList(mResultPluginArr, list.toArray(), disabledList.toArray());
    } else {
      mPluginItemList = new SelectableItemList(mResultPluginArr, tempProgramReceiveIf, disabledList.toArray());
    }

    mPluginItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    int pos = 1;
    layout.appendRow(RowSpec.decode("fill:default:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));
    
    if(targetTable != null) {
    JSplitPane splitPane = new JSplitPane();
        
    splitPane.setLeftComponent(mPluginItemList);
    
    mTargetPanel = new JPanel();
    mTargetPanel.setLayout(new BoxLayout(mTargetPanel, BoxLayout.Y_AXIS));
    
    JScrollPane targetScrollPane = new JScrollPane(mTargetPanel);
    targetScrollPane.getVerticalScrollBar().setUnitIncrement(10);
    
    mPluginItemList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {try {
        if(!e.getValueIsAdjusting()) {
          mTargetPanel.removeAll();
          Object o = mPluginItemList.getSelectedValue();
        
          if(o != null && ((SelectableItem)o).isSelected()) {
            mCurrentTargets = ((ProgramReceiveIf)((SelectableItem)o).getItem()).getProgramReceiveTargets();
          
            if(mCurrentTargets != null) {
              JCheckBox[] targetBoxes = new JCheckBox[mCurrentTargets.length];
              ArrayList<ProgramReceiveTarget> targets = mReceiveTargetTable.get((((SelectableItem)o).getItem()));
              
              boolean hasSelection = false;
              
              for(int i = 0; i < targetBoxes.length; i++) {
                targetBoxes[i] = new JCheckBox(mCurrentTargets[i].toString());
                targetBoxes[i].setEnabled(!arrayContainsValue(disabledReceiveTargets,mCurrentTargets[i]));
                
                if(targets != null && targets.contains(mCurrentTargets[i])) {
                  targetBoxes[i].setSelected(true);
                  hasSelection = true;
                }
                  
                final int j = i;
                targetBoxes[i].addItemListener(new ItemListener() {
                  public void itemStateChanged(ItemEvent e) {
                    ArrayList<ProgramReceiveTarget> targetList = mReceiveTargetTable.get((((SelectableItem)mPluginItemList.getSelectedValue()).getItem()));
                                        
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                      if(targetList == null) {
                        targetList = new ArrayList<ProgramReceiveTarget>();
                        mReceiveTargetTable.put(((ProgramReceiveIf)((SelectableItem)mPluginItemList.getSelectedValue()).getItem()),targetList);
                      }

                      targetList.add(mCurrentTargets[j]);
                    }
                    else if(e.getStateChange() == ItemEvent.DESELECTED && targetList != null) {
                      targetList.remove(mCurrentTargets[j]);
                      
                      if(targetList.isEmpty()) {
                        mReceiveTargetTable.remove((((SelectableItem)mPluginItemList.getSelectedValue()).getItem()));
                      }
                    }
                  }
                });
                               
                mTargetPanel.add(targetBoxes[i]);
              }
              
              if(!hasSelection && targetBoxes.length > 0) {
                targetBoxes[0].setSelected(true);
                
                ArrayList<ProgramReceiveTarget> targetList = mReceiveTargetTable.get((((SelectableItem)mPluginItemList.getSelectedValue()).getItem()));
                
                if(targetList == null) {
                  targetList = new ArrayList<ProgramReceiveTarget>();
                  mReceiveTargetTable.put(((ProgramReceiveIf)((SelectableItem)mPluginItemList.getSelectedValue()).getItem()),targetList);
                }
                else {
                  targetList.clear();
                }
                
                targetList.add(mCurrentTargets[0]);
              }
            }
          }
          
          mTargetPanel.updateUI();

          if(!((SelectableItem)mPluginItemList.getSelectedValue()).isSelected()) {
            mReceiveTargetTable.remove(((SelectableItem)mPluginItemList.getSelectedValue()).getItem());
          }
        }
      }catch(Exception e1) {e1.printStackTrace();}
      }
    });
    
    splitPane.setRightComponent(targetScrollPane);
    contentPane.add(splitPane, cc.xy(1,pos));
    
    } else {
      contentPane.add(mPluginItemList, cc.xy(1,pos));
    }

    pos += 2;
    
    if (description != null) {
      JLabel lb = new JLabel(description);
      layout.appendRow(RowSpec.decode("pref"));
      layout.appendRow(RowSpec.decode("3dlu"));
      contentPane.add(lb, cc.xy(1,pos));
      pos += 2;
    }

    JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mOkWasPressed = true;
        Object[] o = mPluginItemList.getSelection();
        mResultPluginArr = new ProgramReceiveIf[o.length];
        for (int i=0;i<o.length;i++) {
          mResultPluginArr[i]=(ProgramReceiveIf)o[i];
        }
        close();
      }
      });

    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mOkWasPressed = false;
        mResultPluginArr = null;
        close();
      }
    });

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[] {okBt, cancelBt});
    
    layout.appendRow(RowSpec.decode("pref"));
    contentPane.add(builder.getPanel(), cc.xy(1,pos));
    
    Settings.layoutWindow("pluginChooserDlg", this, new Dimension(350,300));
    
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
  }
  
  private boolean arrayContainsValue(ProgramReceiveTarget[] targetArr, ProgramReceiveTarget target) {
    if(targetArr != null && target != null) {
      for(ProgramReceiveTarget arrayEntry : targetArr) {        
        if(arrayEntry.getReceiveIfId().equals(target.getReceiveIfId()) && arrayEntry.getTargetId().equals(target.getTargetId())) {
          return true;
        }
      }
    }
    
    return false;
  }

  /**
   *
   * @return an array of the selected plugins. If the user cancelled the dialog,
   * the array from the constructor call is returned.
   */
  public ProgramReceiveIf[] getPlugins() {

    if (mResultPluginArr==null) {
      return mPluginArr;
    }
    return mResultPluginArr;
  }
  
  public ProgramReceiveTarget[] getReceiveTargets() {
    if(mOkWasPressed) {
      Iterator<ArrayList<ProgramReceiveTarget>> targetIterator = mReceiveTargetTable.values().iterator();
      ArrayList<ProgramReceiveTarget> targetList = new ArrayList<ProgramReceiveTarget>();
      
      while(targetIterator.hasNext()) {
        targetList.addAll(targetIterator.next());
      }
      
      return targetList.toArray(new ProgramReceiveTarget[targetList.size()]);
    } else {
      return null;
    }
  }

  public void close() {
    setVisible(false);
    dispose();
  }

}
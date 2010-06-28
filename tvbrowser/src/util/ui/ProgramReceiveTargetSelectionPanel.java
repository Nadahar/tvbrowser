/*
 * TV-Browser
 * Copyright (C) 05-2010 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

/**
 * A default panel component to select the receive
 * targets for sending programs to plugins.
 * <p>
 * @author René Mach
 * @since 3.0
 */
public class ProgramReceiveTargetSelectionPanel extends JPanel {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ProgramReceiveTargetSelectionPanel.class);
  private JLabel mReceiveTargetLabel;
  private ProgramReceiveTarget[] mReceiveTargets;
  private ChangeListener[] mChangeListenerArray = new ChangeListener[0];
  
  /**
   * Creates an program receive target selection panel.
   * <p>
   * @param parent The parent window for the selection dialog.
   * @param receiveTargetArr The array with the currently selected targets.
   * @param description The description for the selection dialog.
   * @param caller The ProgramReceiveIf that should be excluded from the selection.
   *               (The Plugin that calls this should always be excluded!!!)
   * @param withTitle If the panel should contain a title.
   * @param title The title for the panel, or <code>null</code> if the default title should be shown.
   */
  public ProgramReceiveTargetSelectionPanel(final Window parent, ProgramReceiveTarget[] receiveTargetArr,
      final String description, final ProgramReceiveIf caller, boolean withTitle, String title) {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,5dlu:grow,default,5dlu",withTitle? "pref,5dlu,pref" : "pref"),this);
    
    mReceiveTargets = receiveTargetArr;
    
    mReceiveTargetLabel = new JLabel();
    JButton selectionButton = new JButton(LOCALIZER.msg("selectionButton","Select targets"));
    
    final ProgramReceiveTargetSelectionPanel thisPanel = this;
    
    selectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {try{
        Window w = UiUtilities.getLastModalChildOf(parent);
        PluginChooserDlg chooser = null;
        
        chooser = new PluginChooserDlg(w, mReceiveTargets, description, caller);
        chooser.setLocationRelativeTo(w);
        chooser.setVisible(true);
        
        if(chooser.getReceiveTargets() != null) {
          mReceiveTargets = chooser.getReceiveTargets();
          
          for(ChangeListener listener : mChangeListenerArray) {
            listener.stateChanged(new ChangeEvent(thisPanel));
          }
        }
        
        handlePluginSelection();
        ;}catch(Exception ee) {ee.printStackTrace();}
      }
    });
    
    CellConstraints cc = new CellConstraints();
    
    if(withTitle) {
      pb.addSeparator(title == null ? LOCALIZER.msg("defaultTitle","Send programs to:") : title, cc.xyw(1,1,5));
      pb.add(mReceiveTargetLabel, cc.xy(2,3));
      pb.add(selectionButton, cc.xy(4,3));
    }
    else {
      pb.add(mReceiveTargetLabel, cc.xy(2,1));
      pb.add(selectionButton, cc.xy(4,1));
    }
    
    handlePluginSelection();
    
  }
  
  private void handlePluginSelection() {
    final ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();

    if (mReceiveTargets != null) {
      for (ProgramReceiveTarget target : mReceiveTargets) {
        if (!plugins.contains(target.getReceifeIfForIdOfTarget())) {
          plugins.add(target.getReceifeIfForIdOfTarget());
        }
      }

      final ProgramReceiveIf[] mClientPlugins = plugins
          .toArray(new ProgramReceiveIf[plugins.size()]);

      if (mClientPlugins.length > 0) {
        mReceiveTargetLabel.setText(mClientPlugins[0].toString());
        mReceiveTargetLabel.setEnabled(true);
      } else {
        mReceiveTargetLabel.setText(LOCALIZER.msg("noTargets", "No targets choosen"));
        mReceiveTargetLabel.setEnabled(false);
      }

      for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
        mReceiveTargetLabel.setText(mReceiveTargetLabel.getText() + ", " + mClientPlugins[i]);
      }

      if (mClientPlugins.length > 4) {
        mReceiveTargetLabel.setText(mReceiveTargetLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + LOCALIZER.msg("otherTargets", "others...") + ")");
      }
    }
  }
  
  /**
   * Gets the currently selected receive targets.
   * <p>
   * @return The array with the currently selected receive targets.
   */
  public ProgramReceiveTarget[] getCurrentSelection() {
    return mReceiveTargets;
  }
  
  /**
   * Adds a change listener to this panel.
   * The listener will react, when the selection is changed.
   * @param listener The listener to add.
   */
  public void addChangeListener(ChangeListener listener) {
    ChangeListener[] newArray = new ChangeListener[mChangeListenerArray.length+1];
    System.arraycopy(mChangeListenerArray,0,newArray,0,mChangeListenerArray.length);
    newArray[mChangeListenerArray.length] = listener;
    
    mChangeListenerArray = newArray;
  }
}
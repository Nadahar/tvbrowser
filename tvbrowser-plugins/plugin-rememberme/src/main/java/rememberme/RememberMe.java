/*
 * RememberMe Plugin
 * Copyright (C) 2013 René Mach (rene@tvbrowser.org)
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
 */
package rememberme;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import compat.PersonaCompat;
import compat.PluginCompat;
import compat.UiCompat;
import compat.VersionCompat;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public class RememberMe extends Plugin {
  public static final int DEFAULT_DAY_COUNT = 14; 
  private static final Version mVersion = new Version(0,19,0,true);
  static final Localizer mLocalizer = Localizer.getLocalizerFor(RememberMe.class);
  private static final String TARGET_ID = "###REMEMBERME###";
  
  private RememberedProgramsList<RememberedProgram> mRememberedPrograms;
    
  private PluginCenterPanelWrapper mWrapper;
  
  private JPanel mCenterPanelWrapper;
  
  private RememberMeManagePanel mMangePanel;
  
  private ProgramReceiveTarget[] mReceiveTargets;
  
  private Timer mTimer;
  
  private boolean mAddingPanel;
  
  private int mDayCount;
  
  public RememberMe() {
    mRememberedPrograms = new RememberedProgramsList<RememberedProgram>();
    mReceiveTargets = new ProgramReceiveTarget[] {new ProgramReceiveTarget(this, mLocalizer.msg("name", "Forget me not!"), TARGET_ID)};
    mAddingPanel = false;
    mDayCount = DEFAULT_DAY_COUNT;
  }
  
  public static Version getVersion() {
    return mVersion;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(RememberMe.class, mLocalizer.msg("name", "Forget me not!"), mLocalizer.msg("description", "Remembers expired programs for two weeks"), "Ren\u00e9 Mach", "GPL");
  }
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    int version = 3;
    
    out.writeInt(version); // write version
    
    out.writeInt(mDayCount);
    
    ArrayList<RememberedProgram> saveProgs = new ArrayList<RememberedProgram>();
    
    synchronized (mRememberedPrograms) {
      for(RememberedProgram prog : mRememberedPrograms) {
        if(prog.isValid(mDayCount)) {
          saveProgs.add(prog);
        }
      }
    }
    
    out.writeInt(saveProgs.size());
    
    for(RememberedProgram prog : saveProgs) {
      prog.save(out, version);
    }
    
    out.writeInt(mReceiveTargets.length);
    
    for(ProgramReceiveTarget target : mReceiveTargets) {
      target.writeData(out);
    }
  }
  
  public ActionMenu getButtonAction() {
    ActionMenu result = null;
    
    if(!VersionCompat.isCenterPanelSupported()) {
      
      ContextMenuAction action = new ContextMenuAction(getInfo().getName(),getPluginManager().getIconFromTheme(RememberMe.this, getMarkIconFromTheme())) {
        @Override
        public void actionPerformed(ActionEvent event) {
          final JDialog d = new JDialog(UiUtilities.getLastModalChildOf(getParentFrame()), ModalityType.APPLICATION_MODAL);
          
          d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
              d.dispose();
            }
          });
          d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
          
          UiUtilities.registerForClosing(new WindowClosingIf() {
            @Override
            public JRootPane getRootPane() {
              return d.getRootPane();
            }
            
            @Override
            public void close() {
              d.dispose();
            }
          });
          
          final JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
          close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              d.dispose();
            }
          });
          d.setContentPane(new RememberMeManagePanel(mRememberedPrograms, RememberMe.this, close));
          layoutWindow("rememberMeDialog", d, new Dimension(600, 400));
          d.setVisible(true);
        };
      };
      action.putValue(Plugin.BIG_ICON, createImageIcon("status", "dialog-information", TVBrowserIcons.SIZE_LARGE));
      
      result = new ActionMenu(action);
    }
    
    return result;
  }
  
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    ProgramReceiveTarget[] returnCopy = new ProgramReceiveTarget[mReceiveTargets.length];
    
    System.arraycopy(mReceiveTargets, 0, returnCopy, 0, mReceiveTargets.length);
    
    return returnCopy;
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int version = in.readInt();
    
    if(version > 2) {
      mDayCount = in.readInt();
    }
    
    int n = in.readInt();
    
    for(int i = 0; i < n; i++) {
      synchronized (mRememberedPrograms) {
        mRememberedPrograms.add(RememberedProgram.readData(in, version, this));
      }
    }
    
    synchronized (mRememberedPrograms) {
      Collections.sort(mRememberedPrograms);
    }
    
    getRootNode().update();
    
    if(version > 1) {
      mReceiveTargets = new ProgramReceiveTarget[in.readInt()];
      
      for(int i = 0; i < mReceiveTargets.length; i++) {
        mReceiveTargets[i] = new ProgramReceiveTarget(in);
      }
    }
  }
  
  @Override
  public boolean canUseProgramTree() {
    return true;
  }
  
  @Override
  public int getMarkPriorityForProgram(Program p) {
    return Program.NO_MARK_PRIORITY;
  }
  
  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    ContextMenuAction action = new ContextMenuAction(getInfo().getName(),createImageIcon(getMarkIconFromTheme())) {
      public void actionPerformed(ActionEvent e) {
        receivePrograms(new Program[] {program}, mReceiveTargets[0]);
      }
    };
    
    synchronized (mRememberedPrograms) {
      if(mRememberedPrograms.contains(program)) {
        action = new ContextMenuAction(mLocalizer.msg("forgetMe", "Forget me now!"),createImageIcon(getMarkIconFromTheme())) {
          public void actionPerformed(ActionEvent e) {
            synchronized (mRememberedPrograms) {
              mRememberedPrograms.remove(program,RememberMe.this);
            }
            
            program.unmark(RememberMe.this);
            getRootNode().removeProgram(program);
            getRootNode().update();
            
            if(mMangePanel != null) {
              mMangePanel.updatePanel(RememberMe.this);
            }
            
            saveMe();
          }
        };
      }
    }
    
    return new ActionMenu(action);
  }
  
  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
    
  @Override
  public boolean receivePrograms(Program[] programArr,
      ProgramReceiveTarget receiveTarget) {
    if(mMangePanel != null) {
      if(programArr != null) {
        for(ProgramReceiveTarget target : mReceiveTargets) {
          if(receiveTarget.isReceiveTargetWithIdOfProgramReceiveIf(this, target.getTargetId())) {
            for(Program prog : programArr) {
              RememberedProgram newProg = new RememberedProgram(prog, receiveTarget.isReceiveTargetWithIdOfProgramReceiveIf(this, TARGET_ID) ? "" : receiveTarget.getTargetName());
              
              synchronized (mRememberedPrograms) {
                if(!mRememberedPrograms.contains(newProg)) {
                  mRememberedPrograms.add(newProg);
                  
                  prog.mark(RememberMe.this);
                  getRootNode().addProgram(prog);
                }
              }
            }
          }
        }
      }
      
      getRootNode().update();
      
      synchronized (mRememberedPrograms) {
        Collections.sort(mRememberedPrograms);
      }
      
      mMangePanel.updatePanel(RememberMe.this);
    }
    
    saveMe();
    
    return true;
  }
  
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("status", "dialog-information", TVBrowserIcons.SIZE_SMALL);
  }
  
  public void onActivation() {
    mTimer = new Timer(60000,new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(mMangePanel != null) {
          mMangePanel.updatePanel(RememberMe.this);
        }
      }
    });
    mTimer.start();
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        mCenterPanelWrapper = UiCompat.createPersonaBackgroundPanel();
        
        mCenterPanelWrapper.addAncestorListener(new AncestorListener() {
          
          @Override
          public void ancestorRemoved(AncestorEvent e) {
            if(mTimer != null) {
              mTimer.stop();
            }
          }
          
          @Override
          public void ancestorMoved(AncestorEvent e) {}
          
          @Override
          public void ancestorAdded(AncestorEvent e) {
            if(mMangePanel != null) {
              mMangePanel.updatePanel(RememberMe.this);
              mTimer.start();
            }
          }
        });
        
        mWrapper = new PluginCenterPanelWrapper() {
          @Override
          public PluginCenterPanel[] getCenterPanels() {
            return new PluginCenterPanel[] {new RemeberMePanel()};
          }
        };
        
        addCenterPanel();
      }
    });
  }
  
  public void onDeactivation() {
    mTimer.stop();
    mTimer = null;
    
    if(mMangePanel != null) {
      mCenterPanelWrapper.remove(mMangePanel);
      PersonaCompat.getInstance().registerPersonaListener(mMangePanel);
    }
    
    mMangePanel = null;
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_OTHER;
  }
  
  private void addCenterPanel() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(!mAddingPanel) {
          mAddingPanel = true;
          mMangePanel = new RememberMeManagePanel(mRememberedPrograms, RememberMe.this, null);
          PersonaCompat.getInstance().registerPersonaListener(mMangePanel);
          mCenterPanelWrapper.add(mMangePanel,BorderLayout.CENTER);
          mCenterPanelWrapper.repaint();//.updateUI();
          mAddingPanel = false;
        }
      }
    });    
  }
  
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return mWrapper;
  }
  
  private class RemeberMePanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return getInfo().getName();
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanelWrapper;
    }
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new RemeberMeSettingsTab(this);
  }
  
  void setReceiveTargets(ProgramReceiveTarget[] targets) {
    mReceiveTargets = targets;
  }
  
  void setDayCount(int days) {
    mDayCount = days;
    
    if(mMangePanel != null) {
      mMangePanel.updateFilters(mDayCount);
      mMangePanel.updatePanel(this);
    }
    
    saveMe();
  }
  
  int getDayCount() {
    return mDayCount;
  }
}

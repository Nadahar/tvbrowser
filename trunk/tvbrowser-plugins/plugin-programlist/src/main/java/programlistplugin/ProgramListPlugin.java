/*
 * TV-Browser
 * Copyright (C) 04-2003 TV-Browser team (dev@tvbrowser.org)
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
package programlistplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.persona.Persona;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

/**
 * Shows all important programs in a time sorted list.
 *
 * @author René Mach
 * @since 2.7
 */
public class ProgramListPlugin extends Plugin {
  static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListPlugin.class);

  private static Version mVersion = new Version(3, 20, false);
  
  private static final int MAX_DIALOG_LIST_SIZE = 5000;
  static final int MAX_PANEL_LIST_SIZE = 2500;

  private JDialog mDialog;
  private ProgramListSettings mSettings;
  
  private ProgramFilter mReceiveFilter;

  private static ProgramListPlugin mInstance;
  
  private ProgramListPanel mDialogPanel;
  private ProgramListPanel mCenterPanelEntry;
  private ProgramListCenterPanel mCenterPanel;
  
  private PluginCenterPanelWrapper mWrapper;
  private JPanel mCenterPanelWrapper;

  /**
   * Creates an instance of this class.
   */
  public ProgramListPlugin() {
    mInstance = this;
  }
  
  public void onActivation() {
    mCenterPanelWrapper = new JPanel(new BorderLayout()) {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getAccentColor() != null && Persona.getInstance().getHeaderImage() != null) {
         
          Color c = Persona.testPersonaForegroundAgainst(Persona.getInstance().getAccentColor());
          
          int alpha = c.getAlpha();
          
          g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
          g.fillRect(0,0,getWidth(),getHeight());
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    mCenterPanelWrapper.setOpaque(false);
    
    mCenterPanel = new ProgramListCenterPanel();
    
    mWrapper = new PluginCenterPanelWrapper() {
      @Override
      public PluginCenterPanel[] getCenterPanels() {
        return new PluginCenterPanel[] {mCenterPanel};
      }
      
      public void filterSelected(ProgramFilter filter) {
        if(mCenterPanelEntry != null) {
          mCenterPanelEntry.updateFilter(filter);
        }
      }
      
      public void timeEvent() {
        if(mCenterPanelEntry != null) {System.out.println("xxx");
          mCenterPanelEntry.timeEvent();
        }
      }
    };
  }
  
  public void handleTvDataUpdateFinished() {
    if(mCenterPanelEntry != null) {
      mCenterPanelEntry.fillProgramList();
    }
  }
  
  public void handleTvBrowserStartFinished() {
    mCenterPanelEntry = new ProgramListPanel(null, false, MAX_PANEL_LIST_SIZE);
    Persona.getInstance().registerPersonaListener(mCenterPanelEntry);
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        mCenterPanelWrapper.add(mCenterPanelEntry, BorderLayout.CENTER);
        mCenterPanelEntry.selectFirstEntry();
        
        if(Persona.getInstance().getHeaderImage() != null) {
          mCenterPanelEntry.updatePersona();
        }
      }
    });
  }

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(ProgramListPlugin.class, mLocalizer.msg("name", "Program list"), mLocalizer.msg(
        "description", "Shows programs in a list."), "René Mach");
  }

  /**
   * Gets the instance of this plugin.
   *
   * @return The instance of this plugin.
   */
  public static ProgramListPlugin getInstance() {
    if (mInstance == null) {
      new ProgramListPlugin();
    }

    return mInstance;
  }

  public void loadSettings(final Properties properties) {
    mSettings = new ProgramListSettings(properties);
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        mReceiveFilter = null;
        showProgramList();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("name", "Important programs"));
    action.putValue(Action.SMALL_ICON, createImageIcon("emblems", "emblem-important", 16));
    action.putValue(Plugin.BIG_ICON, createImageIcon("emblems", "emblem-important", 22));

    return new ActionMenu(action);
  }



  public void onDeactivation() {
    if (mDialog != null) {
      mDialog.setVisible(false);
    }
  }


  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this, mLocalizer.msg("programTarget", "Program list"),
        "program list") };
  }

  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    final List<Program> programs = Arrays.asList(programArr);
    mReceiveFilter = new ProgramFilter() {
      @Override
      public boolean accept(Program prog) {
        return programs.contains(prog);
      }

      @Override
      public String getName() {
        return mLocalizer.msg("filterName", "Received programs");
      }

      @Override
      public String toString() {
        return mLocalizer.msg("filterName", "Received programs");
      }
    };
    showProgramList();
    return true;
  }

  private void showProgramList() {
    showProgramList(null);
  }

  private void showProgramList(final Channel selectedChannel) {
    try {
      if (mDialog == null) {
        mDialog = new JDialog(getParentFrame());
        mDialog.setTitle(mLocalizer.msg("name", "Important programs"));
        mDialog.getContentPane().setLayout(new BorderLayout(0,0));
        
        UiUtilities.registerForClosing(new WindowClosingIf() {

          public void close() {
            closeDialog();
          }

          public JRootPane getRootPane() {
            return mDialog.getRootPane();
          }
        });

        mDialogPanel = new ProgramListPanel(selectedChannel,true,MAX_DIALOG_LIST_SIZE);
        
        mDialog.getContentPane().add(mDialogPanel, BorderLayout.CENTER);

        layoutWindow("programListWindow", mDialog, new Dimension(500, 500));

        mDialog.setVisible(true);

        mDialogPanel.selectFirstEntry();
      } else {
        if (!mDialog.isVisible()) {
          mDialogPanel.fillFilterBox();
        }

        mDialog.setVisible(!mDialog.isVisible());

        if (mDialog.isVisible()) {
          mDialogPanel.fillProgramList();
        } else {
          saveMe();
        }
      }
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  void closeDialog() {
    mDialog.setVisible(false);
    saveMe();
  }

  @Override
  public ActionMenu getContextMenuActions(final Channel channel) {
    return new ActionMenu(new AbstractAction(mLocalizer.msg("showChannel", "Show programs of channel in program list")) {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        showProgramList(channel);
      }
    });
  }
  
  public String getPluginCategory() {
    return Plugin.OTHER_CATEGORY;
  }
  
  ProgramFilter getReceiveFilter() {
    return mReceiveFilter;
  }

  ProgramListSettings getSettings() {
    return mSettings;
  }
  
  Frame getSuperFrame() {
    return getParentFrame();
  }
  
  JDialog getDialog() {
    return mDialog;
  }
  
  void updateDescriptionSelection(boolean value) {
    if(mCenterPanelEntry != null) {
      mCenterPanelEntry.updateDescriptionSelection(value);
    }
  }
  
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return mWrapper;
  }
  
  private class ProgramListCenterPanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return getInfo().getName();
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanelWrapper;
    }
  }
}

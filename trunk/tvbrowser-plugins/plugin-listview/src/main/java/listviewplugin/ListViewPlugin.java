/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package listviewplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import util.settings.PluginPictureSettings;
import util.ui.persona.Persona;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.ProgramFilter;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin shows a List of current running Programs
 * 
 * @author bodo
 */
public class ListViewPlugin extends Plugin {
  private static final Version mVersion = new Version(3,20,false);

    protected static final int PROGRAMTABLEWIDTH = 200;
  
    /** Translator */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewPlugin.class);
    
    /** Settings */
    private Properties mSettings;
    
    /** Show at Startup */
    private boolean mShowAtStartup = false;
    
    private static ListViewPlugin mInstance;
    
    private PluginInfo mPluginInfo;
    
    private PluginCenterPanelWrapper mCenterWrapper;
    
    private ListViewPanel mCenterPanel;
    
    private JPanel mCenterPanelWrapper;
    
    private boolean mTvBrowserStarted;
    
    /**
     * Creates the Plugin
     */
    public ListViewPlugin() {
      mInstance = this;
      mTvBrowserStarted = false;
    }
    
    /**
     * @return The instance of this class.
     */
    public static ListViewPlugin getInstance() {
      return mInstance;
    }
    
    public static Version getVersion() {
      return mVersion;
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
      mCenterWrapper = new PluginCenterPanelWrapper() {
        
        @Override
        public PluginCenterPanel[] getCenterPanels() {
          return new PluginCenterPanel[] {new PluginCenterPanelImpl()};
        }
        
        @Override
        public void scrolledToChannel(Channel channel) {
          if(mCenterPanel != null) {
            mCenterPanel.showChannel(channel);
          }
        }
        
        @Override
        public void filterSelected(ProgramFilter filter) {
          if(mCenterPanel != null) {
            mCenterPanel.showForFilter(filter);
          }
        }
        
        @Override
        public void scrolledToDate(Date date) {
          if(mCenterPanel != null) {
            mCenterPanel.showForDate(date, -1);
          }
        }
        
        public void scrolledTo(Date date,int minute) {
          if(mCenterPanel != null) {
            mCenterPanel.showForDate(date,minute);
          }
        }
        
        @Override
        public void scrolledToNow() {
          if(mCenterPanel != null) {
            mCenterPanel.showForNow();
          }
        }
        
        @Override
        public void scrolledToTime(int time) {
          if(mCenterPanel != null) {
            mCenterPanel.showForTimeButton(time);
          }
        }
        
        @Override
        public void timeEvent() {
          if(mCenterPanel != null) {
            mCenterPanel.refreshView();
          }
        }
      };
      
      if(mTvBrowserStarted) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            mCenterPanel = new ListViewPanel(ListViewPlugin.this);
            Persona.getInstance().registerPersonaListener(mCenterPanel);
            mCenterPanel.updatePersona();
            mCenterPanelWrapper.add(mCenterPanel, BorderLayout.CENTER);
          }
        });
      }
    }
    
    public void onDeactivation() {
      if(mCenterPanel != null) {
        Persona.getInstance().removePersonaListerner(mCenterPanel);
      }
      
      mCenterPanel = null;
    }

    /**
     * Returns Informations about this Plugin
     */
    public PluginInfo getInfo() {
      if(mPluginInfo == null) {
        String name = mLocalizer.msg("pluginName", "View List Plugin");
        String desc = mLocalizer.msg("description", "Shows a List of current running Programs");
        String author = "Bodo Tasche";
        
        mPluginInfo = new PluginInfo(ListViewPlugin.class, name, desc, author);
      }
      
      return mPluginInfo;
    }

    /**
     * Creates the Dialog
     */
    public void showDialog() {
        final ListViewDialog dlg = new ListViewDialog(getParentFrame(), this, mSettings);

        layoutWindow("listViewDialog", dlg);
        
        dlg.setVisible(true);
    }

    public ActionMenu getButtonAction() {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showDialog();
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("buttonName", "View Liste"));
        action.putValue(Action.SMALL_ICON, createImageIcon("actions", "view-list", 16));
        action.putValue(BIG_ICON, createImageIcon("actions", "view-list", 22));
        
        
        return new ActionMenu(action);
    }
    
    /**
     * Load the Settings
     */
    public void loadSettings(Properties settings) {
      
      if (settings == null ) {
        settings = new Properties();
      }
      
      mSettings = settings;

      mShowAtStartup = mSettings.getProperty("showAtStartup", "false").equals("true");
    }
    
    public void handleTvBrowserStartFinished() {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mCenterPanel = new ListViewPanel(ListViewPlugin.this);
          Persona.getInstance().registerPersonaListener(mCenterPanel);
          mCenterPanel.updatePersona();
          mCenterPanelWrapper.add(mCenterPanel, BorderLayout.CENTER);

          if (mShowAtStartup) {
            showDialog();
          }
        }
      });
      
      mTvBrowserStarted = true;
    }
        
    /**
     * Store the Settings
     */
    public Properties storeSettings() {
      return mSettings;
    }
    
    /**
     * Parses a Number from a String.
     * @param str Number in String to Parse
     * @return Number if successfull. Default is 0
     */
    public int parseNumber(String str) {
        
        try {
            int i = Integer.parseInt(str);
            return i;
        } catch (Exception e) {
            
        }
        
        return 0;
    }
    
    public SettingsTab getSettingsTab() {
      return new ListViewSettings(mSettings);
    }
    
    /**
     * @return The settings for the program panels of the list.
     * @since 2.6
     */
    protected PluginPictureSettings getPictureSettings() {
      return new PluginPictureSettings(Integer.parseInt(mSettings.getProperty("pictureSettings",String.valueOf(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE))));
    }
    
    public String getPluginCategory() {
      return Plugin.OTHER_CATEGORY;
    }
    
    public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
      return mCenterWrapper;
    }
    
    private class PluginCenterPanelImpl extends PluginCenterPanel {

      @Override
      public String getName() {
        return mLocalizer.msg("pluginName", "Currently running programs");
      }

      @Override
      public JPanel getPanel() {
        return mCenterPanelWrapper;
      }
      
    }
}
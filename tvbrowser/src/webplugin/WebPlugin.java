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
package webplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import util.ui.BrowserLauncher;
import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin is a generic Web-Tool. 
 * A User can configure his favorite Search-Engines and search for the given Movie
 */
public class WebPlugin extends Plugin {
    /** Localizer */
    private static final Localizer mLocalizer = Localizer
    .getLocalizerFor(WebPlugin.class);    
    
    /** Default-Addresses */
    final static WebAddress WEB_GOOGLE = new WebAddress("Google", "http://www.google.com/search?q=%22{0}%22", "webplugin/google.gif", "UTF-8", false, true);
    final static WebAddress WEB_IMDB   = new WebAddress("ImdB", "http://akas.imdb.com/Tsearch?title={0}", "webplugin/imdb.gif", "UTF-8", false, true);
    final static WebAddress WEB_OFDB   = new WebAddress("OfdB", "http://www.ofdb.de/view.php?page=suchergebnis&Kat=DTitel&SText={0}", null, "ISO-8859-1", false, false);
    
    /** The WebAddresses */ 
    private Vector mAddresses;
    
    /**
     * Creates the Plugin
     */
    public WebPlugin() {

    }

    /**
     * Returns the Plugin-Info
     */
    public PluginInfo getInfo() {
        return new PluginInfo("WebPlugin", 
                mLocalizer.msg("desc","Searches on the Web for a Program"),
                "Bodo Tasche",new Version(1, 0));
    }

    /**
     * Loads the Data
     */
    public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mAddresses = new Vector();
        
        int version = in.readInt();
        
        int size = in.readInt();
        
        Vector defaults = new Vector();
        defaults.add(WEB_GOOGLE);
        defaults.add(WEB_IMDB);
        defaults.add(WEB_OFDB);
        
        for (int i = 0; i < size;i++) {
            WebAddress newone = new WebAddress(in);
            
            if (!newone.isUserEntry()) {
                for (int v = 0; v < defaults.size(); v++) {
                    WebAddress def = (WebAddress) defaults.get(v);
                    
                    if (def.getName().equals(newone.getName())) {
                        def.setActive(newone.isActive());
                        newone = def;
                        defaults.remove(v);
                    }
                }
            }
            
            mAddresses.add(newone);
        }
        
        for (int i = 0; i < defaults.size();i++) {
            mAddresses.add(defaults.get(i));
        }
    }

    /**
     * Saves the Data
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(1);
        out.writeInt(mAddresses.size());
        
        for (int i = 0; i < mAddresses.size(); i++) {
            ((WebAddress)mAddresses.get(i)).writeData(out);
        }
        
    }

    /**
     * Returns the Icon-Name
     */
    public String getMarkIconName() {
        return "webplugin/Search16.gif";
    }
    
    /**
     * Creates the Settings-Tab
     */
    public SettingsTab getSettingsTab() {
        if (mAddresses == null) {
            createDefaultSettings();            
        }        
        return new WebSettingsTab((JFrame)getParentFrame(), mAddresses);
    }

    
    /**
     * Create the Default-Settings
     */
    private void createDefaultSettings() {
        mAddresses = new Vector();
        
       WebAddress test = new WebAddress("Test", "http://akas.imdb.com/Tsearch?title={0}", null, "ISO-8859-1", true, false);
        
        mAddresses.add(WEB_GOOGLE);
        mAddresses.add(WEB_IMDB);
        mAddresses.add(WEB_OFDB);
        mAddresses.add(test);
    }
    
    /**
     * Creates the Context-Menu-Entries
     */
    public Action[] getContextMenuActions(final Program program) {
        
        if (mAddresses == null) {
            createDefaultSettings();            
        }
        
        ArrayList actionList = new ArrayList();
        
        for (int i = 0; i < mAddresses.size(); i++) {
            
            final WebAddress adr = (WebAddress) mAddresses.get(i);

            if (adr.isActive()) {
                AbstractAction action = new AbstractAction() {

                    public void actionPerformed(ActionEvent evt) {
                        openUrl(program, adr);
                    }
                };
                action.putValue(Action.NAME, mLocalizer.msg("SearchOn", "Search on ") + adr.getName());
                action.putValue(Action.SMALL_ICON, adr.getIcon());
                
                actionList.add(action);
            }
            
        }

        Action[] actions = new Action[actionList.size()];
        
        for (int i = 0; i < actionList.size(); i++) {
            actions[i] = (Action)actionList.get(i);
        }
        
        return actions;
    }

    /**
     * Opens the Address in a browser
     * @param program Program to search on the Web
     * @param address Search-Engine to use
     */
    protected void openUrl(Program program, WebAddress address) {
		    try {
  	  			String search = URLEncoder.encode(program.getTitle(), address.getEncoding());
  	  			String url = address.getUrl().replaceAll("\\{0\\}", URLEncoder.encode(program.getTitle(), address.getEncoding()));
  	  			BrowserLauncher.openURL(url);
  		    } catch (Exception e) {
  		        e.printStackTrace();
  		    }    
    }

    /**
     * Returns NO Button-Text
     */
    protected String getButtonText() {
        return null;
    }
}
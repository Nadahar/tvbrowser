/*
 * EMailPlugin by Bodo Tasche
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
package emailplugin;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URLEncoder;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import util.exc.ErrorHandler;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin makes it possible to send an email with short
 * info's about the Program
 * 
 * @author bodum
 */
public class EMailPlugin extends Plugin {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailPlugin.class);
    /** Properties */
    private Properties mSettings;
    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getInfo()
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "EMail export");
        String desc = mLocalizer.msg("description",
                "Send a EMail with an external Program");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(0, 1));
    }	
    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getMarkIconName()
     */
    public String getMarkIconName() {
        return "emailplugin/email.gif";
    }

    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
     */
    public ActionMenu getContextMenuActions(final Program program) {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                Program[] programArr = { program };
                createMail(programArr);
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("contextMenuText","Send via EMail"));
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("emailplugin/email.gif", EMailPlugin.class)));
        
        return new ActionMenu(action);
    }    
    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#canReceivePrograms()
     */
    public boolean canReceivePrograms() {
        return true;
    }    
    
    /**
     * This method is invoked for multiple program execution.
     * 
     * @see #supportMultipleProgramExecution()
     */
    public void receivePrograms(Program[] programArr) {
        createMail(programArr);
    }    
    
	/* (non-Javadoc)
	 * @see devplugin.Plugin#getSettingsTab()
	 */
	public SettingsTab getSettingsTab() {

		EMailSettingsTab tab = new EMailSettingsTab(mSettings);
		
		return tab;
	}
	
	/* (non-Javadoc)
	 * @see devplugin.Plugin#loadSettings(java.util.Properties)
	 */
	public void loadSettings(Properties settings) {
		mSettings = settings;
	}
	
	/* (non-Javadoc)
	 * @see devplugin.Plugin#storeSettings()
	 */
	public Properties storeSettings() {
		return mSettings;
	}
	
    /**
     * Create the Mail
     * @param program Programs to show in the Mail
     */
    private void createMail(Program[] program) {
    	
    	String emailText = new String();
    	
    	for (int i = 0; i < program.length; i++) {
    		emailText += program[i].getDateString() + ", "+program[i].getChannel().getName() + "\n" + 
			             program[i].getTimeString() + " - " + program[i].getTimeFieldAsString(ProgramFieldType.END_TIME_TYPE) + "   " + program[i].getTitle() + "\n\n";
    	}

    	if (mSettings.getProperty("application", "").trim().equals("")) {	
    		JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()), 
    				mLocalizer.msg("SpecifyApp", "Please specify a Application in the Settings for this Plugin."));
    		return;
    	}
    	
    	File f = new File(mSettings.getProperty("application"));
    	
    	if (!f.exists() || f.isDirectory()) {
    		JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(getParentFrame()), 
    				mLocalizer.msg("AppNotFound", "Application wasn't found. Please specify a Application in the Settings for this Plugin."));
    		return;
    	}
    	
    	try {
    		String param = mSettings.getProperty("parameter", "").replaceAll("\\{0\\}", "mailto:?body=" + URLEncoder.encode(emailText, "UTF8").replaceAll("\\+", "%20"));
    		Runtime.getRuntime().exec(mSettings.getProperty("application") + " " + param);
    	} catch (Exception e) {
    		e.printStackTrace();
    		ErrorHandler.handle(mLocalizer.msg("ErrorWhileStarting", "Error while starting Mail-Application"), e);
    	}
    }
}
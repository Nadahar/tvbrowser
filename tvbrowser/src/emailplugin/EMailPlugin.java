/*
 * Created on 11.02.2005
 */
package emailplugin;

import java.awt.event.ActionEvent;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import util.ui.ImageUtilities;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.Version;

/**
 * @author bodum
 */
public class EMailPlugin extends Plugin {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailPlugin.class);

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
    
    private void createMail(Program[] program) {
    	
    	String emailText = new String();
    	
    	for (int i = 0; i < program.length; i++) {
    		emailText += program[i].getChannel().getName() + " " + program[i].getDateString() + ", " + program[i].getTimeString() + "-" + program[i].getTimeFieldAsString(ProgramFieldType.END_TIME_TYPE) + " " + program[i].getTitle() + "\n";
    	}

    	try {
        	Runtime.getRuntime().exec("/opt/MozillaThunderbird/bin/thunderbird.sh mailto:?body=" + URLEncoder.encode(emailText, "UTF8").replaceAll("\\+", "%20"));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	System.out.println(emailText.trim());
    }
}
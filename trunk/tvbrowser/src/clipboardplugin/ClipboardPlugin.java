/*
 * Created on 18.06.2004
 */
package clipboardplugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * This Plugin is an internal Clipboard.
 * 
 * @author bodo
 */
public class ClipboardPlugin extends Plugin {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ClipboardPlugin.class);

    /** Contains the Data in this Clipboard */
    private Vector mClipboard = new Vector();


    /** Needed for Position */
    private Point mLocationListDialog = null;

    /** Needed for Position */
    private Dimension mDimensionListDialog = null;

    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getInfo()
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "Clipboard");
        String desc = mLocalizer.msg("description", "A internal Clipboard for receiving and sending Programs from/to other Plugins.");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(0, 10));
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#supportMultipleProgramExecution()
     */
    public boolean supportMultipleProgramExecution() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getMarkIconName()
     */
    public String getMarkIconName() {
        return "clipboardplugin/clipboard.png";
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getContextMenuItemText()
     */
    public String getContextMenuItemText() {
        return mLocalizer.msg("contextMenuText", "Add to Clipboard");
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getButtonText()
     */
    public String getButtonText() {
        return mLocalizer.msg("pluginName", "Clipboard");
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getButtonIconName()
     */
    public String getButtonIconName() {
        return "clipboardplugin/clipboard.png";
    }

    /**
     * Creates the Dialog
     */
    public void execute() {
    	
    	if (mClipboard.size() == 0) {
    		JOptionPane.showMessageDialog(getParentFrame(),  mLocalizer.msg("empty", "The Clipboard is empty."));
    		return;
    	}
    	
        ClipboardDialog dlg = new ClipboardDialog(getParentFrame(), this, mClipboard);

        dlg.pack();
        dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                mDimensionListDialog = e.getComponent().getSize();
            }

            public void componentMoved(ComponentEvent e) {
                e.getComponent().getLocation(mLocationListDialog);
            }
        });

        if ((mLocationListDialog != null) && (mDimensionListDialog != null)) {
            dlg.setLocation(mLocationListDialog);
            dlg.setSize(mDimensionListDialog);
            dlg.show();
        } else {
            dlg.setSize(400, 300);
            UiUtilities.centerAndShow(dlg);
            mLocationListDialog = dlg.getLocation();
            mDimensionListDialog = dlg.getSize();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#execute()
     */
    public void execute(Program program) {
        if (mClipboard.indexOf(program) > -1) {
            program.unmark(this);
            mClipboard.remove(program);
        } else {
            program.mark(this);
            mClipboard.add(program);
        }
        
        
    }

    /**
     * This method is invoked for multiple program execution.
     * 
     * @see #supportMultipleProgramExecution()
     */
    public void execute(Program[] programArr) {
        for (int i = 0; i < programArr.length; i++) {
            programArr[i].mark(this);
            mClipboard.add(programArr[i]);
        }
    }
}
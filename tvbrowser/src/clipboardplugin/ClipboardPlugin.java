/*
 * Created on 18.06.2004
 */
package clipboardplugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import util.ui.ImageUtilities;
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

    public Action getButtonAction() {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showDialog();
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("pluginName", "Clipboard"));
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("clipboardplugin/clipboard.png", ClipboardPlugin.class)));
        
        return action;
    }
    
    public Action[] getContextMenuActions(final Program program) {

        final boolean inList = mClipboard.indexOf(program) > -1; 
        
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                if (inList) {
                    program.unmark(ClipboardPlugin.this);
                    mClipboard.remove(program);
                } else {
                    program.mark(ClipboardPlugin.this);
                    mClipboard.add(program);
                }
            }
        };
        
        if (inList) {
            action.putValue(Action.NAME, mLocalizer.msg("contextMenuRemoveText", "Remove from Clipboard"));
        } else {
            action.putValue(Action.NAME, mLocalizer.msg("contextMenuAddText", "Add to Clipboard"));
        }

        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("clipboardplugin/clipboard.png", ClipboardPlugin.class)));
        
        return new Action[] {action};
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getInfo()
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "Clipboard");
        String desc = mLocalizer.msg("description", "A internal Clipboard for receiving and sending Programs from/to other Plugins.");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(0, 20));
    }
    
    /**
     * Creates the Dialog
     */
    public void showDialog() {
    	
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

    public boolean canReceivePrograms() {
        return true;
    }


    public String getMarkIconName() {
        return "clipboardplugin/clipboard.png";
    }

    
    public void receivePrograms(Program[] programArr) {
        for (int i = 0; i < programArr.length; i++) {
            if (!mClipboard.contains(programArr[i])) {
                programArr[i].mark(this);
            	mClipboard.add(programArr[i]);
            }
        }
    }
    
}
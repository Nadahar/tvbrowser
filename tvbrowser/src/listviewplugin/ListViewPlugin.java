/*
 * Created on 11.04.2004
 */
package listviewplugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;

import javax.swing.SwingUtilities;

import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Version;

/**
 * This Plugin shows a List of current running Programs
 * 
 * @author bodo
 */
public class ListViewPlugin extends Plugin {
   
    /** Translator */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer
            .getLocalizerFor(ListViewPlugin.class);

    /**
     * Creates the Plugin
     */
    public ListViewPlugin() {
        _listViewInstance = this;
    }
    
    /**
     * Returns Informations about this Plugin
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "View List Plugin");
        String desc = mLocalizer
                .msg("description",
                        "Shows a List of current running Programs");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(1, 0));
    }

    /**
     * Creates the Dialog
     */
    public void execute() {
        final ListViewDialog dlg = new ListViewDialog(getParentFrame(), this);
        
        Runnable runLater = new Runnable() {

            public void run() {


                dlg.pack();
                dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

                    public void componentResized(ComponentEvent e) {
                        _dimensionListDialog = e.getComponent().getSize();
                    }

                    public void componentMoved(ComponentEvent e) {
                        e.getComponent().getLocation(_locationListDialog);
                    }
                });

                if ((_locationListDialog != null)
                        && (_dimensionListDialog != null)) {
                    dlg.setLocation(_locationListDialog);
                    dlg.setSize(_dimensionListDialog);
                    dlg.show();
                } else {
                    dlg.setSize(500, 600);
                    UiUtilities.centerAndShow(dlg);
                    _locationListDialog = dlg.getLocation();
                    _dimensionListDialog = dlg.getSize();
                }
            }
        };
        SwingUtilities.invokeLater(runLater);

    }

    /**
     * Returns the Button-Text
     */
    public String getButtonText() {
        return mLocalizer.msg("buttonName", "View Liste");
    }

    public String getMarkIconName() {
        return "listviewplugin/listview16.gif";
    }

    /**
     * Returns the Button-Icon 
     */
    public String getButtonIconName() {
        return "listviewplugin/listview16.gif";
    }

    /**
     * Returns an Instance of this Plugin
     * @return Instance of this Plugin
     */
    public static ListViewPlugin getInstance() {
        return _listViewInstance;
    }
    
    /** Instance of this Plugin */
    private static ListViewPlugin _listViewInstance;
    
    /** Needed for Position */
    private Point _locationListDialog = null;
    /** Needed for Position */
    private Dimension _dimensionListDialog = null;
}
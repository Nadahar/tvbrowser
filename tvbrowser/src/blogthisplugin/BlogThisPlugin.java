/*
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
package blogthisplugin;

import java.awt.event.ActionEvent;
import java.net.URLEncoder;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import util.paramhandler.ParamParser;
import util.ui.BrowserLauncher;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * The Main-Class for the Blog-Plugin
 * 
 * @author bodum
 */
public class BlogThisPlugin extends Plugin {
    /** Translator */
    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(BlogThisPlugin.class);

    /** Default Text */
    private static final String DEFAULT_TITLE = "{title} ({channel_name})";

    private static final String DEFAULT_CONTENT = "<blockquote><strong>{title}</strong>\n\n<em>{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")} {channel_name}</em>\n\n{short_info}</blockquote>\n";

    /** Default URLs */
    public static final String URL_WORDPRESS = "http://yoursite.com/wordpress/wp-admin/bookmarklet.php";

    public static final String URL_B2EVOLUTION = "http://yourblog.com/admin/b2bookmarklet.php";

    /** Service Names */
    public static final String BLOGGER = "BLOGGER";

    public static final String BLOGG = "BLOGG";

    public static final String WORDPRESS = "WORDPRESS";

    public static final String B2EVOLUTION = "B2EVOLUTION";

    /** Settings */
    private Properties mSettings;

    // blogger.popupurl=http://www.blogger.com/blog_this.pyra?&n=%S&t=%S&u=%S&sourceid=%S
    // b2evolution.popupurl=%S?post_title=%S&content=%S&post_url=%S&sourceid=%S
    // wordpress.popupurl=%S?popuptitle=%S&text=%S&popupurl=%S&sourceid=%S

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getInfo()
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "BlogThis");
        String desc = mLocalizer.msg("description", "Creates a new Blog-Entry");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(0, 1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
     */
    public ActionMenu getContextMenuActions(final Program program) {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                blogThis(program);
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("contextMenuText",
                "Create a new Blog-Entry"));
        action.putValue(Action.SMALL_ICON, createImageIcon("apps",
                "internet-web-browser", 16));
        return new ActionMenu(action);
    }

    /**
     * Creates a new Blog-Entry
     * 
     * @param program Program to use for the Entry
     */
    private void blogThis(Program program) {

        if (mSettings.getProperty("BlogService") == null) {
            
            int ret = JOptionPane.showConfirmDialog(getParentFrame(), "Plugin konfigurieren ?!");
            
            if (ret == JOptionPane.YES_NO_CANCEL_OPTION) {
            }
            
            return;
        }
    
        ParamParser parser = new ParamParser();

        String title = parser.analyse(DEFAULT_TITLE, program);
        String content = parser.analyse(DEFAULT_CONTENT, program);
        String url = program.getChannel().getWebpage();

        try {
            BrowserLauncher
                    .openURL("http://blog.wannawork.de/admin/b2bookmarklet.php?"
                            + "post_title="
                            + URLEncoder.encode(title, "ISO-8859-1")
                            + "&"
                            + "content="
                            + URLEncoder.encode(content, "ISO-8859-1")
                            + "&"
                            + "post_url="
                            + URLEncoder.encode(url, "ISO-8859-1")
                            + "&"
                            + "sourceid="
                            + URLEncoder.encode("BlogThis v0.1", "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getSettingsTab()
     */
    public SettingsTab getSettingsTab() {
        return new BlogSettingsTab(this, mSettings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#loadSettings(java.util.Properties)
     */
    public void loadSettings(Properties settings) {
        mSettings = settings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#storeSettings()
     */
    public Properties storeSettings() {
        return mSettings;
    }

}
/**
 * 
 */
package tvbrowser.ui.settings;

import javax.swing.Icon;

import tvbrowser.core.icontheme.IconLoader;
import devplugin.SettingsTab;

/**
 * @author MadMan
 *
 */
public abstract class AbstractSettingsTab implements SettingsTab {

	public Icon getIcon() {
		return null;
	}
	
	protected Icon getPictureIcon() {
	    return IconLoader.getInstance().getIconFromTheme("mimetypes", "image-x-generic", 16);
	}

}

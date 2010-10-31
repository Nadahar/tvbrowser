/**
 *
 */
package tvbrowser.ui.settings;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.icontheme.IconLoader;
import util.ui.EnhancedPanelBuilder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

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

	protected JPanel createEmptyPanel(final String title, final String description) {
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ",pref:grow");
    panel.addParagraph(title);
    panel.addRow();
    panel.add(new JLabel(description), new CellConstraints().xy(2, panel.getRow()));
    return panel.getPanel();
	}

}

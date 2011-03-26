/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
 * @author Bananeweizen
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

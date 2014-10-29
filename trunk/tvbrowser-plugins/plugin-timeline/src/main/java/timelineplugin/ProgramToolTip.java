/*
 * Timeline by Reinhard Lehrbaum
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
 */
package timelineplugin;

import java.awt.Dimension;
import java.awt.Graphics;
import java.lang.reflect.Constructor;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.ProgramPanel;
import devplugin.Plugin;
import devplugin.Program;

public class ProgramToolTip extends JToolTip {
	private static final long serialVersionUID = 1L;

	public ProgramToolTip(final Program p) {
		setUI(new ProgramToolTipUI(p));
	}

	private static class ProgramToolTipUI extends ComponentUI {
		private ProgramPanel mProgramPanel;

		ProgramToolTipUI(final Program p) {
		  PluginPictureSettings pictureSettings = new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE);
		  
			ProgramPanelSettings settings = new ProgramPanelSettings(pictureSettings, false);
			
      try {
        Constructor<? extends ProgramPanelSettings> constructor = settings.getClass().getConstructor(new Class<?>[] {PluginPictureSettings.class, boolean.class, int.class, boolean.class, boolean.class, boolean.class});
        settings = (ProgramPanelSettings)constructor.newInstance(new Object[] {pictureSettings, false, ProgramPanelSettings.X_AXIS, false, true, false});
      } catch (Exception e) {
        // ignore, just use old instance of ProgramPanelSettings, the used TV-Browser version is not able to show channel logos in program panels.
      }
			
			mProgramPanel = new ProgramPanel(p, settings);
			mProgramPanel.setSize(new Dimension(220, 300));
			mProgramPanel.setHeight(300);
			mProgramPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}

		public void paint(final Graphics g, final JComponent c) {
		  g.setColor(UIManager.getColor("List.background"));
		  g.fillRect(0, 0, c.getWidth(), c.getHeight());
			mProgramPanel.paintComponent(g);
			g.drawRect(0, 0, c.getSize().width - 1, c.getSize().height - 1);
		}

		public Dimension getPreferredSize(final JComponent c) {
			return new Dimension(mProgramPanel.getWidth(), mProgramPanel.getPreferredHeight());
		}
	}
}

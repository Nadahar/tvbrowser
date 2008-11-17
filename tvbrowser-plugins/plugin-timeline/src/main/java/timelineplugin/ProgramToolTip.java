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

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import util.ui.ProgramPanel;
import devplugin.*;

public class ProgramToolTip extends JToolTip
{
	private static final long serialVersionUID = 1L;

	public ProgramToolTip(Program p)
	{
		setUI(new ProgramToolTipUI(p));
	}

	private class ProgramToolTipUI extends ComponentUI
	{
		private ProgramPanel mProgramPanel;

		public ProgramToolTipUI(Program p)
		{
			mProgramPanel = new ProgramPanel(p);
			mProgramPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}

		public void paint(Graphics g, JComponent c)
		{
			mProgramPanel.paintComponent(g);
			g.drawRect(0, 0, c.getSize().width - 1, c.getSize().height - 1);
		}

		public Dimension getPreferredSize(JComponent c)
		{
			return new Dimension(200, mProgramPanel.getPreferredHeight());
		}
	}
}

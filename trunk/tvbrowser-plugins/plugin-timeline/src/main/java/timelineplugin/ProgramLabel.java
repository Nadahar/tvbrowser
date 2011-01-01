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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import timelineplugin.format.TextFormatter;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;

public class ProgramLabel extends JComponent implements ChangeListener,
		MouseListener {
	private static final long serialVersionUID = 1L;

	private transient Program mProgram;
	private transient TextFormatter mTextFormatter = null;

	private static Color programPanelOnAirLightColor = Plugin.getPluginManager().getTvBrowserSettings()
			.getProgramPanelOnAirLightColor();

	private static Color programPanelOnAirDarkColor = Plugin.getPluginManager().getTvBrowserSettings()
			.getProgramPanelOnAirDarkColor();

	private Color mBackColor;

	public ProgramLabel(final Program program) {
		addMouseListener(this);

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.black),
				BorderFactory.createEmptyBorder(1, 5, 1, 5)));

		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setToolTipText(" ");
		setProgram(program);
	}

	private void setProgram(final Program program) {
		mProgram = program;
		mBackColor = Plugin.getPluginManager().getTvBrowserSettings()
		.getColorForMarkingPriority(mProgram.getMarkPriority());
	}

	public JToolTip createToolTip() {
		if (mProgram != null) {
			return new ProgramToolTip(mProgram);
		} else {
			return null;
		}
	}

	public void mouseClicked(final MouseEvent e) {
	}

	public void mouseEntered(final MouseEvent e) {
	}

	public void mouseExited(final MouseEvent e) {
	}

	public void mousePressed(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopup(e);
		}
	}

	public void mouseReleased(final MouseEvent e) {
		final PluginManager mng = Plugin.getPluginManager();

		mProgram.addChangeListener(this);

		if (e.isPopupTrigger()) {
			showPopup(e);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			mng.handleProgramMiddleClick(mProgram, TimelinePlugin.getInstance());
		} else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			mng.handleProgramDoubleClick(mProgram, TimelinePlugin.getInstance());
		} else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
			mng.handleProgramSingleClick(mProgram, TimelinePlugin.getInstance());
		} else {
			mProgram.removeChangeListener(this);
		}
	}

	private void showPopup(final MouseEvent e) {
		final JPopupMenu menu = Plugin.getPluginManager().createPluginContextMenu(
				mProgram, TimelinePlugin.getInstance());
		menu.show(this, e.getX() - 15, e.getY() - 15);
	}

	protected void processMouseEvent(final MouseEvent e) {
		redispatch(e);
		super.processMouseEvent(e);
	}

	protected void processMouseMotionEvent(final MouseEvent e) {
		redispatch(e);
		super.processMouseMotionEvent(e);
	}

	public void setFormatter(final TextFormatter formatter) {
		mTextFormatter = formatter;
	}

	private TextFormatter getFormatter() {
		return mTextFormatter == null ? TimelinePlugin.getInstance().getFormatter()
				: mTextFormatter;
	}

	protected void paintComponent(final Graphics g) {
		final Rectangle r = g.getClipBounds();
		final Rectangle rb = this.getBounds();

		final Color oriColor = g.getColor();
		if (mBackColor != null) {
			g.setColor(mBackColor);
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(oriColor);
		}
		if (TimelinePlugin.getSettings().showProgress() && mProgram.isOnAir()) {
			g.setColor(programPanelOnAirDarkColor);
			final int positionX = Math.abs(TimelinePlugin.getInstance()
					.getNowPosition() - rb.x);
			g.fillRect(0, 0, positionX, rb.height);
			g.setColor(programPanelOnAirLightColor);
			g.fillRect(positionX, 0, rb.width - positionX, rb.height);
			g.setColor(oriColor);
		}
		if (mProgram.isExpired()) {
			g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g
					.getColor().getBlue(), (int) (g.getColor().getAlpha() * 6 / 10.)));
		}
		g.setFont(TimelinePlugin.getFont());
		getFormatter().paint(mProgram, g, rb.width, rb.height);
	}

	public void stateChanged(final ChangeEvent evt) {
		if (evt.getSource() == mProgram) {
			repaint();
		}
	}

	public void addNotify() {
		super.addNotify();
		mProgram.addChangeListener(this);
	}

	public void removeNotify() {
		super.addNotify();
		mProgram.removeChangeListener(this);
	}

	private static void redispatch(final MouseEvent e) {
		final Component source = e.getComponent();
		final Component destination = source.getParent();
		destination.dispatchEvent(SwingUtilities.convertMouseEvent(source, e,
				destination));
	}
}

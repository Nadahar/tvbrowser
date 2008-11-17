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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import timelineplugin.format.TextFormatter;
import devplugin.*;

public class ProgramLabel extends JComponent implements ChangeListener, MouseListener
{
	private static final long serialVersionUID = 1L;

	private Program mProgram;
	private TextFormatter mTextFormatter = null;

	public ProgramLabel()
	{
		addMouseListener(this);

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(1, 5, 1, 5)));

		setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	public void setProgram(Program p)
	{
		mProgram = p;

		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append(mProgram.getDateString()).append(" · ");
		sb.append(mProgram.getTimeString()).append(" · ");
		sb.append(mProgram.getChannel().getName()).append("<br>");
		sb.append("<b>").append(mProgram.getTitle()).append("</b><br>");
		String shortInfo = mProgram.getShortInfo();
		sb.append(shortInfo == null ? "" : shortInfo);
		sb.append("</html>");
		setToolTipText(sb.toString());
	}

	public JToolTip createToolTip()
	{
		if (mProgram != null)
		{
			return new ProgramToolTip(mProgram);
		}
		else
		{
			return null;
		}
	}

	public void mouseClicked(MouseEvent e)
	{}

	public void mouseEntered(MouseEvent e)
	{}

	public void mouseExited(MouseEvent e)
	{}

	public void mousePressed(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			showPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		PluginManager mng = Plugin.getPluginManager();

		mProgram.addChangeListener(this);

		if (e.isPopupTrigger())
		{
			showPopup(e);
		}
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			mng.handleProgramMiddleClick(mProgram, TimelinePlugin.getInstance());
		}
		else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
		{
			mng.handleProgramDoubleClick(mProgram, TimelinePlugin.getInstance());
		}
		else
		{
			mProgram.removeChangeListener(this);
		}
	}

	private void showPopup(MouseEvent e)
	{
		JPopupMenu menu = Plugin.getPluginManager().createPluginContextMenu(mProgram, TimelinePlugin.getInstance());
		menu.show(this, e.getX() - 15, e.getY() - 15);
	}

	protected void processMouseEvent(MouseEvent e)
	{
		redispatch(e);
		super.processMouseEvent(e);
	}

	protected void processMouseMotionEvent(MouseEvent e)
	{
		redispatch(e);
		super.processMouseMotionEvent(e);
	}

	public void setFormatter(TextFormatter formatter)
	{
		mTextFormatter = formatter;
	}

	private TextFormatter getFormatter()
	{
		return mTextFormatter == null ? TimelinePlugin.getInstance().getFormatter() : mTextFormatter;
	}

	protected void paintComponent(Graphics g)
	{
		Rectangle r = g.getClipBounds();
		Rectangle rb = this.getBounds();

		Color oriColor = g.getColor();
		Color bc = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(mProgram.getMarkPriority());
		if (bc != null)
		{
			g.setColor(bc);
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(oriColor);
		}
		if (TimelinePlugin.getInstance().showProgress() && mProgram.isOnAir())
		{
			g.setColor(Plugin.getPluginManager().getTvBrowserSettings().getProgramPanelOnAirDarkColor());
			int positionX = Math.abs(TimelinePlugin.getInstance().getNowPosition() - rb.x);
			g.fillRect(0, 0, positionX, rb.height);
			g.setColor(Plugin.getPluginManager().getTvBrowserSettings().getProgramPanelOnAirLightColor());
			g.fillRect(positionX, 0, rb.width - positionX, rb.height);
			g.setColor(oriColor);
		}
		if (mProgram.isExpired())
		{
			g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), (int) (g.getColor().getAlpha() * 6 / 10.)));
		}
		g.setFont(TimelinePlugin.getInstance().getFont());
		//		if (this.getLocation().x >= 0)
		//		{
		getFormatter().paint(mProgram, g, rb.width, rb.height);
		//		}
		//		else
		//		{
		//			int x = Math.abs(this.getLocation().x);
		//			getFormatter().paint(mProgram, g, x, rb.width, rb.height);
		//		}
	}

	public void stateChanged(ChangeEvent evt)
	{
		if (evt.getSource() == mProgram)
		{
			repaint();
		}
	}

	public void addNotify()
	{
		super.addNotify();
		mProgram.addChangeListener(this);
	}

	public void removeNotify()
	{
		super.addNotify();
		mProgram.removeChangeListener(this);
	}

	private void redispatch(MouseEvent e)
	{
		Component source = e.getComponent();
		Component destination = source.getParent();
		destination.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, destination));
	}
}

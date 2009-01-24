/*
 * TV-Pearl by Reinhard Lehrbaum
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
package tvpearlplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import devplugin.Marker;
import devplugin.Program;

public class TVPearlProgramPanel extends JComponent implements ChangeListener
{
	private static final long serialVersionUID = 1L;

	private static final int ICON_SPACE = 25;

	private TVPProgram mPearlProgram;
	private Program mProgram;
	private Color mTextColor;
	private Font mHeaderFont;
	private Font mBodyFont;
	private Icon[] mIconList = null;

	public TVPearlProgramPanel(TVPProgram p)
	{
		mPearlProgram = p;
		mProgram = p.getProgram();
		mHeaderFont = new Font("Dialog", Font.PLAIN, 12);
		mBodyFont = new Font("Dialog", Font.BOLD, 12);
		fillIconList();
		setPreferredSize();
		addNotify();
	}

	public void paintComponent(Graphics g)
	{
		Graphics2D grp = (Graphics2D) g;
		boolean wellKnownProgram = false;

		setForeground(mTextColor);
		grp.setColor(mTextColor);
		Calendar now = Calendar.getInstance();

		if (mProgram != null)
		{
			wellKnownProgram = true;
			if (mProgram.isExpired())
			{
				setForeground(Color.gray);
				grp.setColor(Color.gray);
			}
		}
		else if (mPearlProgram.getStart().before(now))
		{
			setForeground(Color.gray);
			grp.setColor(Color.gray);
		}

		ImageIcon programStatus;
		if (wellKnownProgram)
		{
			programStatus = TVPearlPlugin.getInstance().getProgramFoundIcon();
		}
		else
		{
			programStatus = TVPearlPlugin.getInstance().getProgramUnknownIcon();
		}
		programStatus.paintIcon(this, grp, ICON_SPACE / 2 - 8, getSize().height / 2 - 8);

		setFont(mHeaderFont);

		int headerHeight = grp.getFontMetrics().getHeight();
		int titleHeight = grp.getFontMetrics(mBodyFont).getHeight();

		grp.drawString(getHeader(), 1 + ICON_SPACE, headerHeight);
		grp.drawString(getAuthor(), getSize().width - grp.getFontMetrics().stringWidth(getAuthor()) - 1, headerHeight);

		grp.setFont(mBodyFont);
		grp.drawString(getBody(), 1 + ICON_SPACE, headerHeight + titleHeight);

		int x = getSize().width - 1;
		int y = headerHeight + 3;
		if (mIconList != null)
		{
			for (int i = mIconList.length - 1; i >= 0; i--)
			{
				x -= mIconList[i].getIconWidth() + 1;
				mIconList[i].paintIcon(this, grp, x, y);
			}
		}
	}

	private String getHeader()
	{
		return TVPearlPlugin.getDayName(mPearlProgram.getStart(), true)
        + ", "
        + DateFormat.getDateInstance().format(
            mPearlProgram.getStart().getTime()) + " - "
        + mPearlProgram.getChannel() + "   ";
	}

	private String getAuthor()
	{
		return mPearlProgram.getAuthor();
	}

	private String getBody()
	{
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		return timeFormat.format(mPearlProgram.getStart().getTime()) + " " + mPearlProgram.getTitle();
	}

	private void setPreferredSize()
	{
		int headerWidth = getFontMetrics(mHeaderFont).stringWidth(getHeader() + " " + getAuthor());
		int bodyWidth = getFontMetrics(mBodyFont).stringWidth(getBody());
		int headerHeight = getFontMetrics(mHeaderFont).getHeight();
		int bodyHeight = getFontMetrics(mBodyFont).getHeight();
		Dimension iconSize = getIconSize();

		setPreferredSize(new Dimension(Math.max(headerWidth, bodyWidth + iconSize.width) + 2 + ICON_SPACE, headerHeight + Math.max(bodyHeight, iconSize.height) + 3));
	}

	private void fillIconList()
	{
		if (mProgram != null)
		{
			ArrayList<Icon> list = new ArrayList<Icon>();

			Marker[] markedByPluginArr = mProgram.getMarkerArr();
			for (Marker marker : markedByPluginArr)
			{
				Icon[] icons = marker.getMarkIcons(mProgram);
				if (icons != null)
				{
					for (int i = icons.length - 1; i >= 0; i--)
					{
						list.add(icons[i]);
					}
				}
			}
			mIconList = list.toArray(new Icon[list.size()]);
		}
	}

	private Dimension getIconSize()
	{
		int w = 0;
		int h = 0;

		if (mIconList != null)
		{
			for (Icon item : mIconList)
			{
				w += item.getIconWidth() + 1;
				h = Math.max(h, item.getIconHeight());
			}
		}

		return new Dimension(w, h);
	}

	public void setTextColor(Color col)
	{
		mTextColor = col;
	}

	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == mProgram)
		{
			TVPearlPlugin.getInstance().updateDialog();
		}
	}

	public void addNotify()
	{
		super.addNotify();
		if (mProgram != null)
		{
			mProgram.addChangeListener(this);
		}
	}

	public void removeNotify()
	{
		super.removeNotify();
		if (mProgram != null)
		{
			mProgram.removeChangeListener(this);
		}
	}
}

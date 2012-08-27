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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import timelineplugin.format.TextFormatter;
import util.program.ProgramUtilities;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.TvBrowserSettings;

public class ProgramLabel extends JComponent implements ChangeListener,
		MouseListener {
	private static final long serialVersionUID = 1L;

	private transient Program mProgram;
	private transient TextFormatter mTextFormatter = null;
	private transient boolean mIsSelected;
	private transient boolean mMouseOver;

	public ProgramLabel(final Program program) {
	  mIsSelected = false;
		addMouseListener(this);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setToolTipText(" ");
		setProgram(program);
		
    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1,1,1,1),
        BorderFactory.createEmptyBorder(1, 5, 1, 5)));
	}
	
	public boolean containsProgram(Program prog) {
	  return mProgram.equals(prog);
	}
	 
  public void setSelected(boolean value) {
    mIsSelected = value;
  }

	private void setProgram(final Program program) {
		mProgram = program;
		new ProgramToolTip(mProgram);
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
	  mMouseOver = true;
	  repaint();
	}

	public void mouseExited(final MouseEvent e) {
	  mMouseOver = false;
	  repaint();
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
	  TimelinePlugin.getInstance().deselectProgram();
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
		Color onAirLight = Plugin.getPluginManager().getTvBrowserSettings().getProgramPanelOnAirLightColor();
		Color onAirDark = Plugin.getPluginManager().getTvBrowserSettings().getProgramPanelOnAirDarkColor();
		
		byte programImportance = ProgramUtilities.getProgramImportance(mProgram);
		
    Color backColor = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(mProgram.getMarkPriority());
    backColor = new Color(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), (int)(backColor.getAlpha()*programImportance/10.));
		
		if (backColor != null) {
			g.setColor(backColor);
			g.fillRect(r.x, r.y, rb.width, r.height);
			g.setColor(oriColor);
		}
		
		TvBrowserSettings tvbSet = Plugin.getPluginManager().getTvBrowserSettings();
		
		Color programTableMouseOverColor = null;
		Color programPanelSelectionColor = null;
		Color foregroundColor = null;
		
		try {
      Method colorMethod = TvBrowserSettings.class.getMethod("getProgramTableMouseOverColor", new Class[0]);
      Object o = colorMethod.invoke(tvbSet, new Object[0]);
      
      if(o instanceof Color) {
        programTableMouseOverColor = (Color)o;
      }
      
      colorMethod = TvBrowserSettings.class.getMethod("getProgramPanelSelectionColor", new Class[0]);
      
      o = colorMethod.invoke(tvbSet, new Object[0]);
      
      if(o instanceof Color) {
        programPanelSelectionColor = (Color)o;
      }
      
      colorMethod = TvBrowserSettings.class.getMethod("getProgramTableForegroundColor", new Class[0]);
      
      o = colorMethod.invoke(tvbSet, new Object[0]);
      
      if(o instanceof Color) {
        foregroundColor = (Color)o;
      }
    } catch (Exception e) {
      foregroundColor = UIManager.getColor("List.foreground");
      programPanelSelectionColor = new Color(130, 255, 0, 120);
    }
    
    if (((programTableMouseOverColor != null && mMouseOver) || (programPanelSelectionColor != null && mIsSelected))
        && foregroundColor != null) {
      Color test = programTableMouseOverColor;
      if (mIsSelected) {
        test = programPanelSelectionColor;
      }
      g.setColor(test);
      g.fillRect(0, 0, rb.width - 1, r.height - 1);
        
      Graphics2D grp = (Graphics2D)g;
      Stroke str = grp.getStroke();
      Color col = grp.getColor();
      float[] dash = { 2.0f };
      int lineWidth = 1;
      BasicStroke dashed = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
      
      grp.setColor(foregroundColor);
      grp.setStroke(dashed);

      grp.drawRect(lineWidth, lineWidth, rb.width - lineWidth - 2, r.height - lineWidth - 2);

      grp.setStroke(str);
      grp.setColor(col);
    }
    else {
      g.drawRect(0, 0, getWidth()-1, getHeight()-1);
    }
		
		if (TimelinePlugin.getSettings().showProgress() && mProgram.isOnAir()) {
			g.setColor(new Color(onAirDark.getRed(), onAirDark.getGreen(), onAirDark.getBlue(), (int)(onAirDark.getAlpha()*programImportance/10.)));
			final int positionX = Math.abs(TimelinePlugin.getInstance()
					.getNowPosition() - rb.x);
			g.fillRect(0, 0, positionX, rb.height);
			g.setColor(new Color(onAirLight.getRed(), onAirLight.getGreen(), onAirLight.getBlue(), (int)(onAirLight.getAlpha()*programImportance/10.)));
			g.fillRect(positionX, 0, rb.width - positionX, rb.height);
			g.setColor(oriColor);
		}
		
    if (mProgram.isExpired() && !Plugin.getPluginManager().getExampleProgram().equals(mProgram)) {
      Color c = new Color(Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue(), (int)(Color.gray.getAlpha()*programImportance/10.));

      setForeground(c);
      g.setColor(c);
    } else {
      Color c = new Color(foregroundColor.getRed(), foregroundColor.getGreen(), foregroundColor.getBlue(), (int)(foregroundColor.getAlpha()*programImportance/10.));

      setForeground(c);
      g.setColor(c);
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

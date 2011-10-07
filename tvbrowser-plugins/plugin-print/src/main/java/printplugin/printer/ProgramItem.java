/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-07-16 17:10:26 +0200 (Sa, 16 Jul 2005) $
 *   $Author: darras $
 * $Revision: 1395 $
 */

package printplugin.printer;

import java.awt.Graphics;

import printplugin.settings.ProgramIconSettings;
import devplugin.Program;




public class ProgramItem implements PositionedIcon {
    private Program mProgram;
    private ProgramIcon mIcon;
    private double mx, my;



    public ProgramItem(Program prog, ProgramIconSettings settings, int columnWidth, boolean showChannelName, boolean showEndtime) {
      mProgram = prog;
      mIcon = new ProgramIcon(prog, settings, columnWidth, showChannelName, showEndtime);
    }
    public ProgramItem(Program prog, ProgramIconSettings settings, int columnWidth, boolean showChannelName) {
      this(prog, settings, columnWidth, showChannelName, false);

    }


    public void setPos(double x, double y) {
      mx=x;
      my=y;
    }
    public double getX() { return mx; }
    public double getY() { return my; }
    public int getHeight() {
      return mIcon.getIconHeight();
    }
    public void setMaximumHeight(int maxHeight) {
      mIcon.setMaximumHeight(maxHeight);
    }
    
    public Program getProgram() { return mProgram; }
    public void paint(Graphics g, int x, int y) {
      mIcon.paintIcon(null, g, x, y);
    }
    
    public void paint(Graphics g) {
      paint(g, (int)getX(), (int)getY());
    }
    
  }
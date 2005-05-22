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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package printplugin.printer.queueprinter;

import printplugin.printer.ProgramItem;
import printplugin.settings.ProgramIconSettings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import devplugin.Program;


public class ProgramTableIcon implements Icon {


  private int mWidth;
  private int mHeight;
  private int mCurColumnInx;
  private int mCurY;
  private int mNumOfCols;
  private ArrayList mPrograms;
  private ProgramIconSettings mProgramIconSettings;


  public ProgramTableIcon(ProgramIconSettings settings, int width, int height, int numOfCols) {
    mProgramIconSettings = settings;
    mWidth = width;
    mHeight = height;
    mNumOfCols = numOfCols;
    mCurColumnInx = 0;
    mCurY = 0;
    mPrograms = new ArrayList();
  }

  public boolean add(Program prog, boolean forceAdding) {
    ProgramItem item = new ProgramItem(prog, mProgramIconSettings, mWidth/mNumOfCols-10, true);
    item.setMaximumHeight(200);

    boolean canAdd = false;
    if (forceAdding) {
      canAdd = true;
    }
    else if (mCurY + item.getHeight() < mHeight) {
      canAdd = true;
    }
    else if (mCurColumnInx+1 < mNumOfCols) {
      mCurColumnInx++;
      mCurY = 0;
      canAdd = true;
    }

    if (canAdd) {
      mPrograms.add(item);
      item.setPos(mWidth/mNumOfCols * mCurColumnInx, mCurY);
      mCurY +=item.getHeight() + mProgramIconSettings.getTitleFont().getSize()/2;
      return true;
    }

    return false;
  }

  public int getIconHeight() {
    return mHeight;
  }

  public int getIconWidth() {
    return mWidth;
  }

  public void paintIcon(Component c, Graphics graphics, int x, int y) {
    for (int i=0; i<mPrograms.size(); i++) {
      ProgramItem item = (ProgramItem)mPrograms.get(i);
      item.paint(graphics, (int)(x+item.getX()), (int)(y+item.getY()));
    }

    graphics.setColor(Color.lightGray);
    for (int i=0; i<mNumOfCols-1; i++) {
      int x0 = mWidth/mNumOfCols*(i+1)+x;
      graphics.drawLine(x0, 0+y, x0, mHeight+y);
    }

  }

}

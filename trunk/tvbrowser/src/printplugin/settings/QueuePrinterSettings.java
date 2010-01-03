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

package printplugin.settings;

import java.awt.Font;


public class QueuePrinterSettings implements Settings {

  private boolean mEmptyQueueAfterPrinting;
  private int mColumnsPerPage;
  private ProgramIconSettings mProgramIconSettings;
  private Font mDateFont;

  public QueuePrinterSettings(boolean emptyQueueAfterPrinting, int columnsPerPage, ProgramIconSettings programIconSettings, Font dateFont) {
    mEmptyQueueAfterPrinting = emptyQueueAfterPrinting;
    mColumnsPerPage = columnsPerPage;
    mProgramIconSettings = programIconSettings;
    mDateFont = dateFont;
  }

  public boolean emptyQueueAfterPrinting() {
    return mEmptyQueueAfterPrinting;
  }

  public int getColumnsPerPage() {
    return mColumnsPerPage;
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramIconSettings;
  }

  public Font getDateFont() {
    return mDateFont;
  }
}
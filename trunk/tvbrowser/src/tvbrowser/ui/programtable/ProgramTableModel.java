/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.programtable;

import util.ui.ProgramPanel;
import devplugin.Channel;
import devplugin.ChannelFilter;
import devplugin.Date;
import devplugin.ProgramFilter;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public interface ProgramTableModel {
  
  public void addProgramTableModelListener(ProgramTableModelListener listener);
  
  public Channel[] getShownChannels();
  
  public Date getDate();
  
  public int getColumnCount();
  
  public int getRowCount(int col);
  
  public void setProgramFilter(ProgramFilter filter);
  
  /**
   * set the active channel filter
   * @param channelFilter
   * @since 3.4.1
   */
  public void setChannelFilter(ChannelFilter channelFilter);

  public ProgramPanel getProgramPanel(int col, int row);
}

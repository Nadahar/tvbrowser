/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package devplugin;

import javax.swing.JPanel;

/**
 * A panel with informations that should
 * be shown by a {@link Plugin} after a data update.
 * <p>
 * @author René Mach
 * @since 3.2
 */
public abstract class AfterDataUpdateInfoPanel extends JPanel {
  private AfterDataUpdateInfoPanelListener mListener;
  
  public final void setAfterDataUpdateInfoPanelListener(AfterDataUpdateInfoPanelListener listener) {
    mListener = listener;
  }
  
  /**
   * Call this method to let this Panel
   * be removed from the info dialog.
   */
  public final void removeMe() {
    mListener.remove(this);
  }
  
  /**
   * Is called by the info window
   * when the window was closed.
   */
  public abstract void closed();
  
  /**
   * A listener for removing of AfterDataUpdateInfoPanels from a component.
   * <p>
   * @author René Mach
   */
  public interface AfterDataUpdateInfoPanelListener {
    public void remove(AfterDataUpdateInfoPanel infoPanel);
  }
}

/*
 * TV-Browser
 * Copyright (C) 2015 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2003-12-30 17:07:09 +0100 (Di, 30 Dez 2003) $
 *   $Author: darras $
 * $Revision: 311 $
 */
package devplugin;

/**
 * Allows to also set the indeterminate state of the progress bar.
 * 
 * @author René Mach
 * @since 3.4.2
 */
public interface ProgressMonitorExtended extends ProgressMonitor {
  
  public void setIndeterminate(boolean newValue);
  
  public void setVisible(boolean newValue);
}

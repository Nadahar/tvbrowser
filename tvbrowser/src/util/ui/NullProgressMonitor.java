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
 *     $Date: 2006-05-01 16:36:15 +0200 (Mon, 01 May 2006) $
 *   $Author: darras $
 * $Revision: 2315 $
 */

package util.ui;

import devplugin.ProgressMonitor;

public class NullProgressMonitor implements ProgressMonitor {

  public void setMaximum(int maximum) {
  }

  public void setValue(int value) {
  }

  public void setMessage(String msg) {
  }
}

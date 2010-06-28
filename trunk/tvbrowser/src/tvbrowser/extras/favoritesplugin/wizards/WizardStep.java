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

package tvbrowser.extras.favoritesplugin.wizards;

import javax.swing.JPanel;

public interface WizardStep {

  public static final int BUTTON_NEXT = 1;

  public static final int BUTTON_DONE = 2;

  public static final int BUTTON_BACK = 3;

  public static final int BUTTON_CANCEL = 4;

  public int[] getButtons();

  public String getTitle();

  public JPanel getContent(WizardHandler handler);

  public Object createDataObject(Object obj);

  public WizardStep next();

  public WizardStep back();

  public boolean isValid();

  public String getDoneBtnText();

  /**
   * @return true, if this Step is a Single Step and no Back/Forward is needed
   */
  public boolean isSingleStep();
}

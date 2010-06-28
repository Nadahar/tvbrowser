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
 */
package devplugin;

import javax.swing.JPanel;

import tvbrowser.core.filters.FilterComponent;


/**
 * Overwrite this class to support filter
 * components for your plugin.<br>
 * Implementation note: Your class must be public, so it can be constructed by reflection.
 *
 * @author René Mach
 */
public abstract class PluginsFilterComponent implements FilterComponent {
  private String mName;
  private String mDescription;

  /**
   * Gets the name of this filter component.
   *
   * @return The name of this filter compoent.
   */
  public final String getName() {
    return mName;
  }

  /**
   * Gets the description of this filter component.
   *
   * @return The description of this filter component.
   */
  public final String getDescription() {
    return mDescription;
  }

  /**
   * Sets the name of this filter component.
   *
   * @param name The new name of this filter component.
   */
  public final void setName(String name) {
    mName = name;
  }

  /**
   * Sets the description of this filter component.
   *
   * @param desc The new description of this filter component.
   */
  public final void setDescription(String desc) {
    mDescription = desc;
  }

  /**
   * Gets the settings panel for this filter component.
   *
   * @return The settings panel for this filter component.
   */
  public JPanel getSettingsPanel() {
    return new JPanel();
  }

    /**
     * Is called when the settings should be saved (aka the users pressed the OK-Button in the Settings Dialog)
     */
  public void saveSettings() {
  }

  /**
   * Returns the user presentable name of this class.
   * Don't return <code>null</code>, return always a
   * clear name that discribes this filter component
   * for selection in the filter component dialog.
   *
   * @return The user presentable name of this class.
   */
  public abstract String getUserPresentableClassName();

  public final String toString() {
    return getUserPresentableClassName();
  }
}

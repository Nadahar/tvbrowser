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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

import devplugin.Program;

public class AcceptNoneFilterComponent extends AbstractFilterComponent {
  public AcceptNoneFilterComponent(String name) {
    super(name, "");
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public boolean accept(Program program) {
    return false;
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    // TODO Auto-generated method stub

  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {}

  @Override
  public JPanel getSettingsPanel() {
    return null;
  }

  @Override
  public void saveSettings() {}
}

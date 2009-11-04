/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package tvbrowser.extras.common;

import devplugin.Program;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

public abstract class AbstractInternalPluginProxy implements InternalPluginProxyIf, ProgramReceiveIf {

  @Override
  public int compareTo(ProgramReceiveIf other) {
    if (other instanceof InternalPluginProxyIf) {
      return getName().compareTo(((InternalPluginProxyIf) other).getName());
    }
    return getName().compareTo(other.toString());
  }
  
  @Override
  public boolean canReceiveProgramsWithTarget() {
    return false;
  }
  
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return null;
  }
  
  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    return false;
  }
  
  @Override
  public boolean receiveValues(String[] values, ProgramReceiveTarget receiveTarget) {
    return false;
  }
}

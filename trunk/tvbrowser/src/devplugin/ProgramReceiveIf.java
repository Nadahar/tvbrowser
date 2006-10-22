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
 *     $Date: 2005-08-20 17:18:20 +0200 (Sa, 20 Aug 2005) $
 *   $Author: darras $
 * $Revision: 1443 $
 */
package devplugin;

/**
 * A interface for a object that supports receiving programs.
 * 
 * @author Ren� Mach
 *
 */
public interface ProgramReceiveIf {
  
  /**
   * Gets whether the ProgramReceiveIf supports receiving programs from other plugins.
   * 
   * @return Whether the ProgramReceiveIf supports receiving programs from other plugins.
   * 
   * @see #receivePrograms(Program[])
   * @deprecated Since 2.5 Use {@link #canReceiveProgramsWithTarget()} instead.
   */
  public boolean canReceivePrograms();  
  
  /**
   * Receives a list of programs from another plugin.
   * 
   * @param programArr The programs passed from the other plugin.
   * 
   * @see #canReceivePrograms()
   * @deprecated Since 2.5 Use {@link #receivePrograms(Program[],ProgramReceiveTarget)} instead.
   */
  public void receivePrograms(Program[] programArr);
  
  /**
   * Gets whether the ProgramReceiveIf supports receiving programs from other plugins with a special target.
   * 
   * @return Whether the ProgramReceiveIf supports receiving programs from other plugins with a special target.
   * 
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */  
  public boolean canReceiveProgramsWithTarget();
  
  /**
   * Receives a list of programs from another plugin with a target.
   * 
   * @param programArr The programs passed from the other plugin.
   * @param receiveTarget The receive target of the programs.
   * @return If the programs were correct received (the target really exists).
   * 
   * @see #canReceiveProgramsWithTarget()
   * @since 2.5
   */
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget);

  /**
   * Returns an array of receive target or <code>null</code> if there is no target
   * 
   * @return The supported receive targets.
   * @see #canReceiveProgramsWithTarget()
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  public ProgramReceiveTarget[] getProgramReceiveTargets();
  
  public String getId();
}

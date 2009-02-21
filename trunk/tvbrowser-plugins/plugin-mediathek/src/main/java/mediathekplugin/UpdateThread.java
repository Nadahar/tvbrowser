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
package mediathekplugin;

import java.util.ArrayList;

public final class UpdateThread extends Thread {
  private static UpdateThread instance = null;

  private ArrayList<MediathekProgram> updatePrograms;

  private UpdateThread() {
    super("Fetch Mediathek contents");
    updatePrograms = new ArrayList<MediathekProgram>();
  }

  public static synchronized UpdateThread getInstance() {
    if (instance == null) {
      instance = new UpdateThread();
    }
    return instance;
  }

  public void addProgram(final MediathekProgram program) {
    // check if we already read this program
    if (program.getItemCount() >= 0) {
      return;
    }
    if (!program.canReadEpisodes()) {
      return;
    }
    updatePrograms.add(program);
    program.initializeItems();
    if (!isAlive()) {
      start();
    }
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      if (!updatePrograms.isEmpty()) {
        final MediathekProgram program = updatePrograms.remove(0);
        program.parseEpisodes(this);
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        return;
      }
    }
  }
}

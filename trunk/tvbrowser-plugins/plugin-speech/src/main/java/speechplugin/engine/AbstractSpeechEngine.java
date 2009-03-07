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
package speechplugin.engine;

import java.util.List;

public abstract class AbstractSpeechEngine {
  public void initialize() {
    // empty initialization
  }

  public abstract List<String> getVoices();

  public abstract void setVoice(String voiceName);

  public abstract void speak(String text);

  public abstract void stopSpeaking();

  public abstract boolean isSpeaking();

  public void shutdown() {
    // empty shutdown
  }
}

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

import speechplugin.SpeechParamLibrary;
import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.paramhandler.ParamParser;
import util.ui.Localizer;

public class CommandLineSpeechEngine extends AbstractSpeechEngine {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(CommandLineSpeechEngine.class);

  private static final java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(CommandLineSpeechEngine.class.getName());

  private String mExecutable;
  private String mParameters;

  public CommandLineSpeechEngine(final String executable,
      final String parameters) {
    super();
    mExecutable = executable.trim();
    mParameters = parameters;
  }

  @Override
  public List<String> getVoices() {
    // no voice selection for command line
    return null;
  }

  @Override
  public boolean isSpeaking() {
    // cannot stop, so assume not speaking
    return false;
  }

  @Override
  public void setVoice(final String voiceName) {
    // empty, no voice selection
  }

  @Override
  public void speak(final String text) {
    final ParamParser parser = new ParamParser(new SpeechParamLibrary(text));
    final String parsedParams = parser.analyse(mParameters, null);

    if (!mExecutable.equals("")) {
      try {
        final ExecutionHandler executionHandler = new ExecutionHandler(
            parsedParams, mExecutable);
        executionHandler.execute();
      } catch (Exception exc) {
        final String msg = mLocalizer.msg("error",
            "Error executing program ({0})", mExecutable, exc);
        ErrorHandler.handle(msg, exc);
      }
    } else {
      mLog.warning("Executable program name is not defined!");
    }
  }

  @Override
  public void stopSpeaking() {
    // not available
  }
}

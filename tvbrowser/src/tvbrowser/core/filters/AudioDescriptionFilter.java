package tvbrowser.core.filters;

import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * This Filter filters Movies that have audio description for Handicaped Persons
 */
public class AudioDescriptionFilter implements ProgramFilter {
  /**
   * Localizer
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(AudioDescriptionFilter.class);

  /**
   * Accept only Programs with Audio Description
   *
   * @param prog Program to check
   * @return true if prog has audio description
   */
  public boolean accept(devplugin.Program prog) {
    int info = prog.getInfo();

    return info >= 1 && (bitSet(info, Program.INFO_AUDIO_DESCRIPTION));
  }

  /**
   * Checks if bits are set
   *
   * @param num     check in here
   * @param pattern this pattern
   * @return Pattern set?
   */
  private boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

  public String getName() {
    return toString();
  }

  /**
   * Name of Filter
   */
  public String toString() {
    return mLocalizer.msg("Audiodescription", "Audio description");
  }

  public boolean equals(Object o) {
    return o instanceof ProgramFilter && getClass().equals(o.getClass())
        && getName().equals(((ProgramFilter) o).getName());
  }

}

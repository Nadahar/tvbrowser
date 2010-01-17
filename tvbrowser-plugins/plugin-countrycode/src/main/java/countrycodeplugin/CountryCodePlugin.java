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
package countrycodeplugin;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public final class CountryCodePlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 6);

  private PluginInfo mPluginInfo;

  private Pattern mPattern;

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(CountryCodePlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Country code");
      final String desc = mLocalizer.msg("description",
        "Replaces country codes in the description by country names." );
      mPluginInfo = new PluginInfo(CountryCodePlugin.class, name, desc, "Michael Keppler",
          "GPL 3");
    }
    
    return mPluginInfo;
  }

  @Override
  public void handleTvDataAdded(final MutableChannelDayProgram newProg) {
    final Iterator<Program> iterator = newProg.getPrograms();
    if (iterator != null) {
      while (iterator.hasNext()) {
        final MutableProgram program = (MutableProgram) iterator.next();
        final String origin = program
            .getTextField(ProgramFieldType.ORIGIN_TYPE);
        if (origin != null) {
          final StringBuilder builder = new StringBuilder();
          final Matcher matcher = mPattern.matcher(origin);
          int start = 0;
          int end = 0;
          while (matcher.find()) {
            start = matcher.start(1);
            if (start > 0) {
              builder.append(origin.substring(end, start));
            }
            end = matcher.end(1);
            final String code = matcher.group();
            final String replacement = mLocalizer.msg("code." + code, "");
            if (replacement.length() > 0 && replacement.indexOf('#') < 0) {
              builder.append(replacement);
            }
            else {
              builder.append(code);
            }
          }
          if (end < origin.length()) {
            builder.append(origin.substring(end, origin.length()));
          }
          final String replaced = builder.toString();
          if (!replaced.equals(origin)) {
            program.setTextField(ProgramFieldType.ORIGIN_TYPE, replaced);
          }
        }
      }
    }
  }

  @Override
  public void onActivation() {
    // cache regular expression
    if (mPattern == null) {
      mPattern = Pattern.compile("\\b([A-Z]{1,3})\\b");
    }
  }

}

/*
 * CountryCodePlugin by Michael Keppler
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
 * VCS information:
 *     $Date$
 *   $Author$
 * $Revision$
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

public class CountryCodePlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 2);

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
      String name = mLocalizer.msg("name", "Country code");
      String desc = mLocalizer.msg("description",
        "Replaces country codes in the description by country names." );
      String author = "Michael Keppler" ;
      
      mPluginInfo = new PluginInfo(CountryCodePlugin.class, name, desc, author);
    }
    
    return mPluginInfo;
  }

  @Override
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    Iterator<Program> iterator = newProg.getPrograms();
    if (iterator != null) {
      while (iterator.hasNext()) {
        MutableProgram program = (MutableProgram) iterator.next();
        String origin = program.getTextField(ProgramFieldType.ORIGIN_TYPE);
        if (origin != null) {
          StringBuilder builder = new StringBuilder();
          Matcher matcher = mPattern.matcher(origin);
          int start = 0;
          int end = 0;
          while (matcher.find()) {
            start = matcher.start(1);
            if (start > 0) {
              builder.append(origin.substring(end, start));
            }
            end = matcher.end(1);
            String code = matcher.group();
            String replacement = mLocalizer.msg("code." + code, "");
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
          String replaced = builder.toString();
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

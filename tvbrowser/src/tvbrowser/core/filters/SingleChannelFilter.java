/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.core.filters;

import java.io.IOException;
import java.io.ObjectOutputStream;

import devplugin.Channel;
import devplugin.Program;
import devplugin.ProgramFilter;

public class SingleChannelFilter implements ProgramFilter {
  private Channel mChannel;
  
  public SingleChannelFilter(Channel channel) {
    mChannel = channel;
  }
  
  @Override
  public boolean accept(Program program) {
    if(mChannel != null) {
      return program.getChannel().equals(mChannel);
    }
    return false;
  }

  @Override
  public String getName() {
    if(mChannel != null) {
      return mChannel.getName() + "*";
    }
    
    return "[INVALID]";
  }

  public void store(String id, ObjectOutputStream out) throws IOException {
    if(mChannel != null) {
      out.writeUTF(id);
      mChannel.writeData(out);
    }
  }
  
  public boolean isValidChannel() {
    return mChannel != null;
  }
  
  public boolean containsChannel(Channel ch) {
    return mChannel != null && mChannel.equals(ch);
  }
}

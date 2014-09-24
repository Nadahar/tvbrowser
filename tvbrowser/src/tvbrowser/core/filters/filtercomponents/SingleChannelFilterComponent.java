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
package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

import tvbrowser.core.filters.FilterComponent;
import devplugin.Channel;
import devplugin.Program;

public class SingleChannelFilterComponent implements FilterComponent {
  private Channel mChannel;
  
  public SingleChannelFilterComponent(Channel ch) {
    mChannel = ch;
  }
  
  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public boolean accept(Program program) {
    return mChannel != null ? program.getChannel().equals(mChannel) : false;
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    if(in.readBoolean()) {
      mChannel = Channel.readData(in, true);
    }
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    out.writeBoolean(mChannel != null);
    
    if(mChannel != null) {
      mChannel.writeData(out);
    }
  }

  @Override
  public JPanel getSettingsPanel() {
    // has no settings and cannot be edited
    return null;
  }

  @Override
  public void saveSettings() {
    // Do nothing
  }

  @Override
  public String getName() {
    if(mChannel != null) {
      return "_" + mChannel.getName().replaceAll("\\s+|\\p{Punct}", "_");
    }
    
    return "[INVALID]";
  }

  @Override
  public String getTypeDescription() {
    return "";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public void setName(String name) {
    // Do nothing
  }

  @Override
  public void setDescription(String desc) {
    // Do nothing
  }
  
  public boolean isValidChannel() {
    return mChannel != null;
  }
  
  public boolean containsChannel(Channel ch) {
    return mChannel != null && mChannel.equals(ch);
  }
}

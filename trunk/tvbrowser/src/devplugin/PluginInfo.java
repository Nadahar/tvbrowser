/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package devplugin;

/**
 * This class provides information about your plugin.
 */

public final class PluginInfo {

    private Version version=null;
    private String name="";
    private String description="";
    private String author="";
    private String license=null;

    public PluginInfo() {
    }

    public PluginInfo(String name) {
        this.name=name;
    }

    public PluginInfo(String name, String desc) {
       this(name);
       this.description=desc;
    }

    public PluginInfo(String name, String desc, String author) {
        this(name,desc);
        this.author=author;
    }

    public PluginInfo(String name, String desc, String author, Version version) {
        this(name,desc,author);
        this.version=version;
    }
    
    public PluginInfo(String name, String desc, String author, Version version, String license) {
      this(name,desc,author,version);
      this.license=license;
    }

    public Version getVersion() { return version; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getLicense() { return license; }

}
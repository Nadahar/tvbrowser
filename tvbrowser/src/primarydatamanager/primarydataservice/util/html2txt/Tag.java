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
 *     $Date$
 *   $Author$
 * $Revision$
 */


package primarydatamanager.primarydataservice.util.html2txt;

/**
 * a tag (e.g. html tag) or text node.
 */
public interface Tag {

  /**
   * @return true, if it is a tag - false, if it is a text node
   */
  boolean isTextTag();

  /**
   * @return the contents of a tag (i.e. everything between &lt; and &gt;) or a text node. the contents will be unescaped.
   */
  String getName();

  /**
   * @return the name of the tag (lowercase, without attributes) or "" for text nodes
   */
  String getTagName();

  /**
   * @param attributeName the name of the attribute to get
   * @return the attribute value or null, if no attribute with this name was found
   */
  String getAttribute(String attributeName);
}

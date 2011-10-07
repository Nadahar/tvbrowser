/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */

package printplugin.printer;

import java.util.ArrayList;


public abstract class AbstractPageModel implements PageModel {
  
  private ArrayList<ColumnModel> mColumns;

   public AbstractPageModel() {
     mColumns = new ArrayList<ColumnModel>();
   }
  
   public void addColumn(ColumnModel col) {
     mColumns.add(col);
   }
  
   public int getColumnCount() {
     return mColumns.size();
   }

  
   public ColumnModel getColumnAt(int inx) {
     return mColumns.get(inx);
   }

   public String getFooter() {
     return "Copyright (c) by TV-Browser - http://www.tvbrowser.org";
   }
  
  
}
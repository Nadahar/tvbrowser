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
package tvbrowser.core.search.booleansearch;

/**
 * Implementiert logische und-Verknüpfung zwischen 2 Blocks
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public class And implements Block {

  Block block1;

  Block block2;


  public And(Block b1, Block b2) {
    block1 = b1;
    block2 = b2;
  }


  public boolean test(String s) {
    return block1.test(s) && block2.test(s);
  }


  /**
   * Die Optimierung testet ob: - einer der Unterblocks auch ein AND ist. Dann
   * wird das AND oder ein multiAnd ersetzt - Ruft die Optimierung der
   * Unterblock auf - Wenn ein Block der Klasse matcher dabei ist wird dieser
   * zuerst getestet - Wenn beide Blocks StringCompare implementieren wird der
   * längere nach vorne sortiert.
   */

  public Block finish() {
    if (block1 instanceof And) {
      MultiAnd MA = new MultiAnd((And) block1, block2);
      return MA.finish();
    }
    if (block2 instanceof And) {
      MultiAnd MA = new MultiAnd((And) block2, block1);
      return MA.finish();
    }
    block1 = block1.finish();
    block2 = block2.finish();
    if ((block2 instanceof Matcher) && (!(block1 instanceof Matcher))) {
      Block b = block1;
      block1 = block2;
      block2 = b;
    }
    if ((block2 instanceof StringCompare) && (block1 instanceof StringCompare)) {
      StringCompare m1 = (StringCompare) block1;
      StringCompare m2 = (StringCompare) block2;
      if (m1.size() < m2.size()) {
        Block b = block1;
        block1 = block2;
        block2 = b;
      }
    }
    return this;
  }


  /*
   * Gibt (Unterblock1 AND Unterblock2) zurück.
   */
  public String toString() {
    return "(" + block1.toString() + " AND " + block2.toString() + ")";
  }
}
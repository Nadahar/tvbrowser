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

import java.util.Vector;

/**
 * Implementiert OR-Ketten wie: Wort OR Wort OR Wort
 * 
 * Wird eingesetzt um Konstruktion a la: Wort OR (Wort OR (Wort OR Wort)) zu
 * vermeiden
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public class MultiOr implements Block {

  private Block[] blocks;


  public MultiOr(Or o, Block b2) {
    blocks = new Block[3];
    blocks[0] = b2;
    blocks[1] = o.block1;
    blocks[2] = o.block2;
  }


  public boolean test(String s) {
    for (int i = 0; i < blocks.length; i++) {
      if (blocks[i].test(s)) {
        return true;
      }
    }
    return false;
  }


  public Block finish() {

    //zuerst alle ORs unterhalb einsammeln
    Vector<Block> blocksVector = new Vector<Block>();
    for (int i = 0; i < blocks.length; i++) {
      blocksVector.add(blocks[i]);
    }

    boolean found = true;
    do {
      found = false;
      for (int i = 0; i < blocksVector.size(); i++) {
        Object O = blocksVector.get(i);
        if (O instanceof Or) {
          Or OR = (Or) O;
          blocksVector.set(i, OR.block1);
          i--;
          blocksVector.add(OR.block2);
        }
      }
    } while (found);

    Vector<Block> v = new Vector<Block>();
    for (int i = 0; i < blocksVector.size(); i++) {
      Block BB = blocksVector.get(i);
      BB = BB.finish();
      if (!v.contains(BB)) {
        v.add(BB);
      }
    }
    blocksVector = null;
    blocks = v.toArray(new Block[v.size()]);
    v = null;

    //dann die matcher nach vorne sortieren
    while (found) {
      found = false;
      for (int i = 1; i < blocks.length; i++) {
        if ((blocks[i] instanceof StringCompare)
            && (!(blocks[i - 1] instanceof StringCompare))) {
          Block Btemp = blocks[i];
          blocks[i] = blocks[i - 1];
          blocks[i - 1] = Btemp;
          found = true;
        }
      }
    }
    //dann die matcher nach der Länge sortieren.
    found = true;
    while (found) {
      found = false;
      for (int i = 1; i < blocks.length; i++) {
        if ((blocks[i] instanceof StringCompare)
            && ((blocks[i - 1] instanceof StringCompare))) {
          StringCompare m1 = (StringCompare) blocks[i - 1];
          StringCompare m2 = (StringCompare) blocks[i];
          if (m1.size() > m2.size()) {
            Block Btemp = blocks[i];
            blocks[i] = blocks[i - 1];
            blocks[i - 1] = Btemp;
            found = true;
          }
        } else {
          //Da kommen keine matcher mehr
          break;
        }
      }
    }
    return this;
  }


  public String toString() {
    String temp = "(" + blocks[0];
    for (int i = 1; i < blocks.length; i++) {
      temp += " OR " + blocks[i].toString();
    }
    return temp + ")";
  }
}
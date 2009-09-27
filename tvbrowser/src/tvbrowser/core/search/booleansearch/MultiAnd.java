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
 * Implementiert AND-Ketten wie: Wort AND Wort AND Wort
 * 
 * Wird eingesetzt um Konstruktion a la: Wort AND (Wort AND (Wort AND Wort)) zu
 * vermeiden
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public class MultiAnd implements Block {

  private Block[] blocks;


  public MultiAnd(And a, Block b2) {
    blocks = new Block[3];
    blocks[0] = b2;
    blocks[1] = a.block1;
    blocks[2] = a.block2;
  }


  public boolean test(String s) {
    for (int i = 0; i < blocks.length; i++) {
      if (!blocks[i].test(s)) {
        return false;
      }
    }
    return true;
  }


  public Block finish() {
    //zuerst alle ANDs unterhalb einsammeln
    Vector<Block> blocksVector = new Vector<Block>();
    for (int i = 0; i < blocks.length; i++) {
      blocksVector.add(blocks[i]);
    }

    boolean found = true;
    do {
      found = false;
      for (int i = 0; i < blocksVector.size(); i++) {
        Object O = blocksVector.get(i);
        if (O instanceof And) {
          And AND = (And) O;
          blocksVector.set(i, AND.block1);
          i--;
          blocksVector.add(AND.block2);
          found = true;
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
    found = true;
    while (found) {
      found = false;
      for (int i = 1; i < blocks.length; i++) {
        if ((blocks[i] instanceof Matcher)
            && (!(blocks[i - 1] instanceof Matcher))) {
          Block Btemp = blocks[i];
          blocks[i] = blocks[i - 1];
          blocks[i - 1] = Btemp;
          found = true;
        }
      }
    }
    //dann die matcher nach der L?nge sortieren.
    found = true;
    while (found) {
      found = false;
      for (int i = 1; i < blocks.length; i++) {
        if ((blocks[i] instanceof Matcher)
            && ((blocks[i - 1] instanceof Matcher))) {
          Matcher m1 = (Matcher) blocks[i - 1];
          Matcher m2 = (Matcher) blocks[i];
          if (m1.size() < m2.size()) {
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
    StringBuilder temp = new StringBuilder(100);
    temp.append('(').append(blocks[0]);
    for (int i = 1; i < blocks.length; i++) {
      temp.append(" AND ").append(blocks[i].toString());
    }
    temp.append(')');
    return temp.toString();
  }
}
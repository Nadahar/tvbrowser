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

package tvbrowser.ui.configassistant;

import javax.swing.JPanel;

abstract class AbstractCardPanel implements CardPanel {
  private CardPanel mNext, mPrev;
  private PrevNextButtons mBtns;
  
  protected AbstractCardPanel(PrevNextButtons btns) {
    mBtns=btns;
  }
  
  public abstract JPanel getPanel();
  public boolean onNext() {
    return true;
  }
  public boolean onPrev() {
    return true;
  }
  public void onShow() {
    if (hasNext()) {
      mBtns.enableNextButton();
    } else {
      mBtns.disableNextButton();
    }
      
    if (hasPrev()) {
      mBtns.enablePrevButton();
    } else {
      mBtns.disablePrevButton();
    }
  }
  
  public CardPanel getNext() {
    return mNext;
  }
  public CardPanel getPrev() {
    return mPrev;
  }
  public boolean hasNext() {
    return mNext!=null;
  }
  public boolean hasPrev() {
    return mPrev!=null;
  }
  public void setNext(CardPanel next) {
    mNext=next;
    next.setPrev(this);
  }
  public void setPrev(CardPanel prev) {
    mPrev=prev;
  }
  
  
}

/* ====================================================================
 *
 * Skin Look And Feel 1.2.3 License.
 *
 * Copyright (c) 2000-2002 L2FProd.com.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by L2FProd.com
 *        (http://www.L2FProd.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Skin Look And Feel", "SkinLF" and "L2FProd.com" must not
 *    be used to endorse or promote products derived from this software
 *    without prior written permission. For written permission, please
 *    contact info@L2FProd.com.
 *
 * 5. Products derived from this software may not be called "SkinLF"
 *    nor may "SkinLF" appear in their names without prior written
 *    permission of L2FProd.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL L2FPROD.COM OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package com.l2fprod.gui.event;

import java.awt.event.WindowEvent;

import com.l2fprod.gui.SkinWindow;

/**
 * SkinWindowEvent. <br>
 * Created on 08/06/2000 by Frederic Lavigne, fred@L2FProd.com
 *
 * @author    $Author$
 * @created   27 avril 2002
 * @version   $Revision$, $Date$
 */
public class SkinWindowEvent extends WindowEvent {

  /**
   * Description of the Field
   */
  public final static int SKINWINDOW_FIRST = 19750;

  /**
   * Description of the Field
   */
  public final static int WINDOW_SHADE = 19750;
  /**
   * Description of the Field
   */
  public final static int WINDOW_UNSHADE = 19751;

  /**
   * Description of the Field
   */
  public final static int WINDOW_MAXIMIZE = 19752;
  /**
   * Description of the Field
   */
  public final static int WINDOW_UNMAXIMIZE = 19753;

  /**
   * Description of the Field
   */
  public final static int SKINWINDOW_LAST = 19753;

  /**
   * Constructor for the SkinWindowEvent object
   *
   * @param window  Description of Parameter
   * @param id      Description of Parameter
   */
  public SkinWindowEvent(SkinWindow window, int id) {
    super(window, id);
  }

  /**
   * Description of the Method
   *
   * @return   Description of the Returned Value
   */
  public String paramString() {
    String typeStr;
    switch (id) {
      case WINDOW_SHADE:
        typeStr = "WINDOW_SHADE";
        break;
      case WINDOW_UNSHADE:
        typeStr = "WINDOW_UNSHADE";
        break;
      case WINDOW_MAXIMIZE:
        typeStr = "WINDOW_MAXIMIZE";
        break;
      case WINDOW_UNMAXIMIZE:
        typeStr = "WINDOW_UNMAXIMIZE";
        break;
      default:
        typeStr = super.paramString();
    }
    return typeStr;
  }

}

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

import javax.swing.*;
import tvbrowser.ui.settings.ProxySettingsTab;

class ProxyCardPanel extends AbstractCardPanel {

  private ProxySettingsTab mProxySettingsTab;
  private JPanel mContent;
  private CardPanel mPrev, mNext;
  
  public ProxyCardPanel(PrevNextButtons btns) {
    super(btns);
    mProxySettingsTab=new ProxySettingsTab();
    mContent=mProxySettingsTab.createSettingsPanel();
  }

  public JPanel getPanel() {
    return mContent;
  }

  public boolean onNext() {
    mProxySettingsTab.saveSettings();
	tvbrowser.TVBrowser.updateProxySettings();
    return true;
  }
  
  
}

/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package tvbrowser.ui.filter.dlgs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import util.browserlauncher.Launch;
import util.ui.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder2;

public class Utilities {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(Utilities.class);

  static ButtonBarBuilder2 createFilterButtonBar() {
    ButtonBarBuilder2 bottomBar = new ButtonBarBuilder2();

    JButton helpButton = new JButton(Localizer.getLocalization(Localizer.I18N_HELP));
    helpButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Launch.openURL(mLocalizer.msg("helpUrl", "http://enwiki.tvbrowser.org/index.php/Filters"));
      }
    });
    bottomBar.addButton(helpButton);
    bottomBar.addGlue();
    return bottomBar;
  }
}

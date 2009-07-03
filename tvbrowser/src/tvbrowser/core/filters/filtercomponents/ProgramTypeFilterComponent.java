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
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import util.ui.customizableitems.SelectableItemList;
import devplugin.Channel;
import devplugin.Program;

/**
 * filter component to filter different channel program categories (e.g. pay tv,
 * radio, cinema...)
 * 
 * @author Bananeweizen
 * 
 */
public class ProgramTypeFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramTypeFilterComponent.class);

  private static final int[] CATEGORIES = { Channel.CATEGORY_TV,
      Channel.CATEGORY_RADIO, Channel.CATEGORY_CINEMA, Channel.CATEGORY_EVENTS,
      Channel.CATEGORY_DIGITAL, Channel.CATEGORY_SPECIAL_MUSIC,
      Channel.CATEGORY_SPECIAL_SPORT, Channel.CATEGORY_SPECIAL_NEWS,
      Channel.CATEGORY_SPECIAL_OTHER, Channel.CATEGORY_PAY_TV,
      Channel.CATEGORY_PAYED_DATA_TV };

  private SelectableItemList mList;
  private int mCategories;

  public ProgramTypeFilterComponent(final String name, final String description) {
    super(name, description);
  }

  public ProgramTypeFilterComponent() {
    this("", "");
  }

  @Override
  public boolean accept(final Program program) {
    return (program.getChannel().getCategories() & mCategories) == mCategories;
  }

  @Override
  public JPanel getSettingsPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    ArrayList<String> allCategories = new ArrayList<String>(CATEGORIES.length);
    ArrayList<String> checkedCategories = new ArrayList<String>();
    for (int category : CATEGORIES) {
      final String name = Channel.getLocalizedCategory(category);
      allCategories.add(name);
      if ((category & mCategories) == category) {
        checkedCategories.add(name);
      }
    }
    mList = new SelectableItemList(checkedCategories.toArray(), allCategories
        .toArray());
    panel.add(mList, BorderLayout.CENTER);
    return panel;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public void read(final ObjectInputStream in, final int version)
      throws IOException, ClassNotFoundException {
    try {
      mCategories = in.readInt();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void saveSettings() {
    mCategories = 0;
    for (Object object : mList.getSelection()) {
      String name = (String) object;
      for (int category : CATEGORIES) {
        if (Channel.getLocalizedCategory(category).equals(name)) {
          mCategories |= category;
        }
      }
    }
  }

  @Override
  public void write(final ObjectOutputStream out) throws IOException {
    out.writeInt(mCategories);
  }

  @Override
  public String toString() {
    return mLocalizer.msg("name", "Program type");
  }

}

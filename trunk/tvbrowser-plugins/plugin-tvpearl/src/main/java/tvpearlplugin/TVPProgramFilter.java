/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

public class TVPProgramFilter
{
	static private boolean isInList(final String author)
	{
		for (String item : TVPearlPlugin.getInstance().getComposers())
		{
			if (item.equalsIgnoreCase(author))
			{
				return true;
			}
		}
		return false;
	}

	static public boolean showProgram(final TVPProgram program)
	{
		if (TVPearlPlugin.getSettings().getFilterEnabled())
		{
			final boolean exists = isInList(program.getAuthor());
			return (exists && TVPearlPlugin.getSettings().getFilterIncluding())
          || (!exists && TVPearlPlugin.getSettings().getFilterExcluding());
		}
		return true;
	}
}

/*
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
package wirschauenplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import devplugin.Date;

/**
 * this class bundles the date and the id wich are used to
 * identify and load a program via the PluginManager.
 *
 * @author uzi
 */
public class ProgramId implements Serializable
{
  /**
   * generated uid.
   */
  private static final long serialVersionUID = -7562170035121670412L;

  /**
   * the date for the program.
   */
  private Date mDate;

  /**
   * the id of the program.
   */
  private String mId;



  /**
   * @param date the date of a program
   * @param id the id of a program
   */
  public ProgramId(final Date date, final String id)
  {
    this.mDate = date;
    this.mId = id;
  }


  /**
   * @param obj the other date
   * @return true if date and id are equal
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (obj instanceof ProgramId)
    {
      ProgramId other = (ProgramId) obj;
      return other.mDate.equals(mDate) && other.mId.equals(mId);
    }
    return false;
  }


  /**
   * {@inheritDoc}
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    //taken from Effective Java, Item 9
    int result = 17;
    result = 31 * result + mDate.hashCode();
    result = 31 * result + mId.hashCode();
    return result;
  }


  /**
   * writes this object to a stream. called by the serialization process.
   *
   * @param out the stream to write to
   * @throws IOException if something went wrong
   */
  private void writeObject(final ObjectOutputStream out) throws IOException
  {
    //version for future compatibility issues
    out.writeInt(1);
    //date and id
    mDate.writeData(out);
    out.writeObject(mId);
  }


  /**
   * reads this object from a stream. called by the serialization process.
   *
   * @param in the stream to read from
   * @throws IOException if something went wrong
   * @throws ClassNotFoundException if the format was not readable
   */
  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    //version for future compatibility issues
    int version = in.readInt();
    if (version == 1)
    {
      //date and id
      mDate = new Date(in);
      mId = (String) in.readObject();
    }
  }



  /**
   * @return the date
   */
  public Date getDate()
  {
    return mDate;
  }

  /**
   * @param date the date to set
   */
  public void setDate(final Date date)
  {
    this.mDate = date;
  }

  /**
   * @return the id
   */
  public String getId()
  {
    return mId;
  }

  /**
   * @param id the id to set
   */
  public void setId(final String id)
  {
    this.mId = id;
  }
}

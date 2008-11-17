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

import java.text.DateFormat;
import java.util.Calendar;

public class TVPProgram implements Comparable<TVPProgram>
{
    private String mAuthor;
    private Calendar mCreateDate;
    private String mContentUrl;
    private String mTitle;
    private String mChannel;
    private Calendar mStart;
    private String mInfo;
    private String mProgramID;
    // 0..not found in TVB, 1..only Channel found, 2..program found in TVB
    private Integer mStatus = 0;

    public int compareTo(TVPProgram o)
    {
        return mStart.compareTo(o.getStart());
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(DateFormat.getDateInstance().format(mStart.getTime()));
        buffer.append(" · ");
        buffer.append(DateFormat.getTimeInstance().format(mStart.getTime()));
        buffer.append(" · ");
        buffer.append(mChannel);
        buffer.append(" · ");
        buffer.append(mTitle);

        return buffer.toString();
    }

    public String getAuthor()
    {
        return mAuthor;
    }

    public void setAuthor(String author)
    {
        this.mAuthor = author;
    }

    public String getChannel()
    {
        return mChannel;
    }

    public void setChannel(String channel)
    {
        this.mChannel = channel;
    }

    public String getContentUrl()
    {
        return mContentUrl;
    }

    public void setContentUrl(String contentUrl)
    {
        this.mContentUrl = contentUrl;
    }

    public Calendar getCreateDate()
    {
        return mCreateDate;
    }

    public void setCreateDate(Calendar createDate)
    {
        this.mCreateDate = createDate;
    }

    public String getInfo()
    {
        return mInfo;
    }

    public void setInfo(String info)
    {
        this.mInfo = info;
    }

    public String getProgramID()
    {
        return mProgramID;
    }

    public void setProgramID(String programID)
    {
        this.mProgramID = programID.trim();
        if (this.mProgramID != null && this.mProgramID.trim().length() > 0)
        {
            setStatus(2);
        }
    }

    public Calendar getStart()
    {
        return mStart;
    }

    public void setStart(Calendar start)
    {
        this.mStart = start;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String title)
    {
        this.mTitle = title;
    }

    public Integer getStatus()
    {
        return mStatus;
    }

    public void setStatus(Integer status)
    {
        if (this.mStatus < status)
        {
            this.mStatus = status;
        }
    }

    public void resetStatus()
    {
        mProgramID = "";
        mStatus = 0;
    }
}

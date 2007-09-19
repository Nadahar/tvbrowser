#region Using-Direktiven

using System;

#endregion

namespace PocketTVBrowserCF2
{
    /// <summary>
    /// One single TVBrowser broadcast
    /// </summary>
    public class Broadcast /*: IComparable*/
    {
        private int id;
        private String channel;
        private String channelId;
        private String title;
        private DateTime start;
        private DateTime end;
        private bool favourite;
        private bool reminder;

        public Broadcast(int id, String channel, String channelId, String title, DateTime start, DateTime end, bool favourite, bool reminder)
        {
            this.id = id;
            this.channel = channel;
            this.channelId = channelId;
            this.title = title;
            this.start = start;
            this.end = end;
            this.favourite = favourite;
            this.reminder = reminder;
        }


        public String getTitle()
        {
            return this.title;
        }
       
        public override string ToString()
        {
            String result = "";
            result += this.start.ToShortTimeString();
            result += "|";
            result += this.title.Replace('\n',' ');
            return result;
        }

        public int getID()
        {
            return this.id;
        }

        public String getChannel()
        {
            return this.channel;
        }

        public String getChannelID()
        {
            return this.channelId;
        }
        
        public int getLength()
        {
            TimeSpan result = this.end - this.start;
            return (int)result.TotalMinutes;
        }

        public DateTime getEnd()
        {
            return this.end;
        }

        public DateTime getStart()
        {
            return this.start;
        }

        public int getPercent()
        {
            DateTime now = DateTime.Now;
            TimeSpan first = now.Subtract(this.start);
            TimeSpan second = this.end.Subtract(this.start);
            return (int) Math.Round(((first.TotalMinutes / second.TotalMinutes) * 100),0);
        }

        public int getRelativePercent(DateTime dt)
        {
            TimeSpan first = dt.Subtract(this.start);
            TimeSpan second = this.end.Subtract(this.start);
            return (int)Math.Round(((first.TotalMinutes / second.TotalMinutes) * 100), 0);
        }

        public bool isReminder()
        {
            return this.reminder;
        }

        public void setReminder(bool aBool)
        {
            this.reminder=aBool;
        }

        public bool isFavourite()
        {
            return this.favourite;
        }

        public void setFavourite(bool aBool)
        {
            this.favourite = aBool;
        }
    }
}

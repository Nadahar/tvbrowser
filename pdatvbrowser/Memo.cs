using System;
using System.Collections.Generic;
using System.Text;

namespace TVBrowserMini
{
    class Memo
    {
        private DateTime timer;     //When?
        private bool alreadyDone;   //already reminded?
        private String broadcastTitle;
        private String broadcastChannel;
        private DateTime broadcastStart;
        private DateTime broadcastEnd;
        private Broadcast broadcast;

        public Memo(Broadcast broadcast, DateTime timer)
        {
            this.broadcast = broadcast;
            this.alreadyDone = false;
            this.broadcastTitle = broadcast.getTitle();
            this.broadcastChannel = broadcast.getChannel();
            this.broadcastStart = broadcast.getStart();
            this.broadcastEnd = broadcast.getEnd();
            this.timer = timer;
        }

        public Broadcast getBroadcast()
        {
            return this.broadcast;
        }

        public bool isAlreadyDone()
        {
            return this.alreadyDone;
        }

        public void setAlreadyDone()
        {
            this.alreadyDone = true;
        }

        public void setTimer(DateTime dt)
        {
            this.timer = dt;
        }

        public DateTime getTimer()
        {
            return this.timer;
        }

        public override string ToString()
        {
            String result = this.broadcastStart.ToShortTimeString() + " " + this.broadcastTitle + " (" + this.broadcastChannel + ")";
            return result;
        }

        public String getBroadcastTitle()
        {
            return this.broadcastTitle;
        }

        public String getBroadcastChannel()
        {
            return this.broadcastChannel;
        }

        public DateTime getBroadcastStart()
        {
            return this.broadcastStart;
        }

        public DateTime getBroadcastEnd()
        {
            return this.broadcastEnd;
        }
    }
}

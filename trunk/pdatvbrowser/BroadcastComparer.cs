using System;
using System.Collections;
using System.Text;

namespace TVBrowserMini
{
    /// <summary>
    /// Compares the beginning time of tw broadcasts
    /// </summary>
    class BroadcastComparer : IComparer
    {
        public int Compare(object o1, object o2)
        {
            Broadcast b1 = (Broadcast)o1;
            Broadcast b2 = (Broadcast)o2;
            if (b1.getStart() > b2.getStart())
                return 1;
            else if (b1.getStart() < b2.getStart())
                return (-1);
            else
                return 0;
        }
    }
}

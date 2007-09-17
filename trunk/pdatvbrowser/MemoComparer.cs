using System;
using System.Collections.Generic;
using System.Collections;
using System.Text;

namespace PocketTVBrowserCF2
{
    class MemoComparer : IComparer
    {
        public int Compare(object o1, object o2)
        {
            Memo m1 = (Memo)o1;
            Memo m2 = (Memo)o2;
            if (m1.getTimer() > m2.getTimer())
                return 1;
            else if (m1.getTimer() < m2.getTimer())
                return (-1);
            else
                return 0;
        }
    }
}




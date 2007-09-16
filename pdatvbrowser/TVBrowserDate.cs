using System;
using System.Collections.Generic;
using System.Text;

namespace PocketTVBrowserCF2
{
    class TVBrowserDate
    {
        private DateTime dt;
        private TVBrowserControll con;

        public TVBrowserDate(DateTime dt, TVBrowserControll con)
        {
            this.dt = dt;
            this.con = con;
        }

        public override string ToString()
        {
            if (DateTime.Now.Date == this.dt.Date)
                return this.con.getLanguageElement("TVBrowserDate.Today","today");
            /*else if (DateTime.Now.Date.AddDays(-1) == this.dt.Date)
                return "yesterday";
            else if (DateTime.Now.Date.AddDays(+1) == this.dt.Date)
                return "tomorrow";*/
            else
            {
                String result="";
                String format = this.con.getLanguageElement("Language.ShortDateformat", "MM/dd/yy");
                result += this.con.getLanguageElement("Data." + this.dt.ToLocalTime().DayOfWeek.ToString().Substring(0, 2), this.dt.ToLocalTime().DayOfWeek.ToString().Substring(0, 2));
                result += " " + this.dt.ToString(format);
                return result;
            }
        }

        public String ToLongDateString()
        {
            return this.dt.ToLongDateString();
        }

        public DateTime getDateTime()
        {
            return this.dt;
        }

        public void setDateTime(DateTime dt)
        {
            this.dt = dt;
        }
    }
}

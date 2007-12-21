using System;
using System.Collections.Generic;
using System.Text;

namespace TVBrowserMini
{
    class SearchElement
    {
        String databasename;
        String languagename;

        public SearchElement(String databasename, String languagename)
        {
            this.databasename = databasename;
            this.languagename = languagename;
        }

        public String getDatabasename()
        {
            return this.databasename;
        }

        public String getLanguagename()
        {
            return this.languagename;
        }

        public override string ToString()
        {
            return languagename;
        }
    }
}

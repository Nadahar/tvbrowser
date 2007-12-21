#region Using-Direktiven

using System;
using System.Collections;
using System.Drawing;

#endregion

namespace TVBrowserMini
{
    /// <summary>
    /// One single TVBrowser channel
    /// </summary>
    public class Channel
    {
        private String id;
        private String name;
        private char category;

       
        public Channel(String id, String name, char category)
        {
            this.id = id;
            this.name = name;
            this.category = category;
        }

        public String getName()
        {
            return this.name;
        }

        public String getId()
        {
            return this.id;
        }

        public char getCategory()
        {
            return this.category;
        }

        public override string ToString()
        {
            return this.name;
            //return base.ToString();
        }
    }
}

using System;
using System.Collections.Generic;
using System.Windows.Forms;

namespace TVBrowserMini
{
    static class Program
    {
        /// <summary>
        /// Main Entry for the application
        /// </summary>
        [MTAThread]
        static void Main()
        {
            Application.Run(new Mainform());
        }
    }
}
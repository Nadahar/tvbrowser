using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;

namespace PocketTVBrowserCF2
{
    class ErrorMessage
    {
        private String headline;
        private String message;
        private DialogResult dialogResult;
        private String run;

        public ErrorMessage(String headline, String message, DialogResult dialogResult, String run)
        {
            this.headline = headline;
            this.message = message;
            this.dialogResult = dialogResult;
            this.run = run;
        }

        public ErrorMessage(String headline, String message, DialogResult dialogResult)
        {
            this.headline = headline;
            this.message = message;
            this.dialogResult = dialogResult;
            this.run = "";
        }


        public DialogResult getDialogResult()
        {
            return this.dialogResult;
        }

        public String getMessage()
        {
            return this.message;
        }

        public String getHeadline()
        {
            return this.headline;
        }

        public String getRun()
        {
            return this.run;
        }

        public override string ToString()
        {
            return this.message;
        }
    }
}

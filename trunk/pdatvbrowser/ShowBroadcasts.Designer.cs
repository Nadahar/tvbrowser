namespace PocketTVBrowserCF2
{
    partial class ShowBroadcasts
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        //private System.Windows.Forms.MainMenu mainMenu1;
        

        /// <summary>
        /// Verwendete Ressourcen bereinigen.
        /// </summary>
        /// <param name="disposing">True, wenn verwaltete Ressourcen gelöscht werden sollen; andernfalls False.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Vom Windows Form-Designer generierter Code

        /// <summary>
        /// Erforderliche Methode für die Designerunterstützung.
        /// Der Inhalt der Methode darf nicht mit dem Code-Editor geändert werden.
        /// </summary>
        private void InitializeComponent()
        {
            this.buttonOK = new System.Windows.Forms.Button();
            this.lShowBroadcasts = new System.Windows.Forms.Label();
            this.dateTimePickerStart = new System.Windows.Forms.DateTimePicker();
            this.numericUpDownHour = new System.Windows.Forms.NumericUpDown();
            this.numericUpDownMinute = new System.Windows.Forms.NumericUpDown();
            this.SuspendLayout();
            // 
            // buttonOK
            // 
            this.buttonOK.Location = new System.Drawing.Point(141, 101);
            this.buttonOK.Name = "buttonOK";
            this.buttonOK.Size = new System.Drawing.Size(77, 22);
            this.buttonOK.TabIndex = 0;
            this.buttonOK.Text = "OK";
            this.buttonOK.Click += new System.EventHandler(this.buttonOK_Click);
            // 
            // lShowBroadcasts
            // 
            this.lShowBroadcasts.Font = new System.Drawing.Font("Tahoma", 11F, System.Drawing.FontStyle.Bold);
            this.lShowBroadcasts.Location = new System.Drawing.Point(0, 3);
            this.lShowBroadcasts.Name = "lShowBroadcasts";
            this.lShowBroadcasts.Size = new System.Drawing.Size(240, 18);
            this.lShowBroadcasts.Text = "Show broadcasts running at";
            this.lShowBroadcasts.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // dateTimePickerStart
            // 
            this.dateTimePickerStart.Location = new System.Drawing.Point(18, 24);
            this.dateTimePickerStart.Name = "dateTimePickerStart";
            this.dateTimePickerStart.Size = new System.Drawing.Size(200, 22);
            this.dateTimePickerStart.TabIndex = 6;
            // 
            // numericUpDownHour
            // 
            this.numericUpDownHour.Location = new System.Drawing.Point(18, 64);
            this.numericUpDownHour.Maximum = new decimal(new int[] {
            24,
            0,
            0,
            0});
            this.numericUpDownHour.Name = "numericUpDownHour";
            this.numericUpDownHour.Size = new System.Drawing.Size(76, 22);
            this.numericUpDownHour.TabIndex = 7;
            // 
            // numericUpDownMinute
            // 
            this.numericUpDownMinute.Location = new System.Drawing.Point(118, 64);
            this.numericUpDownMinute.Maximum = new decimal(new int[] {
            59,
            0,
            0,
            0});
            this.numericUpDownMinute.Name = "numericUpDownMinute";
            this.numericUpDownMinute.Size = new System.Drawing.Size(100, 22);
            this.numericUpDownMinute.TabIndex = 8;
            // 
            // Reminder
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.ClientSize = new System.Drawing.Size(240, 130);
            this.Controls.Add(this.numericUpDownMinute);
            this.Controls.Add(this.numericUpDownHour);
            this.Controls.Add(this.dateTimePickerStart);
            this.Controls.Add(this.lShowBroadcasts);
            this.Controls.Add(this.buttonOK);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
            this.KeyPreview = true;
            this.Name = "Reminder";
            this.Text = "Show broadcasts running at";
            this.KeyDown += new System.Windows.Forms.KeyEventHandler(this.Reminder_KeyDown);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button buttonOK;
        private System.Windows.Forms.Label lShowBroadcasts;
        private System.Windows.Forms.DateTimePicker dateTimePickerStart;
        private System.Windows.Forms.NumericUpDown numericUpDownHour;
        private System.Windows.Forms.NumericUpDown numericUpDownMinute;
    }
}
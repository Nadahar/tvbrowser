namespace PocketTVBrowserCF2
{
    partial class SearchStartingAt
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
            this.numericUpDownHour = new System.Windows.Forms.NumericUpDown();
            this.numericUpDownMinute = new System.Windows.Forms.NumericUpDown();
            this.dateTimePicker = new System.Windows.Forms.DateTimePicker();
            this.lHeadline = new System.Windows.Forms.Label();
            this.buttonOK = new System.Windows.Forms.Button();
            this.lColon = new System.Windows.Forms.Label();
            this.buttonCancel = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // numericUpDownHour
            // 
            this.numericUpDownHour.Location = new System.Drawing.Point(30, 61);
            this.numericUpDownHour.Maximum = new decimal(new int[] {
            23,
            0,
            0,
            0});
            this.numericUpDownHour.Name = "numericUpDownHour";
            this.numericUpDownHour.Size = new System.Drawing.Size(87, 22);
            this.numericUpDownHour.TabIndex = 0;
            this.numericUpDownHour.Value = new decimal(new int[] {
            20,
            0,
            0,
            0});
            // 
            // numericUpDownMinute
            // 
            this.numericUpDownMinute.Location = new System.Drawing.Point(127, 61);
            this.numericUpDownMinute.Maximum = new decimal(new int[] {
            59,
            0,
            0,
            0});
            this.numericUpDownMinute.Name = "numericUpDownMinute";
            this.numericUpDownMinute.Size = new System.Drawing.Size(87, 22);
            this.numericUpDownMinute.TabIndex = 1;
            this.numericUpDownMinute.Value = new decimal(new int[] {
            15,
            0,
            0,
            0});
            // 
            // dateTimePicker
            // 
            this.dateTimePicker.Location = new System.Drawing.Point(3, 33);
            this.dateTimePicker.Name = "dateTimePicker";
            this.dateTimePicker.Size = new System.Drawing.Size(234, 22);
            this.dateTimePicker.TabIndex = 2;
            // 
            // lHeadline
            // 
            this.lHeadline.Font = new System.Drawing.Font("Tahoma", 11F, System.Drawing.FontStyle.Bold);
            this.lHeadline.Location = new System.Drawing.Point(0, 5);
            this.lHeadline.Name = "lHeadline";
            this.lHeadline.Size = new System.Drawing.Size(240, 18);
            this.lHeadline.Text = "Show broadcasts running at";
            this.lHeadline.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // buttonOK
            // 
            this.buttonOK.Location = new System.Drawing.Point(30, 88);
            this.buttonOK.Name = "buttonOK";
            this.buttonOK.Size = new System.Drawing.Size(87, 22);
            this.buttonOK.TabIndex = 4;
            this.buttonOK.Text = "OK";
            this.buttonOK.Click += new System.EventHandler(this.buttonOK_Click);
            // 
            // lColon
            // 
            this.lColon.Font = new System.Drawing.Font("Tahoma", 12F, System.Drawing.FontStyle.Bold);
            this.lColon.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(0)))), ((int)(((byte)(0)))));
            this.lColon.Location = new System.Drawing.Point(117, 61);
            this.lColon.Name = "lColon";
            this.lColon.Size = new System.Drawing.Size(7, 24);
            this.lColon.Text = ":";
            this.lColon.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // buttonCancel
            // 
            this.buttonCancel.Location = new System.Drawing.Point(127, 88);
            this.buttonCancel.Name = "buttonCancel";
            this.buttonCancel.Size = new System.Drawing.Size(87, 22);
            this.buttonCancel.TabIndex = 5;
            this.buttonCancel.Text = "Cancel";
            this.buttonCancel.Click += new System.EventHandler(this.buttonCancel_Click);
            // 
            // SearchStartingAt
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.BackColor = System.Drawing.Color.LightCyan;
            this.ClientSize = new System.Drawing.Size(240, 130);
            this.Controls.Add(this.buttonCancel);
            this.Controls.Add(this.lColon);
            this.Controls.Add(this.lHeadline);
            this.Controls.Add(this.buttonOK);
            this.Controls.Add(this.dateTimePicker);
            this.Controls.Add(this.numericUpDownMinute);
            this.Controls.Add(this.numericUpDownHour);
            this.ForeColor = System.Drawing.SystemColors.Window;
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
            this.KeyPreview = true;
            this.MinimizeBox = false;
            this.Name = "SearchStartingAt";
            this.Text = "Search";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.NumericUpDown numericUpDownHour;
        private System.Windows.Forms.NumericUpDown numericUpDownMinute;
        private System.Windows.Forms.DateTimePicker dateTimePicker;
        private System.Windows.Forms.Label lHeadline;
        private System.Windows.Forms.Button buttonOK;
        private System.Windows.Forms.Label lColon;
        private System.Windows.Forms.Button buttonCancel;
    }
}
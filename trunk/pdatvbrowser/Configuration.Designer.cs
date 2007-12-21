namespace TVBrowserMini
{
    partial class Configuration
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.MainMenu mainMenu1;

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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Configuration));
            this.textBoxPathReminderSound = new System.Windows.Forms.TextBox();
            this.mainMenu1 = new System.Windows.Forms.MainMenu();
            this.tabControlConfiguration = new System.Windows.Forms.TabControl();
            this.tabGeneral = new System.Windows.Forms.TabPage();
            this.labelTomorrow = new System.Windows.Forms.Label();
            this.labelToday = new System.Windows.Forms.Label();
            this.comboBoxDayEnds = new System.Windows.Forms.ComboBox();
            this.comboBoxDayStarts = new System.Windows.Forms.ComboBox();
            this.labelDayEnds = new System.Windows.Forms.Label();
            this.labelDayBegin = new System.Windows.Forms.Label();
            this.lSeconds = new System.Windows.Forms.Label();
            this.lLanguage = new System.Windows.Forms.Label();
            this.comboBoxLanguages = new System.Windows.Forms.ComboBox();
            this.comboBoxReload = new System.Windows.Forms.ComboBox();
            this.labelReload = new System.Windows.Forms.Label();
            this.bBrowseTVData = new System.Windows.Forms.Button();
            this.labelPathDB = new System.Windows.Forms.Label();
            this.textBoxPathDB = new System.Windows.Forms.TextBox();
            this.tabSync = new System.Windows.Forms.TabPage();
            this.checkBoxTrans = new System.Windows.Forms.CheckBox();
            this.textBoxPort = new System.Windows.Forms.TextBox();
            this.labelPort = new System.Windows.Forms.Label();
            this.labelIPValue = new System.Windows.Forms.Label();
            this.labelIP = new System.Windows.Forms.Label();
            this.labelDNSValue = new System.Windows.Forms.Label();
            this.labelDNS = new System.Windows.Forms.Label();
            this.labelSyncTutorial = new System.Windows.Forms.Label();
            this.tabReminders = new System.Windows.Forms.TabPage();
            this.checkBoxSoundReminder = new System.Windows.Forms.CheckBox();
            this.textBoxMinutes = new System.Windows.Forms.TextBox();
            this.buttonBrowseReminderSound = new System.Windows.Forms.Button();
            this.checkBoxPopupReminders = new System.Windows.Forms.CheckBox();
            this.lRemindersMinutes = new System.Windows.Forms.Label();
            this.labelRemindersMinutes = new System.Windows.Forms.Label();
            this.labelReminderWav = new System.Windows.Forms.Label();
            this.openFileDialogTVData = new System.Windows.Forms.OpenFileDialog();
            this.openFileDialogSound = new System.Windows.Forms.OpenFileDialog();
            this.inputPanel = new Microsoft.WindowsCE.Forms.InputPanel();
            this.tabControlConfiguration.SuspendLayout();
            this.tabGeneral.SuspendLayout();
            this.tabSync.SuspendLayout();
            this.tabReminders.SuspendLayout();
            this.SuspendLayout();
            // 
            // textBoxPathReminderSound
            // 
            this.textBoxPathReminderSound.Location = new System.Drawing.Point(8, 25);
            this.textBoxPathReminderSound.Name = "textBoxPathReminderSound";
            this.textBoxPathReminderSound.Size = new System.Drawing.Size(172, 21);
            this.textBoxPathReminderSound.TabIndex = 4;
            this.textBoxPathReminderSound.Text = "alarm.wav";
            this.textBoxPathReminderSound.LostFocus += new System.EventHandler(this.tbPath_TextLostFocus);
            this.textBoxPathReminderSound.GotFocus += new System.EventHandler(this.tbPath_TextGotFocus);
            // 
            // tabControlConfiguration
            // 
            this.tabControlConfiguration.Controls.Add(this.tabGeneral);
            this.tabControlConfiguration.Controls.Add(this.tabSync);
            this.tabControlConfiguration.Controls.Add(this.tabReminders);
            this.tabControlConfiguration.Location = new System.Drawing.Point(0, 0);
            this.tabControlConfiguration.Name = "tabControlConfiguration";
            this.tabControlConfiguration.SelectedIndex = 0;
            this.tabControlConfiguration.Size = new System.Drawing.Size(240, 268);
            this.tabControlConfiguration.TabIndex = 0;
            // 
            // tabGeneral
            // 
            this.tabGeneral.AutoScroll = true;
            this.tabGeneral.Controls.Add(this.labelTomorrow);
            this.tabGeneral.Controls.Add(this.labelToday);
            this.tabGeneral.Controls.Add(this.comboBoxDayEnds);
            this.tabGeneral.Controls.Add(this.comboBoxDayStarts);
            this.tabGeneral.Controls.Add(this.labelDayEnds);
            this.tabGeneral.Controls.Add(this.labelDayBegin);
            this.tabGeneral.Controls.Add(this.lSeconds);
            this.tabGeneral.Controls.Add(this.lLanguage);
            this.tabGeneral.Controls.Add(this.comboBoxLanguages);
            this.tabGeneral.Controls.Add(this.comboBoxReload);
            this.tabGeneral.Controls.Add(this.labelReload);
            this.tabGeneral.Controls.Add(this.bBrowseTVData);
            this.tabGeneral.Controls.Add(this.labelPathDB);
            this.tabGeneral.Controls.Add(this.textBoxPathDB);
            this.tabGeneral.Location = new System.Drawing.Point(0, 0);
            this.tabGeneral.Name = "tabGeneral";
            this.tabGeneral.Size = new System.Drawing.Size(240, 245);
            this.tabGeneral.Text = "general";
            // 
            // labelTomorrow
            // 
            this.labelTomorrow.Location = new System.Drawing.Point(165, 155);
            this.labelTomorrow.Name = "labelTomorrow";
            this.labelTomorrow.Size = new System.Drawing.Size(68, 20);
            this.labelTomorrow.Text = "(tomorrow)";
            // 
            // labelToday
            // 
            this.labelToday.Location = new System.Drawing.Point(165, 130);
            this.labelToday.Name = "labelToday";
            this.labelToday.Size = new System.Drawing.Size(68, 20);
            this.labelToday.Text = "(today)";
            // 
            // comboBoxDayEnds
            // 
            this.comboBoxDayEnds.Location = new System.Drawing.Point(85, 153);
            this.comboBoxDayEnds.Name = "comboBoxDayEnds";
            this.comboBoxDayEnds.Size = new System.Drawing.Size(76, 22);
            this.comboBoxDayEnds.TabIndex = 38;
            // 
            // comboBoxDayStarts
            // 
            this.comboBoxDayStarts.Location = new System.Drawing.Point(85, 128);
            this.comboBoxDayStarts.Name = "comboBoxDayStarts";
            this.comboBoxDayStarts.Size = new System.Drawing.Size(76, 22);
            this.comboBoxDayStarts.TabIndex = 37;
            // 
            // labelDayEnds
            // 
            this.labelDayEnds.Location = new System.Drawing.Point(7, 155);
            this.labelDayEnds.Name = "labelDayEnds";
            this.labelDayEnds.Size = new System.Drawing.Size(79, 20);
            this.labelDayEnds.Text = "Day ends at";
            // 
            // labelDayBegin
            // 
            this.labelDayBegin.Location = new System.Drawing.Point(7, 130);
            this.labelDayBegin.Name = "labelDayBegin";
            this.labelDayBegin.Size = new System.Drawing.Size(79, 20);
            this.labelDayBegin.Text = "Day starts at";
            // 
            // lSeconds
            // 
            this.lSeconds.Location = new System.Drawing.Point(161, 61);
            this.lSeconds.Name = "lSeconds";
            this.lSeconds.Size = new System.Drawing.Size(72, 20);
            this.lSeconds.Text = "seconds";
            // 
            // lLanguage
            // 
            this.lLanguage.Location = new System.Drawing.Point(7, 96);
            this.lLanguage.Name = "lLanguage";
            this.lLanguage.Size = new System.Drawing.Size(72, 20);
            this.lLanguage.Text = "language";
            // 
            // comboBoxLanguages
            // 
            this.comboBoxLanguages.Location = new System.Drawing.Point(85, 92);
            this.comboBoxLanguages.Name = "comboBoxLanguages";
            this.comboBoxLanguages.Size = new System.Drawing.Size(148, 22);
            this.comboBoxLanguages.TabIndex = 31;
            // 
            // comboBoxReload
            // 
            this.comboBoxReload.Items.Add("-disabled-");
            this.comboBoxReload.Items.Add("60");
            this.comboBoxReload.Items.Add("120");
            this.comboBoxReload.Items.Add("180");
            this.comboBoxReload.Items.Add("240");
            this.comboBoxReload.Items.Add("300");
            this.comboBoxReload.Items.Add("600");
            this.comboBoxReload.Items.Add("1800");
            this.comboBoxReload.Location = new System.Drawing.Point(85, 58);
            this.comboBoxReload.Name = "comboBoxReload";
            this.comboBoxReload.Size = new System.Drawing.Size(76, 22);
            this.comboBoxReload.TabIndex = 27;
            // 
            // labelReload
            // 
            this.labelReload.Location = new System.Drawing.Point(7, 61);
            this.labelReload.Name = "labelReload";
            this.labelReload.Size = new System.Drawing.Size(79, 20);
            this.labelReload.Text = "refresh every";
            // 
            // bBrowseTVData
            // 
            this.bBrowseTVData.Font = new System.Drawing.Font("Tahoma", 8F, System.Drawing.FontStyle.Bold);
            this.bBrowseTVData.Location = new System.Drawing.Point(186, 26);
            this.bBrowseTVData.Name = "bBrowseTVData";
            this.bBrowseTVData.Size = new System.Drawing.Size(47, 20);
            this.bBrowseTVData.TabIndex = 17;
            this.bBrowseTVData.Text = "browse";
            this.bBrowseTVData.Click += new System.EventHandler(this.bBrowseTVData_Click);
            // 
            // labelPathDB
            // 
            this.labelPathDB.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            this.labelPathDB.Location = new System.Drawing.Point(7, 3);
            this.labelPathDB.Name = "labelPathDB";
            this.labelPathDB.Size = new System.Drawing.Size(226, 14);
            this.labelPathDB.Text = "Path to your TV-Data File (*.tvd)";
            // 
            // textBoxPathDB
            // 
            this.textBoxPathDB.Location = new System.Drawing.Point(7, 25);
            this.textBoxPathDB.Name = "textBoxPathDB";
            this.textBoxPathDB.Size = new System.Drawing.Size(172, 21);
            this.textBoxPathDB.TabIndex = 2;
            this.textBoxPathDB.Text = "tvdata.tvd";
            this.textBoxPathDB.LostFocus += new System.EventHandler(this.tbPath_TextLostFocus);
            this.textBoxPathDB.GotFocus += new System.EventHandler(this.tbPath_TextGotFocus);
            // 
            // tabSync
            // 
            this.tabSync.Controls.Add(this.checkBoxTrans);
            this.tabSync.Controls.Add(this.textBoxPort);
            this.tabSync.Controls.Add(this.labelPort);
            this.tabSync.Controls.Add(this.labelIPValue);
            this.tabSync.Controls.Add(this.labelIP);
            this.tabSync.Controls.Add(this.labelDNSValue);
            this.tabSync.Controls.Add(this.labelDNS);
            this.tabSync.Controls.Add(this.labelSyncTutorial);
            this.tabSync.Location = new System.Drawing.Point(0, 0);
            this.tabSync.Name = "tabSync";
            this.tabSync.Size = new System.Drawing.Size(240, 245);
            this.tabSync.Text = "synchronize";
            // 
            // checkBoxTrans
            // 
            this.checkBoxTrans.Enabled = false;
            this.checkBoxTrans.Location = new System.Drawing.Point(6, 214);
            this.checkBoxTrans.Name = "checkBoxTrans";
            this.checkBoxTrans.Size = new System.Drawing.Size(190, 20);
            this.checkBoxTrans.TabIndex = 16;
            this.checkBoxTrans.Text = "activate synchronisation";
            // 
            // textBoxPort
            // 
            this.textBoxPort.Location = new System.Drawing.Point(100, 180);
            this.textBoxPort.Name = "textBoxPort";
            this.textBoxPort.Size = new System.Drawing.Size(85, 21);
            this.textBoxPort.TabIndex = 7;
            this.textBoxPort.Text = "13267";
            this.textBoxPort.TextChanged += new System.EventHandler(this.textBoxPort_TextChanged);
            // 
            // labelPort
            // 
            this.labelPort.Location = new System.Drawing.Point(7, 182);
            this.labelPort.Name = "labelPort";
            this.labelPort.Size = new System.Drawing.Size(87, 20);
            this.labelPort.Text = "Port:";
            // 
            // labelIPValue
            // 
            this.labelIPValue.Location = new System.Drawing.Point(100, 157);
            this.labelIPValue.Name = "labelIPValue";
            this.labelIPValue.Size = new System.Drawing.Size(132, 20);
            // 
            // labelIP
            // 
            this.labelIP.Location = new System.Drawing.Point(7, 157);
            this.labelIP.Name = "labelIP";
            this.labelIP.Size = new System.Drawing.Size(87, 20);
            this.labelIP.Text = "IP address:";
            // 
            // labelDNSValue
            // 
            this.labelDNSValue.Location = new System.Drawing.Point(100, 137);
            this.labelDNSValue.Name = "labelDNSValue";
            this.labelDNSValue.Size = new System.Drawing.Size(132, 20);
            // 
            // labelDNS
            // 
            this.labelDNS.Location = new System.Drawing.Point(7, 137);
            this.labelDNS.Name = "labelDNS";
            this.labelDNS.Size = new System.Drawing.Size(87, 20);
            this.labelDNS.Text = "DNS name:";
            // 
            // labelSyncTutorial
            // 
            this.labelSyncTutorial.Location = new System.Drawing.Point(6, 8);
            this.labelSyncTutorial.Name = "labelSyncTutorial";
            this.labelSyncTutorial.Size = new System.Drawing.Size(226, 121);
            this.labelSyncTutorial.Text = resources.GetString("labelSyncTutorial.Text");
            this.labelSyncTutorial.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // tabReminders
            // 
            this.tabReminders.Controls.Add(this.checkBoxSoundReminder);
            this.tabReminders.Controls.Add(this.textBoxMinutes);
            this.tabReminders.Controls.Add(this.buttonBrowseReminderSound);
            this.tabReminders.Controls.Add(this.checkBoxPopupReminders);
            this.tabReminders.Controls.Add(this.lRemindersMinutes);
            this.tabReminders.Controls.Add(this.labelRemindersMinutes);
            this.tabReminders.Controls.Add(this.labelReminderWav);
            this.tabReminders.Controls.Add(this.textBoxPathReminderSound);
            this.tabReminders.Location = new System.Drawing.Point(0, 0);
            this.tabReminders.Name = "tabReminders";
            this.tabReminders.Size = new System.Drawing.Size(240, 245);
            this.tabReminders.Text = "reminders";
            // 
            // checkBoxSoundReminder
            // 
            this.checkBoxSoundReminder.Checked = true;
            this.checkBoxSoundReminder.CheckState = System.Windows.Forms.CheckState.Checked;
            this.checkBoxSoundReminder.Location = new System.Drawing.Point(8, 131);
            this.checkBoxSoundReminder.Name = "checkBoxSoundReminder";
            this.checkBoxSoundReminder.Size = new System.Drawing.Size(175, 20);
            this.checkBoxSoundReminder.TabIndex = 19;
            this.checkBoxSoundReminder.Text = "Play Sound";
            // 
            // textBoxMinutes
            // 
            this.textBoxMinutes.Location = new System.Drawing.Point(8, 91);
            this.textBoxMinutes.Name = "textBoxMinutes";
            this.textBoxMinutes.Size = new System.Drawing.Size(58, 21);
            this.textBoxMinutes.TabIndex = 18;
            this.textBoxMinutes.Text = "5";
            this.textBoxMinutes.TextChanged += new System.EventHandler(this.textBoxMinutes_TextChanged);
            // 
            // buttonBrowseReminderSound
            // 
            this.buttonBrowseReminderSound.Font = new System.Drawing.Font("Tahoma", 8F, System.Drawing.FontStyle.Bold);
            this.buttonBrowseReminderSound.Location = new System.Drawing.Point(185, 26);
            this.buttonBrowseReminderSound.Name = "buttonBrowseReminderSound";
            this.buttonBrowseReminderSound.Size = new System.Drawing.Size(48, 20);
            this.buttonBrowseReminderSound.TabIndex = 14;
            this.buttonBrowseReminderSound.Text = "browse";
            this.buttonBrowseReminderSound.Click += new System.EventHandler(this.buttonBrowseReminderSound_Click);
            // 
            // checkBoxPopupReminders
            // 
            this.checkBoxPopupReminders.Checked = true;
            this.checkBoxPopupReminders.CheckState = System.Windows.Forms.CheckState.Checked;
            this.checkBoxPopupReminders.Location = new System.Drawing.Point(8, 114);
            this.checkBoxPopupReminders.Name = "checkBoxPopupReminders";
            this.checkBoxPopupReminders.Size = new System.Drawing.Size(175, 20);
            this.checkBoxPopupReminders.TabIndex = 10;
            this.checkBoxPopupReminders.Text = "Show Popups";
            // 
            // lRemindersMinutes
            // 
            this.lRemindersMinutes.Location = new System.Drawing.Point(70, 93);
            this.lRemindersMinutes.Name = "lRemindersMinutes";
            this.lRemindersMinutes.Size = new System.Drawing.Size(109, 18);
            this.lRemindersMinutes.Text = "minutes";
            // 
            // labelRemindersMinutes
            // 
            this.labelRemindersMinutes.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            this.labelRemindersMinutes.Location = new System.Drawing.Point(7, 59);
            this.labelRemindersMinutes.Name = "labelRemindersMinutes";
            this.labelRemindersMinutes.Size = new System.Drawing.Size(226, 35);
            this.labelRemindersMinutes.Text = "Remind me X minutes before broadcasts beginning";
            // 
            // labelReminderWav
            // 
            this.labelReminderWav.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            this.labelReminderWav.Location = new System.Drawing.Point(7, 4);
            this.labelReminderWav.Name = "labelReminderWav";
            this.labelReminderWav.Size = new System.Drawing.Size(226, 14);
            this.labelReminderWav.Text = "Play this Sound (*.wav)";
            // 
            // openFileDialogTVData
            // 
            this.openFileDialogTVData.FileName = "tvdata.tvd";
            // 
            // openFileDialogSound
            // 
            this.openFileDialogSound.FileName = "alarm.wav";
            // 
            // Configuration
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.ClientSize = new System.Drawing.Size(240, 268);
            this.Controls.Add(this.tabControlConfiguration);
            this.KeyPreview = true;
            this.Menu = this.mainMenu1;
            this.Name = "Configuration";
            this.Text = "Configuration";
            this.Closed += new System.EventHandler(this.bSave_Click);
            this.tabControlConfiguration.ResumeLayout(false);
            this.tabGeneral.ResumeLayout(false);
            this.tabSync.ResumeLayout(false);
            this.tabReminders.ResumeLayout(false);
            this.ResumeLayout(false);

        }  

        #endregion

        private System.Windows.Forms.TabControl tabControlConfiguration;
        private System.Windows.Forms.TabPage tabGeneral;
        private System.Windows.Forms.TabPage tabReminders;
        private System.Windows.Forms.Label labelPathDB;
        private System.Windows.Forms.TextBox textBoxPathDB;
        private System.Windows.Forms.OpenFileDialog openFileDialogTVData;
        private System.Windows.Forms.Label lRemindersMinutes;
        private System.Windows.Forms.Label labelRemindersMinutes;
        private System.Windows.Forms.Label labelReminderWav;
        private System.Windows.Forms.Button bBrowseTVData;
        private System.Windows.Forms.TextBox textBoxPathReminderSound;
        private System.Windows.Forms.CheckBox checkBoxPopupReminders;
        private System.Windows.Forms.Label labelReload;
        private System.Windows.Forms.ComboBox comboBoxReload;
        private System.Windows.Forms.Button buttonBrowseReminderSound;
        private System.Windows.Forms.OpenFileDialog openFileDialogSound;
        private Microsoft.WindowsCE.Forms.InputPanel inputPanel;
        private System.Windows.Forms.Label lLanguage;
        private System.Windows.Forms.ComboBox comboBoxLanguages;
        private System.Windows.Forms.Label lSeconds;
        private System.Windows.Forms.TextBox textBoxMinutes;
        private System.Windows.Forms.CheckBox checkBoxSoundReminder;
        private System.Windows.Forms.ComboBox comboBoxDayEnds;
        private System.Windows.Forms.ComboBox comboBoxDayStarts;
        private System.Windows.Forms.Label labelDayEnds;
        private System.Windows.Forms.Label labelDayBegin;
        private System.Windows.Forms.Label labelTomorrow;
        private System.Windows.Forms.Label labelToday;
        private System.Windows.Forms.TabPage tabSync;
        private System.Windows.Forms.Label labelSyncTutorial;
        private System.Windows.Forms.Label labelIPValue;
        private System.Windows.Forms.Label labelIP;
        private System.Windows.Forms.Label labelDNSValue;
        private System.Windows.Forms.Label labelDNS;
        private System.Windows.Forms.TextBox textBoxPort;
        private System.Windows.Forms.Label labelPort;
        private System.Windows.Forms.CheckBox checkBoxTrans;
    }
}
package de.misi.tvbrowser.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import de.misi.tvbrowser.R;

public class ReminderDialog extends Dialog {

   public long broadcastId;

   public ReminderDialog(Context context) {
      super(context);
      initialize();
   }

   public ReminderDialog(Context context, int theme) {
      super(context, theme);
      initialize();
   }

   public ReminderDialog(Context context, boolean cancelable, OnCancelListener onCancelListener) {
      super(context, cancelable, onCancelListener);
      initialize();
   }

   private void initialize() {
      setContentView(R.layout.reminderdlg);
      Button button = (Button) findViewById(R.id.reminder_ok);
      button.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            doOk();
         }
      });
      button= (Button) findViewById(R.id.reminder_cancel);
      button.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            doCancel();
         }
      });
   }

   private void doOk() {
      //To change body of created methods use File | Settings | File Templates.
   }

   private void doCancel() {
      //To change body of created methods use File | Settings | File Templates.
   }
}

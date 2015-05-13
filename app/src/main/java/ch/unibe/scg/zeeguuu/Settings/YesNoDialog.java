package ch.unibe.scg.zeeguuu.Settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.DialogPreference;

import ch.unibe.scg.zeeguuu.R;
import ch.unibe.zeeguulibrary.ZeeguuAccount;

/**
 * Zeeguu Application
 * Created by Pascal on 20/04/15.
 */
public class YesNoDialog extends DialogPreference {
    private ZeeguuAccount account;
    private SettingsActivity context;

    public YesNoDialog(SettingsActivity context, ZeeguuAccount account) {
        super(context, null);
        this.context = context;
        this.account = account;
    }

    @Override
    protected void onClick() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.dialog_logout_confirmation);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.dialog_logout_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        account.logout();
                        context.finish();
                    }
                }

        );

        dialog.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener()

                {
                    @Override
                    public void onClick(DialogInterface dlg, int which) {
                        dlg.cancel();
                    }
                }

        );

        AlertDialog al = dialog.create();
        al.show();
    }
}
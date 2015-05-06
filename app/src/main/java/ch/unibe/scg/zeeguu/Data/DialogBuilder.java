package ch.unibe.scg.zeeguu.Data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.R;

/**
 * DialogBuilder which is responsible to create dialogs
 * Created by Pascal on 06/05/15.
 */
public class DialogBuilder {
    private Activity activity;

    private User user;
    private ConnectionManager connectionManager;
    private AlertDialog aDialog;


    public DialogBuilder(Activity activity, User user, ConnectionManager connectionManager) {
        this.activity = activity;
        this.user = user;
        this.connectionManager = connectionManager;
    }


    //// information Dialogs ////

    public void getLoginInformation(String tmpEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sign_in, null);

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                        EditText editTextPW = (EditText) aDialog.findViewById(R.id.dialog_password);

                        final String email = editTextEmail.getText().toString();
                        final String pw = editTextPW.getText().toString();

                        if (!user.userHasLoginInfo(email, pw)) {
                            Toast.makeText(activity, activity.getString(R.string.error_userinfo_invalid), Toast.LENGTH_LONG).show();
                            getLoginInformation(email);
                        } else if (!isEmailValid(email)) {
                            Toast.makeText(activity, R.string.error_email_not_valid, Toast.LENGTH_LONG).show();
                            getLoginInformation(email);
                        } else {
                            connectionManager.getSessionIdFromServer(email, pw);
                        }

                    }
                })
                .setNegativeButton(R.string.button_cancel, null);

        aDialog = builder.create();

        TextView noAccountMessage = (TextView) dialogView.findViewById(R.id.dialog_sign_in_no_account_textview);
        noAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
                EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                createNewAccount(editTextEmail.getText().toString(), "");
            }
        });
        aDialog.show();

        //if email address info available, put it in
        if (!tmpEmail.isEmpty()) {
            EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
            editTextEmail.setText(tmpEmail);
        }
    }


    public void createNewAccount(String tmpEmail, String tmpUsername) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_account, null);

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_create_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editTextUsername = (EditText) aDialog.findViewById(R.id.dialog_username);
                        EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                        EditText editTextpw = (EditText) aDialog.findViewById(R.id.dialog_password);

                        String username = editTextUsername.getText().toString();
                        String email = editTextEmail.getText().toString();
                        String pw = editTextpw.getText().toString();

                        if (!user.userHasLoginInfo(email, pw) || username.equals("")) {
                            Toast.makeText(activity, activity.getString(R.string.error_userinfo_invalid), Toast.LENGTH_LONG).show();
                            createNewAccount(email, username);
                        } else if (!isEmailValid(email)) {
                            Toast.makeText(activity, R.string.error_email_not_valid, Toast.LENGTH_LONG).show();
                            createNewAccount(email, username);
                        } else {
                            connectionManager.createAccountOnServer(username, email, pw);
                        }
                    }
                })
                .setNegativeButton(R.string.button_cancel, null);

        aDialog = builder.create();

        TextView noAccountMessage = (TextView) dialogView.findViewById(R.id.dialog_sign_in_no_account_textview);
        noAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
                //open login screen with last entered email address
                EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                getLoginInformation(editTextEmail.getText().toString());
            }
        });
        aDialog.show();

        //if email or username already entered, reload them
        if (!tmpEmail.isEmpty()) {
            EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
            editTextEmail.setText(tmpEmail);
        }
        if (!tmpUsername.isEmpty()) {
            EditText editTextUsername = (EditText) aDialog.findViewById(R.id.dialog_username);
            editTextUsername.setText(tmpUsername);
        }
    }

    //// validation functions ////

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}

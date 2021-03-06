/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.iota.wallet.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.helper.AESCrypt;
import org.iota.wallet.helper.Constants;

public class ChangeSeedPasswordDialog extends DialogFragment implements TextView.OnEditorActionListener {

    private TextInputLayout textInputLayoutPasswordCurrent;
    private TextInputLayout textInputLayoutPasswordNew;
    private TextInputLayout textInputLayoutPasswordNewConfirm;
    private TextInputEditText textInputEditTextPasswordCurrent;
    private TextInputEditText textInputEditTextPasswordNew;
    private TextInputEditText textInputEditTextPasswordNewConfirm;

    public ChangeSeedPasswordDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_change_seed_password, null);

        textInputLayoutPasswordCurrent = (TextInputLayout) view.findViewById(R.id.password_current_text_input_layout);
        textInputLayoutPasswordNew = (TextInputLayout) view.findViewById(R.id.password_new_text_input_layout);
        textInputLayoutPasswordNewConfirm = (TextInputLayout) view.findViewById(R.id.password_new_confirm_input_layout);
        textInputEditTextPasswordCurrent = (TextInputEditText) view.findViewById(R.id.password_current);
        textInputEditTextPasswordNew = (TextInputEditText) view.findViewById(R.id.password_new);
        textInputEditTextPasswordNewConfirm = (TextInputEditText) view.findViewById(R.id.password_new_confirm);

        textInputEditTextPasswordNewConfirm.setOnEditorActionListener(this);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.title_new_password)
                .setMessage(R.string.message_new_password)
                .setPositiveButton(R.string.buttons_save, null)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();


        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        changeSeedPassword();

                    }
                });
            }
        });

        alertDialog.show();
        return alertDialog;
    }


    private void changeSeedPassword() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String passwordCurrent = textInputEditTextPasswordCurrent.getText().toString();
        String passwordNew = textInputEditTextPasswordNew.getText().toString();
        String passwordNewConfirm = textInputEditTextPasswordNewConfirm.getText().toString();
        
        //reset errors
        textInputLayoutPasswordCurrent.setError(null);
        textInputLayoutPasswordNew.setError(null);
        textInputLayoutPasswordNewConfirm.setError(null);

        if (!passwordNew.equals(passwordNewConfirm))
            textInputLayoutPasswordNewConfirm.setError(getActivity().getString(R.string.messages_match_password));
        else if
                (passwordNew.isEmpty())
            textInputLayoutPasswordNew.setError(getActivity().getString(R.string.messages_empty_password));
        else
            try {
                AESCrypt aesOld = new AESCrypt(passwordCurrent);
                String encSeed = prefs.getString(Constants.PREFERENCE_ENC_SEED, "");
                aesOld.decrypt(encSeed);
                try {
                    AESCrypt aesNew = new AESCrypt(passwordNew);
                    prefs.edit().putString(Constants.PREFERENCE_ENC_SEED, aesNew.encrypt(String.valueOf(IOTA.seed))).apply();
                    getDialog().dismiss();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            } catch (Exception e) {
                e.getStackTrace();
                textInputLayoutPasswordCurrent.setError(getActivity().getString(R.string.messages_invalid_password));
                Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                textInputEditTextPasswordCurrent.startAnimation(shake);

            }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            changeSeedPassword();
        }
        return true;
    }
}
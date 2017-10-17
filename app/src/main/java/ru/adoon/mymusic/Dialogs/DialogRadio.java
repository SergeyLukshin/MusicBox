package ru.adoon.mymusic.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.adoon.mymusic.Activities.MainActivity;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;

public class DialogRadio extends DialogFragment implements DialogInterface.OnClickListener {

    public MusicItem mi = null;
    boolean bNeedIgnoreSelect = true;

    public DialogRadio()
    {
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        if (mi == null) adb.setTitle("Добавить радио");
        else adb.setTitle("Изменить радио");
        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_radio, null);
        // устанавливаем ее, как содержимое тела диалога
        adb.setView(view);

        adb.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        adb.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        EditText etURL = (EditText)(view).findViewById(R.id.etURL);
        EditText etName = (EditText)(view).findViewById(R.id.etName);

        if (mi != null) {
            etURL.setText(mi.url);
            etName.setText(mi.name);
            //if (mi.type == 0)
            //    etURL.setEnabled(false);
        }

        return adb.create();
    }

    public void colorAlertDialogTitle(AlertDialog dlg) {
        int color = dlg.getContext().getResources().getColor(R.color.colorPrimary);

        try {
            int dividerId = dlg.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            if (dividerId != 0) {
                View divider = dlg.findViewById(dividerId);
                divider.setBackgroundColor(color);
            }
        }
        catch (Exception ex) {
        }

        try {
            int textViewId = dlg.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
            if (textViewId != 0) {
                TextView tv = (TextView) dlg.findViewById(textViewId);
                tv.setTextColor(color);
            }
        }
        catch (Exception ex) {
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                break;
            case Dialog.BUTTON_NEGATIVE:
                break;
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog ad = (AlertDialog) getDialog();
        colorAlertDialogTitle(ad);
        Button positiveButton = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {
                EditText etURL = (EditText)(ad).findViewById(R.id.etURL);
                EditText etName = (EditText)(ad).findViewById(R.id.etName);
                //Spinner spinner = (Spinner) ad.findViewById(R.id.spSelect);

                if (etName.getText().toString().isEmpty())
                {
                    Toast.makeText(ad.getContext(), "Необходимо ввести наименование",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (etURL.getText().toString().isEmpty())
                {
                    Toast.makeText(ad.getContext(), "Необходимо ввести URL",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mi == null) {
                    mi = new MusicItem(null, 0, etName.getText().toString(), etURL.getText().toString(), false, false, Const.TYPE_RADIO, 0, 0);
                    ((MainActivity) getActivity()).AddRec(mi);
                }
                else {
                    if (!etURL.getText().toString().equalsIgnoreCase(mi.url)) mi.radioid = 0;
                    mi = new MusicItem(null, mi.id, etName.getText().toString(), etURL.getText().toString(), false, false, Const.TYPE_RADIO, mi.radioid, 0);
                    ((MainActivity) getActivity()).UpdRec(mi);
                }

                ad.dismiss();
            }
        });
    }
}

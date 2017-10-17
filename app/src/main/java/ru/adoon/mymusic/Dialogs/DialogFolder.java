package ru.adoon.mymusic.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.adoon.mymusic.Activities.MainActivity;
import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;

public class DialogFolder extends DialogFragment implements DialogInterface.OnClickListener, DialogOpenFile.DialogOpenFileListener, View.OnClickListener {

    public MusicItem mi = null;
    boolean bNeedIgnoreSelect = true;
    Button btn;

    public void OnSelectedFiles(ArrayList<FileItem> files) {
        if (files.size() > 0) {
            AlertDialog ad = (AlertDialog) getDialog();
            EditText etURL = (EditText) (ad).findViewById(R.id.etURL);
            etURL.setText(files.get(0).m_strName);
        }
    }

    /*public void OnSelectedDir(String fileName) {
        AlertDialog ad = (AlertDialog) getDialog();
        EditText etURL = (EditText) (ad).findViewById(R.id.etURL);
        etURL.setText(fileName);
    }*/

    public DialogFolder() {
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        if (mi == null) adb.setTitle("Добавить папку");
        else adb.setTitle("Изменить папку");
        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_folder, null);
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

        btn = (Button)view.findViewById(R.id.button);
        btn.setOnClickListener(this);

        EditText etURL = (EditText) (view).findViewById(R.id.etURL);
        EditText etName = (EditText) (view).findViewById(R.id.etName);
        CheckBox cbSubFolder = (CheckBox) (view).findViewById(R.id.cbSubFolder);
        CheckBox cbRandom = (CheckBox) (view).findViewById(R.id.cbRandom);
        etURL.setEnabled(false);

        if (mi != null) {
            etURL.setText(mi.url);
            etName.setText(mi.name);

            cbSubFolder.setChecked(mi.bSubFolder);
            cbRandom.setChecked(mi.bRandom);
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

    public void onClick(View v) {
        DialogOpenFile dlg = new DialogOpenFile();
        dlg.m_bOnlyDir = true;
        dlg.setOpenDialogListener(this);
        dlg.show(getFragmentManager(), "DialogOpenFile");
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
                CheckBox cbSubFolder = (CheckBox) (ad).findViewById(R.id.cbSubFolder);
                CheckBox cbRandom = (CheckBox) (ad).findViewById(R.id.cbRandom);
                //Spinner spinner = (Spinner) ad.findViewById(R.id.spSelect);

                if (etName.getText().toString().isEmpty())
                {
                    Toast.makeText(ad.getContext(), "Необходимо ввести наименование",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (etURL.getText().toString().isEmpty())
                {
                    Toast.makeText(ad.getContext(), "Необходимо указать папку",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mi == null) {
                    mi = new MusicItem(null, 0, etName.getText().toString(), etURL.getText().toString(), cbSubFolder.isChecked(), cbRandom.isChecked(), Const.TYPE_FOLDER, 0, 0);
                    ((MainActivity) getActivity()).AddRec(mi);
                }
                else {
                    mi = new MusicItem(null, mi.id, etName.getText().toString(), etURL.getText().toString(), cbSubFolder.isChecked(), cbRandom.isChecked(), Const.TYPE_FOLDER, 0, 0);
                    ((MainActivity) getActivity()).UpdRec(mi);
                }

                ad.dismiss();
            }
        });
    }
}

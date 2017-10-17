package ru.adoon.mymusic.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.adoon.mymusic.Activities.MainActivity;
import ru.adoon.mymusic.Adapters.EqualizerAdapter;
import ru.adoon.mymusic.Adapters.RadioItemAdapter;
import ru.adoon.mymusic.Classes.EqualizerBandItem;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Classes.RadioItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;
import ru.adoon.mymusic.Services.MediaService;

public class DialogEqualizer extends DialogFragment implements DialogInterface.OnClickListener, View.OnClickListener {

    EqualizerAdapter miAdapter;
    ArrayList<EqualizerBandItem> objects = null;
    ListView lvData;
    CheckBox cbUseEq;
    SharedPreferences sPref;
    Context ctx;
    public Activity parent;
    ArrayList<EqualizerBandItem> m_vEqBands = new ArrayList<EqualizerBandItem>();

    public DialogEqualizer() {
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("Эквалайзер");
        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_equalizer, null);
        // устанавливаем ее, как содержимое тела диалога
        adb.setView(view);

        ctx = adb.getContext();

        MediaService.LoadPref(ctx);

        miAdapter = new EqualizerAdapter(adb.getContext());
        miAdapter.objects = new ArrayList<EqualizerBandItem>();

        lvData = (ListView) view.findViewById(R.id.lvBands);
        lvData.setAdapter(miAdapter);

        miAdapter.m_iUseEq = MediaService.m_iUseEq;

        cbUseEq = (CheckBox)view.findViewById(R.id.cbUseEq);
        cbUseEq.setOnClickListener(this);

        if (MediaService.m_iUseEq != 0) cbUseEq.setChecked(true);

        /*if (cbUseEq.isChecked()) {
            for (int i = 0; i < lvData.getChildCount(); i++) {
                lvData.getChildAt(i).setEnabled(true);
            }
        }
        else {
            for (int i = 0; i < lvData.getChildCount(); i++) {
                lvData.getChildAt(i).setEnabled(false);
            }
        }*/

        m_vEqBands.clear();
        for (int i = 0; i < MediaService.m_vEqBands.size(); i++) {
            m_vEqBands.add(new EqualizerBandItem(MediaService.m_vEqBands.get(i).m_iName, MediaService.m_vEqBands.get(i).m_iVal));
        }

        if (MediaService.mediaPlayer == null)
            MediaService.mediaPlayer = new MediaPlayer();
        if (MediaService.equalizer == null)
            MediaService.equalizer = new Equalizer(0, MediaService.mediaPlayer.getAudioSessionId());
        MediaService.equalizer.setEnabled(true);
        MediaService.SetEqPref();

        short bands = MediaService.equalizer.getNumberOfBands();
        miAdapter.min = MediaService.equalizer.getBandLevelRange()[0];
        miAdapter.max = MediaService.equalizer.getBandLevelRange()[1];
        for(short i = 0; i < bands; i++)
        {
            int name = MediaService.equalizer.getCenterFreq(i)/1000;
            int band_level = MediaService.equalizer.getBandLevel(i);

            miAdapter.objects.add(new EqualizerBandItem(name, band_level));
        }
        miAdapter.notifyDataSetChanged();

        adb.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        adb.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

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

    public void onClick(View v) {
        if (v.getId() == R.id.cbUseEq) {
            /*if (cbUseEq.isChecked()) {
                for (int i = 0; i < lvData.getChildCount(); i++) {
                    lvData.getChildAt(i).setEnabled(true);
                }
            }
            else {
                for (int i = 0; i < lvData.getChildCount(); i++) {
                    lvData.getChildAt(i).setEnabled(false);
                }
            }*/
            miAdapter.m_iUseEq = cbUseEq.isChecked() ? 1 : 0;
            if (!cbUseEq.isChecked()) {
                MediaService.SetEqDefaultPref();

                miAdapter.objects.clear();
                short bands = MediaService.equalizer.getNumberOfBands();
                for(short i = 0; i < bands; i++)
                {
                    int name = MediaService.equalizer.getCenterFreq(i)/1000;
                    int band_level = MediaService.equalizer.getBandLevel(i);

                    miAdapter.objects.add(new EqualizerBandItem(name, band_level));
                }
            }
            miAdapter.notifyDataSetChanged();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        // нужно вернуться к предыдущим настройкам
        MediaService.LoadPref(ctx);
        MediaService.SetEqPref();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (parent != null) {
            parent.finish();
            parent = null;
        }
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

                sPref = ctx.getSharedPreferences(Const.EQUALIZER_PREF, ctx.MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                if (cbUseEq.isChecked()) {
                    editor.putInt(Const.PREF_USE_EQ, 1);
                    MediaService.m_iUseEq = 1;
                }
                else {
                    editor.putInt(Const.PREF_USE_EQ, 0);
                    MediaService.m_iUseEq = 0;
                }

                String strInfo = "";
                MediaService.m_vEqBands.clear();
                if (MediaService.m_iUseEq == 1) {
                    for (int i = 0; i < miAdapter.objects.size(); i++) {
                        int iName = miAdapter.objects.get(i).m_iName;
                        int iVal = miAdapter.objects.get(i).m_iVal;
                        if (i == 0) strInfo = String.valueOf(iName) + ";" + String.valueOf(iVal);
                        else
                            strInfo = strInfo + "," + String.valueOf(iName) + ";" + String.valueOf(iVal);

                        MediaService.m_vEqBands.add(new EqualizerBandItem(iName, iVal));
                    }
                }
                editor.putString(Const.PREF_EQ_INFO, strInfo);

                editor.commit();
                MediaService.SetEqPref();

                Intent i = new Intent(Const.FORCE_WIDGET_UPDATE);
                ctx.sendBroadcast(i);

                ad.dismiss();
                if (parent != null) {
                    parent.finish();
                    parent = null;
                }
            }
        });

        Button negativeButton = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {

                // нужно вернуться к предыдущим настройкам
                MediaService.LoadPref(ctx);
                MediaService.SetEqPref();

                ad.dismiss();
                if (parent != null) {
                    parent.finish();
                    parent = null;
                }
            }
        });
    }
}

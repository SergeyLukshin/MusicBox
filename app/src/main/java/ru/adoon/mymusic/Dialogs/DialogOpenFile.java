package ru.adoon.mymusic.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ru.adoon.mymusic.Adapters.FileItemAdapter;
import ru.adoon.mymusic.Adapters.OpenFileAdapter;
import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.R;

/**
 * Created by Лукшин on 01.09.2017.
 */

public class DialogOpenFile extends DialogFragment implements DialogInterface.OnClickListener, View.OnClickListener {
    OpenFileAdapter miAdapter;
    Context ctx;
    boolean m_bOnlyDir = false;
    private String[] extensions = null;
    private DialogOpenFileListener listener;

    public ListView lvFiles;
    public TextView tvEmpty;
    public TextView title;

    public DialogOpenFile() {
    }

    public interface DialogOpenFileListener {
        public void OnSelectedFiles(ArrayList<FileItem> files);
    }

    public void setOpenDialogListener(DialogOpenFileListener listener) {
        this.listener = listener;
    }

    public static int dipToPx(Context context, int dip) {
        DisplayMetrics displayMetrics = context.getResources()
                .getDisplayMetrics();
        return (int) (dip * displayMetrics.density + 0.5f);
    }

    private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeightSmall, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        int itemHeight = getItemHeight(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        textView.setMinHeight(itemHeight);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        //textView.setPadding(15, 0, 0, 0);
        return textView;
    }

    private TextView createTitle(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
        textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        int dp_10 = dipToPx(context, 10);
        textView.setPadding(dp_10, dp_10, dp_10, dp_10);
        return textView;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        /*if (m_bOnlyDir)
            adb.setTitle("Выбор папки");
        else
            adb.setTitle("Выбор файлов");*/

        title = createTitle(adb.getContext());
        adb.setCustomTitle(title);

        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_open_file, null);
        // устанавливаем ее, как содержимое тела диалога
        adb.setView(view);

        adb.setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        adb.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        ctx = adb.getContext();

        LinearLayout ll = (LinearLayout) (view).findViewById(R.id.llUp);
        ll.setOnClickListener(this);
        LinearLayout ll_main = (LinearLayout) (view).findViewById(R.id.llMain);
        setLayoutSize(ctx, ll_main);
        //CheckBox cbRandom = (CheckBox) (view).findViewById(R.id.cbRandom);

        // находим список
        lvFiles = (ListView) view.findViewById(R.id.lvFiles);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        miAdapter = new OpenFileAdapter(adb.getContext(), this);
        miAdapter.objects = new ArrayList<FileItem>();
        miAdapter.m_bOnlyDir = m_bOnlyDir;
        if (extensions != null)
            miAdapter.setFilterFileExtention(extensions);
        miAdapter.GetFiles(miAdapter.currentPath);
        miAdapter.changeTitle();

        // присваиваем адаптер списку
        lvFiles.setAdapter(miAdapter);
        lvFiles.setSelector(R.drawable.list_selector);
        lvFiles.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvFiles.setLongClickable(true);

        return adb.create();
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private static Point getScreenSize(Context context) {
        Point screeSize = new Point();
        getDefaultDisplay(context).getSize(screeSize);
        return screeSize;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }

    private LinearLayout setLayoutSize(Context context, LinearLayout linearLayout) {
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

    public void setFilterFileExtention(String... ext) {
        extensions = ext;
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
        if (v.getId() == R.id.llUp) {
            File file = new File(miAdapter.currentPath);
            File parentDirectory = file.getParentFile();
            if (parentDirectory != null) {
                miAdapter.currentPath = parentDirectory.getPath();
                miAdapter.GetFiles(miAdapter.currentPath);
                miAdapter.changeTitle();
            }
        }
        /*if (v.getId() == R.id.ivDelete) {

            if (miAdapter.GetSelectedRecordsCount() == 0) {
                Toast.makeText(ctx, "Необходимо выбрать файлы",
                        Toast.LENGTH_LONG).show();
                return;
            }

            AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
            adb.setTitle(R.string.alert_record_delete_title);
            adb.setMessage(R.string.alert_playlist_record_delete);
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    miAdapter.DeleteSelectedRecords();

                    Toast.makeText(ctx, "Файлы удалены из плейлиста",
                            Toast.LENGTH_SHORT).show();
                }
            });
            adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                }
            });
            // создаем диалог
            AlertDialog alert = adb.create();
            alert.show();
        }*/
    }

    public void onClick(DialogInterface dialog, int which) {
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

                if (listener != null) {
                    ArrayList<FileItem> files = new ArrayList<FileItem>();
                    for (int i = 0; i < miAdapter.objects.size(); i++) {
                        if (miAdapter.objects.get(i).m_bCheck) {
                            files.add(new FileItem(miAdapter.objects.get(i).m_strName, miAdapter.objects.get(i).m_strShortName, miAdapter.objects.get(i).m_bDir));
                        }
                    }

                    if (files.size() == 0) {
                        Toast.makeText(ad.getContext(), "Необходимо выбрать папки или файлы",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listener.OnSelectedFiles(files);
                }
                /*EditText etName = (EditText) (ad).findViewById(R.id.etName);
                CheckBox cbRandom = (CheckBox) (ad).findViewById(R.id.cbRandom);

                if (etName.getText().toString().isEmpty()) {
                    Toast.makeText(ad.getContext(), "Необходимо ввести наименование",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mi == null) {
                    mi = new MusicItem(0, etName.getText().toString(), "", false, cbRandom.isChecked(), Const.TYPE_PLAYLIST, 0, 0);
                    mi.files = new ArrayList<FileItem>();
                    for (int i = 0; i < miAdapter.objects.size(); i++) {
                        mi.files.add(new FileItem(miAdapter.objects.get(i).m_strName, miAdapter.objects.get(i).m_strShortName));
                    }
                    ((MainActivity) getActivity()).AddRec(mi);
                } else {
                    mi = new MusicItem(mi.id, etName.getText().toString(), "", false, cbRandom.isChecked(), Const.TYPE_PLAYLIST, 0, 0);
                    mi.files = new ArrayList<FileItem>();
                    for (int i = 0; i < miAdapter.objects.size(); i++) {
                        mi.files.add(new FileItem(miAdapter.objects.get(i).m_strName, miAdapter.objects.get(i).m_strShortName));
                    }
                    ((MainActivity) getActivity()).UpdRec(mi);
                }*/

                ad.dismiss();
            }
        });
    }
}

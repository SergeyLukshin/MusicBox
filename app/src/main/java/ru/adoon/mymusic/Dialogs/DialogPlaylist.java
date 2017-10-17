package ru.adoon.mymusic.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.adoon.mymusic.Activities.MainActivity;
import ru.adoon.mymusic.Adapters.FileItemAdapter;
import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;

/**
 * Created by Лукшин on 30.08.2017.
 */

public class DialogPlaylist extends DialogFragment implements DialogInterface.OnClickListener, DialogOpenFile.DialogOpenFileListener, View.OnClickListener {

    public MusicItem mi = null;
    boolean bNeedIgnoreSelect = true;
    ImageView ivAdd;
    ImageView ivDelete;
    FileItemAdapter miAdapter;
    Context ctx;
    ListView lvFiles;

    public void OnSelectedFiles(ArrayList<FileItem> files) {
        int iCnt = 0;
        for (int i = 0; i < files.size(); i++) {
            if (!files.get(i).m_bDir)
                iCnt += miAdapter.AddFile(files.get(i).m_strName);
            else
                iCnt += miAdapter.AddFilesInDir(files.get(i).m_strName);
        }
        if (iCnt > 0)
            Toast.makeText(ctx, "Выбранные файлы добавлены в плейлист", Toast.LENGTH_LONG).show();
    }

    /*public void OnSelectedDir(String dirName) {
        int iCnt = miAdapter.AddFilesInDir(dirName);
        if (iCnt > 0)
            Toast.makeText(ctx, "Файлы из выбранной папки добавлены в плейлист", Toast.LENGTH_LONG).show();
    }*/

    public DialogPlaylist() {
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        int iCnt = listAdapter.getCount();
        for (int i = 0; i < iCnt; i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (iCnt - 1));
        listView.setLayoutParams(params);
    }

    /*public void showOptionsMenu(int position)
    {
        new AlertDialog.Builder(ctx)
                .setTitle("test").setCancelable(true).setItems(R.array.menu_playlist,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        //take actions here according to what the user has selected
                    }
                }
        ).show();
    }*/

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        if (mi == null) adb.setTitle("Добавить плейлист");
        else adb.setTitle("Изменить плейлист");
        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_playlist, null);
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

        ctx = adb.getContext();

        EditText etName = (EditText) (view).findViewById(R.id.etName);
        CheckBox cbRandom = (CheckBox) (view).findViewById(R.id.cbRandom);

        LinearLayout llMain = (LinearLayout) (view).findViewById(R.id.llMain);
        setLayoutSize(ctx, llMain);

        // находим список
        lvFiles = (ListView) view.findViewById(R.id.lvFiles);

        miAdapter = new FileItemAdapter(adb.getContext());
        miAdapter.objects = new ArrayList<FileItem>();
        if (mi != null && mi.files != null) {
            for (int i = 0; i < mi.files.size(); i++)
                miAdapter.objects.add(new FileItem(mi.files.get(i).m_strName, mi.files.get(i).m_strShortName));
        }

        // присваиваем адаптер списку
        lvFiles.setAdapter(miAdapter);
        lvFiles.setSelector(R.drawable.list_selector);
        lvFiles.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //getActivity().registerForContextMenu(lvFiles);
        lvFiles.setLongClickable(true);
        /*lvFiles.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
            {
                showOptionsMenu(position);
                return true;
            }
        });
        lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                // TODO Auto-generated method stub
                Toast.makeText(ctx, "Clicked at Position" + position, Toast.LENGTH_SHORT).show();
            }
        });*/

        if (mi != null) {
            etName.setText(mi.name);
            cbRandom.setChecked(mi.bRandom);
        }

        ivAdd = (ImageView)view.findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(this);

        ivDelete = (ImageView)view.findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(this);

        return adb.create();
    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, R.id.action_delete, 0, "Удалить");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = acmi.position;

        if (item.getItemId() == R.id.action_delete) {

            miAdapter.DeleteRecord(pos);
            return true;
        }
        return super.onContextItemSelected(item);
    }*/

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

    public void onClick(View v) {
        if (v.getId() == R.id.ivAdd) {

            DialogOpenFile dlg = new DialogOpenFile();
            dlg.setFilterFileExtention("mp3", "wav", "ogg", "aac");
            dlg.setOpenDialogListener(this);
            dlg.show(getFragmentManager(), "DialogOpenFile");
        }
        if (v.getId() == R.id.ivDelete) {

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
                EditText etName = (EditText) (ad).findViewById(R.id.etName);
                CheckBox cbRandom = (CheckBox) (ad).findViewById(R.id.cbRandom);

                if (etName.getText().toString().isEmpty()) {
                    Toast.makeText(ad.getContext(), "Необходимо ввести наименование",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mi == null) {
                    mi = new MusicItem(null, 0, etName.getText().toString(), "", false, cbRandom.isChecked(), Const.TYPE_PLAYLIST, 0, 0);
                    mi.files = new ArrayList<FileItem>();
                    for (int i = 0; i < miAdapter.objects.size(); i++) {
                        mi.files.add(new FileItem(miAdapter.objects.get(i).m_strName, miAdapter.objects.get(i).m_strShortName));
                    }
                    ((MainActivity) getActivity()).AddRec(mi);
                } else {
                    mi = new MusicItem(null, mi.id, etName.getText().toString(), "", false, cbRandom.isChecked(), Const.TYPE_PLAYLIST, 0, 0);
                    mi.files = new ArrayList<FileItem>();
                    for (int i = 0; i < miAdapter.objects.size(); i++) {
                        mi.files.add(new FileItem(miAdapter.objects.get(i).m_strName, miAdapter.objects.get(i).m_strShortName));
                    }
                    ((MainActivity) getActivity()).UpdRec(mi);
                }

                ad.dismiss();
            }
        });
    }
}

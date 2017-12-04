package ru.adoon.mymusic.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import ru.adoon.mymusic.Adapters.FolderItemAdapter;
import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Classes.FolderItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;
import ru.adoon.mymusic.Services.MediaService;

/**
 * Created by Лукшин on 29.06.2017.
 */

public class DialogFolderSelect extends DialogFragment implements View.OnClickListener {
    ListView lvData;
    FolderItemAdapter miAdapter;
    ArrayList<FolderItem> objects = null;
    ArrayList<FolderItem> filtered_objects = null;
    boolean bNeedIgnoreSelect = true;
    LinearLayout llLoad = null, llMain = null;
    FolderInfo task;
    EditText inputSearch;
    TextView tvFindInfo, tvStop;
    ProgressBar pb;
    private FilenameFilter filenameFilter;
    private HashSet<String> extensions = null;
    boolean mStop = false;

    public class IDComparator
            implements Comparator
    {
        public int compare(Object ri1, Object ri2) {
            FolderItem s1 = ((FolderItem)ri1);
            FolderItem s2 = ((FolderItem)ri2);
            return s1.m_strName.compareToIgnoreCase(s2.m_strName);
        }
    }

    private String getFileExtension(String filename) {
        String ext = "";
        int i = filename .lastIndexOf('.');
        if(i != -1 && i < filename .length()) {
            ext = filename.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public class FolderInfo extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                ArrayList<String> vecDirs = new ArrayList<>();
                vecDirs.add("/sdcard");
                while (vecDirs.size() > 0) {

                    if (mStop) return null;

                    String strDir = vecDirs.get(0);
                    vecDirs.remove(0);
                    File dir = new File(strDir);
                    File[] list = dir.listFiles(filenameFilter);
                    if (list != null) {
                        boolean bFind = false;
                        for (File file : list) {

                            if (mStop) return null;

                            if (file.isDirectory()) {
                                vecDirs.add(file.getPath());
                            } else {
                                if (!bFind) {
                                    /*String strFile = file.getPath();
                                    String ext = getFileExtension(strFile);
                                    if (ext.compareToIgnoreCase("mp3") == 0 || ext.compareToIgnoreCase("wav") == 0 || ext.compareToIgnoreCase("ogg") == 0 || ext.compareToIgnoreCase("aac") == 0) {*/
                                        // папку можно добавлять
                                        bFind = true;
                                        objects.add(new FolderItem(dir.getName(), dir.getPath()));
                                        publishProgress(objects.size());
                                    //}
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            //add the tours from internet to the array
            Comparator mc;
            mc = new IDComparator();
            Collections.sort(objects, mc);

            miAdapter.objects = objects;
            llLoad.setVisibility(View.GONE);
            llMain.setVisibility(View.VISIBLE);

            miAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            tvFindInfo.setText("Найдено " + String.valueOf(progress[0]) + " папок");
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.tvStop || v.getId() == R.id.progressBar) {
            mStop = true;
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.action_add_folder_select);
        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_folder_select, null);
        // устанавливаем ее, как содержимое тела диалога
        adb.setView(view);

        llLoad = (LinearLayout) view.findViewById(R.id.llLoad);
        llMain = (LinearLayout) view.findViewById(R.id.llMain);

        miAdapter = new FolderItemAdapter(adb.getContext());
        miAdapter.objects = new ArrayList<FolderItem>();
        filtered_objects = new ArrayList<FolderItem>();
        objects = new ArrayList<FolderItem>();

        tvFindInfo = (TextView) view.findViewById(R.id.tvFindInfo);
        tvStop = (TextView) view.findViewById(R.id.tvStop);
        tvStop.setOnClickListener(this);
        pb = (ProgressBar)view.findViewById(R.id.progressBar);
        pb.setOnClickListener(this);

        inputSearch = (EditText) view.findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                //Когда пользователь вводит какой-нибудь текст:
                //miAdapter.getFilter().filter(cs);
                filtered_objects.clear();
                for (int i = 0; i < objects.size(); i++) {
                    FolderItem ri = objects.get(i);
                    if (cs == "" || ri.m_strName.toLowerCase().contains(cs)) {
                        filtered_objects.add(ri);
                    }
                }
                miAdapter.objects = filtered_objects;
                miAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        extensions = new HashSet<String>();
        extensions.add("mp3");
        extensions.add("wav");
        extensions.add("ogg");
        extensions.add("aac");

        filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                if (tempFile.isFile())
                    return extensions.isEmpty() ||
                            extensions.contains(getFileExtension(tempFile.getName()).toLowerCase());
                return true;
            }
        };

        task = new FolderInfo();
        task.execute();

        lvData = (ListView) view.findViewById(R.id.lvRadio);
        lvData.setAdapter(miAdapter);
        //lvData.setSelector(R.drawable.list_selector);
        lvData.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                //((ConfigAdapter)parent.).select_pos = position;
            }
        });

        adb.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        adb.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                task.cancel(true);
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

    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog ad = (AlertDialog) getDialog();
        colorAlertDialogTitle(ad);
        Button positiveButton = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {

                int iCnt = 0;
                for (FolderItem ri : objects) {
                    if (ri.m_bCheck)
                        iCnt++;
                }

                if (iCnt + MediaService.musicBox.getCount() > 30) {
                    Toast.makeText(ad.getContext(), "Общее кол-во добавленных элементов не должно превышать 30.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                for (FolderItem ri : objects) {
                    if (ri.m_bCheck) {
                        MusicItem mi = new MusicItem(null, 0, ri.m_strName, ri.m_strURL, false, false, Const.TYPE_FOLDER, 0, 0);
                        MediaService.musicBox.addRec(mi);
                    }
                }

                ad.dismiss();
            }
        });
    }
}

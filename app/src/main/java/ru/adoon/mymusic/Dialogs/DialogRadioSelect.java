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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.adoon.mymusic.Adapters.RadioItemAdapter;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.R;
import ru.adoon.mymusic.Classes.RadioItem;

/**
 * Created by Лукшин on 29.06.2017.
 */

public class DialogRadioSelect extends DialogFragment implements View.OnClickListener {
    ListView lvData;
    RadioItemAdapter miAdapter;
    ArrayList<RadioItem> objects = null;
    ArrayList<RadioItem> filtered_objects = null;
    boolean bNeedIgnoreSelect = true;
    LinearLayout llLoad = null, llMain = null;
    RadioInfo task;
    EditText inputSearch;
    TextView tvFindInfo, tvStop;
    ProgressBar pb;
    boolean mStop = false;

    public class IDComparator
            implements Comparator
    {
        public int compare(Object ri1, Object ri2) {
            RadioItem s1 = ((RadioItem)ri1);
            RadioItem s2 = ((RadioItem)ri2);
            return s1.m_strName.compareToIgnoreCase(s2.m_strName);
        }
    }

    public class RadioInfo extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Document doc;
            try {
                int err = 0;
                for (int i = 1; i < 1000; i++) {

                    if (mStop) return null;
                    // http://101.ru/api/channel/getServers/200/channel/MP3/?dataFormat=json
                    // http://101.ru/foot-radio-search/search/moscow
                    // http://101.ru/api/channel/getServers/723958/personal/?dataFormat=json
                    URL url = new URL("http://101.ru/api/channel/getServers/" + String.valueOf(i) + "/channel/MP3/128/?dataFormat=json");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(10000);
                    urlConnection.connect();

                    //BufferedReader reader= new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String strJson = "", str;
                    // titleTrack, titleExecutor, duration, lastTime
                    while ((str = reader.readLine()) != null) {
                        strJson += str;
                    }

                    JSONObject dataJsonObj = null;
                    String secondName = "";

                    try {
                        dataJsonObj = new JSONObject(strJson);
                        int status = dataJsonObj.getInt("status");
                        if (status == 1) {
                            JSONArray dataResult = dataJsonObj.getJSONArray("result");

                            // 1. достаем инфо о втором друге - индекс 1
                            JSONObject data0 = ((JSONObject) dataResult.get(0));
                            if (data0 != null) {

                                // title_executor

                                String titleChannel = data0.getString("titleChannel");
                                String strURL = data0.getString("urlStream");

                                objects.add(new RadioItem(titleChannel, strURL, i));
                                publishProgress(objects.size());
                                //miAdapter.notifyDataSetChanged();
                                err = 0;
                            }
                            else
                                err ++;
                        }
                        else
                            err ++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        err ++;
                    }

                    if (err > 20) break;
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

            tvFindInfo.setText("Найдено " + String.valueOf(progress[0]) + " станций");
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.tvStop || v.getId() == R.id.progressBar) {
            mStop = true;
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.action_add_radio_select);
        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_radio_select, null);
        // устанавливаем ее, как содержимое тела диалога
        adb.setView(view);

        llLoad = (LinearLayout) view.findViewById(R.id.llLoad);
        llMain = (LinearLayout) view.findViewById(R.id.llMain);

        miAdapter = new RadioItemAdapter(adb.getContext());
        miAdapter.objects = new ArrayList<RadioItem>();
        filtered_objects = new ArrayList<RadioItem>();
        objects = new ArrayList<RadioItem>();

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
                    RadioItem ri = objects.get(i);
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

        task = new RadioInfo();
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
                for (RadioItem ri : objects) {
                    if (ri.m_bCheck)
                        iCnt++;
                }

                if (iCnt + MediaService.musicBox.getCount() > 30) {
                    Toast.makeText(ad.getContext(), "Общее кол-во добавленных элементов не должно превышать 30.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                for (RadioItem ri : objects) {
                    if (ri.m_bCheck) {
                        MusicItem mi = new MusicItem(null, 0, ri.m_strName, ri.m_strURL, false, false, Const.TYPE_RADIO, ri.m_iRadioID, 0);
                        MediaService.musicBox.addRec(mi);
                    }
                }

                ad.dismiss();
            }
        });
    }
}

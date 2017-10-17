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

public class DialogRadioSelect extends DialogFragment {
    ListView lvData;
    RadioItemAdapter miAdapter;
    ArrayList<RadioItem> objects = null;
    ArrayList<RadioItem> filtered_objects = null;
    boolean bNeedIgnoreSelect = true;
    LinearLayout llLoad = null, llMain = null;
    RadioInfo task;
    EditText inputSearch;
    TextView tvFindInfo;

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
                    // http://101.ru/api/channel/getServers/200/channel/MP3/?dataFormat=json
                    // http://101.ru/foot-radio-search/search/moscow
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("Добавить радио с сайта 101.ru");
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

        /*miAdapter.objects.add(new RadioItem("Like FM", "http://ic3.101.ru:8000/v12_1"));
        miAdapter.objects.add(new RadioItem("NRJ", "http://ic3.101.ru:8000/v1_1?setst=-1"));
        miAdapter.objects.add(new RadioItem("Юмор FM", "http://ic3.101.ru:8000/s11"));
        miAdapter.objects.add(new RadioItem("Мегаполис", "http://stream04.media.rambler.ru/megapolis128.mp3"));
        miAdapter.objects.add(new RadioItem("Страна FM", "http://icecast.stranafm.cdnvideo.ru/stranafm"));
        miAdapter.objects.add(new RadioItem("Авторадио", "http://ic3.101.ru:8000/s1"));
        miAdapter.objects.add(new RadioItem("Релакс FM", "http://ic3.101.ru:8000/s40"));
        miAdapter.objects.add(new RadioItem("Эхо Москвы", "http://81.19.85.197/echo.mp3"));
        miAdapter.objects.add(new RadioItem("Культура", "http://icecast.vgtrk.cdnvideo.ru/kulturafm"));
        miAdapter.objects.add(new RadioItem("Москва FM", "http://icecast.vgtrk.cdnvideo.ru:8000/moscowfm128"));
        miAdapter.objects.add(new RadioItem("Спорт FM", "http://sportfm128.streamr.ru"));
        miAdapter.objects.add(new RadioItem("Восток FM", "http://vostokfm.hostingradio.ru:8028/vostokfm128.mp3"));
        miAdapter.objects.add(new RadioItem("Коммерсантъ FM", "http://kommersant77.hostingradio.ru:8016/kommersant128.mp3"));
        miAdapter.objects.add(new RadioItem("Весна FM", "http://91.203.176.214:8000/vesnafm"));
        miAdapter.objects.add(new RadioItem("Говорит Москва", "http://media.govoritmoskva.ru:8000/rufm_m.mp3"));
        miAdapter.objects.add(new RadioItem("Rock FM", "http://nashe1.hostingradio.ru/rock-256"));
        miAdapter.objects.add(new RadioItem("Дорожное радио", "http://dorognoe.hostingradio.ru:8000/radio"));
        miAdapter.objects.add(new RadioItem("Такси FM", "http://95.161.228.74:8000/13_taxi_256"));
        miAdapter.objects.add(new RadioItem("Детское радио", "http://ic3.101.ru:8000/s50"));
        miAdapter.objects.add(new RadioItem("Вести FM", "http://icecast.vgtrk.cdnvideo.ru/vestifm"));
        miAdapter.objects.add(new RadioItem("Chocolate Radio", "http://choco.hostingradio.ru:10010/fm"));
        miAdapter.objects.add(new RadioItem("Новое радио", "http://newradio.fmtuner.ru/newradio3"));
        miAdapter.objects.add(new RadioItem("Романтика", "http://ic3.101.ru:8000/s30"));
        miAdapter.objects.add(new RadioItem("Радио Русский хит", "http://s9.imgradio.pro/RusHit48"));
        miAdapter.objects.add(new RadioItem("Best FM", "http://nashe1.hostingradio.ru/best-256"));
        miAdapter.objects.add(new RadioItem("Comedy Radio", "http://ic3.101.ru:8000/s60"));
        miAdapter.objects.add(new RadioItem("Шансон", "http://chanson.hostingradio.ru:8041/chanson256.mp3"));
        miAdapter.objects.add(new RadioItem("Маяк", "http://icecast.vgtrk.cdnvideo.ru/mayakfm"));
        miAdapter.objects.add(new RadioItem("Радио 7 на семи холмах", "http://radio7server.streamr.ru:8040/radio7256.mp3"));
        miAdapter.objects.add(new RadioItem("Книга", "http://bookradio.hostingradio.ru:8069/fm"));
        miAdapter.objects.add(new RadioItem("Capital FM", "http://icecast.vgtrk.cdnvideo.ru/capitalfmmp3"));
        miAdapter.objects.add(new RadioItem("Европа плюс", "http://ep256.streamr.ru"));
        miAdapter.objects.add(new RadioItem("Love Радио", "http://stream2.n340.com:80/12_love_24_reg_44?type=.aac&UID=18A22C115E21FCCCFBDDFA57E14E8333"));
        miAdapter.objects.add(new RadioItem("Милицейская волна", "http://radio.mvd.ru:8000/mv128.mp3"));
        miAdapter.objects.add(new RadioItem("Радио Рекорд", "http://air2.radiorecord.ru:805/rr_320"));
        miAdapter.objects.add(new RadioItem("Радио России", "http://icecast.vgtrk.cdnvideo.ru/rrzonam"));
        miAdapter.objects.add(new RadioItem("Радио Мир", "http://icecast.mirtv.cdnvideo.ru:8000/radio_mir128"));
        miAdapter.objects.add(new RadioItem("Радио Ultra", "http://nashe1.hostingradio.ru/ultra-256"));
        miAdapter.objects.add(new RadioItem("Kiss FM", "http://online-kissfm.tavrmedia.ua:80/KissFM"));*/

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
        int dividerId = dlg.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dlg.findViewById(dividerId);
            divider.setBackgroundColor(color);
        }

        int textViewId = dlg.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        if (textViewId != 0) {
            TextView tv = (TextView) dlg.findViewById(textViewId);
            tv.setTextColor(color);
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

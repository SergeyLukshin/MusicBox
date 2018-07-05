package ru.adoon.mymusic.Activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import java.util.ArrayList;

import ru.adoon.mymusic.Adapters.ConfigAdapter;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.Classes.MusicBox;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.R;

public class ConfigActivity extends AppCompatActivity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_BLUETOOTH = "bluetooth";
    public final static String WIDGET_EQUALIZER = "equalizer";
    public final static String WIDGET_PREV = "prev";
    public final static String WIDGET_NEXT = "next";
    public final static String WIDGET_PAUSE = "pause";
    public final static String WIDGET_SOUND = "sound";
    public final static String WIDGET_ITEMS = "list";
    //public final static String WIDGET_TYPE = "widget_type";
    public final static String WIDGET_FULL_MODE = "full_mode";
    public final static String WIDGET_SHOW_FILE_NAME = "show_file_name";
    public final static String WIDGET_SHOW_ICON = "show_icon";

    public final static String WIDGET_THEME = "theme";
    public final static String WIDGET_TRANSPARENCY = "transparency";
    public final static String WIDGET_BORDER = "border";

    ListView lvData;
    ConfigAdapter miAdapter;
    int select_pos = -1;

    CheckBox cbShowFullInfo;
    CheckBox cbShowExMode, cbShowNext, cbShowPrev, cbShowPause, cbShowIcon, cbTheme, cbBorder;
    ProgressBar pbTransparency;

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

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    public void onBackPressed() {
        finish();
    }

    public void onHomePressed() {
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.config);

        SharedPreferences sp = getSharedPreferences(ConfigActivity.WIDGET_PREF, MODE_PRIVATE);
        int val = sp.getInt(WIDGET_BLUETOOTH + widgetID, 1);
        CheckBox cb = (CheckBox)findViewById(R.id.cbBluetooth);
        if (val == 1) cb.setChecked(true);
        else cb.setChecked(false);

        val = sp.getInt(WIDGET_SOUND + widgetID, 1);
        cb = (CheckBox)findViewById(R.id.cbSound);
        if (val == 1) cb.setChecked(true);
        else cb.setChecked(false);

        val = sp.getInt(WIDGET_EQUALIZER + widgetID, 1);
        cb = (CheckBox)findViewById(R.id.cbEqualizer);
        if (val == 1) cb.setChecked(true);
        else cb.setChecked(false);

        val = sp.getInt(WIDGET_NEXT + widgetID, 1);
        cbShowNext = (CheckBox)findViewById(R.id.cbShowNext);
        if (val == 1) cbShowNext.setChecked(true);
        else cbShowNext.setChecked(false);

        val = sp.getInt(WIDGET_PREV + widgetID, 1);
        cbShowPrev = (CheckBox)findViewById(R.id.cbShowPrev);
        if (val == 1) cbShowPrev.setChecked(true);
        else cbShowPrev.setChecked(false);

        val = sp.getInt(WIDGET_PAUSE + widgetID, 1);
        cbShowPause = (CheckBox)findViewById(R.id.cbShowPause);
        if (val == 1) cbShowPause.setChecked(true);
        else cbShowPause.setChecked(false);

        val = sp.getInt(WIDGET_FULL_MODE + widgetID, 1);
        cbShowExMode = (CheckBox)findViewById(R.id.cbFullMode);
        if (val == 1) cbShowExMode.setChecked(true);
        else cbShowExMode.setChecked(false);
        cbShowExMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub

                if (buttonView.isChecked()) {
                    cbShowPause.setChecked(true);
                    cbShowPrev.setChecked(true);
                    cbShowNext.setChecked(true);

                    cbShowPause.setEnabled(true);
                    cbShowPrev.setEnabled(true);
                    cbShowNext.setEnabled(true);
                }
                else {
                    cbShowPause.setChecked(false);
                    cbShowPrev.setChecked(false);
                    cbShowNext.setChecked(false);

                    cbShowPause.setEnabled(false);
                    cbShowPrev.setEnabled(false);
                    cbShowNext.setEnabled(false);
                }

            }
        });

        if (cbShowExMode.isChecked()) {
            cbShowPause.setEnabled(true);
            cbShowPrev.setEnabled(true);
            cbShowNext.setEnabled(true);
        }
        else {
            cbShowPause.setEnabled(false);
            cbShowPrev.setEnabled(false);
            cbShowNext.setEnabled(false);
        }

        val = sp.getInt(WIDGET_SHOW_FILE_NAME + widgetID, 1);
        cbShowFullInfo = (CheckBox)findViewById(R.id.cbShowFileName);
        if (val == 1) cbShowFullInfo.setChecked(true);
        else cbShowFullInfo.setChecked(false);

        val = sp.getInt(WIDGET_SHOW_ICON + widgetID, 1);
        cbShowIcon = (CheckBox)findViewById(R.id.cbShowIcon);
        if (val == 1) cbShowIcon.setChecked(true);
        else cbShowIcon.setChecked(false);

        val = sp.getInt(WIDGET_THEME + widgetID, 0);
        cbTheme = (CheckBox)findViewById(R.id.cbTheme);
        if (val == 1) cbTheme.setChecked(true);
        else cbTheme.setChecked(false);

        val = sp.getInt(WIDGET_BORDER + widgetID, 0);
        cbBorder = (CheckBox)findViewById(R.id.cbBorder);
        if (val == 1) cbBorder.setChecked(true);
        else cbBorder.setChecked(false);

        val = sp.getInt(WIDGET_TRANSPARENCY + widgetID, 50);
        pbTransparency = (ProgressBar)findViewById(R.id.pbTransparency) ;
        pbTransparency.setProgress(pbTransparency.getMax() - val / 25);

        /*ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.widget_type_list, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_item);
        Spinner spType = (Spinner) findViewById(R.id.spType);
        spType.setAdapter(adapter);

        int valType = sp.getInt(ConfigActivity.WIDGET_TYPE + widgetID, 0);
        spType.setSelection(valType);*/

        String strIDs = sp.getString(WIDGET_ITEMS + widgetID, null);

        if (MediaService.musicBox == null)
        {
            MediaService.musicBox = new MusicBox(this);
            MediaService.musicBox.GetData();
        }

        miAdapter = new ConfigAdapter(this);
        miAdapter.objects = new ArrayList<MusicItem>();
        for (MusicItem item : MediaService.musicBox.objects) {
            miAdapter.objects.add(item);
        }
        if (strIDs != null)
            miAdapter.setSelectedItems(strIDs);
        //miAdapter.UpdateData();

        lvData = (ListView) findViewById(R.id.lvConfig);
        lvData.setAdapter(miAdapter);
        setListViewHeightBasedOnChildren(lvData);
        //lvData.setSelector(R.drawable.list_selector);
        lvData.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                //((ConfigAdapter)parent.).select_pos = position;
            }
        });

        final ScrollView myScroll = (ScrollView) findViewById(R.id.scroll);

        Handler myHandler = new Handler() {
            public void handleMessage(Message m) {
                myScroll.scrollTo(0, 0);
            }
        };

        Message m = new Message();
        myHandler.sendMessageDelayed(m, 50);
    }

    public void onClickMainParameters(View view) {
        LinearLayout ll = (LinearLayout)findViewById(R.id.llMainParameters);
        int visible = ll.getVisibility();
        if (visible == View.GONE) visible = View.VISIBLE;
        else visible = View.GONE;
        ll.setVisibility(visible);
    }

    public void onClickShapeParameters(View view) {
        LinearLayout ll = (LinearLayout)findViewById(R.id.llShapeParameters);
        int visible = ll.getVisibility();
        if (visible == View.GONE) visible = View.VISIBLE;
        else visible = View.GONE;
        ll.setVisibility(visible);
    }

    public void onClick(View view) {

        // Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        CheckBox cb = (CheckBox)findViewById(R.id.cbBluetooth);
        editor.putInt(WIDGET_BLUETOOTH + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbSound);
        editor.putInt(WIDGET_SOUND + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbEqualizer);
        editor.putInt(WIDGET_EQUALIZER + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbFullMode);
        editor.putInt(WIDGET_FULL_MODE + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbShowPrev);
        editor.putInt(WIDGET_PREV + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbShowNext);
        editor.putInt(WIDGET_NEXT + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbShowPause);
        editor.putInt(WIDGET_PAUSE + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbShowFileName);
        editor.putInt(WIDGET_SHOW_FILE_NAME + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbShowIcon);
        editor.putInt(WIDGET_SHOW_ICON + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbTheme);
        editor.putInt(WIDGET_THEME + widgetID, cb.isChecked() ? 1 : 0);

        cb = (CheckBox)findViewById(R.id.cbBorder);
        editor.putInt(WIDGET_BORDER + widgetID, cb.isChecked() ? 1 : 0);

        ProgressBar pb = (ProgressBar)findViewById(R.id.pbTransparency);
        editor.putInt(WIDGET_TRANSPARENCY + widgetID, (pbTransparency.getMax() - pb.getProgress()) * 25);

        //Spinner spType = (Spinner) findViewById(R.id.spType);
        //editor.putInt(WIDGET_TYPE + widgetID, spType.getSelectedItemPosition());

        String strIDs = "";

        if (miAdapter.selectedItems != null) {
            for (String str_id : miAdapter.selectedItems) {
                int id = Integer.valueOf(str_id);
                if (MediaService.musicBox.getMusicItemByID(id) != null)
                    strIDs += str_id + ",";
            }
        }

        editor.putString(WIDGET_ITEMS + widgetID, strIDs);


        editor.commit();

        Intent i = new Intent(Const.FORCE_WIDGET_UPDATE);
        sendBroadcast(i);

        // положительный ответ
        setResult(RESULT_OK, resultValue);

        finish();
    }
}

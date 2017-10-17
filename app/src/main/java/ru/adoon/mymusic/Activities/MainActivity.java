package ru.adoon.mymusic.Activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.Dialogs.DialogEqualizer;
import ru.adoon.mymusic.Dialogs.DialogFolder;
import ru.adoon.mymusic.Dialogs.DialogPlayFile;
import ru.adoon.mymusic.Dialogs.DialogPlaylist;
import ru.adoon.mymusic.Dialogs.DialogRadio;
import ru.adoon.mymusic.Dialogs.DialogRadioSelect;
import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.Classes.MusicBox;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Adapters.MusicItemAdapter;
import ru.adoon.mymusic.R;

public class MainActivity extends AppCompatActivity  {

    //public static String FORCE_WIDGET_UPDATE = "ru.adoon.mymusic.FORCE_WIDGET_UPDATE";
    private static final long DOUBLE_PRESS_INTERVAL = 250;
    private long lastPressTime;
    private boolean mHasDoubleClicked = false;

    ListView lvData;
    //DB db;
    //final int DIALOG_RADIO = 1;
    //final int DIALOG_FOLDER = 2;

    //ArrayList<MusicItem> music = new ArrayList<MusicItem>();
    //MusicItemAdapter miAdapter;

    //MediaPlayer mediaPlayer;
    //AudioManager am;
    //NotificationManager nm;

    private static long back_pressed;
    BroadcastReceiver br;
    private Menu mOptionsMenu;
    MusicItemAdapter miAdapter;
    //public int NOTIFICATION_ID = 1;

    /*@Override
    public void onPause() {
        super.onPause();
        finish();
    }*/

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }

    @Override
    public void onConfigurationChanged(Configuration _newConfig) {
        super.onConfigurationChanged(_newConfig);
        if (_newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        }
        if (_newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // открываем подключение к БД
        if (MediaService.musicBox == null)
        {
            MediaService.musicBox = new MusicBox(this);
            MediaService.musicBox.GetData();
            MediaService.GetBluetooth();
        }

        miAdapter = new MusicItemAdapter(this);

        // настраиваем список
        lvData = (ListView) findViewById(R.id.lvMain);
        lvData.setAdapter(miAdapter);
        lvData.setSelector(R.drawable.list_selector);
        lvData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lvData.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int position, long arg3) {
                MediaService.musicBox.select_pos = position;
                return false;
            }
        });

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                MediaService.musicBox.select_pos = position;

                MusicItem mi = ((MusicItem) miAdapter.getItem(position));
                if (mi.type == Const.TYPE_FOLDER || mi.type == Const.TYPE_PLAYLIST) {
                    DialogPlayFile dlg = new DialogPlayFile();
                    dlg.mi = mi;
                    dlg.show(getFragmentManager(), "DialogPlayFile");
                }
                else
                    MediaService.musicBox.PlayItem(position);

                /*long pressTime = System.currentTimeMillis();
                if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
                    //Toast.makeText(getBaseContext(), "DBL", Toast.LENGTH_SHORT).show();
                    MediaService.miAdapter.onDblClick(position);
                    mHasDoubleClicked = true;
                }
                else {
                    //MediaService.miAdapter.onSingleClick(position);

                    mHasDoubleClicked = false;
                    Handler myHandler = new Handler() {
                        public void handleMessage(Message m) {
                            if (!mHasDoubleClicked) {
                                //Toast.makeText(getApplicationContext(), "Single Click Event", Toast.LENGTH_SHORT).show();
                                //MediaService.miAdapter.onSingleClick(MediaService.miAdapter.select_pos);
                                MusicItem mi = ((MusicItem) MediaService.miAdapter.getItem(MediaService.miAdapter.select_pos));

                                if (mi.type == MusicItemAdapter.TYPE_FOLDER &&
                                        (mi.state == MusicItemAdapter.STATE_PLAY || mi.state == MusicItemAdapter.STATE_PAUSE)) {
                                    DialogFileSelect dlg = new DialogFileSelect();
                                    dlg.mi = mi;
                                    dlg.show(getFragmentManager(), "DialogFileSelect");
                                }
                            }
                        }
                    };

                    Message m = new Message();
                    myHandler.sendMessageDelayed(m, DOUBLE_PRESS_INTERVAL);
                }*/

                //lastPressTime = pressTime;
            }
        });

        registerForContextMenu(lvData);

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (mOptionsMenu != null)
                {
                    MenuItem bt = mOptionsMenu.findItem(R.id.action_bluetooth);
                    MenuItem sound = mOptionsMenu.findItem(R.id.action_sound);

                    if (MediaService.m_bBluetooth) bt.setIcon(R.drawable.bluetooth_on_large);
                    else bt.setIcon(R.drawable.bluetooth_off_large);

                    if (MediaService.m_bSound) sound.setIcon(R.drawable.sound_on_large);
                    else sound.setIcon(R.drawable.sound_off_large);
                }

                miAdapter.notifyDataSetChanged();
            }
        };

        IntentFilter intFilt = new IntentFilter(Const.FORCE_WIDGET_UPDATE);
        registerReceiver(br, intFilt);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, R.id.action_edit, 0, "Изменить");
        menu.add(0, R.id.action_delete, 0, "Удалить");
    }

    public void UpdateRecord() {
        if (MediaService.musicBox.select_pos < 0) {
            Toast.makeText(MainActivity.this, "Необходимо выбрать запись",
                    Toast.LENGTH_LONG).show();
        } else {
            MusicItem mi = ((MusicItem) miAdapter.getItem(MediaService.musicBox.select_pos));
            if (mi.type == Const.TYPE_RADIO) {
                DialogRadio dlg = new DialogRadio();
                dlg.mi = mi;
                dlg.show(getFragmentManager(), "DialogRadio");
            }
            if (mi.type == Const.TYPE_FOLDER) {
                DialogFolder dlg = new DialogFolder();
                dlg.mi = mi;
                dlg.show(getFragmentManager(), "DialogFolder");
            }
            if (mi.type == Const.TYPE_PLAYLIST) {
                DialogPlaylist dlg = new DialogPlaylist();
                dlg.mi = mi;
                dlg.show(getFragmentManager(), "DialogPlaylist");
            }
        }
    }

    public void DeleteRecord() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.alert_record_delete_title);
        adb.setMessage(R.string.alert_record_delete);
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (MediaService.musicBox.select_pos < 0) {
                    Toast.makeText(MainActivity.this, "Необходимо выбрать запись",
                            Toast.LENGTH_LONG).show();
                } else {
                    int idMusic = ((MusicItem) miAdapter.getItem(MediaService.musicBox.select_pos)).id;
                    MediaService.musicBox.delRec(idMusic);
                    //db.delRec(idMusic);
                    //MediaService.miAdapter.UpdateData(this);
                    //MediaService.miAdapter.notifyDataSetChanged();

                    Toast.makeText(MainActivity.this, "Запись удалена",
                            Toast.LENGTH_SHORT).show();
                }
                MediaService.musicBox.select_pos = -1;
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

    /*public void StopMedia() {
        nm.cancel(NOTIFICATION_ID);

        if (MediaService.mediaPlayer != null) {
            MediaService.mediaPlayer.stop();
            releaseMP();
        }
    }*/

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        MediaService.musicBox.StopAll(true);
        //miAdapter.notifyDataSetChanged();

        //StopMedia();

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MediaService.musicBox.select_pos = acmi.position;

        if (item.getItemId() == R.id.action_edit) {

            UpdateRecord();

            return true;
        }
        if (item.getItemId() == R.id.action_delete) {

            DeleteRecord();

            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mOptionsMenu = menu;

        MenuItem bt = mOptionsMenu.findItem(R.id.action_bluetooth);
        MenuItem sound = mOptionsMenu.findItem(R.id.action_sound);

        if (MediaService.m_bBluetooth) bt.setIcon(R.drawable.bluetooth_on_large);
        else bt.setIcon(R.drawable.bluetooth_off_large);

        if (MediaService.m_bSound) sound.setIcon(R.drawable.sound_on_large);
        else sound.setIcon(R.drawable.sound_off_large);

        /*if (!MediaService.m_bBluetoothExists)
            bt.setVisible(false);*/

        return true;
    }

    public void AddRec(MusicItem mi) {
        MediaService.musicBox.addRec(mi);
    }

    public void UpdRec(MusicItem mi) {
        MediaService.musicBox.updRec(mi);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_eq) {

            DialogEqualizer dlg = new DialogEqualizer();
            dlg.show(getFragmentManager(), "DialogEqualizer");

            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_radio_select) {

            DialogRadioSelect dlg = new DialogRadioSelect();
            dlg.show(getFragmentManager(), "DialogRadioSelect");

            return true;
        }

        if (id == R.id.action_add_radio) {

            DialogRadio dlg = new DialogRadio();
            dlg.show(getFragmentManager(), "DialogRadio");

            return true;
        }

        if (id == R.id.action_add_folder) {

            DialogFolder dlg = new DialogFolder();
            dlg.show(getFragmentManager(), "DialogFolder");

            return true;
        }

        if (id == R.id.action_add_playlist) {

            DialogPlaylist dlg = new DialogPlaylist();
            dlg.show(getFragmentManager(), "DialogPlaylist");

            return true;
        }

        if (id == R.id.action_edit) {

            MediaService.musicBox.StopAll(true);

            UpdateRecord();

            return true;
        }

        if (id == R.id.action_delete) {

            MediaService.musicBox.StopAll(true);

            DeleteRecord();

            return true;
        }

        if (id == R.id.action_sound) {

            Intent intentSoundService = new Intent(this.getApplicationContext(), MediaService.class);
            intentSoundService.setAction(Const.ACTION_SOUND);
            intentSoundService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startService(intentSoundService);

            /*if (MessageService.intentService != null)
            {
                stopService(MessageService.intentService);
            }
            else {
                MessageService.intentService = new Intent(getApplicationContext(), MessageService.class);
                //Toast.makeText(context, "intentService == null", Toast.LENGTH_SHORT).show();
            }

            MessageService.intentService.putExtra("bluetooth", 0);
            MessageService.intentService.putExtra("sound", 1);
            startService(MessageService.intentService);*/

            return true;
        }

        if (id == R.id.action_bluetooth) {

            Intent intentBluetoothService = new Intent(this.getApplicationContext(), MediaService.class);
            intentBluetoothService.setAction(Const.ACTION_BLUETOOTH);
            intentBluetoothService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startService(intentBluetoothService);

            /*if (MessageService.intentService != null)
            {
                stopService(MessageService.intentService);
            }
            else {
                MessageService.intentService = new Intent(getApplicationContext(), MessageService.class);
                //Toast.makeText(context, "intentService == null", Toast.LENGTH_SHORT).show();
            }

            MessageService.intentService.putExtra("bluetooth", 1);
            MessageService.intentService.putExtra("sound", 0);
            startService(MessageService.intentService);*/

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(br);
    }

    public void ShowMessage(String strMessage)
    {
        Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
    }

    // сохранение состояния
    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    // получение ранее сохраненного состояния
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }*/
}

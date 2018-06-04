package ru.adoon.mymusic.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import ru.adoon.mymusic.Adapters.PlayFileAdapter;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.R;

/**
 private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
 private long lastPressTime;

 private boolean mHasDoubleClicked = false;

 @Override
 public boolean onPrepareOptionsMenu(Menu menu) {

 // Get current time in nano seconds.
 long pressTime = System.currentTimeMillis();


 // If double click...
 if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
 Toast.makeText(getApplicationContext(), "Double Click Event", Toast.LENGTH_SHORT).show();
 mHasDoubleClicked = true;
 }
 else {     // If not double click....
 mHasDoubleClicked = false;
 Handler myHandler = new Handler() {
 public void handleMessage(Message m) {
 if (!mHasDoubleClicked) {
 Toast.makeText(getApplicationContext(), "Single Click Event", Toast.LENGTH_SHORT).show();
 }
 }
 };
 Message m = new Message();
 myHandler.sendMessageDelayed(m,DOUBLE_PRESS_INTERVAL);
 }
 // record the last time the menu button was pressed.
 lastPressTime = pressTime;
 return true;
 }
 */

public class DialogPlayFile extends DialogFragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    ListView lvData;
    PlayFileAdapter miAdapter;
    public MusicItem mi;
    public Activity parent;
    BroadcastReceiver br, br2;
    int m_duration = 0;
    TextView tvCurrentPos;
    int m_iSelectedPos = -1;
    int file_beg_index = 0;
    int file_index = 0;
    int last_pos = 0;

    private Handler m_MediaInfoHandler = null;
    private HandlerThread m_MediaInfoHandlerThread = null;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            miAdapter.notifyDataSetChanged();
        }
    };

    private final Runnable m_GetMediaInfo = new Runnable() {
        @Override
        public void run() {

            if (file_index >= 0 && file_index < mi.files.size()) {
                FileItem fi = mi.files.get(file_index);
                if (!fi.m_bReadTitle) {
                    fi.GetFileTitleDescript();
                    Pair<String, Pair<String, String> > obj;
                    //if (!fi.m_strTitleExecutor.equalsIgnoreCase("")) {
                        obj = new Pair<String, Pair<String, String> >(fi.m_strShortName, new Pair<String, String>(fi.m_strTitleExecutor, fi.m_strTitleTrack));
                    //} else {
                    //    obj = new Pair<String, Pair<String, String> >(fi.m_strShortName, "");
                    //}
                    miAdapter.objects.set(file_index, obj);
                }
            }

            m_MediaInfoHandler.post(new Runnable() {
                @Override
                public void run() {
                    file_index++;
                //miAdapter.notifyDataSetChanged();
                    if (file_beg_index >= 0) {
                        if (file_index != file_beg_index) {
                            if (file_index >= mi.files.size()) file_index = 0;
                            m_MediaInfoHandler.postDelayed(m_GetMediaInfo, 10);
                        }
                        else {
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                        }
                    }
                    else {
                        if (file_index < mi.files.size())
                            m_MediaInfoHandler.postDelayed(m_GetMediaInfo, 10);
                        else {
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                        }
                    }
                }
            });
            //m_MediaInfoHandler.postDelayed(m_GetMediaInfo, duration);
        }
    };

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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        int pos_ = m_duration * progress / 100;
        String strCurrentPos = String.format("%02d:%02d", pos_ / 60, pos_ % 60);

        tvCurrentPos.setText(strCurrentPos);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // музыку на паузу
        int progress = seekBar.getProgress();
        MediaService.musicBox.Pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // музыку на продолжение проигрывания
        int progress = seekBar.getProgress();
        last_pos = progress;
        MediaService.musicBox.Resume(progress);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivImage) {
            MediaService.musicBox.StopAll(true);
            final AlertDialog ad = (AlertDialog) getDialog();
            ad.dismiss();
            if (parent != null) parent.finish();
        }
        if (v.getId() == R.id.ivImageNext) {
            MediaService.musicBox.Next();
        }

        if (v.getId() == R.id.ivImagePrev) {
            MediaService.musicBox.Prev();
        }

        if (v.getId() == R.id.ivImagePause) {
            MusicItem mi = MediaService.musicBox.getMusicItemByID(MediaService.musicBox.play_item_id);
            if (mi != null) {
                if (mi.state == Const.STATE_PLAY)
                    MediaService.musicBox.Pause();
                if (mi.state == Const.STATE_PAUSE)
                    MediaService.musicBox.Resume(-1);
            }
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(mi.name);
        // создаем view из dialog.xml
        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_play_file, null);
        // устанавливаем ее, как содержимое тела диалога
        adb.setView(view);

        if (mi.files == null) {
            MediaService.musicBox.GetAudioFiles(mi);
        }

        LinearLayout llMain = (LinearLayout) (view).findViewById(R.id.llMain);
        setLayoutSize(adb.getContext(), llMain);

        miAdapter = new PlayFileAdapter(adb.getContext());
        miAdapter.id = mi.id;
        miAdapter.objects = new ArrayList<Pair<String, Pair<String, String>>>();
        for (int i = 0; i < mi.files.size(); i++) {
            //mi.GetFileTitleDescript(i);
            FileItem fi = mi.files.get(i);
            //if (fi.m_strTitleExecutor != "")
            //    miAdapter.objects.add(new Pair<String, String>(fi.m_strTitleExecutor, fi.m_strTitleTrack));
            //else
                miAdapter.objects.add(new Pair<String, Pair<String,String>>(fi.m_strShortName, new Pair<String, String>(fi.m_strTitleExecutor, fi.m_strTitleTrack)));
        }

        lvData = (ListView) view.findViewById(R.id.lvFiles);
        lvData.setAdapter(miAdapter);
        lvData.setSelector(R.drawable.list_selector);
        lvData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                if (MediaService.musicBox.play_sub_item_pos == position) {
                    MediaService.musicBox.StopAll(true);
                    //m_iSelectedPos = -1;
                    m_iSelectedPos = position;
                    MediaService.musicBox.PlayFile(mi.id, position);
                }
                else {
                    m_iSelectedPos = position;
                    MediaService.musicBox.PlayFile(mi.id, position);
                }
            }
        });

        if (MediaService.musicBox.play_sub_item_pos >= 0) {
            lvData.setSelection(MediaService.musicBox.play_sub_item_pos);
        }

        adb.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                //MediaService.setAdapterForUpdate(null);
                if (parent != null) {
                    parent.finish();
                    parent = null;
                }
            }
        });
        /*if (mi.state != MusicItemAdapter.STATE_STOP) {
            adb.setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                    MediaService.setAdapterForUpdate(null);
                    if (parent != null) parent.finish();
                }
            });
        }*/

        final SeekBar seekBar = (SeekBar)view.findViewById(R.id.seekBar);
        final LinearLayout llSeek = (LinearLayout)view.findViewById(R.id.llSeek);
        ImageView ivImageNext = (ImageView)view.findViewById(R.id.ivImageNext);
        ImageView ivImagePrev = (ImageView)view.findViewById(R.id.ivImagePrev);
        final ImageView ivImagePause = (ImageView)view.findViewById(R.id.ivImagePause);
        ImageView ivImageStop = (ImageView)view.findViewById(R.id.ivImage);
        ivImageNext.setOnClickListener(this);
        ivImagePrev.setOnClickListener(this);
        ivImagePause.setOnClickListener(this);
        ivImageStop.setOnClickListener(this);

        tvCurrentPos = (TextView)view.findViewById(R.id.tvCurrentPos);
        final TextView tvDuration = (TextView)view.findViewById(R.id.tvDuration);

        seekBar.setOnSeekBarChangeListener(this);

        if (MediaService.musicBox.AllMusicStopped()
                || MediaService.musicBox.play_item_id != mi.id) {
            llSeek.setVisibility(View.GONE);
        }
        else {
            llSeek.setVisibility(View.VISIBLE);

            MusicItem mi = MediaService.musicBox.getMusicItemByID(MediaService.musicBox.play_item_id);

            if (mi != null) {
                if (mi.state == Const.STATE_PLAY) ivImagePause.setImageResource(R.drawable.pause);
                if (mi.state == Const.STATE_PAUSE) ivImagePause.setImageResource(R.drawable.pause_off);
            }
        }

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                //Log.d("mytag", "receive");
                int pos = intent.getIntExtra("pos", 0);
                int duration = intent.getIntExtra("duration", 0);
                int progress = pos * 100 / duration;

                if (progress < last_pos && last_pos != 0) {
                    progress = last_pos;
                    last_pos = 0;
                }

                seekBar.setProgress(progress);

                int pos_ = pos / 1000;
                m_duration = duration / 1000;
                String strCurrentPos = String.format("%02d:%02d", pos_ / 60, pos_ % 60);
                String strDuration = String.format("%02d:%02d", m_duration / 60, m_duration % 60);

                tvCurrentPos.setText(strCurrentPos);
                tvDuration.setText(strDuration);
            }
        };

        IntentFilter intFilt = new IntentFilter(Const.FORCE_POS_UPDATE);
        getActivity().registerReceiver(br, intFilt);

        br2 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {

                MusicItem mi = MediaService.musicBox.getMusicItemByID(MediaService.musicBox.play_item_id);

                if (mi != null && MediaService.musicBox.play_sub_item_pos >= 0 && MediaService.musicBox.play_sub_item_pos < mi.files.size()) {
                    FileItem fi = mi.files.get(MediaService.musicBox.play_sub_item_pos);
                    Pair<String, Pair<String, String>> obj;
                    //if (!fi.m_strTitleExecutor.equalsIgnoreCase("")) {
                    //    obj = new Pair<String, String>(fi.m_strTitleExecutor, fi.m_strTitleTrack);
                    //}
                    //else {
                        obj = new Pair<String, Pair<String, String>>(fi.m_strShortName, new Pair<String, String>(fi.m_strTitleExecutor, fi.m_strTitleTrack));
                    //}
                    miAdapter.objects.set(MediaService.musicBox.play_sub_item_pos, obj);
                }

                miAdapter.notifyDataSetChanged();
                if (MediaService.musicBox.AllMusicStopped()
                        || MediaService.musicBox.play_item_id != mi.id) {
                    llSeek.setVisibility(View.GONE);
                }
                else {
                    llSeek.setVisibility(View.VISIBLE);

                    if (mi != null) {
                        if (mi.state == Const.STATE_PLAY) ivImagePause.setImageResource(R.drawable.pause);
                        if (mi.state == Const.STATE_PAUSE) ivImagePause.setImageResource(R.drawable.pause_off);
                        if (m_iSelectedPos != MediaService.musicBox.play_sub_item_pos) {
                            int pos = MediaService.musicBox.play_sub_item_pos;
                            lvData.setSelection(pos);
                        }
                    }
                    /*final int pos = MediaService.musicBox.getMusicItemPosByID(MediaService.musicBox.play_item_id);
                    lvData.post(new Runnable() {
                        @Override
                        public void run() {
                            lvData.smoothScrollToPosition(pos);
                        }
                    });*/
                }
            }
        };

        IntentFilter intFilt2 = new IntentFilter(Const.FORCE_WIDGET_UPDATE);
        getActivity().registerReceiver(br2, intFilt2);

        file_beg_index = MediaService.musicBox.play_sub_item_pos;
        if (file_beg_index >= 0) file_index = file_beg_index;

        if (mi.files.size() > 0) {
            m_MediaInfoHandlerThread = new HandlerThread("GetMediaInfo") {
                @Override
                protected void onLooperPrepared() {
                    m_MediaInfoHandler = new Handler(getLooper());
                    m_MediaInfoHandler.post(m_GetMediaInfo);
                }
            };
            m_MediaInfoHandlerThread.start();
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

    @Override
    public void onResume() {
        super.onResume();

        //MediaService.setAdapterForUpdate(null);

        final AlertDialog ad = (AlertDialog) getDialog();
        colorAlertDialogTitle(ad);
        Button negativeButton = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {

                MediaService.musicBox.StopAll(true);
                ad.dismiss();
                if (parent != null) {
                    parent.finish();
                    parent = null;
                }
            }
        });
    }

    @Override
    public void onDestroy() {

        if (m_MediaInfoHandlerThread != null) {
            m_MediaInfoHandlerThread.quit();
            try {
                m_MediaInfoHandlerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            m_MediaInfoHandler = null;
            m_MediaInfoHandlerThread = null;
        }

        super.onDestroy();

        getActivity().unregisterReceiver(br);
        getActivity().unregisterReceiver(br2);

        if (parent != null) {
            parent.finish();
            parent = null;
        }
    }
}

package ru.adoon.mymusic.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.adoon.mymusic.Activities.ActivityPlayFile;
import ru.adoon.mymusic.Activities.MainActivity;
import ru.adoon.mymusic.Classes.EqualizerBandItem;
import ru.adoon.mymusic.Classes.MusicBox;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;
import ru.adoon.mymusic.Tools.AudioFocusHelper;
import ru.adoon.mymusic.Tools.LockscreenManager;
import ru.adoon.mymusic.Tools.MusicFocusable;
import ru.adoon.mymusic.Tools.MusicIntentReceiver;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MusicFocusable {

    public static MusicBox musicBox;
    public static boolean m_bSound = true;
    public static boolean m_bBluetooth = false;
    //static FileItemAdapter fileAdapter = null;

    public static int m_iUseEq = 0;
    public static ArrayList<EqualizerBandItem> m_vEqBands = new ArrayList<EqualizerBandItem>();
    public static ArrayList<EqualizerBandItem> m_vEqDefaultBands = new ArrayList<EqualizerBandItem>();

    public static MediaPlayer mediaPlayer = null;
    public static Equalizer equalizer = null;
    public static boolean m_bGetEqDefaultPref = false;
    NotificationManager nm;
    final public int NOTIFICATION_ID = 1;

    Timer timerRefreshPos;
    TimerTask m_TimerRefreshPosTask;

    private Handler m_NotifHandler;
    private HandlerThread m_NotifHandlerThread;

    private final Runnable m_SendNotif = new Runnable() {
        @Override
        public void run() {

            MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
            sendNotification(mi);

            m_NotifHandler.post(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    };

    private Handler m_MediaInfoHandler;
    private HandlerThread m_MediaInfoHandlerThread;

    private final Runnable m_GetMediaInfo = new Runnable() {
        @Override
        public void run() {

            Document doc;
            MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
            if (mi != null) {
                if (mi.type == Const.TYPE_RADIO && mi.radioid > 0) {
                    mi.rti.GetTrackInfo(mi.radioid);
                }
                if (mi.type == Const.TYPE_FOLDER || mi.type == Const.TYPE_PLAYLIST ) {
                    if (mi.files != null && mi.files.size() > musicBox.play_sub_item_pos && musicBox.play_sub_item_pos >= 0)
                            mi.files.get(musicBox.play_sub_item_pos).GetImageFromMp3File();
                }
            }

            m_MediaInfoHandler.post(new Runnable() {
                @Override
                public void run() {
                    MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
                    if (mi != null) {
                        Lockscreen.setMediaID( mi );

                        sendNotificationTask(mi);
                        UpdateWidget();

                        if (mi.type == Const.TYPE_RADIO) {
                            if (mi.rti.m_lastTime > 0)
                                m_MediaInfoHandler.postDelayed(m_GetMediaInfo, mi.rti.m_lastTime * 1000);
                            else {
                                m_MediaInfoHandler.postDelayed(m_GetMediaInfo, 2000);
                            }
                        }
                    }
                }
            });
        }
    };

    private Handler m_RadioIconInfoHandler;
    private HandlerThread m_RadioIconInfoHandlerThread;

    private final Runnable m_GetRadioIconInfo = new Runnable() {
        @Override
        public void run() {

            Document doc;
            MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
            if (mi != null) {
                if (mi.type == Const.TYPE_RADIO && mi.radioid > 0) {
                    mi.rti.GetRadioInfo(mi.name, mi.radioid, getBaseContext());
                }
            }

            m_RadioIconInfoHandler.post(new Runnable() {
                @Override
                public void run() {
                    MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
                    if (mi != null) {
                        Lockscreen.setMediaID( mi );

                        sendNotificationTask(mi);
                        UpdateWidget();
                    }
                }
            });
        }
    };

    public Elements content;
    AudioFocusHelper mAudioFocusHelper = null;
    private LockscreenManager Lockscreen;
    private AudioManager mAudioManager;


    //RemoteControlClientCompat mRemoteControlClientCompat;
    //ComponentName mMediaButtonReceiverComponent;
    //AudioManager mAudioManager;

    /*public RemoteControlClientCompat mRemoteControlClientCompat;
    ComponentName mMediaButtonReceiverComponent;
    private AudioManager mAudioManager;
    private MediaSessionCompat mMediaSessionCompat;

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }
    };*/

    /*private void lockScreenControls() {

        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            //intent.setComponent(mMediaButtonReceiverComponent);
            mRemoteControlClientCompat = new RemoteControlClientCompat(PendingIntent.getBroadcast(this ,0, intent, 0));
            RemoteControlHelper.registerRemoteControlClient(mAudioManager,mRemoteControlClientCompat);
        }
        mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        mRemoteControlClientCompat.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_STOP);

        //update remote controls
        mRemoteControlClientCompat.editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "NombreArtista")
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "Titulo Album")
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, "METADATA_KEY_TITLE")
                //.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,playingItem.getDuration())
                // TODO: fetch real item artwork
                //.putBitmap(RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK, getAlbumArt())
                .apply();
    }*/

    @Override public void onGainedAudioFocus() {

    }

    @Override public void onLostAudioFocus( boolean canDuck ) {

        if ( !canDuck ) {

            Lockscreen.remove();

        } else {
        }

    }

    public static void GetBluetooth()
    {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if(bluetooth != null)
        {
            m_bBluetooth = bluetooth.isEnabled();
        }
    }

    void SetSound(boolean bSound)
    {
        m_bSound = bSound;

        if (mediaPlayer != null) {
            if (m_bSound) mediaPlayer.setVolume(1, 1);
            else mediaPlayer.setVolume(0, 0);
        }

        UpdateWidget();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void GetEqDefaultPref() {
        if (!m_bGetEqDefaultPref) {
            mediaPlayer = new MediaPlayer();

            // ставим эквалайзер
            equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);

            short bands = MediaService.equalizer.getNumberOfBands();
            for (short i = 0; i < bands; i++) {
                int name = MediaService.equalizer.getCenterFreq(i) / 1000;
                int band_level = MediaService.equalizer.getBandLevel(i);

                MediaService.m_vEqDefaultBands.add(new EqualizerBandItem(name, band_level));
            }
            m_bGetEqDefaultPref = true;
        }
    }

    public static void SetEqDefaultPref() {
        m_iUseEq = 0;
        MediaService.m_vEqBands.clear();

        short bands = MediaService.equalizer.getNumberOfBands();
        for (short i = 0; i < bands; i++) {
            int name = MediaService.equalizer.getCenterFreq(i) / 1000;
            int band_level = MediaService.equalizer.getBandLevel(i);

            for (int j = 0; j < MediaService.m_vEqDefaultBands.size(); j++) {
                if (name == MediaService.m_vEqDefaultBands.get(j).m_iName) {
                    band_level = MediaService.m_vEqDefaultBands.get(j).m_iVal;
                    equalizer.setBandLevel(i, (short) band_level);
                    break;
                }
            }
        }
    }

    public static void SetEqPref() {
        if (m_iUseEq == 1) {
            short bands = MediaService.equalizer.getNumberOfBands();
            for (short i = 0; i < bands; i++) {
                int name = MediaService.equalizer.getCenterFreq(i) / 1000;
                int band_level = MediaService.equalizer.getBandLevel(i);

                for (int j = 0; j < MediaService.m_vEqBands.size(); j++) {
                    if (name == MediaService.m_vEqBands.get(j).m_iName) {
                        band_level = MediaService.m_vEqBands.get(j).m_iVal;
                        equalizer.setBandLevel(i, (short) band_level);
                        break;
                    }
                }
            }
        }
        else
            SetEqDefaultPref();
    }

    public static void LoadPref(Context ctx) {
        GetEqDefaultPref();

        SharedPreferences sPref = ctx.getSharedPreferences(Const.EQUALIZER_PREF, ctx.MODE_PRIVATE);
        m_iUseEq = sPref.getInt(Const.PREF_USE_EQ, 0);
        String strInfo = sPref.getString(Const.PREF_EQ_INFO, "");
        m_vEqBands.clear();
        if (strInfo != "" && m_iUseEq == 1) {
            String[] separated = strInfo.split(",");
            for (String str : separated) {
                if (!str.isEmpty()) {
                    String[] separated2 = str.split(";");
                    if (separated2.length == 2) {
                        int name = Integer.valueOf(separated2[0]);
                        int band_level = Integer.valueOf(separated2[1]);
                        m_vEqBands.add(new EqualizerBandItem(name, band_level));
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Lockscreen = new LockscreenManager( this );
        mAudioManager = ( AudioManager ) this.getSystemService( Context.AUDIO_SERVICE );
        mAudioManager.registerMediaButtonEventReceiver( new ComponentName( this, MusicIntentReceiver.class ) );
        mAudioFocusHelper = new AudioFocusHelper( this, this );

        if (musicBox == null) {

            LoadPref(this);

            musicBox = new MusicBox(this);
            musicBox.GetData();
        }
        else {
            musicBox.setContext(this);
        }

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        /*mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);

        MediaButtonHelper.registerMediaButtonEventReceiverCompat(
                mAudioManager, mMediaButtonReceiverComponent);

        // Use the remote control APIs (if available) to set the playback state

        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mMediaButtonReceiverComponent);
            mRemoteControlClientCompat = new RemoteControlClientCompat(
                    PendingIntent.getBroadcast(this ,
                            0 , intent , 0 ));
            RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                    mRemoteControlClientCompat);
        }

        mRemoteControlClientCompat.setPlaybackState(
                RemoteControlClient.PLAYSTATE_PLAYING);

        mRemoteControlClientCompat.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_STOP);

        // Update the remote controls
        mRemoteControlClientCompat.editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "playingItem.getArtist()")
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "playingItem.getAlbum()")
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, "playingItem.getTitle()")
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,
                        100)
                // TODO: fetch real item artwork
                .apply();*/
    }

    public void onDestroy() {
        super.onDestroy();

        mAudioFocusHelper.abandonFocus();
        mAudioManager.unregisterMediaButtonEventReceiver( new ComponentName( this, MusicIntentReceiver.class ) );
        Lockscreen.remove();

        musicBox.StopAll(false);
        StopMedia();
        UpdateWidget();
    }

    public void UpdateWidget()
    {
        /*MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
        if (musicBox.AllMusicStopped() || mi == null) {
            Lockscreen.remove();
        }
        else {
            Lockscreen.setMediaID(mi);
        }*/

        Intent i = new Intent(Const.FORCE_WIDGET_UPDATE);
        sendBroadcast(i);
    }

    /*private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       /* MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        mMediaSessionCompat.setActive(true);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);*/

        if (musicBox == null) {
            musicBox = new MusicBox(this);
            musicBox.GetData();
        }
        else {
            musicBox.setContext(this);
        }


        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if(bluetooth != null)
        {
            m_bBluetooth = bluetooth.isEnabled();
        }

        int type = -1;

        int id = -1;//intent.getIntExtra("id", -1);
        int pos = -1;//intent.getIntExtra("pos", -1);

        /*if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }*/

        if (intent != null && intent.getAction() != null) {
            String strAction = intent.getAction();

            if (strAction.indexOf(Const.ACTION_NEXT) == 0) {
                String[] arr = strAction.split("#");
                if (arr.length > 1) id = Integer.valueOf(arr[1]);
                if (arr.length > 2) pos = Integer.valueOf(arr[2]);
                if (id < 0) id = musicBox.play_item_id;
                type = Const.TYPE_NEXT;
            }

            if (strAction.indexOf(Const.ACTION_PREV) == 0) {
                String[] arr = strAction.split("#");
                if (arr.length > 1) id = Integer.valueOf(arr[1]);
                if (arr.length > 2) pos = Integer.valueOf(arr[2]);
                if (id < 0) id = musicBox.play_item_id;
                type = Const.TYPE_PREV;
            }

            if (strAction.indexOf(Const.ACTION_PLAY) == 0) {
                String[] arr = strAction.split("#");
                if (arr.length > 1) id = Integer.valueOf(arr[1]);
                if (id < 0) id = musicBox.play_item_id;
                type = Const.TYPE_PLAY;
                pos = -1;
            }

            if (strAction.equals(Const.ACTION_STOP)) {
                musicBox.StopAll(false);
                StopMedia();
                UpdateWidget();
                return START_NOT_STICKY;
                //return super.onStartCommand(intent, flags, startId);
            }

            if (strAction.equals(Const.ACTION_PAUSE)) {
                id = musicBox.play_item_id;
                type = Const.TYPE_PAUSE;
            }

            if (strAction.indexOf(Const.ACTION_RESUME) == 0) {
                String[] arr = strAction.split("#");
                if (arr.length > 1) pos = Integer.valueOf(arr[1]);
                id = musicBox.play_item_id;
                type = Const.TYPE_RESUME;
                //pos = -1;
            }

            if (strAction.equals(Const.ACTION_SOUND)) {
                MediaService.m_bSound = !MediaService.m_bSound;

                if (mediaPlayer != null) {
                    if (MediaService.m_bSound) mediaPlayer.setVolume(1, 1);
                    else mediaPlayer.setVolume(0, 0);
                }
                UpdateWidget();
                return START_NOT_STICKY;
                //return super.onStartCommand(intent, flags, startId);
            }

            if (strAction.equals(Const.ACTION_BLUETOOTH)) {
                BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
                if(ba != null)
                {
                    MediaService.m_bBluetooth = !MediaService.m_bBluetooth;

                    if (ba.isEnabled() != MediaService.m_bBluetooth) {

                        if (MediaService.m_bBluetooth) ba.enable();
                        else ba.disable();
                    }
                    UpdateWidget();
                }
                return START_NOT_STICKY;
                //return super.onStartCommand(intent, flags, startId);
            }
        }

        MusicItem p = musicBox.getMusicItemByID(id);

        if (p == null || type == -1) {
            musicBox.StopAll(false);
            StopMedia();
            UpdateWidget();
        }
        else {
            if (type == Const.TYPE_PLAY) {
                switch (p.state) {
                    case Const.STATE_STOP:
                        for (MusicItem item : musicBox.objects) {
                            item.state = Const.STATE_STOP;
                        }

                        p.state = Const.STATE_LOAD;
                        musicBox.play_item_id = p.id;

                        if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
                            boolean bRes = musicBox.GetAudioFiles(p);
                            if (!bRes) super.onStartCommand(intent, flags, startId);
                            //play_sub_item_pos = 0;
                            musicBox.GetNextMusic(p);
                        }

                        OnStartMusic(p);
                        //Toast.makeText(this, "OnStopMusic 1", Toast.LENGTH_SHORT).show();

                        break;
                }
            }
            if (type == Const.TYPE_NEXT) {
                for (MusicItem item : musicBox.objects) {
                    if (musicBox.play_item_id != id)
                        item.state = Const.STATE_STOP;
                }

                if (pos < 0) {
                    OnStopMusic(p, false);
                    boolean bRes = musicBox.GetNextMusic(p);
                    if (bRes) OnStartMusic(p);
                    else Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show();

                    //Toast.makeText(this, "OnStopMusic 2", Toast.LENGTH_SHORT).show();
                } else {
                    if (musicBox.play_sub_item_pos != pos) {
                        p.state = Const.STATE_LOAD;
                        musicBox.play_item_id = id;
                        musicBox.play_sub_item_pos = pos;
                        OnStopMusic(p, false);
                        OnStartMusic(p);

                        //Toast.makeText(this, "OnStopMusic 3", Toast.LENGTH_SHORT).show();
                    } else {
                        musicBox.play_item_id = -1;
                        musicBox.play_sub_item_pos = -1;
                        p.state = Const.STATE_STOP;
                        OnStopMusic(p, true);
                    }
                }
            }

            if (type == Const.TYPE_PREV) {
                for (MusicItem item : musicBox.objects) {
                    if (musicBox.play_item_id != id)
                        item.state = Const.STATE_STOP;
                }

                if (pos < 0) {
                    OnStopMusic(p, false);
                    boolean bRes = musicBox.GetPrevMusic(p);
                    if (bRes) OnStartMusic(p);
                    else Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show();

                    //Toast.makeText(this, "OnStopMusic 4", Toast.LENGTH_SHORT).show();
                } else {
                    if (musicBox.play_sub_item_pos != pos) {
                        p.state = Const.STATE_LOAD;
                        musicBox.play_item_id = id;
                        musicBox.play_sub_item_pos = pos;
                        OnStopMusic(p, false);
                        OnStartMusic(p);
                        //Toast.makeText(this, "OnStopMusic 5", Toast.LENGTH_SHORT).show();
                    } else {
                        musicBox.play_item_id = -1;
                        musicBox.play_sub_item_pos = -1;
                        p.state = Const.STATE_STOP;
                        OnStopMusic(p, true);
                    }
                }
            }

            if (type == Const.TYPE_PAUSE) {

                if (mediaPlayer.isPlaying()) {
                    p.state = Const.STATE_PAUSE;
                    mediaPlayer.pause();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(10);
                }
                sendNotificationTask(p);
                UpdateWidget();

                Lockscreen.pause();
            }

            if (type == Const.TYPE_RESUME) {

                if (!mediaPlayer.isPlaying()) {
                    p.state = Const.STATE_PLAY;
                    if (pos >= 0 && pos <= 100) {
                        int msec = pos * mediaPlayer.getDuration() / 100;
                        mediaPlayer.seekTo(msec);
                    }
                    mediaPlayer.start();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(10);
                }
                sendNotificationTask(p);
                UpdateWidget();

                Lockscreen.play();
            }

            if (musicBox.AllMusicStopped()) {
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    public void StopMedia() {

        if (musicBox.AllMusicStopped()) {
            mAudioFocusHelper.abandonFocus();
            Lockscreen.remove();
            nm.cancel(NOTIFICATION_ID);
        }

        if (mediaPlayer != null) {
            if (timerRefreshPos != null) {
                timerRefreshPos.cancel();
                timerRefreshPos = null;
            }

            /*if (m_MediaInfoTask != null) {
                m_MediaInfoTask.cancel(true);
                m_MediaInfoTask = null;
            }*/
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

            if (m_RadioIconInfoHandlerThread != null) {
                m_RadioIconInfoHandlerThread.quit();
                try {
                    m_RadioIconInfoHandlerThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                m_RadioIconInfoHandler = null;
                m_RadioIconInfoHandlerThread = null;
            }

            if (m_NotifHandlerThread != null) {
                m_NotifHandlerThread.quit();
                try {
                    m_NotifHandlerThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                m_NotifHandler = null;
                m_NotifHandlerThread = null;
            }

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        StopMedia();

        if (musicBox.play_item_id > 0) {
            MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
            if (mi != null) {
                if (mi.type == Const.TYPE_RADIO) {
                    boolean bRes = musicBox.StopAll(false);
                    StopMedia();
                    UpdateWidget();
                    //miAdapter.notifyDataSetChanged();
                    if (bRes)
                        Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show();
                    onCompletion(mp);
                }
            }
            else
                Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    class TimerRefreshPosTask extends TimerTask {
        @Override
        public void run() {

            Intent i = new Intent(Const.FORCE_POS_UPDATE);

            if (mediaPlayer != null) {
                i.putExtra("pos", mediaPlayer.getCurrentPosition());
                i.putExtra("duration", mediaPlayer.getDuration());
            }
            sendBroadcast(i);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        musicBox.UpdateState(Const.STATE_PLAY);

        MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
        if (mi != null) {
            sendNotificationTask(mi);
            UpdateWidget();

            if (m_bSound)
                mp.setVolume(1, 1);
            else
                mp.setVolume(0, 0);

            mp.start();

            if (mi.type == Const.TYPE_FOLDER || mi.type == Const.TYPE_PLAYLIST) {
                if (timerRefreshPos != null) {
                    timerRefreshPos.cancel();
                    timerRefreshPos = null;
                }
                timerRefreshPos = new Timer();
                m_TimerRefreshPosTask = new TimerRefreshPosTask();
                timerRefreshPos.schedule(m_TimerRefreshPosTask, 0, 1000);
            }

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

            m_MediaInfoHandlerThread = new HandlerThread("GetMediaInfo") {
                @Override
                protected void onLooperPrepared() {
                    m_MediaInfoHandler = new Handler(getLooper());
                    m_MediaInfoHandler.post(m_GetMediaInfo);
                }
            };
            m_MediaInfoHandlerThread.start();

            mAudioFocusHelper.requestFocus();
            if ( null == Lockscreen || !Lockscreen.ready() ) {

                Lockscreen = new LockscreenManager( this );
            }
            Lockscreen.setMediaID( mi );
            Lockscreen.play();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (musicBox.play_item_id > 0) {
            MusicItem mi = musicBox.getMusicItemByID(musicBox.play_item_id);
            if (mi != null) {
                if (mi.type == Const.TYPE_FOLDER || mi.type == Const.TYPE_PLAYLIST) {

                    boolean bRes = musicBox.GetNextMusic(mi);

                    if (bRes) {
                        OnStartMusic(mi);
                        //Toast.makeText(this, "OnStopMusic 6", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (mi.type == Const.TYPE_RADIO) {
                    mi.state = Const.STATE_LOAD;
                    OnStartMusic(mi);
                    return;
                }
            }
        }
        musicBox.StopAll(false);
        StopMedia();
        UpdateWidget();
    }

    public void FillRemoteView(RemoteViews rv, MusicItem mi, String strTitle, String strText) {
        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        if (mi.name == strTitle) {
            rv.setViewVisibility(R.id.tvMainName, View.GONE);
            rv.setViewVisibility(R.id.llEmpty, View.GONE);
        }
        else {
            rv.setViewVisibility(R.id.tvMainName, View.VISIBLE);
            rv.setViewVisibility(R.id.llEmpty, View.VISIBLE);
        }

        rv.setTextViewText(R.id.tvMainName, mi.name);
        rv.setTextViewText(R.id.tvType, strTitle);
        rv.setTextViewText(R.id.tvName, strText);

        rv.setViewVisibility(R.id.ivImageNext, View.GONE);
        rv.setViewVisibility(R.id.ivImagePrev, View.GONE);
        rv.setViewVisibility(R.id.ivImagePause, View.GONE);
        rv.setViewVisibility(R.id.ivImageLoad, View.GONE);
        rv.setViewVisibility(R.id.ivImage, View.VISIBLE);

        Bitmap btm = mi.GetBitmap();

        if (btm != null)
            rv.setImageViewBitmap(R.id.imageView, btm);
        else
            rv.setImageViewResource(R.id.imageView, R.drawable.ic_main_large2);

        switch (mi.state) {
            case Const.STATE_PAUSE:
                if (mi.type == Const.TYPE_FOLDER || mi.type == Const.TYPE_PLAYLIST) {
                    rv.setViewVisibility(R.id.ivImageNext, View.VISIBLE);
                    rv.setViewVisibility(R.id.ivImagePrev, View.VISIBLE);
                }
                rv.setViewVisibility(R.id.ivImagePause, View.VISIBLE);
                rv.setImageViewResource(R.id.ivImagePause, R.drawable.pause_off);
                rv.setImageViewResource(R.id.ivImage, R.drawable.stop);
                break;
            case Const.STATE_STOP:
                //((ImageView) view.findViewById(R.id.ivImageNext)).setVisibility(View.INVISIBLE);
                rv.setImageViewResource(R.id.ivImage, R.drawable.play);
                break;
            case Const.STATE_PLAY:
                if (mi.type == Const.TYPE_FOLDER || mi.type == Const.TYPE_PLAYLIST) {
                    rv.setViewVisibility(R.id.ivImageNext, View.VISIBLE);
                    rv.setViewVisibility(R.id.ivImagePrev, View.VISIBLE);
                }
                rv.setViewVisibility(R.id.ivImagePause, View.VISIBLE);
                rv.setImageViewResource(R.id.ivImagePause, R.drawable.pause);
                rv.setImageViewResource(R.id.ivImage, R.drawable.stop);
                break;
            case Const.STATE_LOAD:
                //((ImageView) view.findViewById(R.id.ivImageNext)).setVisibility(View.INVISIBLE);
                //remoteViews.setImageViewResource((int)idIV.get(index), R.drawable.load);
                rv.setViewVisibility(R.id.ivImage, View.GONE);
                rv.setViewVisibility(R.id.ivImageLoad, View.VISIBLE);
                break;
        }

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent p_mainIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);
        rv.setOnClickPendingIntent(R.id.llView, p_mainIntent);
        rv.setOnClickPendingIntent(R.id.imageView, p_mainIntent);

        if (mi.type == Const.TYPE_FOLDER || mi.type == Const.TYPE_PLAYLIST) {
            rv.setViewVisibility(R.id.llEmptyLeft, View.GONE);
            rv.setViewVisibility(R.id.llEmptyRight, View.GONE);

            Intent updateIntent = new Intent(this, ActivityPlayFile.class);
            updateIntent.setAction(String.valueOf(mi.id));
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
            rv.setOnClickPendingIntent(R.id.llName, pIntent);
        }
        else
        {
            Intent updateIntent = new Intent(this, MediaService.class);
            if (mi.state == Const.STATE_PLAY)
                updateIntent.setAction(Const.ACTION_PAUSE);
            else
                updateIntent.setAction(Const.ACTION_RESUME);
            updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pIntent = PendingIntent.getService(this, 0, updateIntent, 0);
            rv.setOnClickPendingIntent(R.id.llName, pIntent);
        }

        { // ACTION_NEXT
            Intent intentService = new Intent(this, MediaService.class);
            intentService.setAction(Const.ACTION_NEXT);
            intentService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pNextIntent = PendingIntent.getService(this, 0, intentService, 0);
            rv.setOnClickPendingIntent(R.id.ivImageNext, pNextIntent);
        }

        { // ACTION_PREV
            Intent intentService = new Intent(this, MediaService.class);
            intentService.setAction(Const.ACTION_PREV);
            intentService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pPrevIntent = PendingIntent.getService(this, 0, intentService, 0);
            rv.setOnClickPendingIntent(R.id.ivImagePrev, pPrevIntent);
        }

        { // ACTION_PLAY_STOP
            Intent intentStopService = new Intent(this, MediaService.class);
            if (mi.state == Const.STATE_STOP)
                intentStopService.setAction(Const.ACTION_PLAY);
            else
                intentStopService.setAction(Const.ACTION_STOP);
            intentStopService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pStopIntent = PendingIntent.getService(this, 0, intentStopService, 0);
            rv.setOnClickPendingIntent(R.id.ivImage, pStopIntent);
            rv.setOnClickPendingIntent(R.id.ivImageLoad, pStopIntent);
        }

        { // ACTION_PAUSE
            Intent intentPauseService = new Intent(this, MediaService.class);
            if (mi.state == Const.STATE_PLAY)
                intentPauseService.setAction(Const.ACTION_PAUSE);
            if (mi.state == Const.STATE_PAUSE)
                intentPauseService.setAction(Const.ACTION_RESUME);
            //intentPauseService.putExtra("id", mi.id);
            //intentPauseService.putExtra("type", 2);
            //intentPauseService.putExtra("pos", -1);
            intentPauseService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pPauseIntent = PendingIntent.getService(this, 0, intentPauseService, 0);
            rv.setOnClickPendingIntent(R.id.ivImagePause, pPauseIntent);
        }
    }

    public void sendNotificationTask(MusicItem mi) {
        if (m_NotifHandlerThread != null) {
            m_NotifHandlerThread.quit();
            try {
                m_NotifHandlerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            m_NotifHandler = null;
            m_NotifHandlerThread = null;
        }

        m_NotifHandlerThread = new HandlerThread("SendNotif") {
            @Override
            protected void onLooperPrepared() {
                m_NotifHandler = new Handler(getLooper());
                m_NotifHandler.post(m_SendNotif);
            }
        };
        m_NotifHandlerThread.start();
    }

    public void sendNotification(MusicItem mi) {
        if (mi == null) return;

        String strTitle = mi.GetTitle();
        String strText = mi.GetShortDescript();

        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_item);
        RemoteViews rv_small = new RemoteViews(getPackageName(), R.layout.notification_small_item);

        FillRemoteView(rv, mi, strTitle, strText);
        FillRemoteView(rv_small, mi, strTitle, strTitle + " (" + strText + ")");

        //Resources res = this.getApplicationContext().getResources();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_main)
                        .setContentTitle(strTitle)
                        .setContentText(strText)
                        .setContent(rv_small)
                        //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_main))
                        .setCustomBigContentView(rv)
                        .setTicker(strTitle + ": " + strText);
        //.setContentTitle(getResources().getString(R.string.test_notification))
        //.setContentText(getResources().getString(R.string.go_to_new_activity));

        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //NOTIFICATION_ID = (int)System.currentTimeMillis();
        Notification n = mBuilder.build();
        n.flags = n.flags | Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(NOTIFICATION_ID, n);
    }

    /*FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
mmr.setDataSource(p.files.get(musicBox.play_sub_item_pos));
String str1 = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
//String utf8String= new String(str1.getBytes("UTF-8"), "Cp1251");
String str2 = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
String str3 = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
String str4 = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK);
String str5 = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FILENAME);
mmr.release();*/

    public void OnStartMusic(MusicItem p) {
        StopMedia();

        if (p != null) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(10);

            mediaPlayer = new MediaPlayer();

            // ставим эквалайзер
            equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);
            SetEqPref();
            // ---------------

            try {
                if (p.type == Const.TYPE_RADIO) {
                    p.rti.Clear();
                    mediaPlayer.setDataSource(p.url);
                }
                else {
                    if (musicBox.play_sub_item_pos >= 0 && musicBox.play_sub_item_pos < p.files.size())
                        mediaPlayer.setDataSource(p.files.get(musicBox.play_sub_item_pos).m_strName);
                    else {
                        onCompletion(mediaPlayer);
                        return;
                    }
                }
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show();
                onCompletion(mediaPlayer);
                return;
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(this);
            //miAdapter.notifyDataSetChanged();
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);
            if (p.type == Const.TYPE_RADIO) {
                mediaPlayer.prepareAsync();

                if (m_RadioIconInfoHandlerThread != null) {
                    m_RadioIconInfoHandlerThread.quit();
                    try {
                        m_RadioIconInfoHandlerThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    m_RadioIconInfoHandler = null;
                    m_RadioIconInfoHandlerThread = null;
                }

                m_RadioIconInfoHandlerThread = new HandlerThread("GetRadioIconInfo") {
                    @Override
                    protected void onLooperPrepared() {
                        m_RadioIconInfoHandler = new Handler(getLooper());
                        m_RadioIconInfoHandler.post(m_GetRadioIconInfo);
                    }
                };
                m_RadioIconInfoHandlerThread.start();
             }
            else
                try {
                    p.files.get(musicBox.play_sub_item_pos).GetFileTitleDescript();

                    mediaPlayer.prepare();
                } catch (IOException e) {
                    Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show();
                    onCompletion(mediaPlayer);
                    return;
                }
            sendNotificationTask(p);
        }
        else {
            musicBox.StopAll(false);
        }
        UpdateWidget();
    }

    public void OnStopMusic(MusicItem p, boolean bNeedVibrate) {
        if (bNeedVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(10);
        }
        StopMedia();

        UpdateWidget();
    }

    public void OnPauseMusic(MusicItem p) {
    }

    public void ShowMessage(String strMessage)
    {
        Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
    }
}

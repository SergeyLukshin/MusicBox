package ru.adoon.mymusic.Consts;

/**
 * Created by Лукшин on 13.07.2017.
 */

public class Const {

    final public static String EQUALIZER_PREF = "equalizer_pref";
    final public static String PREF_USE_EQ = "pref_use_eq";
    final public static String PREF_EQ_INFO = "pref_eq_info";

    final public static String ACTION_LIST = "ru.adoon.mymusic.list";
    final public static String ACTION_PLAY = "ru.adoon.mymusic.play";
    final public static String ACTION_STOP = "ru.adoon.mymusic.stop";
    final public static String ACTION_NEXT = "ru.adoon.mymusic.next";
    final public static String ACTION_PREV = "ru.adoon.mymusic.prev";
    final public static String ACTION_PAUSE = "ru.adoon.mymusic.pause";
    final public static String ACTION_RESUME = "ru.adoon.mymusic.resume";
    final public static String ACTION_SOUND = "ru.adoon.mymusic.sound";
    final public static String ACTION_BLUETOOTH = "ru.adoon.mymusic.bluetooth";
    final public static String ACTION_EQUALIZER = "ru.adoon.mymusic.equalizer";

    final public static String FORCE_WIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";//"ru.adoon.mymusic.widget_update";
    final public static String FORCE_POS_UPDATE = "ru.adoon.mymusic.pos_update";

    final public static int DIALOG_RADIO = 1;
    final public static int DIALOG_FOLDER = 2;

    public static final int TYPE_RADIO = 0;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_PLAYLIST = 2;

    public static final int STATE_STOP = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_PLAY = 2;
    public static final int STATE_LOAD = 3;

    static public int TYPE_PLAY = 0;
    static public int TYPE_NEXT = 1;
    static public int TYPE_PAUSE = 2;
    static public int TYPE_RESUME = 3;
    static public int TYPE_PREV = 4;
    //static int TYPE_STOP = 4;
}

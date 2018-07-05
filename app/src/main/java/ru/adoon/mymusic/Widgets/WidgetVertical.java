package ru.adoon.mymusic.Widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Comparator;

import ru.adoon.mymusic.Activities.ActivityEqualizer;
import ru.adoon.mymusic.Activities.ActivityPlayFile;
import ru.adoon.mymusic.Activities.ConfigActivity;
import ru.adoon.mymusic.Activities.MainActivity;
import ru.adoon.mymusic.Classes.MusicBox;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;
import ru.adoon.mymusic.Services.MediaService;

// size = (n*70) - 30
public class WidgetVertical extends AppWidgetProvider {

    ArrayList<Integer> idLL = new ArrayList<Integer>();
    ArrayList<Integer> idIV = new ArrayList<Integer>();
    ArrayList<Integer> idIVNext = new ArrayList<Integer>();
    ArrayList<Integer> idIVPrev = new ArrayList<Integer>();
    ArrayList<Integer> idIVPause = new ArrayList<Integer>();
    ArrayList<Integer> idTV = new ArrayList<Integer>();
    ArrayList<Integer> idTV_ = new ArrayList<Integer>();
    //ArrayList<Integer> idTVFull = new ArrayList<Integer>();
    ArrayList<Integer> idIVLoad = new ArrayList<Integer>();
    ArrayList<Integer> idIVIcon = new ArrayList<Integer>();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        ComponentName thisWidget = new ComponentName(context,
                WidgetVertical.class);

        updateWidget(context, appWidgetManager);
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // Удаляем Preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_BLUETOOTH + widgetID);
            editor.remove(ConfigActivity.WIDGET_SOUND + widgetID);
            editor.remove(ConfigActivity.WIDGET_EQUALIZER + widgetID);
            editor.remove(ConfigActivity.WIDGET_FULL_MODE + widgetID);
            editor.remove(ConfigActivity.WIDGET_NEXT + widgetID);
            editor.remove(ConfigActivity.WIDGET_PREV + widgetID);
            editor.remove(ConfigActivity.WIDGET_PAUSE + widgetID);
            editor.remove(ConfigActivity.WIDGET_SHOW_FILE_NAME + widgetID);
            editor.remove(ConfigActivity.WIDGET_SHOW_ICON + widgetID);
            editor.remove(ConfigActivity.WIDGET_THEME + widgetID);
            editor.remove(ConfigActivity.WIDGET_TRANSPARENCY + widgetID);
            editor.remove(ConfigActivity.WIDGET_BORDER + widgetID);
            editor.remove(ConfigActivity.WIDGET_ITEMS + widgetID);
        }
        editor.commit();
    }

    public class IDComparator
            implements Comparator
    {
        public int compare(Object mi1, Object mi2) {
            int id1 = ((Integer)mi1);
            int id2 = ((Integer)mi2);
            if (id1 < id2) return -1;
            if (id2 < id1) return  1;
            return 0;
        }
    }

    public static int dipToPx(Context context, int dip) {
        DisplayMetrics displayMetrics = context.getResources()
                .getDisplayMetrics();
        return (int) (dip * displayMetrics.density + 0.5f);
    }

    void updateWidget(Context context, AppWidgetManager appWidgetManager) {

        ComponentName thisWidget = new ComponentName(context,
                WidgetVertical.class);

        if (MediaService.musicBox == null)
        {
            MediaService.musicBox = new MusicBox(context);
            MediaService.musicBox.GetData();
            MediaService.GetBluetooth();
            MediaService.LoadPref(context);
        }

        int[] arr_widgets = appWidgetManager.getAppWidgetIds(thisWidget);

        RemoteViews remoteViews = null;
        for (int widgetID : arr_widgets) {

            SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, context.MODE_PRIVATE);

            int valType = 1;//sp.getInt(ConfigActivity.WIDGET_TYPE + widgetID, 0);

            //if (valType == 0)
            //    remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            //else
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_vertical);

            int valBT = sp.getInt(ConfigActivity.WIDGET_BLUETOOTH + widgetID, 1);
            if (valBT == 0) remoteViews.setViewVisibility(R.id.llBluetooth, View.GONE);
            else remoteViews.setViewVisibility(R.id.llBluetooth, View.VISIBLE);

            int valSound = sp.getInt(ConfigActivity.WIDGET_SOUND + widgetID, 1);
            if (valSound == 0) {
                remoteViews.setViewVisibility(R.id.llSound2, View.GONE);
                remoteViews.setViewVisibility(R.id.llSound, View.GONE);
            }
            else {
                if (valBT == 0) {
                    remoteViews.setViewVisibility(R.id.llSound2, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.llSound, View.GONE);
                }
                else {
                    remoteViews.setViewVisibility(R.id.llSound2, View.GONE);
                    remoteViews.setViewVisibility(R.id.llSound, View.VISIBLE);
                }
            }

            int valEq = sp.getInt(ConfigActivity.WIDGET_EQUALIZER + widgetID, 1);
            if (valEq == 0) {
                remoteViews.setViewVisibility(R.id.llEq2, View.GONE);
                remoteViews.setViewVisibility(R.id.llEq, View.GONE);
            }
            else {
                if (valBT == 0 && valSound == 0) {
                    remoteViews.setViewVisibility(R.id.llEq2, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.llEq, View.GONE);
                }
                else {
                    remoteViews.setViewVisibility(R.id.llEq2, View.GONE);
                    remoteViews.setViewVisibility(R.id.llEq, View.VISIBLE);
                }
            }

            int valFullMode = sp.getInt(ConfigActivity.WIDGET_FULL_MODE + widgetID, 1);
            int valShowFileName = sp.getInt(ConfigActivity.WIDGET_SHOW_FILE_NAME + widgetID, 1);
            int valShowPrev = sp.getInt(ConfigActivity.WIDGET_PREV + widgetID, 1);
            int valShowNext = sp.getInt(ConfigActivity.WIDGET_NEXT + widgetID, 1);
            int valShowPause = sp.getInt(ConfigActivity.WIDGET_PAUSE + widgetID, 1);
            int valShowIcon = sp.getInt(ConfigActivity.WIDGET_SHOW_ICON + widgetID, 1);
            int valTheme = sp.getInt(ConfigActivity.WIDGET_THEME + widgetID, 0);
            int valTransparency = sp.getInt(ConfigActivity.WIDGET_TRANSPARENCY + widgetID, 50);
            int valBorder = sp.getInt(ConfigActivity.WIDGET_BORDER + widgetID, 0);

            if (valTheme == 0) // black
            {
                if (valTransparency == 0) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_0);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_0_border);
                }
                if (valTransparency == 25) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_25);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_25_border);
                }
                if (valTransparency == 50) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_50);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_50_border);
                }
                if (valTransparency == 75) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_75);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_75_border);
                }
                if (valTransparency == 100) {
                    remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_black_100);
                }

                remoteViews.setTextColor(R.id.tvMusic1, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic2, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic3, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic4, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic5, context.getResources().getColor(R.color.colorWhite));

                remoteViews.setTextColor(R.id.tvMusic1_, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic2_, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic3_, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic4_, context.getResources().getColor(R.color.colorWhite));
                remoteViews.setTextColor(R.id.tvMusic5_, context.getResources().getColor(R.color.colorWhite));
            }
            else {
                if (valTransparency == 0) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_0);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_0_border);
                }
                if (valTransparency == 25) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_25);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_25_border);
                }
                if (valTransparency == 50) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_50);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_50_border);
                }
                if (valTransparency == 75) {
                    if (valBorder == 0)
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_75);
                    else
                        remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_75_border);
                }
                if (valTransparency == 100) {
                    remoteViews.setInt(R.id.ll, "setBackgroundResource", R.xml.roundcorner_white_100);
                }

                remoteViews.setTextColor(R.id.tvMusic1, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic2, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic3, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic4, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic5, context.getResources().getColor(R.color.colorBlack));

                remoteViews.setTextColor(R.id.tvMusic1_, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic2_, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic3_, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic4_, context.getResources().getColor(R.color.colorBlack));
                remoteViews.setTextColor(R.id.tvMusic5_, context.getResources().getColor(R.color.colorBlack));
            }

            String strIDs = sp.getString(ConfigActivity.WIDGET_ITEMS + widgetID, null);
            //if (arrItemID == null) arrItemID = new HashSet<String>();

            //int iCnt = arrItemID.size();

            ArrayList<Integer> vecIDs = new ArrayList<Integer>();
            if (strIDs != null) {

                String[] separated = strIDs.split(",");
                for (String str : separated) {
                    if (!str.isEmpty()) {
                        int id = Integer.valueOf(str);
                        if (MediaService.musicBox.getMusicItemByID(id) != null)
                            vecIDs.add(Integer.valueOf(str));
                    }
                }
            }

            //Comparator mc;
            //mc = new IDComparator();
            //Collections.sort(vecIDs, mc);

            remoteViews.setViewVisibility(R.id.llTop, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.llmain, View.GONE);
            remoteViews.setViewVisibility(R.id.llmain2, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.llImage, View.GONE);
            remoteViews.setTextViewText(R.id.tvMainName, "");

            if (MediaService.m_bSound) {
                remoteViews.setImageViewResource(R.id.ivSound, R.drawable.sound_on);
                remoteViews.setImageViewResource(R.id.ivSound2, R.drawable.sound_on);
            }
            else {
                remoteViews.setImageViewResource(R.id.ivSound, R.drawable.sound_off);
                remoteViews.setImageViewResource(R.id.ivSound2, R.drawable.sound_off);
            }

            if (MediaService.m_bBluetooth)
                remoteViews.setImageViewResource(R.id.ivBluetooth, R.drawable.bluetooth_on);
            else
                remoteViews.setImageViewResource(R.id.ivBluetooth, R.drawable.bluetooth_off);

            if (MediaService.m_iUseEq == 1) {
                remoteViews.setImageViewResource(R.id.ivEq, R.drawable.eq_color);
                remoteViews.setImageViewResource(R.id.ivEq2, R.drawable.eq_color);
            }
            else {
                remoteViews.setImageViewResource(R.id.ivEq, R.drawable.eq);
                remoteViews.setImageViewResource(R.id.ivEq2, R.drawable.eq);
            }

            idLL.clear();
            idLL.add(R.id.ll1);
            idLL.add(R.id.ll2);
            idLL.add(R.id.ll3);
            idLL.add(R.id.ll4);
            idLL.add(R.id.ll5);

            idTV.clear();
            idTV.add(R.id.tvMusic1);
            idTV.add(R.id.tvMusic2);
            idTV.add(R.id.tvMusic3);
            idTV.add(R.id.tvMusic4);
            idTV.add(R.id.tvMusic5);

            idTV_.clear();
            idTV_.add(R.id.tvMusic1_);
            idTV_.add(R.id.tvMusic2_);
            idTV_.add(R.id.tvMusic3_);
            idTV_.add(R.id.tvMusic4_);
            idTV_.add(R.id.tvMusic5_);

            idIV.clear();
            idIV.add(R.id.ivMusicImage1);
            idIV.add(R.id.ivMusicImage2);
            idIV.add(R.id.ivMusicImage3);
            idIV.add(R.id.ivMusicImage4);
            idIV.add(R.id.ivMusicImage5);

            idIVNext.clear();
            idIVNext.add(R.id.ivMusicImage1Next);
            idIVNext.add(R.id.ivMusicImage2Next);
            idIVNext.add(R.id.ivMusicImage3Next);
            idIVNext.add(R.id.ivMusicImage4Next);
            idIVNext.add(R.id.ivMusicImage5Next);

            idIVPrev.clear();
            idIVPrev.add(R.id.ivMusicImage1Prev);
            idIVPrev.add(R.id.ivMusicImage2Prev);
            idIVPrev.add(R.id.ivMusicImage3Prev);
            idIVPrev.add(R.id.ivMusicImage4Prev);
            idIVPrev.add(R.id.ivMusicImage5Prev);

            idIVPause.clear();
            idIVPause.add(R.id.ivMusicImage1Pause);
            idIVPause.add(R.id.ivMusicImage2Pause);
            idIVPause.add(R.id.ivMusicImage3Pause);
            idIVPause.add(R.id.ivMusicImage4Pause);
            idIVPause.add(R.id.ivMusicImage5Pause);

            idIVLoad.clear();
            idIVLoad.add(R.id.ivMusicImageLoad1_);
            idIVLoad.add(R.id.ivMusicImageLoad2_);
            idIVLoad.add(R.id.ivMusicImageLoad3_);
            idIVLoad.add(R.id.ivMusicImageLoad4_);
            idIVLoad.add(R.id.ivMusicImageLoad5_);

            idIVIcon.clear();
            idIVIcon.add(R.id.ivImageIcon1);
            idIVIcon.add(R.id.ivImageIcon2);
            idIVIcon.add(R.id.ivImageIcon3);
            idIVIcon.add(R.id.ivImageIcon4);
            idIVIcon.add(R.id.ivImageIcon5);

            if (valType == 0) {
                for (int i = 0; i < idIVPause.size(); i++) {
                    remoteViews.setViewPadding(idIVPause.get(i), 0, 0, 0, 0);
                    remoteViews.setViewPadding(idIV.get(i), 0, 0, 0, 0);
                }
            }

            { // ACTION_EQUALIZER
                Intent eqIntent = new Intent(context, ActivityEqualizer.class);
                PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, eqIntent, 0);

                remoteViews.setOnClickPendingIntent(R.id.llEq, pIntent);
                remoteViews.setOnClickPendingIntent(R.id.llEq2, pIntent);
            }

            { // ACTION_SOUND
                Intent soundIntent = new Intent(context, MediaService.class);
                soundIntent.setAction(Const.ACTION_SOUND);
                PendingIntent pIntent = PendingIntent.getService(context, widgetID, soundIntent, 0);

                remoteViews.setOnClickPendingIntent(R.id.llSound, pIntent);
                remoteViews.setOnClickPendingIntent(R.id.llSound2, pIntent);
            }

            { // ACTION_BLUETOOTH
                Intent bluetoothIntent = new Intent(context, MediaService.class);
                bluetoothIntent.setAction(Const.ACTION_BLUETOOTH);
                //bluetoothIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                PendingIntent pIntent = PendingIntent.getService(context, widgetID, bluetoothIntent, 0);

                remoteViews.setOnClickPendingIntent(R.id.llBluetooth, pIntent);
            }

            { // ACTION_APPWIDGET_CONFIGURE
                Intent configIntent = new Intent(context, ConfigActivity.class);
                configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                PendingIntent pIntent = PendingIntent.getActivity(context, widgetID,
                        configIntent, 0);
                remoteViews.setOnClickPendingIntent(R.id.llConfig, pIntent);
            }

            for (int ii = 0; ii < vecIDs.size(); ii++) {
                remoteViews.setViewVisibility(idLL.get(ii), View.VISIBLE);
                remoteViews.setViewVisibility((int) idTV.get(ii), View.VISIBLE);
                remoteViews.setViewVisibility((int) idTV_.get(ii), View.GONE);

                remoteViews.setImageViewResource((int) idIVPrev.get(ii), R.drawable.prev);
                remoteViews.setImageViewResource((int) idIVNext.get(ii), R.drawable.next);
            }
            for (int ii = vecIDs.size(); ii < 5; ii++) {
                remoteViews.setViewVisibility(idLL.get(ii), View.GONE);
            }

            int index = 0;

            remoteViews.setViewVisibility(R.id.llTitle, View.GONE);
            remoteViews.setViewVisibility(R.id.llBtn, View.VISIBLE);

            for (int id : vecIDs) {

                MusicItem p = MediaService.musicBox.getMusicItemByID(id);

                /*if (valType == 0 && p.state != Const.STATE_STOP) {
                    if (valShowFileName == 1) {
                        remoteViews.setTextViewText(idTV.get(index), p.GetTitle() + " (" + p.GetShortDescript() + ")");

                        if (p.name != p.GetTitle()) {
                            remoteViews.setTextViewText(R.id.tvMainName, p.name);
                            remoteViews.setViewVisibility(R.id.llImage, View.VISIBLE);
                            Bitmap btm = p.GetBitmap();
                            if (btm != null) {
                                remoteViews.setImageViewBitmap(R.id.ivImage, btm);
                            }
                            else
                                remoteViews.setImageViewResource(R.id.ivImage, R.drawable.ic_main);
                        }
                    }
                    else
                        remoteViews.setTextViewText(idTV.get(index), p.name);
                }
                else {
                    remoteViews.setTextViewText(idTV.get(index), p.name);
                }

                if (valType == 1 && p.state != Const.STATE_STOP) {
                    if (valShowFileName == 1) {
                        remoteViews.setTextViewText(idTV.get(index), p.GetTitle() + " (" + p.GetShortDescript() + ")");
                    }
                    else
                        remoteViews.setTextViewText(idTV.get(index), p.name);
                }
                else {
                    remoteViews.setTextViewText(idTV.get(index), p.name);
                }*/

                if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
                    Intent updateIntent = new Intent(context, ActivityPlayFile.class);
                    //updateIntent.putExtra("id", p.id);
                    updateIntent.setAction(String.valueOf(p.id));
                    PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, updateIntent, 0);
                    remoteViews.setOnClickPendingIntent(idLL.get(index), pIntent);
                }
                else
                {
                    Intent updateIntent = new Intent(context, MediaService.class);
                    if (p.state == Const.STATE_PLAY) {
                        if (valShowPause == 1)
                            updateIntent.setAction(Const.ACTION_PAUSE);
                        else
                            updateIntent.setAction(Const.ACTION_STOP);
                    }
                    if (p.state == Const.STATE_PAUSE)
                        updateIntent.setAction(Const.ACTION_RESUME);
                    if (p.state == Const.STATE_STOP)
                        updateIntent.setAction(Const.ACTION_PLAY + "#" + String.valueOf(p.id));
                    if (p.state == Const.STATE_LOAD)
                        updateIntent.setAction(Const.ACTION_STOP);
                    PendingIntent pIntent = PendingIntent.getService(context, widgetID, updateIntent, 0);
                    remoteViews.setOnClickPendingIntent(idLL.get(index), pIntent);
                }

                { // ACTION_PLAY_STOP
                    Intent updateIntent = new Intent(context, MediaService.class);
                    if (p.state == Const.STATE_PLAY || p.state == Const.STATE_PAUSE || p.state == Const.STATE_LOAD)
                        updateIntent.setAction(Const.ACTION_STOP);
                    else
                        updateIntent.setAction(Const.ACTION_PLAY + "#" + String.valueOf(p.id));
                    PendingIntent pIntent = PendingIntent.getService(context, 0, updateIntent, 0);
                    remoteViews.setOnClickPendingIntent(idIV.get(index), pIntent);
                    remoteViews.setOnClickPendingIntent(idIVLoad.get(index), pIntent);
                }

                { // ACTION_NEXT
                    Intent updateIntent = new Intent(context, MediaService.class);
                    updateIntent.setAction(Const.ACTION_NEXT);
                    //updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                    PendingIntent pIntent = PendingIntent.getService(context, 0, updateIntent, 0);
                    remoteViews.setOnClickPendingIntent(idIVNext.get(index), pIntent);
                }

                { // ACTION_PREV
                    Intent updateIntent = new Intent(context, MediaService.class);
                    updateIntent.setAction(Const.ACTION_PREV);
                    //updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                    PendingIntent pIntent = PendingIntent.getService(context, 0, updateIntent, 0);
                    remoteViews.setOnClickPendingIntent(idIVPrev.get(index), pIntent);
                }

                { // ACTION_PAUSE
                    Intent updateIntent = new Intent(context, MediaService.class);
                    if (p.state == Const.STATE_PLAY)
                        updateIntent.setAction(Const.ACTION_PAUSE);
                    if (p.state == Const.STATE_PAUSE)
                        updateIntent.setAction(Const.ACTION_RESUME);
                    //updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                    PendingIntent pIntent = PendingIntent.getService(context, widgetID, updateIntent, 0);
                    remoteViews.setOnClickPendingIntent(idIVPause.get(index), pIntent);
                }

                remoteViews.setViewVisibility((int) idIVNext.get(index), View.GONE);
                remoteViews.setViewVisibility((int) idIVPrev.get(index), View.GONE);
                remoteViews.setViewVisibility((int) idIVPause.get(index), View.GONE);
                remoteViews.setViewVisibility((int) idIVLoad.get(index), View.GONE);
                remoteViews.setViewVisibility((int) idIV.get(index), View.VISIBLE);

                if (valShowIcon == 1) {
                    remoteViews.setViewVisibility((int) idIVIcon.get(index), View.VISIBLE);

                    if (p.state != Const.STATE_STOP)
                        remoteViews.setViewVisibility((int) idIVIcon.get(index), View.GONE);
                    else {
                        if (p.type == Const.TYPE_RADIO && p.radioid > 0) {
                            Bitmap icon_btm = p.GetIconBitmap();
                            if (icon_btm != null)
                                remoteViews.setImageViewBitmap((int) idIVIcon.get(index), icon_btm);
                            else
                                remoteViews.setImageViewResource((int) idIVIcon.get(index), R.drawable.radio_);
                        }

                        if (p.type == Const.TYPE_RADIO && p.radioid == 0)
                            remoteViews.setImageViewResource((int) idIVIcon.get(index), R.drawable.radio_);

                        if (p.type == Const.TYPE_FOLDER) {
                            remoteViews.setImageViewResource((int) idIVIcon.get(index), R.drawable.folder_);
                        }

                        if (p.type == Const.TYPE_PLAYLIST) {
                            remoteViews.setImageViewResource((int) idIVIcon.get(index), R.drawable.playlist_);
                        }
                    }
                }
                else
                    remoteViews.setViewVisibility((int) idIVIcon.get(index), View.GONE);

                switch (p.state) {
                    case Const.STATE_STOP:
                        remoteViews.setImageViewResource((int) idIV.get(index), R.drawable.play);
                        /*if (valShowIcon == 1) {
                            remoteViews.setViewVisibility((int) idIV.get(index), View.GONE);
                        }
                        else {
                            remoteViews.setViewVisibility((int) idIV.get(index), View.VISIBLE);
                            remoteViews.setImageViewResource((int) idIV.get(index), R.drawable.play);
                        }*/
                        break;
                    case Const.STATE_PLAY:
                        if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
                            if (valFullMode == 1) {
                                if (valShowNext == 1)
                                    remoteViews.setViewVisibility((int) idIVNext.get(index), View.VISIBLE);
                                if (valShowPrev == 1)
                                    remoteViews.setViewVisibility((int) idIVPrev.get(index), View.VISIBLE);
                                if (valShowPause == 1)
                                    remoteViews.setViewVisibility((int) idIVPause.get(index), View.VISIBLE);
                                remoteViews.setImageViewResource((int) idIVPause.get(index), R.drawable.pause);
                            }
                        }
                        else {
                            if (valFullMode == 1) {
                                if (valShowPause == 1)
                                    remoteViews.setViewVisibility((int) idIVPause.get(index), View.VISIBLE);
                                remoteViews.setImageViewResource((int) idIVPause.get(index), R.drawable.pause);
                            }
                        }
                        remoteViews.setImageViewResource((int) idIV.get(index), R.drawable.stop);
                        break;
                    case Const.STATE_PAUSE:
                        if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
                            if (valFullMode == 1) {
                                if (valShowNext == 1)
                                    remoteViews.setViewVisibility((int) idIVNext.get(index), View.VISIBLE);
                                if (valShowPrev == 1)
                                    remoteViews.setViewVisibility((int) idIVPrev.get(index), View.VISIBLE);
                                if (valShowPause == 1)
                                    remoteViews.setViewVisibility((int) idIVPause.get(index), View.VISIBLE);
                                remoteViews.setImageViewResource((int) idIVPause.get(index), R.drawable.pause_off);
                            }
                        }
                        else {
                            if (valFullMode == 1) {
                                if (valShowPause == 1)
                                    remoteViews.setViewVisibility((int) idIVPause.get(index), View.VISIBLE);
                                remoteViews.setImageViewResource((int) idIVPause.get(index), R.drawable.pause_off);
                            }
                        }
                        remoteViews.setImageViewResource((int) idIV.get(index), R.drawable.stop);
                        break;
                    case Const.STATE_LOAD:
                        remoteViews.setViewVisibility((int) idIV.get(index), View.GONE);
                        remoteViews.setViewVisibility((int) idIVLoad.get(index), View.VISIBLE);
                        break;
                }

                if (valType == 0) {

                    if ((valFullMode == 1 || valShowFileName == 1) && p.state != Const.STATE_STOP) {
                        // прячем соседние элементы
                        for (int k = 0; k < vecIDs.size(); k++) {
                            if (k != index) {
                                remoteViews.setViewVisibility((int) idLL.get(k), View.GONE);
                            }
                            else
                                remoteViews.setViewVisibility((int) idTV.get(k), View.GONE);
                        }

                        remoteViews.setOnClickPendingIntent((int) idLL.get(index), null);

                        if (p.type == Const.TYPE_RADIO && p.state != Const.STATE_LOAD) {
                            remoteViews.setViewVisibility((int) idIVPrev.get(index), View.VISIBLE);
                            remoteViews.setViewVisibility((int) idIVNext.get(index), View.VISIBLE);
                            remoteViews.setImageViewResource((int) idIVPrev.get(index), R.drawable.clear2);
                            remoteViews.setImageViewResource((int) idIVNext.get(index), R.drawable.clear2);
                            remoteViews.setOnClickPendingIntent((int) idIVPrev.get(index), null);
                            remoteViews.setOnClickPendingIntent((int) idIVNext.get(index), null);
                        }
                    }

                    if (p.state != Const.STATE_STOP && (valFullMode == 1 || valShowFileName == 1)) {

                        // тольки для вывода полной информации и для папок
                        remoteViews.setViewVisibility(R.id.llTitle, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.llBtn, View.GONE);

                        if (valShowFileName == 1) {
                            remoteViews.setTextViewText(R.id.tvTitle, p.GetTitle());
                            remoteViews.setTextViewText(R.id.tvTitleSmall, p.GetShortDescript());
                            //remoteViews.setTextViewText(idTV.get(index), p.GetShortDescript());

                            //if (!p.name.equalsIgnoreCase(p.GetTitle())) {
                            remoteViews.setTextViewText(R.id.tvMainName, p.name);
                            remoteViews.setViewVisibility(R.id.llImage, View.VISIBLE);
                            Bitmap btm = p.GetBitmap();
                            if (btm != null) {
                                remoteViews.setImageViewBitmap(R.id.ivImage, btm);
                            } else
                                remoteViews.setImageViewResource(R.id.ivImage, R.drawable.ic_main);
                            //}
                        }
                        else {
                            remoteViews.setTextViewText(R.id.tvTitle, p.name);
                            remoteViews.setTextViewText(R.id.tvTitleSmall, p.GetFileName());
                        }

                        if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
                            Intent updateIntent = new Intent(context, ActivityPlayFile.class);
                            //updateIntent.putExtra("id", p.id);
                            updateIntent.setAction(String.valueOf(p.id));
                            PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, updateIntent, 0);
                            remoteViews.setOnClickPendingIntent(R.id.llTitle, pIntent);
                            remoteViews.setOnClickPendingIntent(idLL.get(index), pIntent);
                        }
                        else {
                            Intent updateIntent = new Intent(context, MediaService.class);
                            if (p.state == Const.STATE_PLAY) {
                                if (valShowPause == 1)
                                    updateIntent.setAction(Const.ACTION_PAUSE);
                                else
                                    updateIntent.setAction(Const.ACTION_STOP);
                            }
                            if (p.state == Const.STATE_PAUSE)
                                updateIntent.setAction(Const.ACTION_RESUME);
                            if (p.state == Const.STATE_STOP)
                                updateIntent.setAction(Const.ACTION_PLAY + "#" + String.valueOf(p.id));
                            if (p.state == Const.STATE_LOAD)
                                updateIntent.setAction(Const.ACTION_STOP);
                            PendingIntent pIntent = PendingIntent.getService(context, widgetID, updateIntent, 0);
                            remoteViews.setOnClickPendingIntent(R.id.llTitle, pIntent);
                            remoteViews.setOnClickPendingIntent(idLL.get(index), pIntent);
                        }

                        Intent updateIntent2 = new Intent(context, MainActivity.class);
                        PendingIntent pIntent2 = PendingIntent.getActivity(context, widgetID, updateIntent2, 0);
                        remoteViews.setOnClickPendingIntent(R.id.llImage, pIntent2);
                    }
                    else
                        remoteViews.setTextViewText(idTV.get(index), p.name);
                }

                if (valType == 1) {
                    if (p.state != Const.STATE_STOP) {
                        if (valShowFileName == 1) {
                            remoteViews.setTextViewText(idTV.get(index), p.GetTitle());
                            remoteViews.setTextViewText(idTV_.get(index), p.GetShortDescript());
                            //remoteViews.setTextViewTextSize(idTV.get(index), TypedValue.COMPLEX_UNIT_SP, 16);
                        }
                        else
                        {
                            remoteViews.setTextViewText(idTV.get(index), p.name);
                            remoteViews.setTextViewText(idTV_.get(index), p.GetFileName());
                        }

                        for (int k = 0; k < vecIDs.size(); k++) {
                            if (k == index)
                                remoteViews.setViewVisibility((int) idTV_.get(k), View.VISIBLE);
                        }
                    }
                    else {
                        remoteViews.setTextViewText(idTV.get(index), p.name);
                        //remoteViews.setTextViewTextSize(idTV.get(index), TypedValue.COMPLEX_UNIT_SP, 16);
                    }
                }

                index ++;
            }

            appWidgetManager.updateAppWidget(widgetID, remoteViews);
        }
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().indexOf(Const.FORCE_WIDGET_UPDATE) == 0) {
            updateWidget(context, AppWidgetManager.getInstance(context));
        }
    }
}

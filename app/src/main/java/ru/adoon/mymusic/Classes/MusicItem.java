package ru.adoon.mymusic.Classes;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.Services.MediaService;

public class MusicItem {

    public int id;
    public String name;
    public String url;
    public int type;
    public int image;
    public int state;
    public boolean bSubFolder;
    public boolean bRandom;
    public ArrayList<FileItem> files;
    public ArrayList<Integer> played_pos;
    public int radioid = 0;
    public RadioTrackInfo rti;

    public MusicItem(Context ctx, int id_, String _name, String _url, boolean subFolder, boolean random, int _type, int _radioid, int _image) {
        id = id_;
        name = _name;
        url = _url;
        bSubFolder = subFolder;
        bRandom = random;

        type = _type;
        image = _image;
        radioid = _radioid;
        state = 0;

        rti = new RadioTrackInfo();

        if (type == Const.TYPE_RADIO && radioid > 0 && ctx != null) {
            rti.GetRadioInfoFromFile(radioid, ctx);
        }
    }

    public Bitmap GetBitmap() {
        if (type == Const.TYPE_RADIO) {
            if (rti.m_btm != null)
                return rti.m_btm;
            else
                return rti.m_btmIconRadio;
        }
        else {
            if (files != null && MediaService.musicBox.play_sub_item_pos >= 0 && MediaService.musicBox.play_sub_item_pos < files.size())
                return files.get(MediaService.musicBox.play_sub_item_pos).m_btm;
            else return null;
        }
    }

    public Bitmap GetIconBitmap() {
        if (type == Const.TYPE_RADIO) {
            return rti.m_btmIconRadio;
        }
        else {
            return null;
        }
    }

    public String GetTitle() {
        if ((state == Const.STATE_PLAY || state == Const.STATE_PAUSE))
        {
            if (type == Const.TYPE_RADIO) {
                if (rti.m_strTitleExecutor != "") return rti.m_strTitleExecutor;
            }
            if (type == Const.TYPE_FOLDER || type == Const.TYPE_PLAYLIST) {
                if (files.get(MediaService.musicBox.play_sub_item_pos).m_strTitleExecutor != "") return files.get(MediaService.musicBox.play_sub_item_pos).m_strTitleExecutor;
            }

        }
        return name;
    }

    public String GetDescript() {
        if (state == Const.STATE_PLAY || state == Const.STATE_PAUSE) {
            if (type == Const.TYPE_RADIO) {
                if (rti.m_strTitleExecutor != "") return rti.m_strTitleTrack;
            }
            if (type == Const.TYPE_FOLDER || type == Const.TYPE_PLAYLIST) {
                if (files.get(MediaService.musicBox.play_sub_item_pos).m_strTitleExecutor != "")
                    return files.get(MediaService.musicBox.play_sub_item_pos).m_strTitleTrack;
                else
                    return files.get(MediaService.musicBox.play_sub_item_pos).m_strShortName;
            }
        }
        if (type == Const.TYPE_RADIO && radioid > 0) return "радио (101.ru)";
        if (type == Const.TYPE_RADIO && radioid == 0) return "радио (" + url + ")";
        if (type == Const.TYPE_FOLDER) return "папка (" + url + ")";
        if (type == Const.TYPE_PLAYLIST) return "плейлист";
        return "";
    }

    public String GetShortDescript() {
        if (state == Const.STATE_PLAY || state == Const.STATE_PAUSE) {
            if (type == Const.TYPE_RADIO) {
                if (rti.m_strTitleExecutor != "") return rti.m_strTitleTrack;
            }
            if (type == Const.TYPE_FOLDER || type == Const.TYPE_PLAYLIST) {
                if (files.get(MediaService.musicBox.play_sub_item_pos).m_strTitleExecutor != "")
                    return files.get(MediaService.musicBox.play_sub_item_pos).m_strTitleTrack;
                else
                    return files.get(MediaService.musicBox.play_sub_item_pos).m_strShortName;
            }
        }
        if (type == Const.TYPE_RADIO && radioid > 0) return "101.ru";
        if (type == Const.TYPE_PLAYLIST) return "";
        return url;
    }

    public String GetFileName() {
       if (type == Const.TYPE_FOLDER || type == Const.TYPE_PLAYLIST) {
           return files.get(MediaService.musicBox.play_sub_item_pos).m_strShortName;
       }
       if (type == Const.TYPE_RADIO && radioid > 0) return "101.ru";
       if (type == Const.TYPE_PLAYLIST) return "";
       return url;
    }
}

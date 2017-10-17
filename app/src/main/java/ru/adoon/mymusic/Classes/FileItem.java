package ru.adoon.mymusic.Classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Лукшин on 02.08.2017.
 */

public class FileItem {
    public volatile String m_strName = "";
    public volatile String m_strShortName = "";
    public volatile String m_strTitleTrack = "";
    public volatile String m_strTitleExecutor = "";
    public volatile Bitmap m_btm = null;
    public volatile boolean m_bReadBtm = false;
    public volatile boolean m_bReadTitle = false;
    public volatile boolean m_bCheck = false; // для плейлиста
    public volatile boolean m_bDir = false; // для выбора файла

    public FileItem(String strFileName) {
        m_strName = strFileName;
        m_strShortName = "";
        m_strTitleTrack = "";
        m_strTitleExecutor = "";
        m_btm = null;
        m_bReadBtm = false;
        GetFileShortName();
    }

    public FileItem(String strFileName, String strFileShortName) {
        m_strName = strFileName;
        m_strShortName = strFileShortName;
        m_strTitleTrack = "";
        m_strTitleExecutor = "";
        m_btm = null;
        m_bReadBtm = false;
    }

    public FileItem(String strFileName, String strFileShortName, boolean bDir) {
        m_strName = strFileName;
        m_strShortName = strFileShortName;
        m_strTitleTrack = "";
        m_strTitleExecutor = "";
        m_btm = null;
        m_bReadBtm = false;
        m_bDir = bDir;
    }

    public void GetFileShortName() {
        m_strShortName = m_strName;
        int pos = m_strShortName.lastIndexOf("/");
        if (pos >= 0) m_strShortName = m_strShortName.substring(pos + 1);
        pos = m_strShortName.lastIndexOf(".");
        if (pos >= 0) m_strShortName = m_strShortName.substring(0, pos);
    }

    public void GetImageFromMp3File() {
        if (!m_bReadBtm) {
            m_btm = null;
            try {
                FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                mmr.setDataSource(m_strName);
                byte[] artByteArray = mmr.getEmbeddedPicture();
                if (artByteArray != null) {
                    m_btm = BitmapFactory.decodeByteArray(artByteArray, 0, artByteArray.length);
                    mmr.release();
                }
            } catch (Exception ex) {
            }
            m_bReadBtm = true;
        }
    }

    public boolean IsUTF8(String str) {
        if (str.indexOf("�") >= 0) return true;
        return false;
    }

    public boolean IsWin1252(String str) {
        String strWin1252 = "âàáäêéèëîìíïôòóöÀÁÂÄÈÉÊËÌÍÎÏÒÓÔÖ";
        for (int i = 0; i < str.length(); i++) {
            String ch = String.valueOf(str.charAt(i));
            if (strWin1252.indexOf(ch) >= 0) return true;
        }
        return false;
    }

    public void GetFileTitleDescript() {
        if (!m_bReadTitle) {
            try {
                FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                mmr.setDataSource(m_strName);
                String strExecutor = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
                String strTitle = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
                m_strTitleTrack = strTitle;
                m_strTitleExecutor = strExecutor;
                if (m_strTitleTrack == null) m_strTitleTrack = "";
                if (m_strTitleExecutor == null) m_strTitleExecutor = "";
                m_strTitleTrack = m_strTitleTrack.trim();
                m_strTitleExecutor = m_strTitleExecutor.trim();
                if (IsWin1252(m_strTitleTrack)) {
                    m_strTitleTrack = new String(m_strTitleTrack.getBytes("Windows-1252"), "Windows-1251");
                }

                if (IsWin1252(m_strTitleExecutor)) {
                    m_strTitleExecutor = new String(m_strTitleExecutor.getBytes("Windows-1252"/*"ISO-8859-1"*/), "Windows-1251");
                }

                mmr.release();
            } catch (Exception ex) {
            }
        }
        m_bReadTitle = true;
        m_btm = null;
        m_bReadBtm = false;
    }
}

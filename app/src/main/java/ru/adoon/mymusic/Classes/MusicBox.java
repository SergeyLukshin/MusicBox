package ru.adoon.mymusic.Classes;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.DB.DB;
import ru.adoon.mymusic.Services.MediaService;

/**
 * Created by Лукшин on 13.07.2017.
 */

public class MusicBox {
    public ArrayList<MusicItem> objects = null;
    Context m_ctx;
    public int select_pos = -1;
    public int play_item_id = -1;
    public int play_sub_item_pos = -1;
    public int play_index_pos = -1;

    public MusicBox(Context ctx) {
        m_ctx = ctx;
    }

    public int getCount() {
        return objects.size();
    }

    public void setContext(Context ctx) {
        m_ctx = ctx;
    }

    public void GetData() {
        DB db = new DB(m_ctx);
        db.open();

        if (objects == null) objects = db.getData();
        else {
            ArrayList<MusicItem> items = db.getData();
            objects.clear();
            for (MusicItem item : items) {
                objects.add(item);
            }
        }

        db.close();
    }

    public class IDComparator
            implements Comparator
    {
        public int compare(Object ri1, Object ri2) {
            FileItem s1 = ((FileItem)ri1);
            FileItem s2 = ((FileItem)ri2);
            return s1.m_strName.compareToIgnoreCase(s2.m_strName);
        }
    }


    public MusicItem getMusicItem(int position) {
        if (position < 0 || position >= objects.size())
            return null;
        return ((MusicItem) objects.get(position));
    }
    public MusicItem getMusicItemByID(int id) {
        for (MusicItem mi: objects)
            if (mi.id == id) return mi;
        return null;
    }
    public int getMusicItemPosByID(int id) {
        int i = 0;
        for (MusicItem mi: objects) {
            if (mi.id == id) return i;
            i ++;
        }
        return -1;
    }

    public boolean AllMusicStopped() {
        for (MusicItem item : objects) {
            if (item.state != Const.STATE_STOP) {
                return false;
            }
        }
        return true;
    }

    public boolean GetNextMusic(MusicItem p)
    {
        if (p != null && p.played_pos != null && p.played_pos.size() > 0) {

            if (play_index_pos < 0 || (play_index_pos + 1) >= p.played_pos.size())
                play_index_pos = 0;
            else
                play_index_pos ++;

            play_sub_item_pos = p.played_pos.get(play_index_pos);
        }
        else
            return false;

        return true;
    }

    public boolean GetPrevMusic(MusicItem p)
    {
        if (p != null && p.played_pos != null && p.played_pos.size() > 0) {

            if (play_index_pos < 0 || (play_index_pos - 1) < 0)
                play_index_pos = p.played_pos.size() - 1;
            else
                play_index_pos --;

            play_sub_item_pos = p.played_pos.get(play_index_pos);
        }
        else
            return false;

        return true;
    }

    public void PlayItem(int position) {
        MusicItem mi = getMusicItem(position);
        if (mi != null) {

            Intent intentService = new Intent(m_ctx, MediaService.class);
            if (mi.state == Const.STATE_STOP)
                intentService.setAction(Const.ACTION_PLAY + "#" + String.valueOf(mi.id));
            else
                intentService.setAction(Const.ACTION_STOP);
            m_ctx.startService(intentService);
        }
    }

    public void Next() {
        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_NEXT);
        m_ctx.startService(intentService);
    }

    public void Prev() {
        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_PREV);
        m_ctx.startService(intentService);
    }

    public void Pause() {
        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_PAUSE);
        m_ctx.startService(intentService);
    }

    public void Resume(int position) {
        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_RESUME + "#" + String.valueOf(position));
        //intentService.putExtra("pos", position);
        m_ctx.startService(intentService);
    }

    public void PlayFile(int id, int position) {

        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_NEXT + "#" + String.valueOf(id) + "#" + String.valueOf(position));
        m_ctx.startService(intentService);
    }

    public void delRec(int idMusic) {
        DB db = new DB(m_ctx);
        db.open();
        db.delRec(idMusic);
        db.close();

        MediaService.musicBox.GetData();

        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_STOP);
        m_ctx.startService(intentService);
    }

    public void updRec(MusicItem mi) {
        DB db = new DB(m_ctx);
        db.open();
        db.updRec(mi);
        db.close();

        MediaService.musicBox.GetData();

        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_STOP);
        m_ctx.startService(intentService);
    }

    public void addRec(MusicItem mi) {
        DB db = new DB(m_ctx);
        db.open();
        db.addRec(mi);
        db.close();

        MediaService.musicBox.GetData();

        Intent intentService = new Intent(m_ctx, MediaService.class);
        intentService.setAction(Const.ACTION_STOP);
        m_ctx.startService(intentService);
    }

    public void UpdateState(int state) {
        if (MediaService.musicBox.play_item_id > 0) {
            MusicItem mi = MediaService.musicBox.getMusicItemByID(MediaService.musicBox.play_item_id);
            if (mi != null) mi.state = state;
            //notifyDataSetChanged();
        }
    }

    public boolean StopAll(boolean bSendService) {
        boolean bRes = false;
        for (MusicItem item : objects) {
            if (item.state != Const.STATE_STOP) {
                item.state = Const.STATE_STOP;
                bRes = true;
            }
        }
        play_item_id = -1;
        play_sub_item_pos = -1;
        //notifyDataSetChanged();

        if (bSendService) {
            Intent intentService = new Intent(m_ctx, MediaService.class);
            intentService.setAction(Const.ACTION_STOP);
            m_ctx.startService(intentService);
        }

        return bRes;
    }

    // товар по позиции
    public String getFileExtension(String path) {
        int pos = path.lastIndexOf(".");
        if (pos != -1) return path.substring(pos + 1);
        else return "";
    }

    public ArrayList<FileItem> listFiles(File dir, boolean bWithSubFolders) {
        ArrayList<FileItem> files = new ArrayList<FileItem>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (bWithSubFolders)
                    files.addAll(listFiles(file, bWithSubFolders));
            }
            else {
                String strFile = file.getPath();
                String ext = getFileExtension(strFile);
                if (ext.compareToIgnoreCase("mp3") == 0 || ext.compareToIgnoreCase("wav") == 0 || ext.compareToIgnoreCase("ogg") == 0 || ext.compareToIgnoreCase("aac") == 0)
                    files.add(new FileItem(strFile));
            }
        }
        return files;
    }

    public boolean GetAudioFiles(MusicItem item) {
        if (item.type == Const.TYPE_FOLDER) {
            File f;
            try {
                f = new File(item.url);
                ArrayList<FileItem> files;
                files = listFiles(f, item.bSubFolder);

                if (item.files == null) item.files = files;
                else {
                    for (int i = item.files.size() - 1; i >= 0; i--) {
                        String strFile1 = item.files.get(i).m_strName;
                        boolean bFind = false;
                        for (int j = 0; j < files.size(); j++) {
                            String strFile2 = files.get(j).m_strName;
                            if (strFile1.equalsIgnoreCase(strFile2)) {
                                bFind = true;
                                files.remove(j);
                                break;
                            }
                        }
                        if (!bFind) {
                            // удаляем запись
                            item.files.remove(i);
                        }
                    }

                    for (int j = 0; j < files.size(); j++) {
                        String strFile2 = files.get(j).m_strName;
                        item.files.add(new FileItem(strFile2));
                    }
                }

                /*Comparator mc;
                mc = new IDComparator();
                Collections.sort(item.files, mc);

                item.played_pos = new ArrayList<Integer>();
                if (item.bRandom) {
                    ArrayList<Integer> tmp = new ArrayList<Integer>();
                    for (int i = 0; i < item.files.size(); i++) {
                        tmp.add(Integer.valueOf(i));
                    }
                    // создаем перемешанный список
                    while (tmp.size() > 0) {
                        int pos = new Random().nextInt(tmp.size());
                        item.played_pos.add(tmp.get(pos));
                        tmp.remove(pos);
                    }
                } else {
                    for (int i = 0; i < item.files.size(); i++) {
                        item.played_pos.add(Integer.valueOf(i));
                    }
                }*/
            } catch (Exception e) {
                ShowMessage("Ошибка при воспроизведении");
                return false;
            }
        }

        if (item.files == null) item.files = new ArrayList<FileItem>();

        try {
            Comparator mc;
            mc = new IDComparator();
            Collections.sort(item.files, mc);

            item.played_pos = new ArrayList<Integer>();
            if (item.bRandom) {
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                for (int i = 0; i < item.files.size(); i++) {
                    tmp.add(Integer.valueOf(i));
                }
                // создаем перемешанный список
                while (tmp.size() > 0) {
                    int pos = new Random().nextInt(tmp.size());
                    item.played_pos.add(tmp.get(pos));
                    tmp.remove(pos);
                }
            } else {
                for (int i = 0; i < item.files.size(); i++) {
                    item.played_pos.add(Integer.valueOf(i));
                }
            }
        } catch (Exception e) {
            ShowMessage("Ошибка при воспроизведении");
            return false;
        }

        if (item.files.size() == 0) {
            ShowMessage("Нет файлов для воспроизведения");
            return false;
        }
        return true;
    }

    public void ShowMessage(String strMessage)
    {
        Toast.makeText(m_ctx, strMessage, Toast.LENGTH_SHORT).show();
    }
}

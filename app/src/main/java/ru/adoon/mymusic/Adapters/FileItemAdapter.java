package ru.adoon.mymusic.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;

public class FileItemAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<FileItem> objects = null;
    //public ArrayList<Integer> selectedItems = null;

    public FileItemAdapter(Context context) {
        ctx = context;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void showOptionsMenu(final int position)
    {
        new AlertDialog.Builder(ctx)
                .setCancelable(true).setItems(R.array.menu_playlist,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        DeleteRecord(position);
                    }
                }
        ).show();
    }

    @Override
    public boolean onLongClick(View view) {

        String strTag = "";
        CheckBox cb = null;

        if (view.getId() == R.id.cbCheck) {
            cb = (CheckBox) view;
            strTag = cb.getTag().toString();
        }
        if (view.getId() == R.id.linearLayout1) {
            LinearLayout ll = (LinearLayout) view;
            strTag = ll.getTag().toString();
            cb = ((CheckBox)((View)(view.getParent())).findViewById(R.id.cbCheck));
            cb.setChecked(!cb.isChecked());
        }
        int pos = Integer.valueOf(strTag);
        showOptionsMenu(pos);
        return true;
    }

    @Override
    public void onClick(View view) {

        String strTag = "";
        CheckBox cb = null;

        if (view.getId() == R.id.cbCheck) {
            cb = (CheckBox) view;
            strTag = cb.getTag().toString();
        }
        if (view.getId() == R.id.linearLayout1) {
            LinearLayout ll = (LinearLayout) view;
            strTag = ll.getTag().toString();
            cb = ((CheckBox)((View)(view.getParent())).findViewById(R.id.cbCheck));
            cb.setChecked(!cb.isChecked());
        }
        int pos = Integer.valueOf(strTag);

        getFileItem(pos).m_bCheck = cb.isChecked();

        notifyDataSetChanged();
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_file_playlist, parent, false);
        }

        FileItem p = getFileItem(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        CheckedTextView ctv = ((CheckedTextView) view.findViewById(R.id.tvFileName));
        TextView tv = ((TextView) view.findViewById(R.id.tvFileURL));
        CheckBox cb = ((CheckBox) view.findViewById(R.id.cbCheck));

        ctv.setText(p.m_strShortName);
        tv.setText(p.m_strName);

        LinearLayout ll = ((LinearLayout) view.findViewById(R.id.linearLayout1));

        //if (selectedItems == null) selectedItems = new ArrayList<String>();

        //int pos = selectedItems.indexOf(String.valueOf(p.id));

        if (p.m_bCheck) cb.setChecked(true);
        else cb.setChecked(false);

        cb.setOnClickListener(this);
        ll.setOnClickListener(this);

        cb.setOnLongClickListener(this);
        ll.setOnLongClickListener(this);

        cb.setTag(position);
        ll.setTag(position);

        return view;
    }

    public int GetSelectedRecordsCount() {
        int iCnt = 0;
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).m_bCheck) iCnt ++;
        }
        return iCnt;
    }

    public void DeleteSelectedRecords() {
        for (int i = objects.size() - 1; i >= 0; i --) {
            if (objects.get(i).m_bCheck)
                objects.remove(i);
        }
        notifyDataSetChanged();
    }

    public void DeleteRecord(int pos) {
        objects.remove(pos);
        notifyDataSetChanged();
    }

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

    public int AddFilesInDir(String dirName) {

        File f;
        try {
            int iCnt = 0;
            f = new File(dirName);
            ArrayList<FileItem> files;
            files = listFiles(f, false);

            if (files != null) {
                for (int j = 0; j < files.size(); j++) {
                    boolean bFind = false;
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i).m_strName.compareToIgnoreCase(files.get(j).m_strName) == 0) {
                            bFind = true;
                            break;
                        }
                    }
                    if (!bFind) {
                        objects.add(new FileItem(files.get(j).m_strName));
                        iCnt ++;
                    }
                }
                notifyDataSetChanged();
                return iCnt;
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public int AddFile(String fileName) {
        for (int i = 0; i < objects.size(); i++){
            if (objects.get(i).m_strName.compareToIgnoreCase(fileName) == 0)
                return 0;
        }
        objects.add(new FileItem(fileName));
        notifyDataSetChanged();
        return 1;
    }

    // товар по позиции
    FileItem getFileItem(int position) {
        return ((FileItem) getItem(position));
    }
}

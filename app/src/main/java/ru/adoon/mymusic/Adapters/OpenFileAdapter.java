package ru.adoon.mymusic.Adapters;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.Dialogs.DialogOpenFile;
import ru.adoon.mymusic.R;

public class OpenFileAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<FileItem> objects = null;
    public boolean m_bShowCheck = true;
    public boolean m_bOnlyDir = false;
    private FilenameFilter filenameFilter;
    private HashSet<String> extensions = null;
    DialogOpenFile parent;
    static public String currentPath = Environment.getExternalStorageDirectory().getPath();

    public OpenFileAdapter(Context context, DialogFragment p) {
        ctx = context;
        parent = (DialogOpenFile)p;
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
                        //DeleteRecord(position);
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

    public void changeTitle() {
        String titleText = currentPath;
        parent.title.setText(titleText);
    }

    @Override
    public void onClick(View view) {

        String strTag = "";
        CheckBox cb = null;

        if (view.getId() == R.id.cbCheck) {
            cb = (CheckBox) view;
            strTag = cb.getTag().toString();
            int pos = Integer.valueOf(strTag);
            getFileItem(pos).m_bCheck = cb.isChecked();
            if (m_bOnlyDir) {
                for (int i = 0; i < objects.size(); i++) {
                    if (i != pos) getFileItem(i).m_bCheck = false;
                }
            }
            notifyDataSetChanged();
        }
        if (view.getId() == R.id.linearLayout1) {
            LinearLayout ll = (LinearLayout) view;
            strTag = ll.getTag().toString();
            int pos = Integer.valueOf(strTag);

            if (getFileItem(pos).m_bDir) {
                currentPath = getFileItem(pos).m_strName;
                changeTitle();
                GetFiles(currentPath);
            }
            else {
                getFileItem(pos).m_bCheck = !getFileItem(pos).m_bCheck;
                notifyDataSetChanged();
            }
        }
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_file_select, parent, false);
        }

        FileItem p = getFileItem(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        TextView ctv = ((TextView) view.findViewById(R.id.tvFileName));
        CheckBox cb = ((CheckBox) view.findViewById(R.id.cbCheck));
        ImageView iv = ((ImageView) view.findViewById(R.id.ivImage));

        ctv.setText(p.m_strShortName);

        LinearLayout ll = ((LinearLayout) view.findViewById(R.id.linearLayout1));

        //if (selectedItems == null) selectedItems = new ArrayList<String>();

        //int pos = selectedItems.indexOf(String.valueOf(p.id));

        if (p.m_bCheck) cb.setChecked(true);
        else cb.setChecked(false);

        if (p.m_bDir) iv.setVisibility(View.VISIBLE);
        else iv.setVisibility(View.GONE);

        if (m_bShowCheck) cb.setVisibility(View.VISIBLE);
        else cb.setVisibility(View.GONE);

        cb.setOnClickListener(this);
        ll.setOnClickListener(this);

        /*cb.setOnLongClickListener(this);
        ll.setOnLongClickListener(this);*/

        cb.setTag(position);
        ll.setTag(position);

        return view;
    }

    private String getFileExtension(String filename) {
        String ext = "";
        int i = filename .lastIndexOf('.');
        if(i != -1 && i < filename .length()) {
            ext = filename.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public void setFilterFileExtention(String... ext) {
        List<String> l = Arrays.asList(ext);
        extensions = new HashSet<String>();
        for (int i = 0; i < ext.length; i++) {
            extensions.add(ext[i]);
        }

        filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                if (tempFile.isFile())
                    return extensions.isEmpty() ||
                            extensions.contains(getFileExtension(tempFile.getName()).toLowerCase());
                return true;
            }
        };
    }

    public void GetFiles(String strPath) {

        File directory = new File(strPath);
        File[] list = directory.listFiles(filenameFilter);
        if(list == null)
            list = new File[]{};
        List<File> fileList = Arrays.asList(list);

        objects.clear();
        for (int i = 0; i < fileList.size(); i++)
        {
            if (!fileList.get(i).isDirectory() && !fileList.get(i).isFile()) continue;
            if (m_bOnlyDir && !fileList.get(i).isDirectory()) continue;
            objects.add(new FileItem(fileList.get(i).getPath(), fileList.get(i).getName(), fileList.get(i).isDirectory()));
        }

        Collections.sort(objects, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem file, FileItem file2) {
                if (file.m_bDir && !file2.m_bDir)
                    return -1;
                else if (!file.m_bDir && file2.m_bDir)
                    return 1;
                else
                    return file.m_strName.compareTo(file2.m_strName);
            }
        });

        notifyDataSetChanged();

        if (parent != null) {
            if (objects.size() == 0) {
                parent.tvEmpty.setVisibility(View.VISIBLE);
                parent.lvFiles.setVisibility(View.GONE);
            }
            else {
                parent.tvEmpty.setVisibility(View.GONE);
                parent.lvFiles.setVisibility(View.VISIBLE);
            }
        }
    }

    // товар по позиции
    FileItem getFileItem(int position) {
        return ((FileItem) getItem(position));
    }
}

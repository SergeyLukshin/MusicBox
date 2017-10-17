package ru.adoon.mymusic.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import android.widget.Toast;

import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.R;

public class ConfigAdapter extends BaseAdapter implements View.OnClickListener {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<MusicItem> objects = null;
    public ArrayList<String> selectedItems = null;

    public ConfigAdapter(Context context) {
        ctx = context;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSelectedItems(String strIDs) {
        selectedItems = new ArrayList<String>();
        String[] separated = strIDs.split(",");
        for (String str : separated) {
            if (!str.isEmpty()) selectedItems.add(str);
        }
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

        MusicItem p = getMusicItem(pos);
        if (cb.isChecked())
        {
            if (selectedItems.size() == 5) {
                Toast.makeText(ctx, "Можно выбрать не более 5 элементов", Toast.LENGTH_SHORT).show();
                cb.setChecked(false);
                return;
            }
            selectedItems.add(String.valueOf(p.id));
        }
        else {
            selectedItems.remove(String.valueOf(p.id));
        }

        notifyDataSetChanged();
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_config, parent, false);
        }

        MusicItem p = getMusicItem(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        CheckedTextView ctv = ((CheckedTextView) view.findViewById(R.id.tvNameConfig));
        TextView tv = ((TextView) view.findViewById(R.id.tvTypeConfig));
        CheckBox cb = ((CheckBox) view.findViewById(R.id.cbCheck));

        ctv.setText(p.name);
        switch (p.type) {
            case Const.TYPE_RADIO:
                tv.setText("радио (" + p.url + ")");
                break;
            case Const.TYPE_FOLDER:
                tv.setText("папка (" + p.url + ")");
                break;
            case Const.TYPE_PLAYLIST:
                tv.setText("плейлист");
                break;
        }

        LinearLayout ll = ((LinearLayout) view.findViewById(R.id.linearLayout1));

        if (selectedItems == null) selectedItems = new ArrayList<String>();

        int pos = selectedItems.indexOf(String.valueOf(p.id));

        if (pos >= 0) {
            ctv.setText(String.valueOf(pos + 1) + ". " + p.name);
            cb.setChecked(true);
        }
        else cb.setChecked(false);

        cb.setOnClickListener(this);
        ll.setOnClickListener(this);

        cb.setTag(position);
        ll.setTag(position);

        return view;
    }

    // товар по позиции
    MusicItem getMusicItem(int position) {
        return ((MusicItem) getItem(position));
    }
}

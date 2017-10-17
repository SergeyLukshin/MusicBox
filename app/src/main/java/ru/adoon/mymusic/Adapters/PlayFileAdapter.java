package ru.adoon.mymusic.Adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.R;

/**
 * Created by Лукшин on 29.06.2017.
 */

public class PlayFileAdapter extends BaseAdapter implements View.OnClickListener {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<Pair<String, String> > objects = null;
    public int id;

    //HashSet<String> selectedItems = null;

    public PlayFileAdapter(Context context) {
        ctx = context;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onClick(View view) {
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

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_file, parent, false);
        }

        String strFileName = objects.get(position).first;
        String strDescript = objects.get(position).second;

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        TextView tv = ((TextView) view.findViewById(R.id.tvName));
        TextView tv2 = ((TextView) view.findViewById(R.id.tvDescript));
        ImageView iv = ((ImageView) view.findViewById(R.id.ivImage));

        if (position != MediaService.musicBox.play_sub_item_pos || id != MediaService.musicBox.play_item_id) iv.setImageResource(R.drawable.clear);
        else iv.setImageResource(R.drawable.file_play);

        tv.setText(strFileName);
        tv2.setText(strDescript);
        if (strDescript.equalsIgnoreCase("")) tv2.setVisibility(View.GONE);
        else  tv2.setVisibility(View.VISIBLE);

        //tv.setOnClickListener(this);
        tv.setTag(position);

        return view;
    }
}

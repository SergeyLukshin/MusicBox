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

import ru.adoon.mymusic.R;
import ru.adoon.mymusic.Classes.RadioItem;

/**
 * Created by Лукшин on 29.06.2017.
 */

public class RadioItemAdapter extends BaseAdapter implements View.OnClickListener/*, Filterable*/ {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<RadioItem> objects = null;
    //ArrayList<RadioItem> filtered_objects = null;
    //private ItemFilter mFilter = new ItemFilter();
    //HashSet<String> selectedItems = null;

    /*public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<RadioItem> list = objects;

            int count = list.size();
            final ArrayList<RadioItem> nlist = new ArrayList<RadioItem>(count);

            RadioItem filterableObject ;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.m_strName.toLowerCase().contains(filterString)) {
                    nlist.add(filterableObject);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered_objects = (ArrayList<RadioItem>) results.values;
            notifyDataSetChanged();
        }

    }*/

    public RadioItemAdapter(Context context) {
        ctx = context;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        objects.get(pos).m_bCheck = cb.isChecked();
        notifyDataSetChanged();
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
            view = lInflater.inflate(R.layout.list_item_config, parent, false);
        }

        RadioItem p = getRadioItem(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        CheckedTextView ctv = ((CheckedTextView) view.findViewById(R.id.tvNameConfig));
        TextView tv = ((TextView) view.findViewById(R.id.tvTypeConfig));
        CheckBox cb = ((CheckBox) view.findViewById(R.id.cbCheck));
        LinearLayout ll = ((LinearLayout) view.findViewById(R.id.linearLayout1));

        ctv.setText(p.m_strName);
        tv.setText(p.m_strURL);
        cb.setChecked(p.m_bCheck);

        cb.setOnClickListener(this);
        ll.setOnClickListener(this);

        cb.setTag(position);
        ll.setTag(position);

        return view;
    }

    // товар по позиции
    RadioItem getRadioItem(int position) {
        return ((RadioItem) getItem(position));
    }
}

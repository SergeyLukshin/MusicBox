package ru.adoon.mymusic.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import ru.adoon.mymusic.Classes.EqualizerBandItem;
import ru.adoon.mymusic.R;
import ru.adoon.mymusic.Services.MediaService;

/**
 * Created by Лукшин on 29.06.2017.
 */

public class EqualizerAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<EqualizerBandItem> objects = null;
    //public ArrayList<TextView> tv_arr = new ArrayList<TextView>();
    public int min;
    public int max;
    SeekBar bar;
    boolean bInitData = false;
    public int m_iUseEq;

    public EqualizerAdapter(Context context) {
        ctx = context;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    @Override
    public boolean isEnabled(int position){
        if(m_iUseEq == 0)
            return false;
        else
            return true;
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
            view = lInflater.inflate(R.layout.list_item_equalizer, parent, false);
        }

        bInitData = true;

        EqualizerBandItem val = getBandItem(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        TextView tvBand = ((TextView) view.findViewById(R.id.tvBandName));
        if (Math.abs(val.m_iName) > 1000)
            tvBand.setText(String.valueOf(val.m_iName/1000) + " Mhz");
        else
            tvBand.setText(String.valueOf(val.m_iName) + " hz");

        TextView tvMin = ((TextView) view.findViewById(R.id.tvMin));
        tvMin.setText(String.valueOf((float)min / 100) + " dB");

        TextView tvMax = ((TextView) view.findViewById(R.id.tvMax));
        tvMax.setText(String.valueOf((float)max / 100) + " dB");

        TextView tvVal = ((TextView) view.findViewById(R.id.tvVal));
        tvVal.setText(String.valueOf((float)val.m_iVal / 100) + " dB");

        //if (position >= tv_arr.size())
        //    tv_arr.add(tvVal);

        bar = ((SeekBar) view.findViewById(R.id.sbVal));
        bar.setMax(max - min);
        bar.setProgress(val.m_iVal - min);
        bar.setTag(position);

        if(m_iUseEq == 0)
        {
            bar.setEnabled(false);
        }
        else
        {
            bar.setEnabled(true);
        }

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                if (!bInitData && m_iUseEq == 1) {
                    int val = progress + min;

                    String strTag = seekBar.getTag().toString();
                    int pos = Integer.valueOf(strTag);
                    //notifyDataSetChanged();
                    View view = (View)(seekBar.getParent());
                    TextView tvVal = ((TextView) view.findViewById(R.id.tvVal));
                    tvVal.setText(String.valueOf((float)val / 100) + " db");

                    MediaService.equalizer.setBandLevel((short) pos, (short) val);
                    val = MediaService.equalizer.getBandLevel((short) pos);
                    objects.get(pos).m_iVal = val;

                    // это необходимо делать, если эквалайзер меняется во время смены музыки
                    int name = MediaService.equalizer.getCenterFreq((short) pos)/1000;
                    for (int j = 0; j < MediaService.m_vEqBands.size(); j++) {
                        if (name == MediaService.m_vEqBands.get(j).m_iName) {
                            MediaService.m_vEqBands.get(j).m_iVal = val;
                            break;
                        }
                    }
                }
            }
        });

        bInitData = false;

        /*CheckBox cb = ((CheckBox) view.findViewById(R.id.cbCheck));
        LinearLayout ll = ((LinearLayout) view.findViewById(R.id.linearLayout1));

        ctv.setText(p.m_strName);
        tv.setText(p.m_strURL);
        cb.setChecked(p.m_bCheck);

        cb.setOnClickListener(this);
        ll.setOnClickListener(this);

        cb.setTag(position);
        ll.setTag(position);*/

        return view;
    }

    // товар по позиции
    EqualizerBandItem getBandItem(int position) {
        return ((EqualizerBandItem) getItem(position));
    }
}


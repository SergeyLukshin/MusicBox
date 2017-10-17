package ru.adoon.mymusic.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.adoon.mymusic.Activities.MainActivity;
import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.Dialogs.DialogPlayFile;
import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.Classes.MusicItem;
import ru.adoon.mymusic.R;

public class MusicItemAdapter extends BaseAdapter implements View.OnClickListener {
    Context ctx;
    LayoutInflater lInflater;
    //ArrayList<MusicItem> objects = null;
    //public MusicItemAdapterListener listener;
    //private GifAnimationDrawable img_load;

    public MusicItemAdapter(Context context) {
        ctx = context;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return MediaService.musicBox.objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return MediaService.musicBox.objects.get(position);
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
            view = lInflater.inflate(R.layout.list_item, parent, false);
        }

        MusicItem p = MediaService.musicBox.getMusicItem(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvName)).setText(p.name);
        if (!p.name.equalsIgnoreCase(p.GetTitle()))
            ((TextView) view.findViewById(R.id.tvType)).setText(p.GetTitle() + " (" + p.GetShortDescript() + ")");
        else
            ((TextView) view.findViewById(R.id.tvType)).setText(p.GetDescript());

        ((ImageView) view.findViewById(R.id.ivImageNext)).setOnClickListener(this);
        ((ImageView) view.findViewById(R.id.ivImagePrev)).setOnClickListener(this);
        ((ImageView) view.findViewById(R.id.ivImagePause)).setOnClickListener(this);

        ((ImageView) view.findViewById(R.id.ivImageNext)).setVisibility(View.GONE);
        ((ImageView) view.findViewById(R.id.ivImagePrev)).setVisibility(View.GONE);
        ((ImageView) view.findViewById(R.id.ivImagePause)).setVisibility(View.GONE);
        ImageView iv = ((ImageView) view.findViewById(R.id.ivImage));
        iv.clearAnimation();

        ImageView ivPause = ((ImageView) view.findViewById(R.id.ivImagePause));
        ImageView ivNext = ((ImageView) view.findViewById(R.id.ivImageNext));
        ImageView ivPrev = ((ImageView) view.findViewById(R.id.ivImagePrev));

        ImageView ivIcon = ((ImageView) view.findViewById(R.id.ivImageIcon));

        if (p.state != Const.STATE_STOP)
            ivIcon.setImageResource(R.drawable.playing);
        else {
            if (p.type == Const.TYPE_RADIO && p.radioid > 0) {
                Bitmap icon_btm = p.GetIconBitmap();
                if (icon_btm != null)
                    ivIcon.setImageBitmap(icon_btm);
                else
                    ivIcon.setImageResource(R.drawable.radio_);
            }

            if (p.type == Const.TYPE_RADIO && p.radioid == 0)
                ivIcon.setImageResource(R.drawable.radio_);

            if (p.type == Const.TYPE_FOLDER) {
                ivIcon.setImageResource(R.drawable.folder_);
            }

            if (p.type == Const.TYPE_PLAYLIST) {
                ivIcon.setImageResource(R.drawable.playlist_);
            }
        }

        ((TextView) view.findViewById(R.id.tvType)).setTextColor(ctx.getResources().getColor(R.color.colorGrey));

        switch (p.state) {
            case Const.STATE_PAUSE:
                iv.setImageResource(R.drawable.stop);
                if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
                    ivNext.setVisibility(View.VISIBLE);
                    ivPrev.setVisibility(View.VISIBLE);
                    //ivPause.setImageResource(R.drawable.pause_off);
                    //ivPause.setVisibility(View.VISIBLE);
                }
                ivPause.setImageResource(R.drawable.pause_off);
                ivPause.setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.tvType)).setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
                break;
            case Const.STATE_STOP:
                //((ImageView) view.findViewById(R.id.ivImageNext)).setVisibility(View.INVISIBLE);
                iv.setImageResource(R.drawable.play);
                break;
            case Const.STATE_PLAY:
                if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
                    ivNext.setVisibility(View.VISIBLE);
                    ivPrev.setVisibility(View.VISIBLE);
                    //ivPause.setVisibility(View.VISIBLE);
                    //((TextView) view.findViewById(R.id.tvType)).setTextColor(Color.argb(255, 0, 0, 255));
                }
                ivPause.setImageResource(R.drawable.pause);
                ivPause.setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.tvType)).setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
                //else
                    //((ImageView) view.findViewById(R.id.ivImageNext)).setVisibility(View.INVISIBLE);
                iv.setImageResource(R.drawable.stop);
                break;
            case Const.STATE_LOAD: {
                Animation anim = null;
                anim = AnimationUtils.loadAnimation(ctx, R.anim.myrotate);
                //((ImageView) view.findViewById(R.id.ivImageNext)).setVisibility(View.INVISIBLE);
                iv.setImageResource(R.drawable.load);
                iv.startAnimation(anim);
                /*try {
                    img_load = new GifAnimationDrawable(view.getResources().openRawResource(R.raw.load));
                    img_load.setOneShot(false);
                    ((ImageView) view.findViewById(R.id.ivImage)).setImageDrawable(img_load);
                } catch (IOException ioe) {

                }*/
                break;
            }
        }

        ((ImageView) view.findViewById(R.id.ivImage)).setOnClickListener(this);
        /*if (p.type == Const.TYPE_FOLDER || p.type == Const.TYPE_PLAYLIST) {
            ((TextView) view.findViewById(R.id.tvName)).setBackgroundResource(R.drawable.widget_dark_selector);
            ((TextView) view.findViewById(R.id.tvName)).setOnClickListener(this);
        }
        else
            ((TextView) view.findViewById(R.id.tvName)).setBackground(null);*/


        view.setTag(p.id);


        /*try{
            img_load = new GifAnimationDrawable(view.getResources().openRawResource(R.raw.load));
            img_load.setOneShot(false);
            ((ImageView) view.findViewById(R.id.ivImageLoad)).setImageDrawable(img_load);
        }catch(IOException ioe){

        }*/

        return view;
    }

    /*public interface MusicItemAdapterListener {
        public void OnStartMusic(MusicItem p);

        public void OnStopMusic(MusicItem p);

        public void OnPauseMusic(MusicItem p);

        public void ShowMessage(String strMessage);
    }*/

    /*public void StopAll(int excludeID)
    {
        for (MusicItem item : objects) {
            if (excludeID != excludeID)
        }
    }*/

    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.tvName: {
                View vParent = (View) v.getParent().getParent();
                int id = Integer.parseInt(vParent.getTag().toString());

                DialogPlayFile dlg = new DialogPlayFile();
                dlg.mi = MediaService.musicBox.getMusicItemByID(id);
                if (dlg.mi.type == Const.TYPE_FOLDER || dlg.mi.type == Const.TYPE_PLAYLIST)
                    dlg.show(((MainActivity)ctx).getFragmentManager(), "DialogFileSelect");
            }
            break;*/
            case R.id.ivImage: {
                View vParent = (View) v.getParent().getParent().getParent();
                int id = Integer.parseInt(vParent.getTag().toString());

                MusicItem mi = MediaService.musicBox.getMusicItemByID(id);

                if (mi != null) {
                    Intent intentService = new Intent(ctx, MediaService.class);
                    if (mi.state == Const.STATE_STOP)
                        intentService.setAction(Const.ACTION_PLAY + "#" + String.valueOf(mi.id));
                    else
                        intentService.setAction(Const.ACTION_STOP);
                    ctx.startService(intentService);
                }
            }
            break;
            case R.id.ivImageNext: {
                Intent intentService = new Intent(ctx, MediaService.class);
                intentService.setAction(Const.ACTION_NEXT);
                ctx.startService(intentService);
            }
            break;
            case R.id.ivImagePrev: {
                Intent intentService = new Intent(ctx, MediaService.class);
                intentService.setAction(Const.ACTION_PREV);
                ctx.startService(intentService);
            }
               break;
            case R.id.ivImagePause: {
                View vParent = (View) v.getParent().getParent().getParent();
                int id = Integer.parseInt(vParent.getTag().toString());
                MusicItem mi = MediaService.musicBox.getMusicItemByID(id);

                if (mi != null) {
                    Intent intentService = new Intent(ctx, MediaService.class);
                    if (mi.state == Const.STATE_PLAY)
                        intentService.setAction(Const.ACTION_PAUSE);
                    if (mi.state == Const.STATE_PAUSE)
                        intentService.setAction(Const.ACTION_RESUME);
                    ctx.startService(intentService);
                }
            }
            break;
        }
    }
}

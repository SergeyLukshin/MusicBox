package ru.adoon.mymusic.Tools;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import ru.adoon.mymusic.Consts.Const;
import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.Classes.MusicItem;

/**
 * Receives broadcasted intents. In particular, we are interested in the
 * android.media.AUDIO_BECOMING_NOISY and android.intent.action.MEDIA_BUTTON intents, which is
 * broadcast, for example, when the user disconnects the headphones. This class works because we are
 * declaring it in a &lt;receiver&gt; tag in AndroidManifest.xml.
 */
public class MusicIntentReceiver extends BroadcastReceiver {

    @Override public void onReceive( Context context, Intent intent ) {

        String action = intent.getAction();

        Intent mIntent = new Intent();


        if ( action.equals( android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY ) ) {

            //context.sendBroadcast( new Intent( MusicPlayerService.ACTION_PAUSE ) );

        } else if ( action.equals( Intent.ACTION_MEDIA_BUTTON ) ) {

            if ( intent.hasExtra( Intent.EXTRA_KEY_EVENT ) ) {

                KeyEvent keyEvent = ( KeyEvent ) intent.getExtras().get( Intent.EXTRA_KEY_EVENT );

                if ( keyEvent.getAction() != KeyEvent.ACTION_DOWN )
                    return;


                //mIntent.putExtra( GAEvent.Categories.LOCKSCREEN, true );

                switch ( keyEvent.getKeyCode() ) {

                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        {
                        Intent intentService = new Intent(context, MediaService.class);
                        MusicItem mi = MediaService.musicBox.getMusicItemByID(MediaService.musicBox.play_item_id);
                        if (mi != null) {
                            if (mi.state == Const.STATE_PLAY)
                                intentService.setAction(Const.ACTION_PAUSE);
                            if (mi.state == Const.STATE_PAUSE)
                                intentService.setAction(Const.ACTION_RESUME);
                            context.startService(intentService);
                        }
                    }
                    break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT: {

                        Intent intentService = new Intent(context, MediaService.class);
                        intentService.setAction(Const.ACTION_NEXT);
                        context.startService(intentService);
                    }
                    break;

                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {

                        Intent intentService = new Intent(context, MediaService.class);
                        intentService.setAction(Const.ACTION_PREV);
                        context.startService(intentService);
                    }
                    break;

                }

            }

        }

    }

}
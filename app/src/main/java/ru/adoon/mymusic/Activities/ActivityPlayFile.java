package ru.adoon.mymusic.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.adoon.mymusic.Classes.MusicBox;
import ru.adoon.mymusic.Dialogs.DialogPlayFile;
import ru.adoon.mymusic.Services.MediaService;
import ru.adoon.mymusic.R;

public class ActivityPlayFile extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invisible);

        Intent intent = getIntent();
        String strAction = intent.getAction();

        int id = -1;
        if (strAction != null) {
            id  = Integer.valueOf(strAction);
        }

        if (MediaService.musicBox == null)
        {
            MediaService.musicBox = new MusicBox(this);
            MediaService.musicBox.GetData();
            MediaService.GetBluetooth();
        }

        DialogPlayFile dlg = new DialogPlayFile();
        dlg.parent = this;
        dlg.mi = MediaService.musicBox.getMusicItemByID(id);
        if (dlg.mi != null)
            dlg.show(getFragmentManager(), "DialogFileSelect");
        else finish();
    }
}

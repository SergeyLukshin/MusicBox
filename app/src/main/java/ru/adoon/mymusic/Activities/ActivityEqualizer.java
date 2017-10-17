package ru.adoon.mymusic.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.adoon.mymusic.Dialogs.DialogEqualizer;
import ru.adoon.mymusic.R;

/**
 * Created by Лукшин on 30.08.2017.
 */

public class ActivityEqualizer extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invisible);

        DialogEqualizer dlg = new DialogEqualizer();
        dlg.parent = this;
        dlg.show(getFragmentManager(), "DialogEqualizer");
    }
}

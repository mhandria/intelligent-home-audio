package com.example.michaelhandria.ihaapplicationv030;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        LinearLayout mainContent = (LinearLayout)findViewById(R.id.homePage);
        MusicControl musicControl = new MusicControl();
        getFragmentManager().beginTransaction().add(mainContent.getId(), musicControl).commit();
    }

}

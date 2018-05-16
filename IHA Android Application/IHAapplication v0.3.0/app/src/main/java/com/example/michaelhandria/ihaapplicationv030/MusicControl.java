 package com.example.michaelhandria.ihaapplicationv030;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


 /**
 * A simple {@link Fragment} subclass.
 */
public class MusicControl extends Fragment {


    private boolean isPlaying = false;

    public MusicControl(){
        //empty constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View mView = inflater.inflate(R.layout.fragment_music_control, container, false);
        TextView previousSkip = (TextView)mView.findViewById(R.id.prevButton);
        TextView playPause = (TextView)mView.findViewById(R.id.playPause);
        TextView nextSkip = (TextView)mView.findViewById(R.id.nextButton);

        previousSkip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){goBackPlay(view);}
        });

        playPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){resumePause(view);}
        });
        nextSkip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){goForwardPlay(view);}
        });
        return mView;

    }

    public void goBackPlay(View view){
        Toast.makeText(getActivity(), "you clicked me", Toast.LENGTH_SHORT).show();
    }

    public void goForwardPlay(View view){

    }

    public void resumePause(View view){
        if(!isPlaying)
            ((TextView)view).setText(R.string.pause);
        else
            ((TextView)view).setText(R.string.play_arrow);
        isPlaying = !isPlaying;

    }

}

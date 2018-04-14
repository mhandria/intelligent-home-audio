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

    private Context parent;
    public MusicControl(){
        //empty constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View mView = inflater.inflate(R.layout.fragment_music_control, container, false);
        TextView previousSkip = (TextView)mView.findViewById(R.id.prevButton);
        previousSkip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){doSomething(view);}
        });
        return mView;

    }

    public void doSomething(View view){
        Toast.makeText(getActivity(), "you clicked me", Toast.LENGTH_SHORT);
    }
}

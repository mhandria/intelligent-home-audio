package handria.com.iha_application;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LandingPage extends AppCompatActivity {

    //only sign in or sign up.
    Intent userChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
    }

    public void signIn(View view) {
        //userChoice= new Intent(this, SignIn.class);
        userChoice= new Intent(this, MusicPage.class);
        startActivity(userChoice);
    }

    public void signUp(View view) {
        userChoice = new Intent(this, SignUp.class);
        startActivity(userChoice);
    }
}

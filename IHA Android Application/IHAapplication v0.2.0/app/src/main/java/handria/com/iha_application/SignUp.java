package handria.com.iha_application;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class SignUp extends AppCompatActivity {

    Intent back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void goBack(View view) {
        back = new Intent(this, LandingPage.class);
        startActivity(back);
    }
}

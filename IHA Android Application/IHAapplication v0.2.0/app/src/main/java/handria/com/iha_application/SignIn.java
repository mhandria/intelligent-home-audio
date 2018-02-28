package handria.com.iha_application;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class SignIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }

    public void signInLogin(View view) {
        EditText usernameInput = (EditText) findViewById(R.id.username_login);
        EditText passwordInput = (EditText) findViewById(R.id.password_login);
        boolean rightUser = (usernameInput.getText().toString()).equals("michaelhandria");
        boolean rightPass = (passwordInput.getText().toString()).equals("password");
        if(rightPass && rightUser){
            Intent homePage = new Intent(this, HomePage.class);
            saveToken("abcd...blablabla");
            homePage.putExtra("token", "abcd...blablabla");
            startActivity(homePage);
        }else{
            Toast.makeText(this,"Invalid Username/Password", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean saveToken(String token){
        try{
            FileOutputStream file = openFileOutput("token.src", MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(file);
            osw.write(token);
            osw.flush();
            osw.close();
            return true;
        }catch(Exception e) {
            Log.e("TOKEN", "failed to save token to internal memory");
            return false;
        }
    }
}

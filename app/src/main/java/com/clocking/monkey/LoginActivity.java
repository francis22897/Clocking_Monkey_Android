package com.clocking.monkey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.base.Strings;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText email, password;
    Button btnLogin, btnRegister;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init(){
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);


        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:

                if(Strings.isNullOrEmpty(email.getText().toString()) || Strings.isNullOrEmpty(password.getText().toString())){
                    Toast.makeText(this.getApplicationContext(), "No puedes dejar campos vacíos", Toast.LENGTH_LONG).show();
                }else{
                    if(FirebaseUtils.loginUser(email.getText().toString(), password.getText().toString())){

                        String user = FirebaseUtils.searchUser(email.getText().toString());

                        if(!Strings.isNullOrEmpty(user)){
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("user", user);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }

                break;
            case R.id.btn_register:
                break;
        }
    }


    public void showMessage(String message){
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}

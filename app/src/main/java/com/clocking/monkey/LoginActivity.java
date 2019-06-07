package com.clocking.monkey;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Strings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText email, password;
    Button btnLogin, btnRegister;
    SharedPreferences prefs;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    ProgressDialog dialog;

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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

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
                    dialog = ProgressDialog.show(this, "",
                            "Cargando... espere por favor", true);
                    firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                addCredentials();
                            }else{
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Fallo al realizar el logueo", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                break;
            case R.id.btn_register:
                break;
        }
    }

    public void addCredentials(){

        firebaseFirestore.collection("Users").whereEqualTo("email", email.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        String email = task.getResult().getDocuments().get(0).getData().get("email").toString();
                        String name = task.getResult().getDocuments().get(0).getData().get("name").toString();
                        String first_lastname = task.getResult().getDocuments().get(0).getData().get("first_lastname").toString();
                        String second_lastname = task.getResult().getDocuments().get(0).getData().get("second_lastname").toString();

                        User user = new User(email, name, first_lastname, second_lastname);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user", user.toJson());
                        editor.apply();

                        dialog.dismiss();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }else{
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error al buscar usuario", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}

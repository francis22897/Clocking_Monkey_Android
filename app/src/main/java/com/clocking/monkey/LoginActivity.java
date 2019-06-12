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

        //Inicializo firebase y firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                //Compruebo que los datos no sean vacíos
                if(Strings.isNullOrEmpty(email.getText().toString()) || Strings.isNullOrEmpty(password.getText().toString())){
                    Toast.makeText(this.getApplicationContext(), "No puedes dejar campos vacíos", Toast.LENGTH_LONG).show();
                }else{
                    //Muestro un dialog de carga mientras realizo las operaciones para el login
                    dialog = ProgressDialog.show(this, "",
                            "Cargando... espere por favor", true);

                    //Hago el login en firebase auth
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

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

                break;
        }
    }

    public void addCredentials(){

        //Recojo los datos del usuario que coincida con el email del usuario logueado
        firebaseFirestore.collection("Users").whereEqualTo("email", email.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //En caso de que haya más de un registro quiere decir que lo ha encontrado
                    if (task.getResult().getDocuments().size() > 0) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else{
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "No se han encontrado datos del usuario en la bd", Toast.LENGTH_LONG).show();
                    }
                }else{
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error al buscar usuario", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}

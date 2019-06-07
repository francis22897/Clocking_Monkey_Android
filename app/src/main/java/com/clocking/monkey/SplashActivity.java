package com.clocking.monkey;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SplashActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Creo la instancia de firestore
        firebaseFirestore = FirebaseFirestore.getInstance();

        getAllowedUsers();

        //FALTA COMPROBAR QUE SI YA HAY UN USUARIO LOGUEADO NO OBTENGA LOS USUARIOS
    }

    private void getAllowedUsers(){
        final SQLiteDB sqLiteDB = new SQLiteDB(this.getApplicationContext());

        //Obtengo todos los usuarios permitidos de la base de datos
        firebaseFirestore.collection("AllowedUsers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Añado cada registro (email y rol) a sqlite
                        sqLiteDB.insertDB(document.getData().values().toArray()[0].toString(), document.getData().values().toArray()[1].toString());
                    }

                    //Compruebo que hay un usuario que coincida con un email para comprobar que se ha realizado bien la inserción

                    //Log.d("PRUEBA", String.valueOf(sqLiteDB.searchUser("francisco.adan@escuelaestech.es")));

                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}

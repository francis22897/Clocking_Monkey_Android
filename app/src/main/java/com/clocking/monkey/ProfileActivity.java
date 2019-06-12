package com.clocking.monkey;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.L;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import javax.security.auth.login.LoginException;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences prefs;
    Button logoutBtn, saveBtn;
    EditText name, firstLastname, secondLastname;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();

    }

    private void init(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.profile_name);
        firstLastname = findViewById(R.id.profile_firstLastname);
        secondLastname = findViewById(R.id.profile_secondLastname);

        logoutBtn = findViewById(R.id.logout_btn);
        saveBtn = findViewById(R.id.saveChanges_btn);
        prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        if(! prefs.getString("user", "").equals("")){
            loadFields();
        }

        logoutBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
    }

    private void loadFields(){
        User user = User.fromJSON(prefs.getString("user", ""));
        name.setText(user.getName());
        firstLastname.setText(user.getFirst_lastname());
        secondLastname.setText(user.getSecond_lastname());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_fragments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.back){

            if(!prefs.getString("user", "").equals("")){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }else{
                Toast.makeText(getApplicationContext(), "Debes rellenar tus datos", Toast.LENGTH_LONG).show();
            }
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.saveChanges_btn:

                if (!Strings.isEmptyOrWhitespace(name.getText().toString()) && !Strings.isEmptyOrWhitespace(firstLastname.getText().toString()) && !Strings.isEmptyOrWhitespace(secondLastname.getText().toString())) {
                    if (prefs.getString("user", "").equals("")) {

                        User user = new User(firebaseAuth.getCurrentUser().getEmail(), name.getText().toString(), firstLastname.getText().toString(), secondLastname.getText().toString());
                        addUser(user);
                    } else {
                        User user = new User(firebaseAuth.getCurrentUser().getEmail(), name.getText().toString(), firstLastname.getText().toString(), secondLastname.getText().toString());
                        editUser(user);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "No puedes dehar campos vacíos", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.logout_btn:
                firebaseAuth.signOut();

                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void addUser(final User user){
        dialog = ProgressDialog.show(this, "",
                "Cargando... espere por favor", true);

        firebaseFirestore.collection("Users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.putString("user", user.toJson());
                editor.apply();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Se ha guardado el usuario", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "No se ha podido guardar el usuario", Toast.LENGTH_LONG).show();
                Log.i("PRUEBA", e.getMessage());
            }
        });
    }

    private void editUser(final User user){

        dialog = ProgressDialog.show(this, "",
                "Cargando... espere por favor", true);

        firebaseFirestore.collection("Users").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if (task.getResult().getDocuments().size() > 0) {
                        firebaseFirestore.collection("Users").document(task.getResult().getDocuments().get(0).getId()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.clear();
                                editor.putString("user", user.toJson());
                                editor.apply();
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Se han guardado los cambios", Toast.LENGTH_LONG).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "No se han podido guardar los cambios", Toast.LENGTH_LONG).show();
                                Log.i("PRUEBA", e.getMessage());
                            }
                        });
                    }
                }
            }
        });

        /*
        firebaseFirestore.collection("Users").document().set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
        */
    }
}

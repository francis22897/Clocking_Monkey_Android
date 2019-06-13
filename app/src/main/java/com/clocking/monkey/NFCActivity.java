package com.clocking.monkey;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NFCActivity extends AppCompatActivity {

    private Button btnClockinNfc;
    private NfcAdapter nfcAdapter;
    private Boolean type;
    private Date date;
    private String comment;
    private ProgressDialog dialog;

    private Tag tag;
    private Ndef ndef;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();

        //inicializo NFC
        initNFC();
    }

    private void init(){
        btnClockinNfc = findViewById(R.id.NFCActivity_btn_clockin);
        btnClockinNfc.setEnabled(false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //Creo un alert dialog para advertir al usuario que hasta que no encuentre el nfc no
        //se habilita el botón

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Debes detectar el NFC para poder habilitar el botón")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        // Create the AlertDialog object and return it
        builder.create();
        builder.show();

        btnClockinNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUser();
            }
        });
    }

    //Compruebo la asistencia anterior

    private void checkAssitance() {
        dialog = ProgressDialog.show(this, "",
                "Cargando... espere por favor", true);

        firebaseFirestore.collection("Assists").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).orderBy("date", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.getResult().getDocuments().size() > 0) {
                    Timestamp time = (Timestamp) task.getResult().getDocuments().get(0).getData().get("date");
                    date = new Date(time.getSeconds() * 1000);
                    Date now = new Date();
                    if(date.getDate() == now.getDate() && date.getMonth() == now.getMonth() && date.getYear() == now.getYear()){
                        //Si la asistencia anterior coincide con la misma fecha que hoy cambio el tipo de fichaje y lo realizo
                        type = !(Boolean) task.getResult().getDocuments().get(0).getData().get("type");
                        toggleButton();
                        dialog.dismiss();
                    }else{
                        //Si la asistencia es de otro día, compruebo si es de entrada o salida

                        if((Boolean) task.getResult().getDocuments().get(0).getData().get("type")){
                            try {
                                SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                Date newDate = formatter.parse(new SimpleDateFormat("dd/MM/yyyy").format(date) + " " + Utils.HOUR_MAX);

                                //Si es de entrada quiere decir que no se ha fichado para salir, por lo que genero una asistencia de salida con el horario máximo de salida
                                Assistance assistance = new Assistance(new Timestamp(newDate), firebaseAuth.getCurrentUser().getEmail(), true, false, "");
                                firebaseFirestore.collection("Assists").add(assistance).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()){
                                            type = true;
                                            toggleButton();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            } catch (ParseException e) {
                                Log.i("PRUEBA", e.getMessage());
                            }
                        }else{

                            //Si la asistencia anterior es de otro dia pero es de tipo salida quiere decir que fichó bien
                            //Establezco el tipo de fichaje como entrada
                            type = true;
                            toggleButton();
                            dialog.dismiss();
                        }
                    }
                }else{
                    //Si no hay ninguna asistencia establezco el tipo como entrada
                    type = true;
                    toggleButton();
                    dialog.dismiss();
                }
            }
        });
    }

    //Compruebo si el usuario está activo o no para pulsar el botón

    private void checkUser(){
        dialog = ProgressDialog.show(this, "",
                "Cargando... espere por favor", true);

        firebaseFirestore.collection("Users").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {

                        //Si el usuario está activo ficho

                        if((Boolean) task.getResult().getDocuments().get(0).getData().get("active")){
                            dialog.dismiss();
                            clockIn();
                        }else{
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Los usuarios inactivos no pueden fichar", Toast.LENGTH_LONG).show();

                        }
                    }else{
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "No puedes fichar", Toast.LENGTH_LONG).show();
                    }
                }else{
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No puedes fichar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Cambio el texto del botón en función de si es entrada o salida

    private void toggleButton(){
        if(type){
            btnClockinNfc.setText(getString(R.string.inBtn_text));
        }else{
            btnClockinNfc.setText(getString(R.string.outBtn_text));
        }
    }

    //Realizo el fichaje

    private void clockIn(){

        //Si no han pasado más de 10 minutos entre entrada y salida saco un dialogo para comentar el por qué

        if(!type){
            //Comparo la fecha de la salida con la fecha de ahora
            if(TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - date.getTime()) < Utils.MINUTES_MIN){

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
                View mView = getLayoutInflater().inflate(R.layout.comment_dialog, null);
                final EditText commentText = mView.findViewById(R.id.comment_text);
                Button commentBtn = mView.findViewById(R.id.saveComment_btn);

                mBuilder.setView(mView);
                final AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();

                commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!Strings.isEmptyOrWhitespace(commentText.getText().toString())){
                            if(commentText.getText().toString().length() > 50){
                                Toast.makeText(getApplication(), "No puedes superar el límite de 50 caracteres", Toast.LENGTH_LONG).show();
                            }else{
                                //Recojo el comentario
                                comment = commentText.getText().toString();
                                alertDialog.dismiss();
                                addAssist(); //añado la asistencia a la bd
                            }
                        }else{
                            Toast.makeText(getApplication(), "No puedes dejar el campo vacío", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }else{
                addAssist();
            }
        }else{
            addAssist();
        }

    }

    //Añado la asistencia a la base de datos

    private void addAssist(){
        final ProgressDialog dialog = ProgressDialog.show(this, "",
                "Cargando... espere por favor", true);
        Assistance assistance = new Assistance(new Timestamp(new Date()), firebaseAuth.getCurrentUser().getEmail(), false, type, comment);
        firebaseFirestore.collection("Assists").add(assistance).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()){
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Has fichado", Toast.LENGTH_LONG).show();
                    type = !type; //cambio el tipo (entrada/salida)
                    toggleButton(); //cambio el botón
                    btnClockinNfc.setEnabled(false); //desactivo el botón
                }else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error al fichar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initNFC(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null){
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_SHORT).show();
            finish();
        }
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
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        comment = "";

        checkAssitance();


        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        IntentFilter[] intentFilters = new IntentFilter[] {tagDetected,ndefDetected,techDetected};

        Intent intent = new Intent(this,getClass());

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);

        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        btnClockinNfc.setEnabled(false);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null){
            Toast.makeText(this, "YA PUEDES FICHAR!", Toast.LENGTH_SHORT).show();
            ndef = Ndef.get(tag);
            readNFC();
        }
    }

    private void readNFC(){
        if (ndef != null){
            try {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                String message = new String(ndefMessage.getRecords()[0].getPayload());

                if (message.equals(Utils.NFC_KEY)){
                    btnClockinNfc.setEnabled(true);
                }
                
                ndef.close();
            } catch (IOException | FormatException e){
                e.printStackTrace();
            }
        }
    }



}

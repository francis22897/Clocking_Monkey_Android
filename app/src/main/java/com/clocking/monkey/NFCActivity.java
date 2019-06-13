package com.clocking.monkey;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class NFCActivity extends AppCompatActivity {

    Button btnClockinNfc;
    NfcAdapter nfcAdapter;
    Boolean type;

    Tag tag;
    Ndef ndef;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    
    String clave = "M9Spr0aclI";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        checkAssitance();

        initNFC();
    }

    private void checkAssitance() {
        firebaseFirestore.collection("Assists").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).orderBy("date", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().getDocuments().size() > 0) {
                    Timestamp time = (Timestamp) task.getResult().getDocuments().get(0).getData().get("date");
                    Date date = new Date(time.getSeconds() * 1000);
                    Date now = new Date();
                    if(date.getDate() == now.getDate() && date.getMonth() == now.getMonth() && date.getYear() == now.getYear()){
                        Log.i("PRUEBA", "MISMA FECHA");
                        type = (Boolean) task.getResult().getDocuments().get(0).getData().get("type");
                    }else{
                        if((Boolean) task.getResult().getDocuments().get(0).getData().get("type")){
                            try {

                                SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                Date p = formatter.parse(new SimpleDateFormat("dd/MM/yyyy").format(date) + " 22:00");

                                Assistance assistance = new Assistance(new Timestamp(p), firebaseAuth.getCurrentUser().getEmail(), true, false);
                                firebaseFirestore.collection("Assists").add(assistance).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(NFCActivity.this, "Asistencia Registrada!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } catch (ParseException e) {
                                Log.i("PRUEBA", e.getMessage());
                            }


                        }
                    }
                }
            }
        });
    }


    private void init(){
        btnClockinNfc = findViewById(R.id.NFCActivity_btn_clockin);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        /*
        btnClockinNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NFCActivity.this, "fichaje exito", Toast.LENGTH_SHORT).show();

                Assistance assistance;
                assistance = new Assistance(false, new Timestamp(new Date()),null ,"prueba");
                firebaseFirestore.collection("Assists").add(assistance).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(NFCActivity.this, "Asistencia Registrada!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        */
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

        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null){
            Toast.makeText(this, "NFC detectado!", Toast.LENGTH_SHORT).show();
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

                if (message.equals(clave)){
                    btnClockinNfc.setEnabled(true);
                }
                
                ndef.close();
            } catch (IOException | FormatException e){
                e.printStackTrace();
            }
        }
    }



}

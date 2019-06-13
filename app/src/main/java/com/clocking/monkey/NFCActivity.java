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

    private Tag tag;
    private Ndef ndef;

    AssistsBDUtils assistsBDUtils;


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

        assistsBDUtils = new AssistsBDUtils(this, this.getLayoutInflater().inflate(R.layout.activity_nfc, null), btnClockinNfc);


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
                assistsBDUtils.checkUser();
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

        assistsBDUtils.resetComment();
        assistsBDUtils.checkAssitance();

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

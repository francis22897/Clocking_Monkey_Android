package com.clocking.monkey;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AssistsBDUtils {
    private Button btn;
    private Context context;
    private View view;

    private Boolean type;
    private Date date;
    private String comment;
    private ProgressDialog dialog;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public AssistsBDUtils(Context context, View view, Button button){
        this.context = context;
        this.view = view;
        this.btn = button;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }


    //Compruebo la asistencia anterior

    public void checkAssitance() {
        dialog = ProgressDialog.show(context, "",
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
                    date = new Date();
                    type = true;
                    toggleButton();
                    dialog.dismiss();
                }

                dialog.dismiss();
            }
        });
    }

    //Cambio el texto del botón en función de si es entrada o salida

    public void toggleButton(){
        if(type){
            btn.setText(context.getString(R.string.inBtn_text));
        }else{
            btn.setText(context.getString(R.string.outBtn_text));
        }
    }

    //Compruebo si el usuario está activo o no para pulsar el botón

    public void checkUser(){
        dialog = ProgressDialog.show(context, "",
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
                            Toast.makeText(context, "Los usuarios inactivos no pueden fichar", Toast.LENGTH_LONG).show();

                        }
                    }else{
                        dialog.dismiss();
                        Toast.makeText(context, "No puedes fichar", Toast.LENGTH_LONG).show();
                    }
                }else{
                    dialog.dismiss();
                    Toast.makeText(context, "No puedes fichar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    //Realizo el fichaje

    public void clockIn(){

        //Si no han pasado más de 10 minutos entre entrada y salida saco un dialogo para comentar el por qué

        if(!type){
            //Comparo la fecha de la salida con la fecha de ahora
            if(TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - date.getTime()) < Utils.MINUTES_MIN){

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View mView = inflater.inflate(R.layout.comment_dialog, null);
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
                                Toast.makeText(context, "No puedes superar el límite de 50 caracteres", Toast.LENGTH_LONG).show();
                            }else{
                                //Recojo el comentario
                                comment = commentText.getText().toString();
                                alertDialog.dismiss();
                                addAssist(); //añado la asistencia a la bd
                            }
                        }else{
                            Toast.makeText(context, "No puedes dejar el campo vacío", Toast.LENGTH_LONG).show();
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

    public void addAssist(){
        final ProgressDialog dialog = ProgressDialog.show(context, "",
                "Cargando... espere por favor", true);
        Assistance assistance = new Assistance(new Timestamp(new Date()), firebaseAuth.getCurrentUser().getEmail(), false, type, comment);
        firebaseFirestore.collection("Assists").add(assistance).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()){
                    dialog.dismiss();
                    Toast.makeText(context, "Has fichado", Toast.LENGTH_LONG).show();
                    type = !type; //cambio el tipo (entrada/salida)
                    toggleButton(); //cambio el botón
                    btn.setEnabled(false); //desactivo el botón
                }else {
                    dialog.dismiss();
                    Toast.makeText(context, "Error al fichar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void resetComment(){
        comment = "";
    }

}

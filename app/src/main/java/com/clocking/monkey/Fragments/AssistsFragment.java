package com.clocking.monkey.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.clocking.monkey.Assistance;
import com.clocking.monkey.Assists;
import com.clocking.monkey.MainActivity;
import com.clocking.monkey.R;
import com.clocking.monkey.RVAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class AssistsFragment extends Fragment {

    private RecyclerView rview;
    RVAdapter adapter;
    Assists assists;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;


    public AssistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_assists, container,false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        rview = root.findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rview.setLayoutManager(layoutManager);
        rview.setHasFixedSize(true);

        assists = new Assists();
        adapter = new RVAdapter(getActivity(), assists.getAssists());
        rview.setAdapter(adapter);

        return root;
    }

    public void getAllAssistance(){
    final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "Cargando... espere por favor", true);

        firebaseFirestore.collection("Assists").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if (task.getResult().getDocuments().size() > 0) {
                        assists.getAssists().clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            assists.addAssistance(new Assistance((Timestamp) document.getData().get("date"), document.getData().get("email").toString(), (Boolean) document.getData().get("fail"), (Boolean) document.getData().get("type"), document.getData().get("comment").toString()));
                        }
                        adapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(getContext(), "No hay ninguna asistencia registrada", Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            getAllAssistance();
        }
    }
}

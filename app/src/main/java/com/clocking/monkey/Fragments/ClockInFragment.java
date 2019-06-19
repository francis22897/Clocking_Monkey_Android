package com.clocking.monkey.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.clocking.monkey.NFCActivity;
import com.clocking.monkey.EbeaconActivity;
import com.clocking.monkey.QrActivity;
import com.clocking.monkey.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClockInFragment extends Fragment {

    Button btn_nfc;

    public ClockInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_clock_in, container,false);

        Button btn_nfc = (Button) root.findViewById(R.id.btn_option_NFC);
        Button btn_beacon = (Button) root.findViewById(R.id.btn_option_ebeacon);

        btn_nfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NFCActivity.class);
                startActivity(intent);
            }
        });

        Button btn2 = (Button) root.findViewById(R.id.btn_option_QR);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QrActivity.class);
                startActivity(intent);
            }
        });

        btn_beacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EbeaconActivity.class);
                startActivity(intent);
            }
        });
        return root;

    }

}
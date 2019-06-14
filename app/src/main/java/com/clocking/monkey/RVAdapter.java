package com.clocking.monkey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.AssistanceViewHolder> {


    public class AssistanceViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView typeText;
        ImageView showCommentBtn;

        AssistanceViewHolder(View itemView){
            super(itemView);
            dateText = itemView.findViewById(R.id.assistance_date_text);
            typeText = itemView.findViewById(R.id.assistance_type_text);
            showCommentBtn = itemView.findViewById(R.id.showComment_btn);
        }
    }

    List<Assistance> assists;
    Context context;

    public RVAdapter(Context context, List<Assistance> assists){
        this.context = context;
        this.assists = assists;
    }

    @NonNull
    @Override
    public AssistanceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_assistance, viewGroup, false);

        final AssistanceViewHolder avh = new AssistanceViewHolder(v);
        return avh;
    }

    @Override
    public void onBindViewHolder(@NonNull AssistanceViewHolder assistanceViewHolder, final int i) {

        if(assists.get(i).getFail()){
            assistanceViewHolder.dateText.setTextColor(ContextCompat.getColor(context, R.color.colorAssistanceFail));
            assistanceViewHolder.typeText.setTextColor(ContextCompat.getColor(context, R.color.colorAssistanceFail));
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        assistanceViewHolder.dateText.setText(formatter.format(new Date(assists.get(i).getDate().getSeconds() * 1000)));

        if(assists.get(i).getType()){
            assistanceViewHolder.typeText.setText(R.string.inBtn_text);
        }else{
            assistanceViewHolder.typeText.setText(R.string.outBtn_text);
        }

        if(! assists.get(i).getComment().equals("")){
            assistanceViewHolder.showCommentBtn.setImageResource(R.drawable.ic_show_comment);
            assistanceViewHolder.showCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Comentario")
                            .setMessage(assists.get(i).getComment())
                            .setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                    builder.create();
                    builder.show();

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return assists.size();
    }
}

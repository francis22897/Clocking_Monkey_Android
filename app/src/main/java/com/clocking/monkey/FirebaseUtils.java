package com.clocking.monkey;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseUtils {

    static protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    static protected FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    static private boolean canLogin = false;
    static private String searchUserResult = null;
    static private LoginActivity loginActivity = new LoginActivity();

    static protected boolean loginUser(String email, String password){

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(loginActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    canLogin = true;
                }else{
                    loginActivity.showMessage("Inicio de sesión fallido");
                }
            }
        });
        return canLogin;
    }

    static protected String searchUser(String email){
        firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        String email = task.getResult().getDocuments().get(0).getData().get("email").toString();
                        String name = task.getResult().getDocuments().get(0).getData().get("name").toString();
                        String first_lastname = task.getResult().getDocuments().get(0).getData().get("first_lastname").toString();
                        String second_lastname = task.getResult().getDocuments().get(0).getData().get("second_lastname").toString();

                        User user = new User(email, name, first_lastname, second_lastname);
                        searchUserResult = user.toJson();
                    }
                }
            }
        });

        return searchUserResult;
    }
}

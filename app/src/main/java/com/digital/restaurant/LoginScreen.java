package com.digital.restaurant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginScreen extends AppCompatActivity implements View.OnClickListener {

    public static final String userPersistenceKey = "userPersistenceKey";
    private String email, password;
    private ImageView showHideBtn;
    private CheckBox rememberMeCb;
    private ProgressDialog pdloading;
    private EditText email_txt, passwordtxt;
    private boolean isPasswordShown = false;
    private FirebaseAuth mAuth;
    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        email_txt = findViewById(R.id.input_name);
        passwordtxt = findViewById(R.id.input_password);
        Button btn_signin = findViewById(R.id.loginBtn);
        showHideBtn = findViewById(R.id.showHideBtn);
        rememberMeCb = findViewById(R.id.rememberMeCb);
        showHideBtn.setOnClickListener(this);


        btn_signin.setOnClickListener(this);


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.loginBtn) {
            if (isNetworkAvailable()) {
                if (TextUtils.isEmpty(email_txt.getText())) {
                    email_txt.setError("Enter Number!");
                } else if (email_txt.getText().toString().contains(" ")) {
                    email_txt.setError("No Spaces Allowed");
                } else if (TextUtils.isEmpty(passwordtxt.getText())) {
                    passwordtxt.setError("Enter password");
                } else if (!TextUtils.isEmpty(email_txt.getText())) {
                    email = email_txt.getText().toString();
                    password = passwordtxt.getText().toString();
                    attemptLogin();
                }
            } else {
                //No Network Connection
                Toast.makeText(this, "Pas de connexion réseau", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.showHideBtn) {
            if (isPasswordShown) {
                passwordtxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordtxt.setSelection(passwordtxt.getText().length());
                showHideBtn.setImageResource(R.drawable.ic_show);
            } else {
                passwordtxt.setInputType(InputType.TYPE_CLASS_TEXT);
                passwordtxt.setSelection(passwordtxt.getText().length());
                showHideBtn.setImageResource(R.drawable.ic_hide);
            }
            isPasswordShown = !isPasswordShown;
        }
    }

    private void attemptLogin() {
        pdloading = new ProgressDialog(getBaseContext());
      //  Verifying User...
        pdloading = ProgressDialog.show(this, "", "Vérification de l'utilisateur...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pdloading.dismiss();
                        if (task.isSuccessful()) {
                            if (rememberMeCb.isChecked())
                                PreferenceManager.getDefaultSharedPreferences(LoginScreen.this).edit()
                                        .putString(userPersistenceKey, email).apply();
                            pdloading.dismiss();

                            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                            finish();

                        } else {
                           // Invalid email id or password
                            Toast.makeText(getBaseContext(), "Identifiant email ou mot de passe invalide", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

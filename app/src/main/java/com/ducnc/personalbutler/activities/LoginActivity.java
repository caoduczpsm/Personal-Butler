package com.ducnc.personalbutler.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.ducnc.personalbutler.R;
import com.ducnc.personalbutler.ultilities.Constants;
import com.ducnc.personalbutler.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    TextView txtSignIn, txtSignUp, txtRememberPass, txtForgetPass;
    View viewSignIn, viewSignUp;
    ConstraintLayout layoutLogin, layoutRegis, layoutMain, layoutTopLogin, layoutBottomLogin, layoutTopRegis;
    EditText inputPhone, inputPass, inputPhoneRes, inputPassRes, inputRePassRes, inputName;
    CheckBox cbRememberPass;
    Button btnLogin, btnRegis;
    ProgressBar progressBar;

    FirebaseFirestore database;
    SwipeListener swipeListener;
    PreferenceManager preferenceManager;

    String phoneRes, passRes, name, phoneLogin, passLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setListener();

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void init() {

        database = FirebaseFirestore.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());

        cbRememberPass = findViewById(R.id.cbRememberPass);

        viewSignIn = findViewById(R.id.viewSignIn);
        viewSignUp = findViewById(R.id.viewSignUp);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegis = findViewById(R.id.btnRegis);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutRegis = findViewById(R.id.layoutRegis);
        layoutMain = findViewById(R.id.layoutMain);
        layoutTopLogin = findViewById(R.id.layoutLoginTop);
        layoutBottomLogin = findViewById(R.id.layoutBottomLogin);
        layoutTopRegis = findViewById(R.id.layoutTopRegis);

        txtSignIn = findViewById(R.id.txtSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);
        txtForgetPass = findViewById(R.id.txtForgetPass);
        txtRememberPass = findViewById(R.id.txtRememberPass);
        progressBar = findViewById(R.id.progressBar);

        inputPhone = findViewById(R.id.inputPhoneNumber);
        inputPass = findViewById(R.id.inputPass);
        inputPhoneRes = findViewById(R.id.inputPhoneNumberRes);
        inputPassRes = findViewById(R.id.inputPassRes);
        inputRePassRes = findViewById(R.id.inputRePassRes);
        inputName = findViewById(R.id.inputName);

        phoneRes = inputPhoneRes.getText().toString();
        passRes= inputPassRes.getText().toString();
        name = inputName.getText().toString();
        phoneLogin = inputPhone.getText().toString();
        passLogin = inputPass.getText().toString();

        swipeListener = new SwipeListener(layoutMain);
        swipeListener = new SwipeListener(layoutLogin);
        swipeListener = new SwipeListener(layoutRegis);
        swipeListener = new SwipeListener(layoutTopLogin);
        swipeListener = new SwipeListener(layoutBottomLogin);
        swipeListener = new SwipeListener(layoutTopRegis);
    }

    private void setListener() {

        txtSignIn.setOnClickListener(view -> {
            layoutLogin.setVisibility(View.VISIBLE);
            layoutRegis.setVisibility(View.GONE);
            viewSignIn.setVisibility(View.VISIBLE);
            viewSignUp.setVisibility(View.GONE);
        });

        txtSignUp.setOnClickListener(view -> {
            layoutLogin.setVisibility(View.GONE);
            layoutRegis.setVisibility(View.VISIBLE);
            viewSignIn.setVisibility(View.GONE);
            viewSignUp.setVisibility(View.VISIBLE);
        });

        btnRegis.setOnClickListener(view -> {
            if (phoneRes.equals("") || passRes.equals("")
                    || inputRePassRes.getText().toString().equals("") || name.equals("")) {
                showToast("Vui lòng nhập đầy đủ thông tin!");
            } else if (!passRes.equals(inputRePassRes.getText().toString())){
                showToast("Nhập lại mật khẩu sai!");
            }else {
                regisAccount();
            }
        });

        btnLogin.setOnClickListener(view -> {
            if (inputPhone.getText().toString().equals("") || inputPass.getText().toString().equals("")){
                showToast("Vui lòng nhập đầy đủ thông tin!");
            } else {
                login();
            }
        });

    }

    private void login() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_PHONE, inputPhone.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, inputPass.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                })
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }

    private void regisAccount() {
        loading(true);
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_PHONE, inputPhoneRes.getText().toString());
        user.put(Constants.KEY_PASSWORD, inputPassRes.getText().toString());
        user.put(Constants.KEY_NAME, inputName.getText().toString());
        database.collection(Constants.KEY_COLLECTION_USER)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    showToast("Đăng ký thành công");
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, name);
                    preferenceManager.putString(Constants.KEY_PHONE, phoneRes);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            btnRegis.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else{
            btnRegis.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private class SwipeListener implements View.OnTouchListener {

        GestureDetector gestureDetector;

        SwipeListener(View view) {

            int threshold = 100;
            int velocity_threshold = 100;

            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {

                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e1.getY();
                    try {
                        if (Math.abs(xDiff) > Math.abs(yDiff)) {
                            if (Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold) {
                                if (xDiff > 0) {
                                    layoutLogin.setVisibility(View.GONE);
                                    layoutRegis.setVisibility(View.VISIBLE);
                                    viewSignIn.setVisibility(View.GONE);
                                    viewSignUp.setVisibility(View.VISIBLE);
                                } else {
                                    layoutLogin.setVisibility(View.VISIBLE);
                                    layoutRegis.setVisibility(View.GONE);
                                    viewSignIn.setVisibility(View.VISIBLE);
                                    viewSignUp.setVisibility(View.GONE);
                                }
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };

            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }
    }

}
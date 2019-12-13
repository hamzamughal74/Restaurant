package com.digital.restaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class CategoryScreen extends AppCompatActivity {
    private Spinner spinner;
    private static final String[] paths = {"item 1", "item 2", "item 3"};
    private Button btnnext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_screen);
        btnnext = (Button) findViewById(R.id.btnnext);

        Spinner dropdown = findViewById(R.id.categoryop);
        String[] items = new String[]{"Enviroment", "Plate", "Others"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent signup = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(signup);


            }
        });
    }

}


package com.example.administrator.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.binder_annotation.BindView;
import com.example.binder_api.InjectHelper;


public class MainActivity extends AppCompatActivity {

    @BindView(resid = R.id.txtView)
    TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectHelper.inject(this);
        txtView.setText("我自己封装的butterKnife");

    }
}

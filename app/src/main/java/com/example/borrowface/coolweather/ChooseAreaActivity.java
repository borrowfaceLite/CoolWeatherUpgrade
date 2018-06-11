package com.example.borrowface.coolweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ChooseAreaActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
    }

    public void getWeatherId(String weatherId,String countyName){
        Intent intent=new Intent();
        intent.putExtra("weatherId_return",weatherId);
        intent.putExtra("countyName_return",countyName);
        setResult(RESULT_OK,intent);
        finish();
    }
}

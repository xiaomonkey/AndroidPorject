package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button bt_fix;
    private  Button cat_cry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_fix = (Button) findViewById(R.id.bt_fix);
        cat_cry = (Button) findViewById(R.id.tv_cat_cry);
        bt_fix.setOnClickListener( this);
        cat_cry.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_fix:
                InjectUtil util = new InjectUtil(this);
                util.fix();
                break;
            case R.id.tv_cat_cry:
                Toast.makeText(this,"猫的叫声是"+new Cat().getCry(),Toast.LENGTH_SHORT).show();
                break;
        }


    }
}

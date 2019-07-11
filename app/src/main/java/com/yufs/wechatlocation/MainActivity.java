package com.yufs.wechatlocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends BaseActivity {
    private static final int LOCATION_CODE = 1;
    private static final int REQUEST_SELECT_ADDRESS_CODE=2;
    TextView tv_address;
    Button btn_select_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent=new Intent(MainActivity.this,LocationSelectActivity.class);
                    startActivityForResult(intent,REQUEST_SELECT_ADDRESS_CODE);

                } else {
                    //6.0以上检测没有申请成功权限，可以封装自己的dialog提示用户重新申请
               }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_SELECT_ADDRESS_CODE&&resultCode==2){
            double latitude=data.getDoubleExtra("latitude",0.0);
            double longitude=data.getDoubleExtra("longitude",0.0);
            String address=data.getStringExtra("address");
            tv_address.setText("详细地址："+address+"\n经度："+longitude+"\n纬度："+latitude);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setListener() {
        btn_select_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        LOCATION_CODE);

            }
        });
    }

    private void initView() {
        tv_address= (TextView) findViewById(R.id.tv_address);
        btn_select_address= (Button) findViewById(R.id.btn_select_address);
    }
}

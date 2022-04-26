package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;

import com.umeng.commonsdk.UMConfigure;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //UMConfigure.init(this,"6263de1a30a4f67780b312f7","Umeng",UMConfigure.DEVICE_TYPE_PHONE,"");
        Button loginBtn=findViewById(R.id.BT_login_test),registerBtn=findViewById(R.id.BT_register_test);
        loginBtn.setOnClickListener(click->{
            Intent intent=new Intent(this,Login.class);
            startActivity(intent);
        });
        registerBtn.setOnClickListener(click->{
            Intent intent=new Intent(this,RegisterActivity.class);
            startActivity(intent);
        });
    }
}
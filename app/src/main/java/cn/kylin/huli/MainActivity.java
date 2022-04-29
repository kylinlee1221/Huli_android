package cn.kylin.huli;

import static java.lang.Thread.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.umeng.commonsdk.UMConfigure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.kylin.huli.view.BulletinView;
import cn.kylin.huli.view.HomeAdapter;

public class MainActivity extends AppCompatActivity {
    BulletinView bulletinView;
    HomeAdapter adapter;
    List<String> infoList=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //UMConfigure.init(this,"6263de1a30a4f67780b312f7","Umeng",UMConfigure.DEVICE_TYPE_PHONE,"");
        ViewFlipper viewFlipper=findViewById(R.id.VF_NotifyBar2);
        Button loginBtn=findViewById(R.id.BT_login_test),registerBtn=findViewById(R.id.BT_register_test),checkInBtn=findViewById(R.id.BT_checkin_test),logoutBtn=findViewById(R.id.BT_logout_test);
        Button checkOutBtn=findViewById(R.id.BT_checkout_test);
        loginBtn.setOnClickListener(click->{
            Intent intent=new Intent(this,Login.class);
            startActivity(intent);
        });
        registerBtn.setOnClickListener(click->{
            Intent intent=new Intent(this,RegisterActivity.class);
            startActivity(intent);
        });
        GetAnnouncementTask getAnnouncementTask=new GetAnnouncementTask();
        getAnnouncementTask.execute("abc");
        String result="";
        try {
            result=getAnnouncementTask.get();
            Log.e("res in main",result);
            /*if(infoList!=null){
                adapter=new HomeAdapter(infoList);
                bulletinView.setAdapter(adapter);
                bulletinView.setOnItemClickListener(new BulletinView.OnItemClickListener() {
                    @Override
                    public void onItemClickListener(Object itemData, int pointer, View view) {
                        if(itemData instanceof String){
                            Toast.makeText(MainActivity.this, (String) itemData, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }*/
            viewFlipper.setInAnimation(getApplicationContext(), R.anim.anim_marquee_in);//设置滚动进入动画
            viewFlipper.setOutAnimation(getApplicationContext(), R.anim.anim_marquee_out);//设置滚动退出动画
            viewFlipper.setFlipInterval(3000);//设置滚动间隔
            for (int i = 0; i < infoList.size(); i++) {
                viewFlipper.addView(getTextView(infoList.get(i)));//添加子布局
            }
            if (viewFlipper.getChildCount() > 1) {
                viewFlipper.startFlipping();//开始滚动，如果只有一个子view，则只有进入动画不会有退出动画
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AsyncTask.Status status=getAnnouncementTask.getStatus();
        //if (status.)
        //sleep(5000);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        String user_fullname=sp.getString("fullname","empty");
        if(!user_fullname.equals("empty")){
            Log.e("fullname",user_fullname);
        }
        checkInBtn.setOnClickListener(click->{
            String user_fullname1=sp.getString("fullname","empty").trim();
            Log.e("full in checkin",user_fullname1);
            if(user_fullname1.equals("empty")){
                Toast.makeText(this,"You Should log in first",Toast.LENGTH_LONG).show();
            }else{
                StartCheckInTask startCheckInTask=new StartCheckInTask();
                startCheckInTask.execute(sp.getString("userid","empty"));
            }
        });
        checkOutBtn.setOnClickListener(click->{
            String user_fullname1=sp.getString("fullname","empty").trim();
            Log.e("full in out",user_fullname1);
            if(user_fullname1.equals("empty")){
                Toast.makeText(this,"You Should log in first",Toast.LENGTH_LONG).show();
            }else{
                StartCheckOutTask startCheckOutTask=new StartCheckOutTask();
                startCheckOutTask.execute(sp.getString("userid","empty"));
            }
        });
        logoutBtn.setOnClickListener(click->{
            sp.edit()
                    .remove("fullname")
                    .remove("userid")
                    .remove("telephone")
                    .remove("role")
                    .remove("checked").apply();
        });
    }

    public class StartCheckInTask extends AsyncTask<String,Void,String> {

        private String mCheckInUrl = "https://huli.kylin1221.com/apis/checkIn.php?userid={0}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String user = params[0];
            Log.e("user", user);
            String url = MessageFormat.format(mCheckInUrl, user);
            Log.e("url", url);
            StringBuffer buffer = new StringBuffer();
            try {
                URL url1 = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(5000);
                try {
                    conn.connect();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    return "timeout";
                }
                int rCode = conn.getResponseCode();
                if (rCode == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                    char[] charArr = new char[1024 * 8];
                    int len = 0;
                    while ((len = reader.read(charArr)) != -1) {
                        // 字符数组转字符串
                        String str = new String(charArr, 0, len);
                        // 在结尾追加字符串
                        buffer.append(str);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            Log.e("result", buffer.toString());

            return buffer.toString();
        }
    }

    public class StartCheckOutTask extends AsyncTask<String,Void,String> {

        private String mCheckInUrl = "https://huli.kylin1221.com/apis/checkOut.php?userid={0}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String user = params[0];
            Log.e("user", user);
            String url = MessageFormat.format(mCheckInUrl, user);
            Log.e("url", url);
            StringBuffer buffer = new StringBuffer();
            try {
                URL url1 = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(5000);
                try {
                    conn.connect();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    return "timeout";
                }
                int rCode = conn.getResponseCode();
                if (rCode == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                    char[] charArr = new char[1024 * 8];
                    int len = 0;
                    while ((len = reader.read(charArr)) != -1) {
                        // 字符数组转字符串
                        String str = new String(charArr, 0, len);
                        // 在结尾追加字符串
                        buffer.append(str);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            Log.e("result", buffer.toString());

            return buffer.toString();
        }
    }

    public class GetAnnouncementTask extends AsyncTask<String,Void,String>{

        private String url="https://huli.kylin1221.com/apis/getAnnouncement.php";

        @Override
        protected String doInBackground(String... strings) {
            StringBuffer buffer=new StringBuffer();
            try{
                URL url1=new URL(url);
                HttpURLConnection conn=(HttpURLConnection) url1.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(5000);
                try{
                    conn.connect();
                }catch (SocketTimeoutException e){
                    e.printStackTrace();
                    return "timeout";
                }
                int rCode=conn.getResponseCode();
                if(rCode==200){
                    InputStreamReader reader=new InputStreamReader(conn.getInputStream());
                    char[] charArr = new char[1024 * 8];
                    int len = 0;
                    while ((len = reader.read(charArr)) != -1) {
                        // 字符数组转字符串
                        String str = new String(charArr, 0, len);
                        // 在结尾追加字符串
                        buffer.append(str);
                    }
                }
                insertIntoData(buffer.toString());
            }catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());
            return buffer.toString();
        }
    }

    public void insertIntoData(String result){
        try {
            //JSONObject jsonObject=new JSONObject(result);
            JSONArray jsonArray=new JSONArray(result);
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                infoList.add(jsonObject.getString("info"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private TextView getTextView(String keyword) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#666666"));
        textView.setMaxLines(1);
        textView.setText(keyword);
        return textView;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //我们自己的方法
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        if(!sp.getBoolean("checked",true)){
            sp.edit()
                    .remove("fullname")
                    .remove("userid")
                    .remove("telephone")
                    .remove("role")
                    .remove("checked").apply();
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        if(!sp.getBoolean("checked",true)){
            sp.edit()
                    .remove("fullname")
                    .remove("userid")
                    .remove("telephone")
                    .remove("role")
                    .remove("checked").apply();
        }
        super.onDestroy();
    }
}
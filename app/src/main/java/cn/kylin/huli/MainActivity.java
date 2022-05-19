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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.commonsdk.debug.I;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.kylin.huli.view.BulletinView;
import cn.kylin.huli.view.HomeAdapter;

public class MainActivity extends AppCompatActivity {
    BulletinView bulletinView;
    HomeAdapter adapter;
    List<String> infoList=new ArrayList<String>();
    private int day,month,year,hour,minute,second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //UMConfigure.init(this,"6263de1a30a4f67780b312f7","Umeng",UMConfigure.DEVICE_TYPE_PHONE,"");
        ViewFlipper viewFlipper=findViewById(R.id.VF_NotifyBar2);
        RadioButton myInfoBtn=findViewById(R.id.RB_myInfo_Main);
        ImageButton closeAnnouncement=findViewById(R.id.IB_closeAnnouncement_Main),orderMarketBtn=findViewById(R.id.IB_orderMarket_Main),myOrderBtn=findViewById(R.id.IB_myOrder_Main);
        LinearLayout announceLayout=findViewById(R.id.LL_Announce_Main);
        closeAnnouncement.setOnClickListener(click->{
            announceLayout.setVisibility(View.GONE);
        });
        getDateTime();
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
        //AsyncTask.Status status=getAnnouncementTask.getStatus();
        //if (status.)
        //sleep(5000);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        String user_fullname=sp.getString("fullname","empty");
        Long userid=sp.getLong("id",-1);
        Log.e("userid",String.valueOf(userid));
        if(!user_fullname.equals("empty")){
            Log.e("fullname",user_fullname);
        }
        orderMarketBtn.setOnClickListener(click->{
            String user_fullname1=sp.getString("fullname","empty").trim();
            Log.e("full in out",user_fullname1);
            if(user_fullname1.equals("empty")){
                Toast.makeText(this,"You Should log in first",Toast.LENGTH_LONG).show();
            }else{
                Intent intent=new Intent(this,OrderMarketActivity.class);
                startActivity(intent);
            }
        });
        myOrderBtn.setOnClickListener(click->{
            String user_fullname1=sp.getString("fullname","empty").trim();
            Log.e("full in out",user_fullname1);
            if(user_fullname1.equals("empty")){
                Toast.makeText(this,"You Should log in first",Toast.LENGTH_LONG).show();
            }else{
                Intent intent=new Intent(this,MyOrderActivity.class);
                startActivity(intent);
            }
        });
        myInfoBtn.setOnClickListener(click->{
            Intent intent=new Intent(this,MyInfoActivity.class);
            startActivity(intent);
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
                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date1=dateFormat.parse(jsonObject.getString("endtime"));
                String nowDate=String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day)+" "+String.valueOf(hour)+":"+String.valueOf(minute);
                Log.e("nowDate",nowDate);
                Date date2=dateFormat.parse(nowDate);
                if(date2.getTime()<date1.getTime()){
                    infoList.add(jsonObject.getString("info"));
                    Log.e("time1",date1.toString());
                    Log.e("time2",date2.toString());
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void getDateTime(){
        Calendar calendar = Calendar.getInstance();//取得当前时间的年月日 时分秒


        year = calendar.get(Calendar.YEAR);


        month = calendar.get(Calendar.MONTH)+1;


        day = calendar.get(Calendar.DAY_OF_MONTH);


        hour = calendar.get(Calendar.HOUR_OF_DAY);


        minute = calendar.get(Calendar.MINUTE);


        second = calendar.get(Calendar.SECOND);

    }

    private TextView getTextView(String keyword) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#666666"));
        textView.setMaxLines(1);
        textView.setText(keyword);
        textView.setGravity(Gravity.CENTER_VERTICAL);
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
                    .remove("id")
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
                    .remove("id")
                    .remove("checked").apply();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }
}
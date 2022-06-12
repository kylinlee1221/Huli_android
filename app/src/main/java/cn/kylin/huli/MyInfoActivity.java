package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

public class MyInfoActivity extends AppCompatActivity {
    private TextView infoTV;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        Button myOrderBtn=findViewById(R.id.BT_myOrder_MyInfo),addOrderBtn=findViewById(R.id.BT_addOrder_MyInfo),marketBtn=findViewById(R.id.BT_orderMarket_MyInfo);
        Button orderManageBtn=findViewById(R.id.BT_orderManage_MyInfo),nowOrderBtn=findViewById(R.id.BT_nowOrder_MyInfo),announceManageBtn=findViewById(R.id.BT_announceManage_MyInfo),logoutBtn=findViewById(R.id.BT_logout_MyInfo);
        ImageView infoImage=findViewById(R.id.IV_head_MyInfo);
        infoTV=findViewById(R.id.TV_userInfo_MyInfo);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        String user_fullname=sp.getString("fullname","empty");
        Long userid=sp.getLong("id",-1);
        Log.e("userid",String.valueOf(userid));
        if(user_fullname.equals("empty")){
            Toast.makeText(this,getResources().getString(R.string.no_login_hint),Toast.LENGTH_LONG).show();
            Intent intent=new Intent(MyInfoActivity.this,Login.class);
            startActivity(intent);
            finish();
        }else{
            GetUserInfoTask getUserInfoTask=new GetUserInfoTask();
            getUserInfoTask.execute(String.valueOf(userid));
            logoutBtn.setOnClickListener(click->{
                    sp.edit()
                            .remove("fullname")
                            .remove("userid")
                            .remove("telephone")
                            .remove("role")
                            .remove("id")
                            .remove("checked").apply();
                    Toast.makeText(this,getResources().getString(R.string.user_logout_hint),Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(this,Login.class);
                    startActivity(intent);
                    finish();
            });
            infoImage.setOnClickListener(click->{
                Intent intent=new Intent(MyInfoActivity.this,EditUserActivity.class);
                startActivity(intent);
                finish();
            });
            infoTV.setOnClickListener(click->{
                GetUserInfoTask getUserInfoTask1=new GetUserInfoTask();
                getUserInfoTask1.execute(String.valueOf(userid));
            });
            myOrderBtn.setOnClickListener(click->{
                Intent intent=new Intent(this,MyOrderActivity.class);
                startActivity(intent);
                finish();
            });
            nowOrderBtn.setOnClickListener(click->{
                Intent intent=new Intent(this,NowOrderActivity.class);
                startActivity(intent);
                finish();
            });
            addOrderBtn.setOnClickListener(click->{
                String user_role=sp.getString("role","empty");
                if(user_role.equals("0")){
                    Toast.makeText(this,getResources().getString(R.string.user_permission_hint),Toast.LENGTH_LONG).show();
                }else{
                    Intent intent=new Intent(this,AddOrderActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            marketBtn.setOnClickListener(click->{
                Intent intent=new Intent(this,OrderMarketActivity.class);
                startActivity(intent);
                finish();
            });
            orderManageBtn.setOnClickListener(click->{
                String user_role=sp.getString("role","empty");
                if(user_role.equals("0")){
                    Toast.makeText(this,getResources().getString(R.string.user_permission_hint),Toast.LENGTH_LONG).show();
                }else{
                    Intent intent=new Intent(this,OrderManageActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            announceManageBtn.setOnClickListener(click->{
                String user_role=sp.getString("role","empty");
                if(user_role.equals("0")){
                    Toast.makeText(this,getResources().getString(R.string.user_permission_hint),Toast.LENGTH_LONG).show();
                }else{
                    Intent intent=new Intent(this,AnnounceManageActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
    public class GetUserInfoTask extends AsyncTask<String,Void,String>{

        private String mUrl="https://huli.kylin1221.com/apis/getUserInfo.php?userid={0}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String userid=params[0];
            String url = MessageFormat.format(mUrl,userid);
            StringBuffer buffer=new StringBuffer();
            Log.e("url", url);
            try{
                URL url1=new URL(url);
                HttpURLConnection conn=(HttpURLConnection) url1.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(3000);
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
                String result=buffer.toString();
                //Log.e("result",result);

                //addToInfo(buffer.toString());
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            //Log.e("result",buffer.toString());

            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String string){
            //Log.e("post execute",string);
            super.onPostExecute(string);
            if(string.trim().equals("timeout")||string.trim().equals("error")||string.trim().equals("[]")){
                Toast.makeText(MyInfoActivity.this,string,Toast.LENGTH_LONG).show();
                infoTV.setText(getResources().getString(R.string.user_info_get_error_hint));
                infoTV.setTextColor(Color.BLACK);
                infoTV.setTextSize(16);
                infoTV.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            }else{
                try {
                    JSONObject jsonObject=new JSONObject(string);
                    String telephone=jsonObject.getString("telephone");
                    String fullname=jsonObject.getString("fullname");
                    String money=jsonObject.getString("money");
                    String role=jsonObject.getString("role");
                    if(role.equals("0")){
                        infoTV.setText(getResources().getString(R.string.user_info_role_user)+"\n"+getResources().getString(R.string.user_info_fullname)+fullname+"\n"+getResources().getString(R.string.user_info_telephone)+telephone+"\n"+getResources().getString(R.string.user_info_money)+money);
                    }else{
                        infoTV.setText(getResources().getString(R.string.user_info_role_admin)+"\n"+getResources().getString(R.string.user_info_fullname)+fullname+"\n"+getResources().getString(R.string.user_info_telephone)+telephone+"\n"+getResources().getString(R.string.user_info_money)+money);
                    }
                    infoTV.setTextSize(17);
                    infoTV.setTextColor(Color.BLACK);
                    infoTV.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
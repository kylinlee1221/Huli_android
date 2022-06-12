package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditUserActivity extends AppCompatActivity {

    private EditText usernameET,realNameET,desET;
    private String originUsername="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        Button modifyBtn=findViewById(R.id.BT_modifyInfo_EditInfo),changePassBtn=findViewById(R.id.BT_changePass_EditInfo),changePhoneBtn=findViewById(R.id.BT_changePhone_EditInfo);
        usernameET=findViewById(R.id.ET_username_EditInfo);
        realNameET=findViewById(R.id.ET_realName_EditInfo);
        desET=findViewById(R.id.ET_des_EditInfo);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        String user_fullname=sp.getString("fullname","empty");
        Long userid=sp.getLong("id",-1);
        Log.e("userid",String.valueOf(userid));
        GetUserInfoTask getUserInfoTask=new GetUserInfoTask();
        getUserInfoTask.execute(String.valueOf(userid));
        /*if(originUsername.equals("")){
            Toast.makeText(EditUserActivity.this,"Get Info error",Toast.LENGTH_LONG).show();
        }else{*/
            modifyBtn.setOnClickListener(click->{
                String usernameST=usernameET.getText().toString(),realnameST=realNameET.getText().toString(),desST=desET.getText().toString();
                if(!usernameST.equals("")&&!realnameST.equals("")&&!desST.equals("")){
                    if(injectSqlChecker(usernameST)&&injectSqlChecker(realnameST)&&injectSqlChecker(desST)){
                        String params=usernameST+"/"+realnameST+"/"+desST+"/"+String.valueOf(userid);
                        GetModifyResultTask getModifyResultTask=new GetModifyResultTask();
                        getModifyResultTask.execute(params);
                    }
                }
            });
            changePassBtn.setOnClickListener(click->{
                Intent intent=new Intent(EditUserActivity.this,findPasswordActivity.class);
                startActivity(intent);
                finish();
            });
        //}
    }

    public class GetModifyResultTask extends AsyncTask<String,Void,String>{

        private String mUrl="https://huli.kylin1221.com/apis/changeUserInfo.php?username={0}&fullname={1}&des={2}&userid={3}";
        private String username;


        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            username=params[0];
            String fullname=params[1];
            String des=params[2];
            String userid=params[3];
            String url= MessageFormat.format(mUrl,username,fullname,des,userid);
            Log.e("url",url);
            StringBuffer buffer=new StringBuffer();
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
            }catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result.equals("timeout")||result.equals("error")||result.equals("[]")){
                Toast.makeText(EditUserActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                try{
                    JSONObject jsonObject=new JSONObject(result);
                    if(jsonObject.getString("status").equals("1")){
                        if(!username.equals(originUsername)){
                            Toast.makeText(EditUserActivity.this,"You changed username,please re-login",Toast.LENGTH_LONG).show();
                            SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
                            sp.edit()
                                    .remove("fullname")
                                    .remove("userid")
                                    .remove("telephone")
                                    .remove("role")
                                    .remove("id")
                                    .remove("checked").apply();
                            //Toast.makeText(this,getResources().getString(R.string.user_logout_hint),Toast.LENGTH_LONG).show();
                            Intent intent=new Intent(EditUserActivity.this,Login.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(EditUserActivity.this,jsonObject.getString("msgs"),Toast.LENGTH_LONG).show();
                            Intent intent=new Intent(EditUserActivity.this,MyInfoActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        Toast.makeText(EditUserActivity.this,jsonObject.getString("msgs"),Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public class GetUserInfoTask extends AsyncTask<String,Void,String> {

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
                Toast.makeText(EditUserActivity.this,string,Toast.LENGTH_LONG).show();
            }else{
                try{
                    JSONObject jsonObject=new JSONObject(string);
                    String username=jsonObject.getString("username");
                    String fullname=jsonObject.getString("fullname");
                    String description=jsonObject.getString("description");
                    originUsername=username;
                    usernameET.setText(username);
                    realNameET.setText(fullname);
                    desET.setText(description);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean injectSqlChecker(String input){
        String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
                + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
        Pattern sqlPattern=Pattern.compile(reg,Pattern.CASE_INSENSITIVE);
        if(input==null||input.length()<=0){
            return false;
        }
        Matcher matcher=sqlPattern.matcher(input);
        if(matcher.find()){
            return false;
        }
        return true;
    }
}
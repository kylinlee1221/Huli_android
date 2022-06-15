package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.kylin.huli.Utils.HttpRequestUtil;
import cn.kylin.huli.Utils.tool.HttpReqData;
import cn.kylin.huli.Utils.tool.HttpRespData;
import cn.kylin.huli.model.User;

public class Login extends AppCompatActivity {
    private ArrayList<User> userList=new ArrayList<User>();
    private CheckBox rememberCB;
    private int totalTriedTimes=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText usernameET=findViewById(R.id.ET_username_login),passwordET=findViewById(R.id.ET_password_login);
        Button loginBtn=findViewById(R.id.BT_login),registerBtn=findViewById(R.id.BT_register),changePassBtn=findViewById(R.id.BT_findPassword_login);
        rememberCB=findViewById(R.id.CB_remember_login);
        SharedPreferences sharedPreferences=getSharedPreferences("login",MODE_PRIVATE);
        changePassBtn.setOnClickListener(click->{
            Intent intent=new Intent(Login.this,findPasswordActivity.class);
            startActivity(intent);
            finish();
        });
        loginBtn.setOnClickListener(click->{
            String usernameST=usernameET.getText().toString();
            final String[] passwordST = { passwordET.getText().toString() };
            if(usernameST.equals("")|| passwordST[0].equals("")||!injectSqlChecker(usernameST)||!injectSqlChecker(passwordST[0])){
                Toast.makeText(this,"Empty error",Toast.LENGTH_LONG).show();
            }else{
                if(matchPhoneNumber(usernameST)){
                    try {
                        passwordST[0] =md5(passwordST[0]);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    String userInfo=usernameST+"/"+ passwordST[0];
                    String result="";
                    GetLoginResultByPhoneTask getLoginResultByPhoneTask=new GetLoginResultByPhoneTask();
                    getLoginResultByPhoneTask.execute(userInfo);
                }else{
                    try {
                        passwordST[0] =md5(passwordST[0]);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    String userInfo=usernameST+"/"+ passwordST[0];
                    String result="";
                    GetLoginResultTask getLoginResultTask=new GetLoginResultTask();
                    getLoginResultTask.execute(userInfo);
                }
            }
        });
        registerBtn.setOnClickListener(click->{
            Intent intent=new Intent(this,RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
    public class GetLoginResultTask extends AsyncTask<String,Void,String>{
        private String mLoginUrl="https://huli.kylin1221.com/apis/login.php?type=username&username={0}&password={1}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            String user=params[0];
            String pass=params[1];
            Log.e("user",user);
            Log.e("pass",pass);
            String url= MessageFormat.format(mLoginUrl,user,pass);
            Log.e("url",url);
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
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
        }

        //public void
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            SharedPreferences sharedPreferences=getSharedPreferences("login",MODE_PRIVATE);
            if(s.equals("timeout")||s.equals("error")||s.equals("[]")){
                Toast.makeText(Login.this,s,Toast.LENGTH_LONG).show();
            }else{
                try {
                    //JSONObject jsonObject=new JSONObject(buffer.toString());
                    //jsonObject.getJSONObject(buffer.toString());
                    JSONObject jsonObject=new JSONObject(s);
                    Log.e("json",jsonObject.toString());
                    //Long id=jsonObject.getLong("id")
                    if(jsonObject.getString("status").equals("1")){
                        String fullname=jsonObject.getString("fullname");
                        String userid=jsonObject.getString("userid");
                        String telephone=jsonObject.getString("telephone");
                        String role=jsonObject.getString("role");
                        String des=jsonObject.getString("description");
                        Long id=jsonObject.getLong("id");
                        userList.add(new User(id,fullname,userid,telephone,role,des));
                        if(rememberCB.isChecked()) {
                            sharedPreferences.edit()
                                    .putString("fullname", fullname)
                                    .putString("userid", userid)
                                    .putString("telephone", telephone)
                                    .putString("role", role)
                                    .putLong("id",id)
                                    .putBoolean("checked",true).apply();
                            Toast.makeText(Login.this, "login success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            sharedPreferences.edit()
                                    .putString("fullname", fullname)
                                    .putString("userid", userid)
                                    .putString("telephone", telephone)
                                    .putString("role", role)
                                    .putLong("id",id)
                                    .putBoolean("checked",false).apply();
                            Toast.makeText(Login.this, "login success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else if(jsonObject.getString("status").equals("2")){
                        totalTriedTimes++;
                        if(totalTriedTimes<3){
                            Toast.makeText(Login.this,"password error",Toast.LENGTH_LONG).show();
                        }else{
                            AlertDialog.Builder builder=new AlertDialog.Builder(Login.this);
                            builder.setTitle("You tried 3 times,Do you want change password?");
                            builder.setPositiveButton(getResources().getString(R.string.yes_btn),(click,arg)->{

                            }).setNegativeButton(getResources().getString(R.string.no_btn),(click,arg)->{

                            }).create().show();
                            Toast.makeText(Login.this,"password error",Toast.LENGTH_LONG).show();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public class GetLoginResultByPhoneTask extends AsyncTask<String,Void,String>{
        private String mLoginUrl="https://huli.kylin1221.com/apis/login.php?type=phone&username={0}&password={1}";
        public JSONObject jsonObject=new JSONObject();
        //OnData
        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            String user=params[0];
            String pass=params[1];
            Log.e("user",user);
            Log.e("pass",pass);
            String url= MessageFormat.format(mLoginUrl,user,pass);
            Log.e("url",url);
            StringBuffer buffer=new StringBuffer();
            /*HttpReqData reqData=new HttpReqData(url);
            HttpRespData respData= HttpRequestUtil.getData(reqData);
            if(respData.err_msg.length()<=0){
                try{
                    JSONObject obj=new JSONObject(respData.content);

                }catch (JSONException e){
                    Log.e("jsonException",e.toString());
                }
            }*/
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
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
        }

        //public void
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SharedPreferences sharedPreferences=getSharedPreferences("login",MODE_PRIVATE);
            if(s.equals("timeout")||s.equals("error")||s.equals("[]")){
                Toast.makeText(Login.this,s,Toast.LENGTH_LONG).show();
            }else{
                try {
                    //JSONObject jsonObject=new JSONObject(buffer.toString());
                    //jsonObject.getJSONObject(buffer.toString());
                    JSONObject jsonObject=new JSONObject(s);
                    Log.e("json",jsonObject.toString());
                    //Long id=jsonObject.getLong("id")
                    if(jsonObject.getString("status").equals("1")){
                        String fullname=jsonObject.getString("fullname");
                        String userid=jsonObject.getString("userid");
                        String telephone=jsonObject.getString("telephone");
                        String role=jsonObject.getString("role");
                        String des=jsonObject.getString("description");
                        Long id=jsonObject.getLong("id");
                        userList.add(new User(id,fullname,userid,telephone,role,des));
                        if(rememberCB.isChecked()) {
                            sharedPreferences.edit()
                                    .putString("fullname", fullname)
                                    .putString("userid", userid)
                                    .putString("telephone", telephone)
                                    .putString("role", role)
                                    .putLong("id",id)
                                    .putBoolean("checked",true).apply();
                            Toast.makeText(Login.this, "login success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            sharedPreferences.edit()
                                    .putString("fullname", fullname)
                                    .putString("userid", userid)
                                    .putString("telephone", telephone)
                                    .putString("role", role)
                                    .putLong("id",id)
                                    .putBoolean("checked",false).apply();
                            Toast.makeText(Login.this, "login success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else if(jsonObject.getString("status").equals("2")){
                        totalTriedTimes++;
                        if(totalTriedTimes<3){
                            Toast.makeText(Login.this,"password error",Toast.LENGTH_LONG).show();
                        }else{
                            AlertDialog.Builder builder=new AlertDialog.Builder(Login.this);
                            builder.setTitle("You tried 3 times,Do you want change password?");
                            builder.setPositiveButton(getResources().getString(R.string.yes_btn),(click,arg)->{

                            }).setNegativeButton(getResources().getString(R.string.no_btn),(click,arg)->{

                            }).create().show();
                            Toast.makeText(Login.this,"password error",Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean matchPhoneNumber(String phoneNumber) {
        String regex = "^1\\d{10}$";
        if(phoneNumber==null||phoneNumber.length()<=0){
            return false;
        }
        return Pattern.matches(regex, phoneNumber);
    }

    private static String md5(String input) throws NoSuchAlgorithmException{
        String result=input;
        if(input!=null){
            MessageDigest md=MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            BigInteger hash =new BigInteger(1,md.digest());
            result=hash.toString(16);
            while(result.length()<32){
                result="0"+result;
            }
        }
        return result;
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
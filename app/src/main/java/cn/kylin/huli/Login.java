package cn.kylin.huli;

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

import cn.kylin.huli.Utils.HttpRequestUtil;
import cn.kylin.huli.Utils.tool.HttpReqData;
import cn.kylin.huli.Utils.tool.HttpRespData;
import cn.kylin.huli.model.User;

public class Login extends AppCompatActivity {
    private ArrayList<User> userList=new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText usernameET=findViewById(R.id.ET_username_login),passwordET=findViewById(R.id.ET_password_login);
        Button loginBtn=findViewById(R.id.BT_login);
        CheckBox rememberCB=findViewById(R.id.CB_remember_login);
        SharedPreferences sharedPreferences=getSharedPreferences("login",MODE_PRIVATE);
        loginBtn.setOnClickListener(click->{
            String usernameST=usernameET.getText().toString(),passwordST=passwordET.getText().toString();
            if(usernameST.equals("")||passwordST.equals("")){
                Toast.makeText(this,"Empty error",Toast.LENGTH_LONG).show();
            }else{
                try {
                    passwordST=md5(passwordST);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                String userInfo=usernameST+"/"+passwordST;
                String result="";
                GetLoginResultTask getLoginResultTask=new GetLoginResultTask();
                getLoginResultTask.execute(userInfo);
                //AsyncTask.Status status=getLoginResultTask.getStatus();
                //status.toString();
                //Log.e("status",status.toString());
                //getLoginResultTask.setOnData
                try {
                    result=getLoginResultTask.get();
                    Log.e("res in main",result);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(result.trim().equals("timeout")||result.trim().equals("error")){
                    Toast.makeText(this,result,Toast.LENGTH_LONG).show();
                }else{
                    try {
                        //JSONObject jsonObject=new JSONObject(buffer.toString());
                        //jsonObject.getJSONObject(buffer.toString());
                        JSONObject jsonObject=new JSONObject(result);
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
                                        .putBoolean("checked",true).apply();
                                Toast.makeText(this, "login success", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                sharedPreferences.edit()
                                        .putString("fullname", fullname)
                                        .putString("userid", userid)
                                        .putString("telephone", telephone)
                                        .putString("role", role)
                                        .putBoolean("checked",false).apply();
                                Toast.makeText(this, "login success", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }else if(jsonObject.getString("status").equals("2")){
                            Toast.makeText(this,"password error",Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public class GetLoginResultTask extends AsyncTask<String,Void,String>{
        private String mLoginUrl="http://huli.kylin1221.com/apis/login.php?username={0}&password={1}";
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
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
}
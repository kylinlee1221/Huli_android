package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        sp=getSharedPreferences("verify",MODE_PRIVATE);
        Long userid=sp.getLong("userid",-1);
        Log.e("userid",String.valueOf(userid));
        EditText passwordET=findViewById(R.id.ET_password_changePass);
        Button changePassBtn=findViewById(R.id.BT_changePass_changePass);
        changePassBtn.setOnClickListener(click->{
            String passwordST=passwordET.getText().toString();
            if(injectSqlChecker(passwordST)){
                try {
                    passwordST=md5(passwordST);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                String params=passwordST+"/"+String.valueOf(userid);
                ChangePasswordTask changePasswordTask=new ChangePasswordTask();
                changePasswordTask.execute(params);
            }
        });
    }
    public class ChangePasswordTask extends AsyncTask<String,Void,String> {
        private String mUrl="https://huli.kylin1221.com/apis/changePassword.php?newpass={0}&userid={1}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String newpass=params[0];
            String userid=params[1];
            String url = MessageFormat.format(mUrl,newpass,userid);
            StringBuffer buffer=new StringBuffer();
            Log.e("url", url);
            try{
                URL url1=new URL(url);
                HttpURLConnection conn=(HttpURLConnection) url1.openConnection();
                conn.setRequestMethod("GET");
                //conn.addRequestProperty("Cookie", sharedPreferences.getString("jsessionid", ""));
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
                //addToInfo(buffer.toString());
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            SharedPreferences loginInfoSp=getSharedPreferences("login",MODE_PRIVATE);
            if(result.equals("error")||result.equals("timeout")||result.equals("[]")){
                Toast.makeText(ChangePasswordActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    String status=jsonObject.getString("status");
                    String msgs=jsonObject.getString("msgs");
                    if(status.equals("1")){
                        //verifyEditor.putBoolean("isVerified",true);
                        //verifyEditor.apply();
                        sp.edit()
                                        .remove("isVerified")
                                                .remove("userid").apply();
                        loginInfoSp.edit()
                                .remove("fullname")
                                .remove("userid")
                                .remove("telephone")
                                .remove("role")
                                .remove("id")
                                .remove("checked").apply();
                        Toast.makeText(ChangePasswordActivity.this,msgs,Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(ChangePasswordActivity.this,Login.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(ChangePasswordActivity.this,msgs,Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
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
    private static String md5(String input) throws NoSuchAlgorithmException {
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
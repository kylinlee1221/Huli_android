package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cn.kylin.huli.model.User;

public class findPasswordActivity extends AppCompatActivity {
    private ArrayList<User> userArrayList;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);
        sp=getSharedPreferences("verify",MODE_PRIVATE);
        userArrayList=new ArrayList<User>();
        Boolean isVerified=sp.getBoolean("isVerified",false);
        EditText usernameET=findViewById(R.id.ET_username_findPass),passwordET=findViewById(R.id.ET_password_findPass);
        Button findBtn=findViewById(R.id.BT_changePass_findPass);
        GetUserListTask getUserListTask=new GetUserListTask();
        getUserListTask.execute("gogogo");
        findBtn.setOnClickListener(click->{
            String UsernameST=usernameET.getText().toString().trim();
            if(matchPhoneNumber(UsernameST)){
                if(checkUserNameByPhone(UsernameST)){
                        Intent intent=new Intent(findPasswordActivity.this,VerifyUserActivity.class);
                        startActivity(intent);
                        //sp.edit().putLong()
                        finish();
                }
            }else{
                if(checkUserName(UsernameST)){
                        Intent intent=new Intent(findPasswordActivity.this,VerifyUserActivity.class);
                        startActivity(intent);
                        finish();
                }
            }
        });
    }

    public class GetUserListTask extends AsyncTask<String,Void,String> {
        private String murl="https://huli.kylin1221.com/apis/getUserList.php";

        @Override
        protected String doInBackground(String... strings) {
            StringBuffer buffer=new StringBuffer();
            try {
                URL url1 = new URL(murl);
                HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(3000);
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
                Log.e("result",buffer.toString());
                //insertIntoData(buffer.toString());
            }catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            try {
                if(!result.equals("error")&&!result.equals("timeout")&&!result.equals("[]")) {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        //userList.add(jsonObject.getString("fullname"));
                        userArrayList.add(new User(jsonObject.getLong("id"),jsonObject.getString("fullname"),jsonObject.getString("telephone"),jsonObject.getString("username")));
                        //userSP.setAdapter(arrayAdapter);
                    }
                }else{
                    Toast.makeText(findPasswordActivity.this,"getUserList error",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
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

    private boolean checkUserNameByPhone(String userName){
        for(User user:userArrayList){
            if(user.getTelephone().equals(userName)){
                sp.edit().putLong("userid",user.getId()).apply();
                return true;
            }
        }
        return false;
    }

    private boolean checkUserName(String userName){
        for(User user:userArrayList){
            if(user.getUsername().equals(userName)){
                sp.edit().putLong("userid",user.getId()).apply();
                return true;
            }
        }
        return false;
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
package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EditText usernameET=findViewById(R.id.ET_username_register),passwordET=findViewById(R.id.ET_password_register),fullName=findViewById(R.id.ET_realName_register);
        EditText telET=findViewById(R.id.ET_phoneNum_register),desET=findViewById(R.id.ET_des_register);
        RadioGroup roleET=findViewById(R.id.RG_role_register);
        Button registerBt=findViewById(R.id.BT_register);
        final String[] roleST = {""};
        roleET.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton chosen=findViewById(radioGroup.getCheckedRadioButtonId());
                //Log.e("chosen",String.valueOf(radioGroup.getCheckedRadioButtonId()));
                roleST[0] =chosen.getText().toString();
            }
        });
        RadioButton chosen=findViewById(roleET.getCheckedRadioButtonId());
        //Log.e("chosen",String.valueOf(roleET.getCheckedRadioButtonId()));
        roleST[0] =chosen.getText().toString();
        registerBt.setOnClickListener(click->{
            Log.e("role",roleST[0]);
            String usernameST=usernameET.getText().toString(),passwordST=passwordET.getText().toString(),fullST=fullName.getText().toString();
            String telST=telET.getText().toString(),desST=desET.getText().toString();
            if(injectSqlChecker(usernameST)&&injectSqlChecker(passwordST)&&injectSqlChecker(fullST)&&injectSqlChecker(telST)&&injectSqlChecker(desST)){
                if(matchPhoneNumber(telST)){
                    try{
                        passwordST=md5(passwordST);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(roleST[0].equals("user")||roleST[0].equals("User")){
                        String params=usernameST+"/"+passwordST+"/"+fullST+"/"+telST+"/"+"0/"+desST;
                        GetRegisterResultTask getRegisterResultTask=new GetRegisterResultTask();
                        getRegisterResultTask.execute(params);
                    }else{
                        String params=usernameST+"/"+passwordST+"/"+fullST+"/"+telST+"/"+"1/"+desST;
                        GetRegisterResultTask getRegisterResultTask=new GetRegisterResultTask();
                        getRegisterResultTask.execute(params);
                    }
                }
            }
        });
    }

    public class GetRegisterResultTask extends AsyncTask<String,Void,String>{

        private String mRegisterUrl="http://huli.kylin1221.com/apis/register.php?username={0}&password={1}&fullname={2}&telephone={3}&role={4}&des={5}";


        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            String user=params[0];
            String pass=params[1];
            String full=params[2];
            String tel=params[3];
            String role=params[4];
            String des=params[5];
            String url= MessageFormat.format(mRegisterUrl,user,pass,full,tel,role,des);
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
            }catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());
            return buffer.toString();
        }
    }

    private static boolean matchPhoneNumber(String phoneNumber) {
        String regex = "^1\\d{10}$";
        if(phoneNumber==null||phoneNumber.length()<=0){
            return false;
        }
        return Pattern.matches(regex, phoneNumber);
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
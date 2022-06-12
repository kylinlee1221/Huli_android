package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

public class VerifyUserActivity extends AppCompatActivity {
    private EditText verifyET;
    private ImageView verifyCode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences verifySP;
    private SharedPreferences.Editor verifyEditor;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_user);
        sharedPreferences=getSharedPreferences("session",MODE_PRIVATE);
        verifySP=getSharedPreferences("verify",MODE_PRIVATE);
        verifyEditor=verifySP.edit();
        editor=sharedPreferences.edit();
        verifyCode=findViewById(R.id.IV_verifyImage_verify);
        verifyET=findViewById(R.id.ET_verifyCode_verify);
        Button verifyBtn=findViewById(R.id.BT_verify_verify);
        GetVerifyCodeTask getVerifyCodeTask=new GetVerifyCodeTask();
        getVerifyCodeTask.execute("gogogo");
        verifyCode.setOnClickListener(click->{
            GetVerifyCodeTask getVerifyCodeTask1=new GetVerifyCodeTask();
            getVerifyCodeTask1.execute("gogogo");
        });
        verifyBtn.setOnClickListener(click->{
            String verifyST=verifyET.getText().toString();
            CheckVerifyCodeTask checkVerifyCodeTask=new CheckVerifyCodeTask();
            if(!verifyST.equals("")){
                checkVerifyCodeTask.execute(verifyST.trim());
            }
        });
    }

    public class CheckVerifyCodeTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/checkCode.php?userCode={0}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String userCode=params[0];
            String url = MessageFormat.format(mUrl,userCode);
            StringBuffer buffer=new StringBuffer();
            Log.e("url", url);
            try{
                URL url1=new URL(url);
                HttpURLConnection conn=(HttpURLConnection) url1.openConnection();
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Cookie", sharedPreferences.getString("jsessionid", ""));
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
            if(result.equals("error")||result.equals("timeout")||result.equals("[]")){
                Toast.makeText(VerifyUserActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    String status=jsonObject.getString("status");
                    String msgs=jsonObject.getString("msgs");
                    if(status.equals("1")){
                        verifyEditor.putBoolean("isVerified",true);
                        verifyEditor.apply();
                        Toast.makeText(VerifyUserActivity.this,msgs,Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(VerifyUserActivity.this,findPasswordActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(VerifyUserActivity.this,msgs,Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public class GetVerifyCodeTask extends AsyncTask<String,Void, Bitmap>{
        private String mUrl="https://huli.kylin1221.com/apis/verifyCode.php";

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap=null;
            try {
                URL url=new URL(mUrl);
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                InputStream is=conn.getInputStream();
                final String cookieString=conn.getHeaderField("Set-Cookie");
                //然后保存在本地
                editor.putString("jsessionid", cookieString);
                editor.commit();
                conn.setInstanceFollowRedirects(true);
                //InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            super.onPostExecute(result);
            verifyCode.setImageBitmap(result);
        }
    }
}
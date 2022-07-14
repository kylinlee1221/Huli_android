package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAnnouncementActivity extends AppCompatActivity {
    //TimePicker pvTime;
    private int day,month,year,hour,minute,second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_annoucement);
        getDateTime();
        Log.e("language",Locale.getDefault().getLanguage());
        //Button timePicker=findViewById(R.id.BT_TimeChooser_Announ);
        Button dateChoser=findViewById(R.id.BT_DateChooser_Announ),timeChoser=findViewById(R.id.BT_TimeChooser_Announ),addBtn=findViewById(R.id.BT_AddAnnouncement_Announ);
        EditText announET=findViewById(R.id.ET_announcement_Announ);
        dateChoser.setOnClickListener(click->{
            final String[] timeChosed = {""};
            DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    dateChoser.setText(String.valueOf(i)+"-"+String.valueOf((++i1))+"-"+String.valueOf(i2));
                }
            };

            DatePickerDialog datePickerDialog=new DatePickerDialog(this,0,listener,year,month,day);
            datePickerDialog.setCancelable(false);
            datePickerDialog.show();

            //Log.e("time",timeChosed[0]);
            //dateChoser.setText(timeChosed[0]);
        });
        timeChoser.setOnClickListener(click->{
            final String[] timeChosed = {""};
            TimePickerDialog.OnTimeSetListener timeListener=new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    timeChoser.setText(String.valueOf(i)+":"+String.valueOf((i1)));
                }
            };
            TimePickerDialog timePickerDialog=new TimePickerDialog(this,timeListener,hour,minute,true);
            timePickerDialog.setCancelable(false);
            timePickerDialog.show();
        });
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        Long userid=sp.getLong("id",-1);
        Log.e("uid",String.valueOf(userid));
        addBtn.setOnClickListener(click->{
            if(!announET.getText().toString().equals("")&&injectSqlChecker(announET.getText().toString())){
                String time=dateChoser.getText().toString()+" "+timeChoser.getText().toString();
                String originDateBtn=getResources().getString(R.string.date_chose_btn)+" "+getResources().getString(R.string.time_chose_btn);
                originDateBtn=originDateBtn.toLowerCase(Locale.ROOT);
                time=time.toLowerCase(Locale.ROOT);
                if(time.equals(originDateBtn)||time.contains(getResources().getString(R.string.date_chose_btn).toLowerCase(Locale.ROOT))||time.contains(getResources().getString(R.string.time_chose_btn).toLowerCase(Locale.ROOT))||time.equals(" ")){
                    Toast.makeText(CreateAnnouncementActivity.this,getResources().getText(R.string.chose_date_error),Toast.LENGTH_LONG).show();
                }else {
                    CreateAnnouncementTask createAnnouncementTask = new CreateAnnouncementTask();
                    String params = String.valueOf(userid) + "/" + announET.getText().toString() + "/" + dateChoser.getText().toString() + " " + timeChoser.getText().toString();
                    createAnnouncementTask.execute(params);
                }
            }
        });
    }

    public class CreateAnnouncementTask extends AsyncTask<String,Void,String>{

        private String mAddUrl="https://huli.kylin1221.com/apis/addAnnoucement.php?sendby={0}&announ={1}&endtime={2}";
        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            String sendby=params[0];
            String announ=params[1];
            String endtime=params[2];
            //Log.e("user",user);
            //Log.e("pass",pass);
            String url= MessageFormat.format(mAddUrl,sendby,announ,endtime);
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
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
            //return null;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result.trim().equals("timeout")||result.trim().equals("error")||result.trim().equals("[]")){
                Toast.makeText(CreateAnnouncementActivity.this,result,Toast.LENGTH_LONG).show();
            }else {
                try {
                    //JSONObject jsonObject=new JSONObject(buffer.toString());
                    //jsonObject.getJSONObject(buffer.toString());
                    JSONObject jsonObject = new JSONObject(result);
                    Log.e("json", jsonObject.toString());
                    //Long id=jsonObject.getLong("id")
                    if (jsonObject.getString("status").equals("1")) {
                        Toast.makeText(CreateAnnouncementActivity.this, "add success", Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(CreateAnnouncementActivity.this,AnnounceManageActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void getDateTime(){
        Calendar calendar = Calendar.getInstance();//取得当前时间的年月日 时分秒


        year = calendar.get(Calendar.YEAR);


        month = calendar.get(Calendar.MONTH);


        day = calendar.get(Calendar.DAY_OF_MONTH);


        hour = calendar.get(Calendar.HOUR_OF_DAY);


        minute = calendar.get(Calendar.MINUTE);


        second = calendar.get(Calendar.SECOND);

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
package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import cn.kylin.huli.model.User;

public class AddOrderActivity extends AppCompatActivity {

    private ArrayList<String> userList=new ArrayList<String>();
    private int day,month,year,hour,minute,second;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<User> userArrayList;
    private Spinner userSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);
        userSP=findViewById(R.id.SP_userChose_Add);
        EditText orderNameET=findViewById(R.id.ET_name_Add),moneyET=findViewById(R.id.ET_money_Add),paidET=findViewById(R.id.ET_alreadyPay_Add),placeET=findViewById(R.id.ET_place_Add);
        EditText deatisET=findViewById(R.id.ET_place_details_Add),phoneET=findViewById(R.id.ET_customerPhone_Add);
        Button dateChoser=findViewById(R.id.BT_DateChooser_Add),timeChoser=findViewById(R.id.BT_TimeChooser_Add),createOrderBtn=findViewById(R.id.BT_addOrder_Add);
        Button BeginDateChoser=findViewById(R.id.BT_DateChooser_start_Add),BeginTimeChoser=findViewById(R.id.BT_TimeChooser_start_Add);
        userArrayList=new ArrayList<User>();
        getDateTime();
        //userList.add("all");
        arrayAdapter=new ArrayAdapter<String>(this,R.layout.item_dropdown,userList);
        userSP.setAdapter(arrayAdapter);
        GetUserListTask getUserListTask=new GetUserListTask();
        getUserListTask.execute("gogogo");
        dateChoser.setOnClickListener(click->{
            final String[] timeChosed = {""};
            DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    dateChoser.setText(String.valueOf(i)+"-"+String.valueOf((++i1))+"-"+String.valueOf(i2));
                }
            };

            DatePickerDialog datePickerDialog=new DatePickerDialog(this,0,listener,year,month,day);
            datePickerDialog.show();

            Log.e("time",timeChosed[0]);
            dateChoser.setText(timeChosed[0]);
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
            timePickerDialog.show();
        });
        BeginDateChoser.setOnClickListener(click->{
            final String[] timeChosed = {""};
            DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    BeginDateChoser.setText(String.valueOf(i)+"-"+String.valueOf((++i1))+"-"+String.valueOf(i2));
                }
            };

            DatePickerDialog datePickerDialog=new DatePickerDialog(this,0,listener,year,month,day);
            datePickerDialog.show();

            Log.e("time",timeChosed[0]);
            BeginDateChoser.setText(timeChosed[0]);
        });
        BeginTimeChoser.setOnClickListener(click->{
            final String[] timeChosed = {""};
            TimePickerDialog.OnTimeSetListener timeListener=new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    BeginTimeChoser.setText(String.valueOf(i)+":"+String.valueOf((i1)));
                }
            };
            TimePickerDialog timePickerDialog=new TimePickerDialog(this,timeListener,hour,minute,true);
            timePickerDialog.show();
        });
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        Long userid=sp.getLong("id",-1);
        Log.e("uid",String.valueOf(userid));
        createOrderBtn.setOnClickListener(click->{
            String nameST=orderNameET.getText().toString(),moneyST=moneyET.getText().toString(),paidST=paidET.getText().toString(),placeST=placeET.getText().toString(),details=deatisET.getText().toString();
            String time=dateChoser.getText().toString()+" "+timeChoser.getText().toString(),phoneST=phoneET.getText().toString(),beginTime=BeginDateChoser.getText().toString()+" "+BeginTimeChoser.getText().toString();
            Long toid = null;
            for(User user:userArrayList){
                if(user.getFullname().equals(userSP.getSelectedItem())){
                    toid=user.getId();
                }
            }
            if(userid>=0&&matchPhoneNumber(phoneST)){
                String params=nameST+"/"+moneyST+"/"+paidST+"/"+String.valueOf(userid)+"/"+String.valueOf(toid)+"/"+time+"/"+beginTime+"/"+placeST+"/"+details+"/"+phoneST;
                AddOrderTask addOrderTask=new AddOrderTask();
                addOrderTask.execute(params);
            }

        });
    }

    public class GetUserListTask extends AsyncTask<String,Void,String>{
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
                if(!result.equals("error")&&!result.equals("timeout")) {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        userList.add(jsonObject.getString("fullname"));
                        userArrayList.add(new User(jsonObject.getLong("id"),jsonObject.getString("fullname"),jsonObject.getString("telephone")));
                        userSP.setAdapter(arrayAdapter);
                    }
                }else{
                    Toast.makeText(AddOrderActivity.this,"getUserList error",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class AddOrderTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/addOrder.php?name={0}&price={1}&paid={2}&sendby={3}&sendto={4}&orderdate={5}&orderstart={6}&place={7}&details={8}&phone={9}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String name = params[0];
            String price= params[1];
            String paid= params[2];
            String sendby= params[3];
            String sendto=params[4];
            String orderdate=params[5];
            String orderStart=params[6];
            String place=params[7];
            String details=params[8];
            String phone=params[9];
            String url = MessageFormat.format(mUrl, name,price,paid,sendby,sendto,orderdate,orderStart,place,details,phone);
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
            if(result.equals("error")||result.equals("timeout")){
                Toast.makeText(AddOrderActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(AddOrderActivity.this,"add success",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(AddOrderActivity.this,OrderManageActivity.class);
                startActivity(intent);
                finish();
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

    private static boolean matchPhoneNumber(String phoneNumber) {
        String regex = "^1\\d{10}$";
        if(phoneNumber==null||phoneNumber.length()<=0){
            return false;
        }
        return Pattern.matches(regex, phoneNumber);
    }

    public void insertIntoData(String result){
        try {
            //JSONObject jsonObject=new JSONObject(result);
            JSONArray jsonArray=new JSONArray(result);
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                userList.add(jsonObject.getString("fullname"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
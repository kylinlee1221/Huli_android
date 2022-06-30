package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.kylin.huli.model.Order;

public class OrderMarketActivity extends AppCompatActivity {
    private int day,month,year,hour,minute,second;
    private ArrayList<Order> orderList=new ArrayList<Order>();
    private infoAdapter adapter;
    private ListView infoList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Long latestOrderId=Long.parseLong("-1");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_market);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        Long userid=sp.getLong("id",-1);
        Log.e("uid",String.valueOf(userid));
        infoList=findViewById(R.id.LV_orderList_Market);
        swipeRefreshLayout=findViewById(R.id.SW_OrderM);
        swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        GetOrderListByIdTask getOrderListByIdTask=new GetOrderListByIdTask();
        getOrderListByIdTask.execute(String.valueOf(userid));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetOrderListByIdTask getOrderListByIdTask1=new GetOrderListByIdTask();
                getOrderListByIdTask1.execute(String.valueOf(userid));
                //swipeRefreshLayout.setRefreshing(false);
            }
        });
        if(orderList.size()!=0){
            latestOrderId=orderList.get(orderList.size()-1).getId();
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ScheduledExecutorService executor= Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //SystemClock.sleep(10000L);
                GetOrderListByIdTask getOrderListByIdTask1=new GetOrderListByIdTask();
                getOrderListByIdTask1.execute(String.valueOf(userid));
                //Log.e("TAG", "runnable just do it! time =" +  simpleDateFormat.format(System.currentTimeMillis()));
            }
        },60,60, TimeUnit.SECONDS);
    }

    public class GetOrderListByIdTask extends AsyncTask<String,Void,String>{

        private String mUrl="https://huli.kylin1221.com/apis/getOrderList.php?type=byid&userid={0}";
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
                    swipeRefreshLayout.setRefreshing(false);
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
                swipeRefreshLayout.setRefreshing(false);
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
            orderList.clear();
            if(!result.equals("error")&&!result.equals("timeout")&&!result.equals("[]")){
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        Long id=jsonObject.getLong("id");
                        String ordername=jsonObject.getString("ordername");
                        Double orderprice=jsonObject.getDouble("orderprice");
                        String orderplace=jsonObject.getString("orderplace");
                        Double orderpaid=jsonObject.getDouble("orderpaid");
                        String orderStart=jsonObject.getString("orderstart");
                        String orderend=jsonObject.getString("orderend");
                        String orderStatus=jsonObject.getString("status");
                        String cusPhone=jsonObject.getString("cusphone");
                        int status=Integer.parseInt(orderStatus);
                        if(status==1) {
                            Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone);
                            orderList.add(tmpOrder);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(!result.equals("error")&&!result.equals("timeout")&&!result.equals("[]")){
                    if(orderList.size()!=0){
                        adapter=new infoAdapter();
                        infoList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        //MediaPlayer mediaPlayer=new MediaPlayer(OrderMarketActivity.this,R.raw.ordercomes);
                        Long newOrderId=orderList.get(orderList.size()-1).getId();
                        if(newOrderId>latestOrderId){
                            latestOrderId=newOrderId;
                            MediaPlayer mediaPlayer=MediaPlayer.create(OrderMarketActivity.this,R.raw.ordercomes);
                            if(!mediaPlayer.isPlaying()){
                                //mediaPlayer.setVolume(1.0f,1.0f);
                                mediaPlayer.start();
                            }
                        }
                    }
                }else{
                    Toast.makeText(OrderMarketActivity.this,"Connection error",Toast.LENGTH_LONG).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public class AcceptOrderTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/acceptOrder.php?orderid={0}";
        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String orderid=params[0];
            String url = MessageFormat.format(mUrl,orderid);
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
                //addToInfo(buffer.toString());
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
            super.onPostExecute("result");
            if(result.equals("error")||result.equals("timeout")) {
                Toast.makeText(OrderMarketActivity.this,result,Toast.LENGTH_LONG).show();
            }else {
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    String status=jsonObject.getString("status");
                    if(status.equals("1")) {
                        Toast.makeText(OrderMarketActivity.this,"success",Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(OrderMarketActivity.this,MyOrderActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    public class RefuseOrderTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/refuseOrder.php?orderid={0}&reason={1}";
        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String orderid=params[0];
            String reason=params[1];
            String url = MessageFormat.format(mUrl,orderid,reason);
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
                //addToInfo(buffer.toString());
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
            if(result.equals("error")||result.equals("timeout")) {
                Toast.makeText(OrderMarketActivity.this,result,Toast.LENGTH_LONG).show();
            }else {
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    String status=jsonObject.getString("status");
                    if(status.equals("1")) {
                        Toast.makeText(OrderMarketActivity.this,"success",Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(OrderMarketActivity.this,MyOrderActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addToInfo(String result){
        orderList.clear();
        if(!result.equals("error")){
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    Long id=jsonObject.getLong("id");
                    String ordername=jsonObject.getString("ordername");
                    Double orderprice=jsonObject.getDouble("orderprice");
                    String orderplace=jsonObject.getString("orderplace");
                    Double orderpaid=jsonObject.getDouble("orderpaid");
                    String orderStart=jsonObject.getString("orderstart");
                    String orderend=jsonObject.getString("orderend");
                    String orderStatus=jsonObject.getString("status");
                    String cusPhone=jsonObject.getString("cusphone");
                    int status=Integer.parseInt(orderStatus);
                    if(status==1) {
                        Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone);
                        orderList.add(tmpOrder);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    protected class infoAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return orderList.size();
        }

        @Override
        public Order getItem(int i) {
            return orderList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater=getLayoutInflater();
            View rowView;
            TextView rowMessage,orderStatus;
            Button orderBtn,contactBtn;
            Order thisRow=getItem(i);
            rowView=inflater.inflate(R.layout.show_list_resource,viewGroup,false);
            rowMessage=rowView.findViewById(R.id.info_text_view);
            orderStatus=rowView.findViewById(R.id.info_status_view);
            orderBtn=rowView.findViewById(R.id.info_action_button);
            contactBtn=rowView.findViewById(R.id.info_contact_button);
            orderStatus.setTextColor(Color.BLACK);
            Boolean isOD=false;
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String nowDate=String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day)+" "+String.valueOf(hour)+":"+String.valueOf(minute);
            Date date1 = new Date(),date2 = new Date();
            try {
                date1=simpleDateFormat.parse(thisRow.getOrderDate());
                date2=simpleDateFormat.parse(nowDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(date2.getTime()>date1.getTime()){
                isOD=true;
            }
            if(isOD){
                contactBtn.setText(R.string.refuse_order_btn);
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_available)+getResources().getString(R.string.status_outOfDate));
                orderBtn.setText(R.string.accept_order_btn);
            }else{
                contactBtn.setText(R.string.refuse_order_btn);
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_available));
                orderBtn.setText(R.string.accept_order_btn);
            }
            orderBtn.setOnClickListener(click->{
                AcceptOrderTask acceptOrderTask=new AcceptOrderTask();
                acceptOrderTask.execute(String.valueOf(thisRow.getId()));
            });
            contactBtn.setOnClickListener(click->{
                AlertDialog.Builder builder1=new AlertDialog.Builder(OrderMarketActivity.this);
                View refuseView=View.inflate(OrderMarketActivity.this,R.layout.refuse_details_view,null);
                EditText refuseET=refuseView.findViewById(R.id.ET_refuseReason_refuse);
                builder1.setTitle("Refuse reason");
                builder1.setView(refuseView);
                builder1.setPositiveButton("Refuse",(click1,arg1)->{
                    RefuseOrderTask refuseOrderTask=new RefuseOrderTask();
                    refuseOrderTask.execute(String.valueOf(thisRow.getId())+"/"+refuseET.getText().toString());
                }).create().show();
            });
            rowMessage.setText(getResources().getString(R.string.order_name_detail)+thisRow.getOrdername()+"\n"+getResources().getString(R.string.order_money_detail)+thisRow.getOrderprice()+"\n"+getResources().getString(R.string.order_paid_detail)+thisRow.getOrderpaid()+"\n"+getResources().getString(R.string.order_place_detail)+thisRow.getOrderplace()+"\n"+getResources().getString(R.string.order_start_detail)+thisRow.getOrderStart()+"\n"+getResources().getString(R.string.order_end_detail)+thisRow.getOrderDate());
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            return rowView;
        }
    }

    public void getDateTime(){
        Calendar calendar = Calendar.getInstance();//取得当前时间的年月日 时分秒


        year = calendar.get(Calendar.YEAR);


        month = calendar.get(Calendar.MONTH)+1;


        day = calendar.get(Calendar.DAY_OF_MONTH);


        hour = calendar.get(Calendar.HOUR_OF_DAY);


        minute = calendar.get(Calendar.MINUTE);


        second = calendar.get(Calendar.SECOND);

    }


    @Override
    protected void onDestroy() {
        
        super.onDestroy();
    }
}
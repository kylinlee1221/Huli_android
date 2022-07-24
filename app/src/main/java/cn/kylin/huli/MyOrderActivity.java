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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import cn.kylin.huli.model.Order;

public class MyOrderActivity extends AppCompatActivity {
    private int day,month,year,hour,minute,second;
    private ArrayList<Order> orderArrayList=new ArrayList<Order>();
    private ArrayList<Order> workingOrderArrayList=new ArrayList<Order>();
    private ArrayList<Order> doneOrderArrayList=new ArrayList<Order>();
    private ArrayList<Order> outODOrderArrayList=new ArrayList<Order>();
    private infoAdapter adapter;
    private ListView orderList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Long userId;
    private Boolean isAlreadyOnWork=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        getDateTime();
        Spinner OrderFilter=findViewById(R.id.SP_orderFilter_MyOrder);
        orderList=findViewById(R.id.LV_orderList_MyOrder);
        swipeRefreshLayout=findViewById(R.id.SW_MyOrder);
        swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        userId=sp.getLong("id",-1);
        GetOrderListByIdTask getOrderListByIdTask=new GetOrderListByIdTask();
        getOrderListByIdTask.execute(String.valueOf(userId));
            //addToInfo(getOrderListByIdTask.get());
        if (orderArrayList.size() != 0) {
            adapter = new infoAdapter();
            orderList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        OrderFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("select pos",String.valueOf(i));
                switch (i){
                    case 0:
                        if (orderArrayList.size() != 0) {
                            adapter = new infoAdapter();
                            orderList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case 1:
                        WorkingInfoAdapter WorkingAdapter=new WorkingInfoAdapter();
                        orderList.setAdapter(WorkingAdapter);
                        WorkingAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        DoneInfoAdapter doneInfoAdapter=new DoneInfoAdapter();
                        orderList.setAdapter(doneInfoAdapter);
                        doneInfoAdapter.notifyDataSetChanged();
                        break;
                    case 3:
                        OutODInfoAdapter outODInfoAdapter=new OutODInfoAdapter();
                        orderList.setAdapter(outODInfoAdapter);
                        outODInfoAdapter.notifyDataSetChanged();
                        break;
                    default:
                        if (orderArrayList.size() != 0) {
                            adapter = new infoAdapter();
                            orderList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetOrderListByIdTask getOrderListByIdTask1=new GetOrderListByIdTask();
                getOrderListByIdTask1.execute(String.valueOf(userId));
                OrderFilter.setSelection(0);
            }
        });
    }

    public class GetOrderListByIdTask extends AsyncTask<String,Void,String> {

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
            if(result.equals("timeout")||result.equals("error")||result.equals("[]")){
                Toast.makeText(MyOrderActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                orderArrayList.clear();
                workingOrderArrayList.clear();
                outODOrderArrayList.clear();
                doneOrderArrayList.clear();
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
                            String orderend=jsonObject.getString("orderend");
                            String orderStart=jsonObject.getString("orderstart");
                            String orderStatus=jsonObject.getString("status");
                            String cusPhone=jsonObject.getString("cusphone");
                            String orderfrom=jsonObject.getString("orderfrom");
                            int status=Integer.parseInt(orderStatus);
                            if(status>1) {
                                Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone,orderfrom);
                                orderArrayList.add(tmpOrder);
                            }
                            if(status==2){
                                Long remainStartTime=TimeDiffByMinute(orderStart);
                                if(remainStartTime<=5){
                                    MediaPlayer mediaPlayer=MediaPlayer.create(MyOrderActivity.this,R.raw.checkin_hint);
                                    if(!mediaPlayer.isPlaying()){
                                        //mediaPlayer.setVolume(1.0f,1.0f);
                                        mediaPlayer.start();
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (orderArrayList.size() != 0) {
                    spiltArrayList(orderArrayList);
                    adapter = new infoAdapter();
                    orderList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    checkOnWorkStatus(orderArrayList);
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void spiltArrayList(ArrayList<Order> tempArrayList){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String nowDate=String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day)+" "+String.valueOf(hour)+":"+String.valueOf(minute);

        for(Order order:tempArrayList){
            if(order.getOrderStatus().equals("3")){
                workingOrderArrayList.add(order);
            }
            if(order.getOrderStatus().equals("5")){
                doneOrderArrayList.add(order);
            }
            Date date1 = new Date(),date2 = new Date();
            try {
                date1=simpleDateFormat.parse(order.getOrderDate());
                date2=simpleDateFormat.parse(nowDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(checkOD(date1,date2)){
                outODOrderArrayList.add(order);
            }
        }
    }

    public void checkOnWorkStatus(ArrayList<Order> tempArrayList){
        for(Order order:tempArrayList){
            if(order.getOrderStatus().equals("3")){
                isAlreadyOnWork=true;
            }
        }
    }

    public class WorkOnOrderTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/workOnOrder.php?orderid={0}";
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
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result.equals("error")||result.equals("timeout")||result.equals("[]")){
                Toast.makeText(MyOrderActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    String status=jsonObject.getString("status");
                    String msgs=jsonObject.getString("msgs");
                    if(status.equals("1")){
                        Toast.makeText(MyOrderActivity.this,msgs,Toast.LENGTH_LONG).show();
                        GetOrderListByIdTask getOrderListByIdTask=new GetOrderListByIdTask();
                        getOrderListByIdTask.execute(String.valueOf(userId));
                        Intent intent=new Intent(MyOrderActivity.this,NowOrderActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(MyOrderActivity.this,msgs,Toast.LENGTH_LONG).show();
                        GetOrderListByIdTask getOrderListByIdTask=new GetOrderListByIdTask();
                        getOrderListByIdTask.execute(String.valueOf(userId));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void addToInfo(String result){
        orderArrayList.clear();
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
                    String orderend=jsonObject.getString("orderend");
                    String orderStart=jsonObject.getString("orderstart");
                    String orderStatus=jsonObject.getString("status");
                    String cusPhone=jsonObject.getString("cusphone");
                    String orderfrom=jsonObject.getString("orderfrom");
                    int status=Integer.parseInt(orderStatus);
                    if(status>1) {
                        Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone,orderfrom);
                        orderArrayList.add(tmpOrder);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean checkOD(Date date1,Date date2){
        Boolean isOD=false;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String nowDate=String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day)+" "+String.valueOf(hour)+":"+String.valueOf(minute);
        //Date date1 = new Date(),date2 = new Date();
        if(date2.getTime()>date1.getTime()){
            isOD=true;
        }
        return isOD;
    }

    protected class infoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return orderArrayList.size();
        }

        @Override
        public Order getItem(int i) {
            return orderArrayList.get(i);
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
            isOD=checkOD(date1,date2);
            Log.e("isod",String.valueOf(isOD));
            if(thisRow.getOrderStatus().equals("2")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }
                if(!isAlreadyOnWork){
                    orderBtn.setOnClickListener(click->{
                        WorkOnOrderTask workOnOrderTask=new WorkOnOrderTask();
                        workOnOrderTask.execute(String.valueOf(thisRow.getId()));
                    });
                }else{
                    orderBtn.setOnClickListener(click->{
                        Toast.makeText(MyOrderActivity.this,"you already have an on work order",Toast.LENGTH_LONG).show();
                    });
                }

            }else if(thisRow.getOrderStatus().equals("3")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }
                orderBtn.setOnClickListener(click->{
                    Intent intent=new Intent(MyOrderActivity.this,NowOrderActivity.class);
                    startActivity(intent);
                    finish();
                });
            }else if(thisRow.getOrderStatus().equals("4")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }
            }else if(thisRow.getOrderStatus().equals("5")){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_done));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText("delete");
            }
            contactBtn.setOnClickListener(click->{
                Intent callPhone=new Intent(Intent.ACTION_DIAL);
                Uri phoneNum=Uri.parse("tel:"+thisRow.getOrderPhone());
                callPhone.setData(phoneNum);
                startActivity(callPhone);
            });
            rowMessage.setText(getResources().getString(R.string.order_name_detail)+thisRow.getOrdername()+"\n"+getResources().getString(R.string.order_from_detail)+thisRow.getOrderFrom()+"\n"+getResources().getString(R.string.order_money_detail)+thisRow.getOrderprice()+"\n"+getResources().getString(R.string.order_paid_detail)+thisRow.getOrderpaid()+"\n"+getResources().getString(R.string.order_place_detail)+thisRow.getOrderplace()+"\n"+getResources().getString(R.string.order_phone_detail)+thisRow.getOrderPhone()+"\n"+getResources().getString(R.string.order_start_detail)+thisRow.getOrderStart()+"\n"+getResources().getString(R.string.order_end_detail)+thisRow.getOrderDate());
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            return rowView;
        }
    }

    protected class DoneInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return doneOrderArrayList.size();
        }

        @Override
        public Order getItem(int i) {
            return doneOrderArrayList.get(i);
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
            isOD=checkOD(date1,date2);
            Log.e("isod",String.valueOf(isOD));
            if(thisRow.getOrderStatus().equals("2")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }
                if(!isAlreadyOnWork){
                    orderBtn.setOnClickListener(click->{
                        WorkOnOrderTask workOnOrderTask=new WorkOnOrderTask();
                        workOnOrderTask.execute(String.valueOf(thisRow.getId()));
                    });
                }else{
                    orderBtn.setOnClickListener(click->{
                        Toast.makeText(MyOrderActivity.this,"you already have an on work order",Toast.LENGTH_LONG).show();
                    });
                }

            }else if(thisRow.getOrderStatus().equals("3")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }
                orderBtn.setOnClickListener(click->{
                    Intent intent=new Intent(MyOrderActivity.this,NowOrderActivity.class);
                    startActivity(intent);
                    finish();
                });
            }else if(thisRow.getOrderStatus().equals("4")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }
            }else if(thisRow.getOrderStatus().equals("5")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_done));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText("delete");
            }
            contactBtn.setOnClickListener(click->{
                Intent callPhone=new Intent(Intent.ACTION_DIAL);
                Uri phoneNum=Uri.parse("tel:"+thisRow.getOrderPhone());
                callPhone.setData(phoneNum);
                startActivity(callPhone);
            });
            rowMessage.setText(getResources().getString(R.string.order_name_detail)+thisRow.getOrdername()+"\n"+getResources().getString(R.string.order_money_detail)+thisRow.getOrderprice()+"\n"+getResources().getString(R.string.order_paid_detail)+thisRow.getOrderpaid()+"\n"+getResources().getString(R.string.order_place_detail)+thisRow.getOrderplace()+"\n"+getResources().getString(R.string.order_phone_detail)+thisRow.getOrderPhone()+"\n"+getResources().getString(R.string.order_start_detail)+thisRow.getOrderStart()+"\n"+getResources().getString(R.string.order_end_detail)+thisRow.getOrderDate());
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            return rowView;
        }


    }

    protected class WorkingInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return workingOrderArrayList.size();
        }

        @Override
        public Order getItem(int i) {
            return workingOrderArrayList.get(i);
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
            isOD=checkOD(date1,date2);
            Log.e("isod",String.valueOf(isOD));
            if(thisRow.getOrderStatus().equals("2")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }
                if(!isAlreadyOnWork){
                    orderBtn.setOnClickListener(click->{
                        WorkOnOrderTask workOnOrderTask=new WorkOnOrderTask();
                        workOnOrderTask.execute(String.valueOf(thisRow.getId()));
                    });
                }else{
                    orderBtn.setOnClickListener(click->{
                        Toast.makeText(MyOrderActivity.this,"you already have an on work order",Toast.LENGTH_LONG).show();
                    });
                }

            }else if(thisRow.getOrderStatus().equals("3")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }
                orderBtn.setOnClickListener(click->{
                    Intent intent=new Intent(MyOrderActivity.this,NowOrderActivity.class);
                    startActivity(intent);
                    finish();
                });
            }else if(thisRow.getOrderStatus().equals("4")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }
            }else if(thisRow.getOrderStatus().equals("5")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_done));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText("delete");
            }
            contactBtn.setOnClickListener(click->{
                Intent callPhone=new Intent(Intent.ACTION_DIAL);
                Uri phoneNum=Uri.parse("tel:"+thisRow.getOrderPhone());
                callPhone.setData(phoneNum);
                startActivity(callPhone);
            });
            rowMessage.setText(getResources().getString(R.string.order_name_detail)+thisRow.getOrdername()+"\n"+getResources().getString(R.string.order_money_detail)+thisRow.getOrderprice()+"\n"+getResources().getString(R.string.order_paid_detail)+thisRow.getOrderpaid()+"\n"+getResources().getString(R.string.order_place_detail)+thisRow.getOrderplace()+"\n"+getResources().getString(R.string.order_phone_detail)+thisRow.getOrderPhone()+"\n"+getResources().getString(R.string.order_start_detail)+thisRow.getOrderStart()+"\n"+getResources().getString(R.string.order_end_detail)+thisRow.getOrderDate());
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            return rowView;
        }


    }

    protected class OutODInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return outODOrderArrayList.size();
        }

        @Override
        public Order getItem(int i) {
            return outODOrderArrayList.get(i);
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
            isOD=checkOD(date1,date2);
            Log.e("isod",String.valueOf(isOD));
            if(thisRow.getOrderStatus().equals("2")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_work));
                }
                if(!isAlreadyOnWork){
                    orderBtn.setOnClickListener(click->{
                        WorkOnOrderTask workOnOrderTask=new WorkOnOrderTask();
                        workOnOrderTask.execute(String.valueOf(thisRow.getId()));
                    });
                }else{
                    orderBtn.setOnClickListener(click->{
                        Toast.makeText(MyOrderActivity.this,"you already have an on work order",Toast.LENGTH_LONG).show();
                    });
                }

            }else if(thisRow.getOrderStatus().equals("3")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.on_work_btn));
                }
                orderBtn.setOnClickListener(click->{
                    Intent intent=new Intent(MyOrderActivity.this,NowOrderActivity.class);
                    startActivity(intent);
                    finish();
                });
            }else if(thisRow.getOrderStatus().equals("4")){
                if(isOD){
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse)+getResources().getString(R.string.status_outOfDate));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }else{
                    orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse));
                    orderStatus.setTextColor(Color.BLACK);
                    orderBtn.setText(getResources().getString(R.string.status_accept));
                }
            }else if(thisRow.getOrderStatus().equals("5")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_done));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText("delete");
            }
            contactBtn.setOnClickListener(click->{
                Intent callPhone=new Intent(Intent.ACTION_DIAL);
                Uri phoneNum=Uri.parse("tel:"+thisRow.getOrderPhone());
                callPhone.setData(phoneNum);
                startActivity(callPhone);
            });
            rowMessage.setText(getResources().getString(R.string.order_name_detail)+thisRow.getOrdername()+"\n"+getResources().getString(R.string.order_money_detail)+thisRow.getOrderprice()+"\n"+getResources().getString(R.string.order_paid_detail)+thisRow.getOrderpaid()+"\n"+getResources().getString(R.string.order_place_detail)+thisRow.getOrderplace()+"\n"+getResources().getString(R.string.order_phone_detail)+thisRow.getOrderPhone()+"\n"+getResources().getString(R.string.order_start_detail)+thisRow.getOrderStart()+"\n"+getResources().getString(R.string.order_end_detail)+thisRow.getOrderDate());
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

    private Long TimeDiffByMinute(String endTime){
        //获取结束的时间戳
        //long expirationTime = data.getExpirationTime();
        //获得当前时间戳
        Long min = Long.parseLong("0");
        long timeStamp = System.currentTimeMillis();
        //格式
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //转换为String类型
        //String endDate = formatter.format(endTime);//结束的时间戳
        String startDate = formatter.format(timeStamp);//开始的时间戳
        // 获取服务器返回的时间戳 转换成"yyyy-MM-dd HH:mm:ss"
        // 计算的时间差
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d1 = df.parse(endTime);//后的时间
            Date d2 = df.parse(startDate); //前的时间
            Long diff = d1.getTime() - d2.getTime(); //两时间差，精确到毫秒
            Long day = diff / (1000 * 60 * 60 * 24); //以天数为单位取整
            Long hour=(diff/(60*60*1000)-day*24); //以小时为单位取整
            min=((diff/(60*1000))-day*24*60-hour*60); //以分钟为单位取整
            Long second=(diff/1000-day*24*60*60-hour*60*60-min*60);//秒
            //Log.e("tag","day =" +day);
            //Log.e("tag","hour =" +hour);
            //Log.e("tag","min =" +min);
            //Log.e("tag","second =" +second);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return min;
    }
}
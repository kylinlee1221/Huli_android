package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import cn.kylin.huli.model.Order;

public class OrderManageActivity extends AppCompatActivity {
    private ArrayList<Order> orderList=new ArrayList<Order>();
    private infoAdapter adapter;
    private ListView infoList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int day,month,year,hour,minute,second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_manage);
        infoList=findViewById(R.id.LV_orderList_OManage);
        swipeRefreshLayout=findViewById(R.id.SW_OrderManage);
        swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        GetOrderListTask getOrderListTask=new GetOrderListTask();
        getDateTime();
        getOrderListTask.execute("gogogo");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetOrderListTask getOrderListByIdTask1=new GetOrderListTask();
                getOrderListByIdTask1.execute("gogogo");
            }
        });

    }

    public class ModifyOrderTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/orderManage.php?type=update&orderid={0}&orderprice={1}&orderpaid={2}&orderend={3}&orderplace={4}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params = strings[0].split("/");
            String orderid=params[0];
            String orderprice=params[1];
            String orderpaid=params[2];
            String orderend=params[3];
            String orderplace=params[4];
            String url = MessageFormat.format(mUrl,orderid,orderprice,orderpaid,orderend,orderplace);
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
            if(!result.equals("error")&&!result.equals("timeout")&&!result.equals("[]")){
                //JSONArray jsonArray = null;
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    if(jsonObject.getString("status").equals("1")){
                        Toast.makeText(OrderManageActivity.this,jsonObject.getString("msgs"),Toast.LENGTH_LONG).show();
                        GetOrderListTask getOrderListTask=new GetOrderListTask();
                        getOrderListTask.execute("gogogo");
                    }else{
                        Toast.makeText(OrderManageActivity.this,"update failed",Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(OrderManageActivity.this,"connection error",Toast.LENGTH_LONG).show();
            }
        }
    }

    public class DeleteOrderTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/orderManage.php?type=delete&orderid={0}";

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
            super.onPostExecute(result);
            //orderList.clear();
            if(!result.equals("error")&&!result.equals("timeout")&&!result.equals("[]")){
                //JSONArray jsonArray = null;
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    if(jsonObject.getString("status").equals("1")){
                        Toast.makeText(OrderManageActivity.this,jsonObject.getString("msgs"),Toast.LENGTH_LONG).show();
                        GetOrderListTask getOrderListTask=new GetOrderListTask();
                        getOrderListTask.execute("gogogo");
                    }else{
                        Toast.makeText(OrderManageActivity.this,"delete failed",Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(OrderManageActivity.this,"connection error",Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetOrderListTask extends AsyncTask<String,Void,String>{
        private String mUrl="https://huli.kylin1221.com/apis/getOrderList.php?type=all";

        @Override
        protected String doInBackground(String... strings) {
            //String[] params = strings[0].split("/");
            //String userid=params[0];
            //String url = MessageFormat.format(mUrl,userid);
            StringBuffer buffer=new StringBuffer();
            Log.e("url", mUrl);
            try{
                URL url1=new URL(mUrl);
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
                        Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone);
                        orderList.add(tmpOrder);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(orderList.size()!=0){
                    adapter=new infoAdapter();
                    infoList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
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
                    String orderFrom=jsonObject.getString("orderfrom");
                    int status=Integer.parseInt(orderStatus);
                    Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone,orderFrom);
                    orderList.add(tmpOrder);
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
    protected class infoAdapter extends BaseAdapter {

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
            contactBtn.setText(getResources().getString(R.string.edit_btn));
            if(thisRow.getOrderStatus().equals("2")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.delete_btn));
            }else if(thisRow.getOrderStatus().equals("3")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.delete_btn));
            }else if(thisRow.getOrderStatus().equals("4")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.delete_btn));
            }else if(thisRow.getOrderStatus().equals("5")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_done));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.delete_btn));
            }else if(thisRow.getOrderStatus().equals("1")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_available));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.delete_btn));
            }
            orderBtn.setOnClickListener(click->{
                String params=String.valueOf(thisRow.getId());
                AlertDialog.Builder deleteAlert=new AlertDialog.Builder(OrderManageActivity.this);
                deleteAlert.setTitle(getResources().getString(R.string.delete_confirm_hint));
                deleteAlert.setPositiveButton(getResources().getString(R.string.yes_btn),(click1,arg1)->{
                    DeleteOrderTask deleteOrderTask=new DeleteOrderTask();
                    deleteOrderTask.execute(params);
                }).setNegativeButton(getResources().getString(R.string.no_btn),(click1,arg1)->{

                }).create().show();
            });
            contactBtn.setOnClickListener(click->{
                View updateView=View.inflate(OrderManageActivity.this,R.layout.edit_order_view,null);
                EditText orderMoneyET=updateView.findViewById(R.id.ET_money_Edit),alreadyPaidET=updateView.findViewById(R.id.ET_alreadyPay_Edit),placeET=updateView.findViewById(R.id.ET_place_Edit);
                Button dateChoser=updateView.findViewById(R.id.BT_DateChooser_Edit),timeChoser=updateView.findViewById(R.id.BT_TimeChooser_Edit);
                orderMoneyET.setText(String.valueOf(thisRow.getOrderprice()));
                alreadyPaidET.setText(String.valueOf(thisRow.getOrderpaid()));
                placeET.setText(thisRow.getOrderplace());
                AlertDialog.Builder updateBuilder=new AlertDialog.Builder(OrderManageActivity.this);
                dateChoser.setOnClickListener(click2->{
                    final String[] timeChosed = {""};
                    DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            dateChoser.setText(String.valueOf(i)+"-"+String.valueOf((++i1))+"-"+String.valueOf(i2));
                        }
                    };

                    DatePickerDialog datePickerDialog=new DatePickerDialog(OrderManageActivity.this,0,listener,year,month,day);
                    datePickerDialog.setCancelable(false);
                    datePickerDialog.show();
                    //Log.e("time",timeChosed[0]);
                    //dateChoser.setText(timeChosed[0]);
                });
                timeChoser.setOnClickListener(click2->{
                    final String[] timeChosed = {""};
                    TimePickerDialog.OnTimeSetListener timeListener=new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                            timeChoser.setText(String.valueOf(i)+":"+String.valueOf((i1)));
                        }
                    };
                    TimePickerDialog timePickerDialog=new TimePickerDialog(OrderManageActivity.this,timeListener,hour,minute,true);
                    timePickerDialog.setCancelable(false);
                    timePickerDialog.show();
                });
                updateBuilder.setView(updateView);
                updateBuilder.setTitle("Modify order");
                updateBuilder.setPositiveButton("Modify",(click1,arg1)->{
                    String originDateBtn=getResources().getString(R.string.date_chose_btn)+" "+getResources().getString(R.string.time_chose_btn);
                    originDateBtn=originDateBtn.toLowerCase(Locale.ROOT);
                    String moneyST=orderMoneyET.getText().toString(),paidST=alreadyPaidET.getText().toString(),dateST=dateChoser.getText().toString(),timeST=timeChoser.getText().toString(),placeST=placeET.getText().toString();
                    dateST=dateST.toLowerCase(Locale.ROOT);
                    timeST=timeST.toLowerCase(Locale.ROOT);
                    Log.e("dateST",dateST);
                    Log.e("timeST",timeST);
                    //Log.e("date+timest",dateST+" "+timeST);
                    //Log.e("originDate",getResources().getString(R.string.date_chose_btn));
                    //Log.e("compare Date",String.valueOf(getResources().getString(R.string.date_chose_btn).equals(dateST)));
                    if(originDateBtn.equals(dateST+" "+timeST)||dateST.equals(getResources().getString(R.string.date_chose_btn).toLowerCase(Locale.ROOT))||timeST.equals(getResources().getString(R.string.time_chose_btn).toLowerCase(Locale.ROOT))||dateST.equals("")||timeST.equals("")){
                        Toast.makeText(OrderManageActivity.this,getResources().getText(R.string.chose_date_error),Toast.LENGTH_LONG).show();
                    }else{
                        String params=String.valueOf(thisRow.getId())+"/"+moneyST+"/"+paidST+"/"+dateST+" "+timeST+"/"+placeST;
                        ModifyOrderTask modifyOrderTask=new ModifyOrderTask();
                        modifyOrderTask.execute(params);
                    }
                }).create().show();
            });
            rowMessage.setText(getResources().getString(R.string.order_name_detail)+thisRow.getOrdername()+"\n"+getResources().getString(R.string.order_from_detail)+thisRow.getOrderFrom()+"\n"+getResources().getString(R.string.order_money_detail)+thisRow.getOrderprice()+"\n"+getResources().getString(R.string.order_paid_detail)+thisRow.getOrderpaid()+"\n"+getResources().getString(R.string.order_place_detail)+thisRow.getOrderplace()+"\n"+getResources().getString(R.string.order_phone_detail)+thisRow.getOrderPhone()+"\n"+getResources().getString(R.string.order_start_detail)+thisRow.getOrderStart()+"\n"+getResources().getString(R.string.order_end_detail)+thisRow.getOrderDate());
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            return rowView;
        }
    }
}
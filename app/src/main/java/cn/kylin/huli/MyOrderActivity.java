package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import cn.kylin.huli.model.Order;

public class MyOrderActivity extends AppCompatActivity {
    private ArrayList<Order> orderArrayList=new ArrayList<Order>();
    private ArrayList<Order> workingOrderArrayList=new ArrayList<Order>();
    private ArrayList<Order> doneOrderArrayList=new ArrayList<Order>();
    private ArrayList<Order> outODOrderArrayList=new ArrayList<Order>();
    private infoAdapter adapter;
    private ListView orderList;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        Spinner OrderFilter=findViewById(R.id.SP_orderFilter_MyOrder);
        orderList=findViewById(R.id.LV_orderList_MyOrder);
        swipeRefreshLayout=findViewById(R.id.SW_MyOrder);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        Long userId=sp.getLong("id",-1);
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
                addToInfo(buffer.toString());
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
            if(result.equals("timeout")||result.equals("error")){
                Toast.makeText(MyOrderActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
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
                            int status=Integer.parseInt(orderStatus);
                            if(status>1) {
                                Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone);
                                orderArrayList.add(tmpOrder);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (orderArrayList.size() != 0) {
                    adapter = new infoAdapter();
                    orderList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class MarkOrderCompleteTask extends AsyncTask<String,Void,String>{

        private String mUrl="https://huli.kylin1221.com/apis/completeOrder.php?orderid={0}";

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
                addToInfo(buffer.toString());
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
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
                addToInfo(buffer.toString());
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
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
                    int status=Integer.parseInt(orderStatus);
                    if(status>1) {
                        Order tmpOrder = new Order(id, ordername, orderplace, orderend, orderStart, orderprice, orderpaid, orderStatus,cusPhone);
                        orderArrayList.add(tmpOrder);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
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
            if(thisRow.getOrderStatus().equals("2")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_accept));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.status_work));

            }else if(thisRow.getOrderStatus().equals("3")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_work));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.status_done));
            }else if(thisRow.getOrderStatus().equals("4")){
                orderStatus.setText(getResources().getString(R.string.order_status_detail)+getResources().getString(R.string.status_refuse));
                orderStatus.setTextColor(Color.BLACK);
                orderBtn.setText(getResources().getString(R.string.status_accept));
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
}
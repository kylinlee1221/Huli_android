package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.kylin.huli.model.Order;

public class OrderMarketActivity extends AppCompatActivity {
    private ArrayList<Order> orderList=new ArrayList<Order>();
    private infoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_market);
        SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
        Long userid=sp.getLong("id",-1);
        Log.e("uid",String.valueOf(userid));
        ListView infoList=findViewById(R.id.LV_orderList_Market);
        GetOrderListByIdTask getOrderListByIdTask=new GetOrderListByIdTask();
        getOrderListByIdTask.execute(String.valueOf(userid));
        try {
            if(!getOrderListByIdTask.get().equals("error")||getOrderListByIdTask.get().equals("timeout")){
                addToInfo(getOrderListByIdTask.get());
                if(orderList.size()!=0){
                    adapter=new infoAdapter();
                    infoList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                //addToInfo(buffer.toString());
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
            //return null;
        }
    }
    private void addToInfo(String result){
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
                    Order tmpOrder=new Order(id,ordername,orderplace,orderend,orderprice,orderpaid);
                    orderList.add(tmpOrder);
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

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater=getLayoutInflater();
            View rowView;
            TextView rowMessage;
            Order thisRow=getItem(i);
            rowView=inflater.inflate(R.layout.show_list_resource,viewGroup,false);
            rowMessage=rowView.findViewById(R.id.info_text_view);
            rowMessage.setText(thisRow.getOrdername()+"\n"+thisRow.getOrderprice()+"\n"+thisRow.getOrderpaid()+"\n"+thisRow.getOrderplace()+"\n"+thisRow.getOrderDate());
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            return rowView;
        }
    }
}
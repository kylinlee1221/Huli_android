package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.concurrent.ExecutionException;

import cn.kylin.huli.model.Announcement;
import cn.kylin.huli.model.Order;

public class AnnounceManageActivity extends AppCompatActivity {

    private ArrayList<Announcement> announcementList=new ArrayList<Announcement>();
    private int day,month,year,hour,minute,second;
    private infoAdapter adapter;
    private ListView anList;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announce_manage);
        TextView hintTV=findViewById(R.id.TV_Hint_AManage);
        anList=findViewById(R.id.LV_AList_Amanage);
        swipeRefreshLayout=findViewById(R.id.swiperefresh);
        Button addButton=findViewById(R.id.BT_addAnnouncement_Manage);
        getDateTime();
        GetAnnouncementTask getAnnouncementTask=new GetAnnouncementTask();
        getAnnouncementTask.execute("gogogo");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetAnnouncementTask getIn=new GetAnnouncementTask();
                getIn.execute("gogogo");
            }

        });
        anList.setOnItemLongClickListener((p,b,pos,id)->{
            Announcement tmpA=announcementList.get(pos);
            View tmpView=View.inflate(AnnounceManageActivity.this,R.layout.show_announce_details_view,null);
            TextView infoTV=tmpView.findViewById(R.id.TV_anInfo_Details),timeTV=tmpView.findViewById(R.id.TV_anEndtime_Details);
            AlertDialog.Builder builder=new AlertDialog.Builder(AnnounceManageActivity.this);
            infoTV.setText(tmpA.getInfo());
            timeTV.setText(tmpA.getEndtime());
            builder.setTitle("Modify or delete");
            builder.setView(tmpView);
            builder.setPositiveButton("Modify",(click,arg)->{
                View updateView=View.inflate(AnnounceManageActivity.this,R.layout.edit_announce_view,null);
                EditText updateInfo=updateView.findViewById(R.id.ET_AnInfo_Edit);
                AlertDialog.Builder updateBuilder=new AlertDialog.Builder(AnnounceManageActivity.this);
                updateInfo.setText(tmpA.getInfo());
                updateBuilder.setView(updateView);
                updateBuilder.setPositiveButton("update",(click1,arg1)->{
                    String newInfo=updateInfo.getText().toString();
                    String params="update/"+String.valueOf(tmpA.getId())+"/"+newInfo;
                    UpdateAnnounceTask updateAnnounceTask=new UpdateAnnounceTask();
                    updateAnnounceTask.execute(params);
                    GetAnnouncementTask getInDelete=new GetAnnouncementTask();
                    getInDelete.execute("gogogo");
                }).create().show();
            }).setNegativeButton("Delete",(click,arg)->{
                DeleteAnnouncementTask deleteAnnouncementTask=new DeleteAnnouncementTask();
                String params="delete/"+String.valueOf(tmpA.getId());
                deleteAnnouncementTask.execute(params);
                GetAnnouncementTask getInDelete=new GetAnnouncementTask();
                getInDelete.execute("gogogo");
            }).create().show();
            return true;
        });
        addButton.setOnClickListener(click->{
            Intent intent=new Intent(AnnounceManageActivity.this,CreateAnnouncementActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public class GetAnnouncementTask extends AsyncTask<String,Void,String> {

        private String url="https://huli.kylin1221.com/apis/getAnnouncement.php";

        @Override
        protected String doInBackground(String... strings) {
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
                //insertIntoData(buffer.toString());
            }catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());
            return buffer.toString();
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result.equals("timeout")||result.equals("error")){
                Toast.makeText(AnnounceManageActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                announcementList.clear();
                try{
                    JSONArray jsonArray=new JSONArray(result);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        Long id=jsonObject.getLong("id");
                        Long sendby=jsonObject.getLong("sendby");
                        String info=jsonObject.getString("info");
                        String endtime=jsonObject.getString("endtime");
                        announcementList.add(new Announcement(id,sendby,info,endtime));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(announcementList.size()!=0){
                    adapter=new infoAdapter();
                    anList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void insertIntoData(String result){
        announcementList.clear();
        try{
            JSONArray jsonArray=new JSONArray(result);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                Long id=jsonObject.getLong("id");
                Long sendby=jsonObject.getLong("sendby");
                String info=jsonObject.getString("info");
                String endtime=jsonObject.getString("endtime");
                announcementList.add(new Announcement(id,sendby,info,endtime));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class UpdateAnnounceTask extends AsyncTask<String,Void,String>{

        private String mUrl="https://huli.kylin1221.com/apis/manageAnnouncement.php?type={0}&anid={1}&anin={2}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            String type=params[0];
            String anid=params[1];
            String anin=params[2];
            String url= MessageFormat.format(mUrl,type,anid,anin);
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
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result.trim().equals("timeout")||result.trim().equals("error")){
                Toast.makeText(AnnounceManageActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                announcementList.clear();
                try{
                    JSONArray jsonArray=new JSONArray(result);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        Long id=jsonObject.getLong("id");
                        Long sendby=jsonObject.getLong("sendby");
                        String info=jsonObject.getString("info");
                        String endtime=jsonObject.getString("endtime");
                        announcementList.add(new Announcement(id,sendby,info,endtime));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(announcementList.size()!=0){
                    adapter=new infoAdapter();
                    anList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public class DeleteAnnouncementTask extends AsyncTask<String,Void,String>{

        private String mUrl="https://huli.kylin1221.com/apis/manageAnnouncement.php?type={0}&anid={1}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            String type=params[0];
            String anid=params[1];
            String url= MessageFormat.format(mUrl,type,anid);
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
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result.trim().equals("timeout")||result.trim().equals("error")){
                Toast.makeText(AnnounceManageActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                announcementList.clear();
                try{
                    JSONArray jsonArray=new JSONArray(result);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        Long id=jsonObject.getLong("id");
                        Long sendby=jsonObject.getLong("sendby");
                        String info=jsonObject.getString("info");
                        String endtime=jsonObject.getString("endtime");
                        announcementList.add(new Announcement(id,sendby,info,endtime));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(announcementList.size()!=0){
                    adapter=new infoAdapter();
                    anList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
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

    protected class infoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return announcementList.size();
        }

        @Override
        public Announcement getItem(int i) {
            return announcementList.get(i);
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
            TextView rowMessage;
            Announcement thisRow=getItem(i);
            rowView=inflater.inflate(R.layout.show_announce_view,viewGroup,false);
            rowMessage=rowView.findViewById(R.id.info_text_view2);
            rowMessage.setText(getResources().getString(R.string.announce_info_detail)+thisRow.getInfo()+"\n"+getResources().getString(R.string.order_end_detail)+thisRow.getEndtime()+"\n");
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            return rowView;
        }
    }
}
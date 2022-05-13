package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announce_manage);
        TextView hintTV=findViewById(R.id.TV_Hint_AManage);
        ListView anList=findViewById(R.id.LV_AList_Amanage);
        SwipeRefreshLayout swipeRefreshLayout=findViewById(R.id.swiperefresh);
        getDateTime();
        GetAnnouncementTask getAnnouncementTask=new GetAnnouncementTask();
        getAnnouncementTask.execute("gogogo");
        String getResult="";
        try {
            getResult=getAnnouncementTask.get();
            if(getResult.trim().equals("timeout")||getResult.trim().equals("error")){
                Toast.makeText(this,"get error",Toast.LENGTH_LONG).show();
            }else{
                if(announcementList.size()!=0){
                    adapter=new infoAdapter();
                    anList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetAnnouncementTask getIn=new GetAnnouncementTask();
                String resIn="";
                getIn.execute("gogogo");
                try {
                    resIn=getIn.get();
                    if(resIn.trim().equals("timeout")||resIn.trim().equals("error")){
                        Toast.makeText(AnnounceManageActivity.this,"get error",Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }else{
                        if(announcementList.size()!=0){
                            adapter=new infoAdapter();
                            anList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                        }

                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);
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
                    String updateRes="",getResultIn="";
                    try{
                        updateRes=updateAnnounceTask.get();
                        Log.e("update res",updateRes);
                        if(updateRes.trim().equals("timeout")||updateRes.trim().equals("error")){
                            Toast.makeText(AnnounceManageActivity.this,updateRes,Toast.LENGTH_LONG).show();
                        }else{
                            GetAnnouncementTask getInDelete=new GetAnnouncementTask();
                            getInDelete.execute("gogogo");
                            getResultIn=getInDelete.get();
                            if(getResultIn.trim().equals("timeout")||getResultIn.trim().equals("error")){
                                Toast.makeText(this,"get error",Toast.LENGTH_LONG).show();
                            }else{
                                if(announcementList.size()!=0){
                                    adapter=new infoAdapter();
                                    anList.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).create().show();
            }).setNegativeButton("Delete",(click,arg)->{
                DeleteAnnouncementTask deleteAnnouncementTask=new DeleteAnnouncementTask();
                String params="delete/"+String.valueOf(tmpA.getId());
                deleteAnnouncementTask.execute(params);
                String deleteRes="",getResultIn="";
                try {
                    deleteRes=deleteAnnouncementTask.get();
                    Log.e("delete res",deleteRes);
                    if(deleteRes.trim().equals("timeout")||deleteRes.trim().equals("error")){
                        Toast.makeText(AnnounceManageActivity.this,deleteRes,Toast.LENGTH_LONG).show();
                    }else{
                        GetAnnouncementTask getInDelete=new GetAnnouncementTask();
                        getInDelete.execute("gogogo");
                        getResultIn=getInDelete.get();
                        if(getResultIn.trim().equals("timeout")||getResultIn.trim().equals("error")){
                            Toast.makeText(this,"get error",Toast.LENGTH_LONG).show();
                        }else{
                            if(announcementList.size()!=0){
                                adapter=new infoAdapter();
                                anList.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    //getAnnouncementTask.execute("gogogo");

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).create().show();
            return true;
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
                insertIntoData(buffer.toString());
            }catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());
            return buffer.toString();
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
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
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
            }catch (Exception e){
                e.printStackTrace();
                return "error";
            }
            Log.e("result",buffer.toString());

            return buffer.toString();
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

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater=getLayoutInflater();
            View rowView;
            TextView rowMessage;
            Announcement thisRow=getItem(i);
            rowView=inflater.inflate(R.layout.show_list_resource,viewGroup,false);
            rowMessage=rowView.findViewById(R.id.info_text_view);
            rowMessage.setText(thisRow.getInfo()+"\n"+thisRow.getEndtime()+"\n");
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            return rowView;
        }
    }
}
package cn.kylin.huli;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

            }).setNegativeButton("Delete",(click,arg)->{

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
            if(result.equals("timeout")||result.equals("error")||result.equals("[]")){
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

        private String mUrl="https://huli.kylin1221.com/apis/manageAnnouncement.php?type={0}&anid={1}&anin={2}&endtime={3}";

        @Override
        protected String doInBackground(String... strings) {
            String[] params=strings[0].split("/");
            String type=params[0];
            String anid=params[1];
            String anin=params[2];
            String endtime=params[3];
            String url= MessageFormat.format(mUrl,type,anid,anin,endtime);
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
            if(result.trim().equals("timeout")||result.trim().equals("error")||result.trim().equals("[]")){
                Toast.makeText(AnnounceManageActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                try{
                    JSONObject jsonObject=new JSONObject(result);
                    String status=jsonObject.getString("status");
                    String msgs=jsonObject.getString("msgs");
                    if(status.equals("1")){
                        Toast.makeText(AnnounceManageActivity.this,msgs,Toast.LENGTH_LONG).show();
                        GetAnnouncementTask getAnnouncementTask=new GetAnnouncementTask();
                        getAnnouncementTask.execute("gogogo");
                    }else{
                        Toast.makeText(AnnounceManageActivity.this,msgs,Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
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
            if(result.trim().equals("timeout")||result.trim().equals("error")||result.trim().equals("[]")){
                Toast.makeText(AnnounceManageActivity.this,result,Toast.LENGTH_LONG).show();
            }else{
                try{
                    JSONObject jsonObject=new JSONObject(result);
                    String status=jsonObject.getString("status");
                    String msgs=jsonObject.getString("msgs");
                    if(status.equals("1")){
                        Toast.makeText(AnnounceManageActivity.this,msgs,Toast.LENGTH_LONG).show();
                        GetAnnouncementTask getAnnouncementTask=new GetAnnouncementTask();
                        getAnnouncementTask.execute("gogogo");
                    }else{
                        Toast.makeText(AnnounceManageActivity.this,msgs,Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
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
            TextView rowMessage,endTime;
            Button deleteBtn,editBtn;
            Announcement thisRow=getItem(i);
            rowView=inflater.inflate(R.layout.show_announce_view,viewGroup,false);
            rowMessage=rowView.findViewById(R.id.info_text_view2);
            endTime=rowView.findViewById(R.id.info_status_view2);
            deleteBtn=rowView.findViewById(R.id.info_delete_button);
            editBtn=rowView.findViewById(R.id.info_edit_button);
            rowMessage.setText(getResources().getString(R.string.announce_info_detail)+thisRow.getInfo());
            endTime.setText(getResources().getString(R.string.order_end_detail)+thisRow.getEndtime());
            deleteBtn.setOnClickListener(click->{
                AlertDialog.Builder deleteAlert=new AlertDialog.Builder(AnnounceManageActivity.this);
                deleteAlert.setTitle(getResources().getString(R.string.delete_confirm_hint));
                deleteAlert.setPositiveButton(getResources().getString(R.string.yes_btn),(click1,arg1)->{
                    DeleteAnnouncementTask deleteAnnouncementTask=new DeleteAnnouncementTask();
                    String params="delete/"+String.valueOf(thisRow.getId());
                    deleteAnnouncementTask.execute(params);
                    GetAnnouncementTask getInDelete=new GetAnnouncementTask();
                    getInDelete.execute("gogogo");
                }).setNegativeButton(getResources().getString(R.string.no_btn),(click1,arg1)->{

                }).create().show();

            });
            editBtn.setOnClickListener(click->{
                View updateView=View.inflate(AnnounceManageActivity.this,R.layout.edit_announce_view,null);
                EditText updateInfo=updateView.findViewById(R.id.ET_AnInfo_Edit);
                Button dateChoser=updateView.findViewById(R.id.BT_DateChooser_Edit),timeChoser=updateView.findViewById(R.id.BT_TimeChooser_Edit);
                dateChoser.setOnClickListener(click1->{
                    final String[] timeChosed = {""};
                    DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            dateChoser.setText(String.valueOf(i)+"-"+String.valueOf((i1))+"-"+String.valueOf(i2));
                        }
                    };

                    DatePickerDialog datePickerDialog=new DatePickerDialog(AnnounceManageActivity.this,0,listener,year,month,day);
                    datePickerDialog.show();

                    Log.e("time",timeChosed[0]);
                    dateChoser.setText(timeChosed[0]);
                });
                timeChoser.setOnClickListener(click1->{
                    final String[] timeChosed = {""};
                    TimePickerDialog.OnTimeSetListener timeListener=new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                            timeChoser.setText(String.valueOf(i)+":"+String.valueOf((i1)));
                        }
                    };
                    TimePickerDialog timePickerDialog=new TimePickerDialog(AnnounceManageActivity.this,timeListener,hour,minute,true);
                    timePickerDialog.show();
                });
                AlertDialog.Builder updateBuilder=new AlertDialog.Builder(AnnounceManageActivity.this);
                updateInfo.setText(thisRow.getInfo());
                updateBuilder.setView(updateView);
                updateBuilder.setPositiveButton("update",(click1,arg1)->{
                    String newInfo=updateInfo.getText().toString();
                    String time=dateChoser.getText().toString()+" "+timeChoser.getText().toString();
                    String params="update/"+String.valueOf(thisRow.getId())+"/"+newInfo+"/"+time;
                    UpdateAnnounceTask updateAnnounceTask=new UpdateAnnounceTask();
                    updateAnnounceTask.execute(params);
                }).create().show();
            });
            rowMessage.setTextColor(Color.BLACK);
            rowMessage.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            return rowView;
        }
    }
}
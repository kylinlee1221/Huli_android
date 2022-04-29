package cn.kylin.huli;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateAnnouncementActivity extends AppCompatActivity {
    TimePicker pvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_annoucement);
        Button timePicker=findViewById(R.id.BT_TimeChooser_Announ);
    }
}
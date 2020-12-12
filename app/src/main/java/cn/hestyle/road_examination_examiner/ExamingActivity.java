package cn.hestyle.road_examination_examiner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.hestyle.road_examination_examiner.entity.Candidate;
import cn.hestyle.road_examination_examiner.entity.Exam;
import cn.hestyle.road_examination_examiner.entity.ExamItem;
import cn.hestyle.road_examination_examiner.entity.ExamTemplate;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;

public class ExamingActivity extends AppCompatActivity {
    private Exam exam;
    private Candidate candidate;
    private ExamTemplate lightExamTemplate;
    private ExamTemplate roadExamTemplate;

    private ImageView candidatePhotoImageView;
    private TextView candidateNameTextView;
    private TextView candidateGenderTextView;
    private TextView candidateIdTextView;
    private TextView candidatePhoneNumberTextView;
    private TextView candidateDriverSchoolTextView;

    private ListView examItemListView;
    private ExamItemAdapter roadExamItemAdapter;

    private List<ExamItem> lightExamItemList = new ArrayList<>();
    private List<ExamItem> roadExamItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examing);

        Intent intent = getIntent();
        exam = (Exam) intent.getSerializableExtra("exam");
        candidate = (Candidate) intent.getSerializableExtra("candidate");
        lightExamTemplate = (ExamTemplate) intent.getSerializableExtra("lightExamTemplate");
        roadExamTemplate = (ExamTemplate) intent.getSerializableExtra("roadExamTemplate");

        candidatePhotoImageView = findViewById(R.id.candidatePhotoImageView);
        candidateNameTextView = findViewById(R.id.candidateNameTextView);
        candidateGenderTextView = findViewById(R.id.candidateGenderTextView);
        candidateIdTextView = findViewById(R.id.candidateIdTextView);
        candidatePhoneNumberTextView = findViewById(R.id.candidatePhoneNumberTextView);
        candidateDriverSchoolTextView = findViewById(R.id.candidateDriverSchoolTextView);

        examItemListView = findViewById(R.id.examItemListView);
        roadExamItemAdapter = new ExamItemAdapter();
        examItemListView.setAdapter(roadExamItemAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (candidate != null) {
            candidateNameTextView.setText(candidate.getName() +"");
            candidateIdTextView.setText(candidate.getId() + "");
            candidateGenderTextView.setText(candidate.getGender() + "");
            candidatePhoneNumberTextView.setText(candidate.getPhoneNumber() + "");
            candidateDriverSchoolTextView.setText(candidate.getDriverSchool() + "");
            // 加载网络图片
            Glide.with(ExamingActivity.this).load("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + candidate.getPhotoPath()).into(candidatePhotoImageView);
        } else {
            Toast.makeText(ExamingActivity.this, "考生信息查询失败！", Toast.LENGTH_LONG).show();
            return;
        }
        // 查找灯光考试的所有examItem
        if (lightExamTemplate != null) {
            lightExamItemList.clear();
            lightExamItemList.addAll(lightExamTemplate.getExamItemList());
        }
        // 查找道路考试的所有examItem
        if (roadExamTemplate != null) {
            roadExamItemList.clear();
            roadExamItemList.addAll(roadExamTemplate.getExamItemList());
            roadExamItemAdapter.notifyDataSetChanged();
        }
    }

    //自定义适配器
    class ExamItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return roadExamItemList.size();
        }
        @Override
        public Object getItem(int position) {
            return roadExamItemList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            //通过下面的条件判断语句，来循环利用。如果convertView = null ，表示屏幕上没有可以被重复利用的对象。
            if(convertView == null ){
                //创建View
                convertView = getLayoutInflater().inflate(R.layout.content_exam_list_item, null);
                viewHolder.examItemButton = convertView.findViewById(R.id.examItemButton);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ExamItem examItem = roadExamItemList.get(position);
            viewHolder.examItemButton.setText(examItem.getName());
            return convertView;
        }
    }

    static class ViewHolder{
        public Button examItemButton;
    }
}
package cn.hestyle.road_examination_examiner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.hestyle.road_examination_examiner.entity.Candidate;
import cn.hestyle.road_examination_examiner.entity.Car;
import cn.hestyle.road_examination_examiner.entity.Exam;
import cn.hestyle.road_examination_examiner.entity.ExamItem;
import cn.hestyle.road_examination_examiner.entity.ExamTemplate;
import cn.hestyle.road_examination_examiner.entity.ExamUpdateUiBroadcastMessage;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import cn.hestyle.road_examination_examiner.tcp.ExamItemProcess;
import cn.hestyle.road_examination_examiner.util.ExamUpdateUiBroadcastUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExamingActivity extends AppCompatActivity {
    private Car car;
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

    private Button settingExamCarButton;
    private Button startExamButton;
    private Button stopExamButton;

    private ListView examItemListView;
    private ExamItemAdapter roadExamItemAdapter;

    private List<ExamItem> lightExamItemList = new ArrayList<>();
    private List<ExamItem> roadExamItemList = new ArrayList<>();

    private ExamBroadcastReceiver examBroadcastReceiver = null;
    /** 其它子线程发送通知的上下文 */
    public static ExamingActivity examingActivity = null;

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

        settingExamCarButton = findViewById(R.id.settingExamCarButton);
        settingExamCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 配置车辆信息
                ExamItemProcess.checkCarInfo();
            }
        });
        // 默认开始考试、停止考试按钮都置灰，只有当配置完考试车辆后，再enable
        startExamButton = findViewById(R.id.startExamButton);
        startExamButton.setEnabled(false);
        startExamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开始考试
                ExamItemProcess.startExam();
            }
        });
        stopExamButton = findViewById(R.id.stopExamButton);
        stopExamButton.setEnabled(false);
        stopExamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 停止考试（弹出提示框）
                ExamItemProcess.stopExam();
            }
        });
        // 请求车辆信息，然后启动tcp服务
        if (exam != null && exam.getCarId() != null) {
            getCarFromNetwork();
        } else {
            settingExamCarButton.setEnabled(false);
            Toast.makeText(ExamingActivity.this, "考试异常，未设置考试车辆信息！", Toast.LENGTH_LONG).show();
        }
        // 注册通知
        examingActivity = this;
        examBroadcastReceiver = new ExamBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ExamUpdateUiBroadcastUtil.EXAM_UPDATE_UI_THREAD_ACTION_TYPE);
        registerReceiver(examBroadcastReceiver, intentFilter);
    }

    /**
     * 请求车辆信息
     */
    private void getCarFromNetwork() {
        // 请求车辆信息
        FormBody formBody = new FormBody.Builder()
                .add("carId", exam.getCarId() + "")
                .build();
        Request request = new Request.Builder()
                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/car/findByCarId.do")
                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("Car", "车辆 id = " + exam.getCarId() + "信息请求失败！");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 车辆请求失败
                        Toast.makeText(ExamingActivity.this, "车辆 id = " + exam.getCarId() + "车辆信息请求失败！", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new Gson();
                Type type =  new TypeToken<ResponseResult<Car>>(){}.getType();
                ResponseResult<Car> responseResult = gson.fromJson(responseString, type);
                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                    // 操作项获取失败
                    Log.e("Car", "车辆 id = " + exam.getCarId() + "信息请求失败！");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExamingActivity.this, "车辆 id = " + exam.getCarId() + "车辆信息请求失败！", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                car = responseResult.getData();
                ExamItemProcess.initExam(car);
            }
        });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (ExamItemProcess.isExaming) {
                // 如果正在考试中，则不退出
                return false;
            } else {
                ExamItemProcess.stopExam();
            }
        }
        return super.onKeyDown(keyCode, event);
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

    class ExamBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 取出intent中的ExamUpdateUiBroadcastMessage对象
            ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = (ExamUpdateUiBroadcastMessage) intent.getSerializableExtra(ExamUpdateUiBroadcastMessage.MESSAGE_NAME);
            // 根据type类型分别处理
            if (ExamUpdateUiBroadcastMessage.CAR_SETTING_RESULT.equals(examUpdateUiBroadcastMessage.getTypeName())) {
                // 车辆连接结果
                Boolean isSuccess = (Boolean) examUpdateUiBroadcastMessage.getData().get("isSuccess");
                if (isSuccess) {
                    // 车辆配置成功，车辆配置按钮失效，enable开始考试按钮考试
                    settingExamCarButton.setEnabled(false);
                    startExamButton.setEnabled(true);
                }
                Toast.makeText(context,examUpdateUiBroadcastMessage.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
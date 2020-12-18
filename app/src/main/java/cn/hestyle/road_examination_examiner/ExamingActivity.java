package cn.hestyle.road_examination_examiner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

    private Integer hadScore;
    private TextView hadScoreTextView;
    private TextView calculateScoreInfoTextView;

    private ScrollView contentScrollView;
    private ListView examItemListView;
    private ExamItemAdapter roadExamItemAdapter;

    private List<ExamItem> lightExamItemList = new ArrayList<>();
    private List<ExamItem> roadExamItemList = new ArrayList<>();
    // 标记道路考试项是否已点考
    private List<Boolean> roadExamItemHadExamedList = new ArrayList<>();

    private ExamBroadcastReceiver examBroadcastReceiver = null;
    /** 其它子线程发送通知的上下文 */
    public static ExamingActivity examingActivity = null;

    /** 考试计时器相关 */
    private long examStartTime = 0;
    private TimerTask calExamTimerTask;
    private TextView examTimingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examing);

        Intent intent = getIntent();
        exam = (Exam) intent.getSerializableExtra("exam");
        candidate = (Candidate) intent.getSerializableExtra("candidate");
        lightExamTemplate = (ExamTemplate) intent.getSerializableExtra("lightExamTemplate");
        roadExamTemplate = (ExamTemplate) intent.getSerializableExtra("roadExamTemplate");
        roadExamItemHadExamedList = new ArrayList<>();

        candidatePhotoImageView = findViewById(R.id.candidatePhotoImageView);
        candidateNameTextView = findViewById(R.id.candidateNameTextView);
        candidateGenderTextView = findViewById(R.id.candidateGenderTextView);
        candidateIdTextView = findViewById(R.id.candidateIdTextView);
        candidatePhoneNumberTextView = findViewById(R.id.candidatePhoneNumberTextView);
        candidateDriverSchoolTextView = findViewById(R.id.candidateDriverSchoolTextView);

        examTimingTextView = findViewById(R.id.examTimingTextView);

        // 初始，默认100分
        hadScore = 100;
        hadScoreTextView = findViewById(R.id.hadScoreTextView);
        calculateScoreInfoTextView = findViewById(R.id.calculateScoreInfoTextView);
        calculateScoreInfoTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        examItemListView = findViewById(R.id.examItemListView);
        roadExamItemAdapter = new ExamItemAdapter();
        examItemListView.setAdapter(roadExamItemAdapter);
        contentScrollView = findViewById(R.id.contentScrollView);
//        examItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // 检查该考试项是否已点考
//                if (position < roadExamItemHadExamedList.size() && roadExamItemHadExamedList.get(position)) {
//                    return;
//                }
//                // 开始新road examItem
//                ExamItemProcess.startRoadExamItem(roadExamItemList.get(position), position);
//            }
//        });

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
                ExamItemProcess.startExam(lightExamTemplate);
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
            roadExamItemHadExamedList = new ArrayList<>();
            // 默认所有道路考试项都没有点考
            for (int i = 0; i < roadExamItemList.size(); ++i) {
                roadExamItemHadExamedList.add(false);
            }
            roadExamItemAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        examingActivity = null;
        // activity销毁时，注销广播
        unregisterReceiver(examBroadcastReceiver);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
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
            // 检查该考试项是否已点考
            if (position < roadExamItemHadExamedList.size() && roadExamItemHadExamedList.get(position)) {
                viewHolder.examItemButton.setEnabled(false);
            } else {
                viewHolder.examItemButton.setEnabled(true);
            }
            ExamItem examItem = roadExamItemList.get(position);
            viewHolder.examItemButton.setText(examItem.getName());
            viewHolder.examItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!ExamItemProcess.isExamStarted || !ExamItemProcess.isExaming) {
                        // 判断是否在考试
                        Toast.makeText(ExamingActivity.this, "请配置考试车辆，点击开始考试按钮，再点考！", Toast.LENGTH_LONG).show();
                    } else if (ExamItemProcess.isLightExaming) {
                        // 灯光考试过程中无法点考
                        Toast.makeText(ExamingActivity.this, "正在灯光考试中，无法点考！", Toast.LENGTH_LONG).show();
                    } else {
                        // 开始新road examItem
                        ExamItemProcess.startRoadExamItem(roadExamItemList.get(position), position);
                    }
                }
            });
            return convertView;
        }
    }

    static class ViewHolder{
        public Button examItemButton;
    }

    class ExamBroadcastReceiver extends BroadcastReceiver {
        private ExamItem examItem = null;
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
            } else if (ExamUpdateUiBroadcastMessage.EXAM_STOPPED_BY_EXCEPTION.equals(examUpdateUiBroadcastMessage.getTypeName())) {
                // 结束计时器
                if (calExamTimerTask != null) {
                    calExamTimerTask.cancel();
                }
                // 考试车辆tcp连接中断
                AlertDialog alertDialog = new AlertDialog.Builder(ExamingActivity.examingActivity)
                        .setTitle("错误信息")
                        .setMessage(examUpdateUiBroadcastMessage.getMessage())
                        .setCancelable(false)
                        .setPositiveButton("退出考试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ExamingActivity.examingActivity.finish();
                            }
                        })
                        .create();
                alertDialog.show();
            } else if (ExamUpdateUiBroadcastMessage.EXAM_ITEM_START.equals(examUpdateUiBroadcastMessage.getTypeName())) {
                // 开始了一个新的examItem
                Map<String, Object> dataMap = examUpdateUiBroadcastMessage.getData();
                examItem = (ExamItem) dataMap.get("examItem");
                System.err.println("开始了一个新ExamItem " + examItem.toString());
                if (dataMap.containsKey("examItemPosition")) {
                    Integer examItemPosition = (Integer) dataMap.get("examItemPosition");
                    if (examItemPosition < roadExamItemList.size()) {
                        // 已点考了一个考试项
                        roadExamItemHadExamedList.set(examItemPosition, true);
                        roadExamItemAdapter.notifyDataSetChanged();
                    }
                }
            } else if (ExamUpdateUiBroadcastMessage.EXAM_ITEM_OPERATE_RESULT.equals(examUpdateUiBroadcastMessage.getTypeName())) {
                // examItem操作结果
                Map<String, Object> dataMap = examUpdateUiBroadcastMessage.getData();
                Boolean isCorrect = (Boolean) dataMap.get("isCorrect");
                String resultMessage = (String) dataMap.get("resultMessage");
                if (!isCorrect) {
                    // 减分
                    hadScore -= examItem.getScore();
                    hadScoreTextView.setText(hadScore + "");
                    if (hadScore < 80) {
                        hadScoreTextView.setTextColor(Color.RED);
                    } else {
                        hadScoreTextView.setTextColor(Color.BLACK);
                    }
                    calculateScoreInfoTextView.append("考试项【" + examItem.getName() + "】扣 " + examItem.getScore() + " 分，因为" + resultMessage + "\n");
                } else {
                    calculateScoreInfoTextView.append("考试项【" + examItem.getName() + "】得 " + examItem.getScore() + " 分\n");
                }
            } else if (ExamUpdateUiBroadcastMessage.EXAM_HAS_STARTED.equals(examUpdateUiBroadcastMessage.getTypeName())) {
                // 考试已经开始，开始考试按钮disable，停止考试enable
                startExamButton.setEnabled(false);
                stopExamButton.setEnabled(true);
                Toast.makeText(context,examUpdateUiBroadcastMessage.getMessage(), Toast.LENGTH_SHORT).show();
                // 获取当前时间，开启计时器
                examStartTime = SystemClock.elapsedRealtime();
                calExamTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        int time = (int)((SystemClock.elapsedRealtime() - examStartTime) / 1000);
                        String hh = new DecimalFormat("00").format(time / 3600);
                        String mm = new DecimalFormat("00").format(time % 3600 / 60);
                        String ss = new DecimalFormat("00").format(time % 60);
                        final String timeFormat = new String(hh + ":" + mm + ":" + ss);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                examTimingTextView.setText(timeFormat);
                            }
                        });
                    }
                };
                new Timer("考试计时器").scheduleAtFixedRate(calExamTimerTask, 0, 1000L);
            }
        }
    }
}
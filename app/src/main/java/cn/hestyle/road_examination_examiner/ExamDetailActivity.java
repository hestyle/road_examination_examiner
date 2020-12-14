package cn.hestyle.road_examination_examiner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import cn.hestyle.road_examination_examiner.entity.Candidate;
import cn.hestyle.road_examination_examiner.entity.Exam;
import cn.hestyle.road_examination_examiner.entity.ExamItem;
import cn.hestyle.road_examination_examiner.entity.ExamTemplate;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExamDetailActivity extends AppCompatActivity {
    private Exam exam;
    private Candidate candidate;
    private ExamTemplate lightExamTemplate;
    private ExamTemplate roadExamTemplate;

    private TextView exam_admissionNoTextView;
    private TextView exam_carIdTextView;
    private TextView exam_stateTextView;
    private TextView exam_timeTextView;

    private ImageView candidatePhotoImageView;
    private TextView candidateNameTextView;
    private TextView candidateGenderTextView;
    private TextView candidateIdTextView;
    private TextView candidatePhoneNumberTextView;
    private TextView candidateDriverSchoolTextView;

    private TextView lightExamTextView;
    private TextView roadExamTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_detail);

        exam_admissionNoTextView = findViewById(R.id.exam_admissionNoTextView);
        exam_carIdTextView = findViewById(R.id.exam_carIdTextView);
        exam_stateTextView = findViewById(R.id.exam_stateTextView);
        exam_timeTextView = findViewById(R.id.exam_timeTextView);

        candidatePhotoImageView = findViewById(R.id.candidatePhotoImageView);
        candidateNameTextView = findViewById(R.id.candidateNameTextView);
        candidateGenderTextView = findViewById(R.id.candidateGenderTextView);
        candidateIdTextView = findViewById(R.id.candidateIdTextView);
        candidatePhoneNumberTextView = findViewById(R.id.candidatePhoneNumberTextView);
        candidateDriverSchoolTextView = findViewById(R.id.candidateDriverSchoolTextView);

        lightExamTextView = findViewById(R.id.lightExamTextView);
        roadExamTextView = findViewById(R.id.roadExamTextView);

        Intent intent = getIntent();
        exam = (Exam) intent.getSerializableExtra("exam");
        Toast.makeText(ExamDetailActivity.this, exam.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (exam == null) {
            Toast.makeText(ExamDetailActivity.this, "请先进行登录！", Toast.LENGTH_SHORT).show();
            return;
        }
        exam_admissionNoTextView.setText(exam.getAdmissionNo());
        exam_carIdTextView.setText(String.format("%d", exam.getCarId()));
        exam_timeTextView.setText(exam.getExamTime());
        if (exam.getState() == 0) {
            exam_stateTextView.setText("未考试");
            exam_stateTextView.setTextColor(Color.RED);
        } else {
            exam_stateTextView.setText("已考试");
            exam_stateTextView.setTextColor(Color.BLACK);
        }

        // 根据candidateId查找candidate
        if (exam.getCandidateId() != null) {
            this.getCandidate();
        }
        // 根据examTemplateId查找examTemplateId
        if (exam.getLightExamTemplateId() != null) {
            this.getLightExamTemplate();
        }
        if (exam.getExamTemplateId() != null) {
            this.getRoadExamTemplate();
        }
    }

    /**
     * 获取考生信息
     */
    private void getCandidate() {
        FormBody formBody = new FormBody.Builder()
                .add("candidateId", exam.getCandidateId())
                .build();
        Request request = new Request.Builder()
                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/candidate/findCandidateById.do")
                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ExamDetailActivity.this, "考生查找失败，发生网络错误！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new Gson();
                Type type =  new TypeToken<ResponseResult<Candidate>>(){}.getType();
                final ResponseResult<Candidate> responseResult = gson.fromJson(responseString, type);
                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExamDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                candidate = responseResult.getData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        candidateNameTextView.setText(candidate.getName());
                        candidateIdTextView.setText(candidate.getId());
                        candidateGenderTextView.setText(candidate.getGender());
                        candidatePhoneNumberTextView.setText(candidate.getPhoneNumber());
                        candidateDriverSchoolTextView.setText(candidate.getDriverSchool());
                        // 加载网络图片
                        Glide.with(ExamDetailActivity.this).load("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + candidate.getPhotoPath()).into(candidatePhotoImageView);
                    }
                });
            }
        });
    }

    /**
     * 获取灯光考试信息
     */
    private void getLightExamTemplate() {
        FormBody formBody = new FormBody.Builder()
                .add("examTemplateId", exam.getLightExamTemplateId())
                .build();
        Request request = new Request.Builder()
                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/examTemplate/findByExamTemplateId.do")
                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ExamDetailActivity.this, "灯光考试项查找失败，发生网络错误！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new Gson();
                Type type =  new TypeToken<ResponseResult<ExamTemplate>>(){}.getType();
                final ResponseResult<ExamTemplate> responseResult = gson.fromJson(responseString, type);
                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExamDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                lightExamTemplate = responseResult.getData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int index = 1;
                        String content = "";
                        for (ExamItem examItem : lightExamTemplate.getExamItemList()) {
                            content += index + "、" + examItem.getName() + "\n";
                            index += 1;
                        }
                        lightExamTextView.setText(content);
                    }
                });
            }
        });
    }

    /**
     * 获取道路考试信息
     */
    private void getRoadExamTemplate() {
        FormBody formBody = new FormBody.Builder()
                .add("examTemplateId", exam.getExamTemplateId())
                .build();
        Request request = new Request.Builder()
                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/examTemplate/findByExamTemplateId.do")
                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ExamDetailActivity.this, "道路考试项查找失败，发生网络错误！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new Gson();
                Type type =  new TypeToken<ResponseResult<ExamTemplate>>(){}.getType();
                final ResponseResult<ExamTemplate> responseResult = gson.fromJson(responseString, type);
                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExamDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                roadExamTemplate = responseResult.getData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int index = 1;
                        String content = "";
                        for (ExamItem examItem : roadExamTemplate.getExamItemList()) {
                            content += index + "、" + examItem.getName() + "\n";
                            index += 1;
                        }
                        roadExamTextView.setText(content);
                    }
                });
            }
        });
    }
}
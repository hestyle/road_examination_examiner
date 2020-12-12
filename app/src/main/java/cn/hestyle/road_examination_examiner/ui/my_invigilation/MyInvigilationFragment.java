package cn.hestyle.road_examination_examiner.ui.my_invigilation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.hestyle.road_examination_examiner.LoginActivity;
import cn.hestyle.road_examination_examiner.R;
import cn.hestyle.road_examination_examiner.entity.Exam;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyInvigilationFragment extends Fragment {
    /** 筛选 */
    private RadioButton allExamRadioButton = null;
    private RadioButton noExamRadioButton = null;
    private RadioButton examedRadioButton = null;
    private RadioButton todayExamRadioButton = null;
    /** 排序 */
    private RadioButton defaultSortRadioButton = null;
    private RadioButton dateDescSortRadioButton = null;
    private RadioButton dateAscSortRadioButton = null;
    /** list */
    private ListView examListView = null;
    /** list adapter */
    private ExamAdapter examAdapter = null;
    /** 登录账号所有的监考 */
    private List<Exam> allExam = null;
    /** listView中的exam */
    private List<Exam> inListData = new ArrayList<>();


    private MyInvigilationViewModel myInvigilationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myInvigilationViewModel =
                ViewModelProviders.of(this).get(MyInvigilationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_my_invigilation, container, false);
        allExamRadioButton = root.findViewById(R.id.allExamRadioButton);
        noExamRadioButton = root.findViewById(R.id.noExamRadioButton);
        examedRadioButton = root.findViewById(R.id.examedRadioButton);
        todayExamRadioButton = root.findViewById(R.id.todayExamRadioButton);
        defaultSortRadioButton = root.findViewById(R.id.defaultSortRadioButton);
        dateDescSortRadioButton = root.findViewById(R.id.dateDescSortRadioButton);
        dateAscSortRadioButton = root.findViewById(R.id.dateAscSortRadioButton);
        examListView = root.findViewById(R.id.examListView);
        examAdapter = new ExamAdapter();
        examListView.setAdapter(examAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (LoginActivity.examiner != null) {
            // 发送请求当前考官账号的所有考试
            // 访问服务器，提交登录表单
            FormBody formBody = new FormBody.Builder()
                    .add("examinerId", LoginActivity.examiner.getId())
                    .build();
            Request request = new Request.Builder()
                    .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/examiner/findExamByExaminerId.do")
                    .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                    .post(formBody)
                    .build();
            OkHttpClient httpClient = new OkHttpClient();
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    MyInvigilationFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyInvigilationFragment.this.getActivity(), "数据访问失败！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // 转json
                    Gson gson = new Gson();
                    Type type =  new TypeToken<ResponseResult<List<Exam>>>(){}.getType();
                    final ResponseResult<List<Exam>> responseResult = gson.fromJson(responseString, type);
                    // 判断是否登录成功
                    if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                        MyInvigilationFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyInvigilationFragment.this.getActivity(), responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    allExam = responseResult.getData();
                    inListData = new ArrayList<Exam>();
                    inListData.addAll(responseResult.getData());
                    MyInvigilationFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 修改inListData后，更新list
                            examAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(MyInvigilationFragment.this.getActivity(), "请先进行登录！", Toast.LENGTH_SHORT).show();
        }
    }

    //自定义适配器
    class ExamAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return inListData.size();
        }
        @Override
        public Object getItem(int position) {
            return inListData.get(position);
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
                convertView = getLayoutInflater().inflate(R.layout.fragment_my_exam_item, null);
                viewHolder.exam_admissionNoTextView = convertView.findViewById(R.id.exam_admissionNoTextView);
                viewHolder.exam_stateTextView = convertView.findViewById(R.id.exam_stateTextView);
                viewHolder.exam_candidateIdTextView = convertView.findViewById(R.id.exam_candidateIdTextView);
                viewHolder.exam_examTimeTextView = convertView.findViewById(R.id.exam_examTimeTextView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Exam exam = inListData.get(position);
            viewHolder.exam_admissionNoTextView.setText(exam.getAdmissionNo());
            // 判断考试状态的值
            if (exam.getState() == 0) {
                viewHolder.exam_stateTextView.setText("未考试");
                viewHolder.exam_stateTextView.setTextColor(Color.RED);
            } else if (exam.getState() == 2) {
                viewHolder.exam_stateTextView.setText("已考试");
                viewHolder.exam_stateTextView.setTextColor(Color.GREEN);
            }
            viewHolder.exam_candidateIdTextView.setText(exam.getCandidateId());
            viewHolder.exam_examTimeTextView.setText(exam.getExamTime());
            return convertView;
        }
    }

    static class ViewHolder{
        public TextView exam_admissionNoTextView;
        public TextView exam_stateTextView;
        public TextView exam_candidateIdTextView;
        public TextView exam_examTimeTextView;
    }
}
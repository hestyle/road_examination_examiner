package cn.hestyle.road_examination_examiner.ui.my_account;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.hestyle.road_examination_examiner.LoginActivity;
import cn.hestyle.road_examination_examiner.MainActivity;
import cn.hestyle.road_examination_examiner.R;
import cn.hestyle.road_examination_examiner.entity.Examiner;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

public class MyAccountFragment extends Fragment {

    private Button btnEdit;
    private Button btnEditPassword;
    private Button btnBack;

    private TextView tvIdNumber;
    private TextView tvName;
    private TextView tvPhoneNumber;
    private TextView tvAge;

    private ImageView ivDetail;

    private RadioButton rbMale;
    private RadioButton rbFemale;

    private Examiner examiner = LoginActivity.examiner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_my_account, container, false);
        btnEdit = root.findViewById(R.id.btn_account_edit);
        btnEditPassword = root.findViewById(R.id.btn_edit_password);
        btnBack = root.findViewById(R.id.btn_account_back);

        tvIdNumber = root.findViewById(R.id.text_id_number);
        tvName = root.findViewById(R.id.text_name);
        tvPhoneNumber =root.findViewById(R.id.text_phone_number);
        tvAge = root.findViewById(R.id.text_age);

        ivDetail = root.findViewById(R.id.img_detail);

        rbMale = root.findViewById(R.id.rBtn_male);
        rbFemale = root.findViewById(R.id.rBtn_female);
        //设置数据
        if(examiner.getPhotoPath() != null) {
            Glide.with(this).load("http://" + SettingFragment.serverIpAddressString + ":" +SettingFragment.serverPortString + examiner.getPhotoPath()).into(ivDetail);
        }
        tvIdNumber.setText(examiner.getId());
        tvName.setText(examiner.getName());
        tvAge.setText(examiner.getAge().toString());
        tvPhoneNumber.setText(examiner.getPhoneNumber());
        if(examiner.getGender().equals('男')){
            rbMale.setChecked(true);
        }else{
            rbFemale.setChecked(true);
        }

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View viewEdit = LayoutInflater.from(getContext()).inflate(R.layout.layout_edit_info,null);
                final EditText etAge,etPhone;
                final TextView tvEditId,tvEditName;
                final RadioButton rbEditMale,rbEditFemale;
                etAge = viewEdit.findViewById(R.id.et_edit_age);
                etPhone = viewEdit.findViewById(R.id.et_edit_phone);
                tvEditId = viewEdit.findViewById(R.id.tv_edit_id);
                tvEditName = viewEdit.findViewById(R.id.tv_edit_name);
                rbEditMale = viewEdit.findViewById(R.id.rBtn_edit_male);
                rbEditFemale = viewEdit.findViewById(R.id.rBtn_edit_female);
                if(examiner.getGender().equals('男')){
                    rbEditMale.setChecked(true);
                }else{
                    rbEditFemale.setChecked(true);
                }
                Glide.with(getActivity()).load("http://" + SettingFragment.serverIpAddressString + ":" +SettingFragment.serverPortString + examiner.getPhotoPath()).into(ivDetail);
                tvEditId.setText(examiner.getId());
                tvEditName.setText(examiner.getName());
                builder.setView(viewEdit).setTitle("请输入信息！").setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(etAge.getText().toString().length() == 0 || etPhone.getText().toString().length() != 11){
                            Toast.makeText(getActivity(),"请输入完整信息！",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // 访问服务器，提交json
                        OkHttpClient httpClient = new OkHttpClient();
                        examiner.setPhoneNumber(etPhone.getText().toString());
                        examiner.setAge(Integer.parseInt(etAge.getText().toString()));
                        examiner.setGender(rbEditMale.isChecked()? "男":"女");
                        Gson gson = new Gson();
                        String json = gson.toJson(examiner);
                        FormBody formBody = new FormBody.Builder()
                                .add("newBaseInfoJsonData",json)
                                .build();
                        Request request = new Request.Builder()
                                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/examiner/modifyExaminerBaseInfo.do")
                                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                                .post(formBody)
                                .build();
                        Call call = httpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(),"请求修改失败",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                                String responseString = response.body().string();
                                // 转json
                                Gson gson = new Gson();
                                Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                                final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                                // 判断是否登录成功
                                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(examiner.getGender().equals('男')){
                                            rbMale.setChecked(true);
                                        }else{
                                            rbFemale.setChecked(true);
                                        }
                                        tvAge.setText(examiner.getAge().toString());
                                        tvPhoneNumber.setText(examiner.getPhoneNumber());
                                        Toast.makeText(getActivity(),responseResult.getMessage(),Toast.LENGTH_SHORT).show();


                                    }
                                });
                            }
                        });
                    }
                }).show();
            }
        });

        btnEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderPassword = new AlertDialog.Builder(getContext());
                View viewPassword = LayoutInflater.from(getContext()).inflate(R.layout.layout_reset_password,null);
                final EditText etNewPassword,etRePut;
                final TextView tvIdPassword;
                etNewPassword = viewPassword.findViewById(R.id.et_new_password);
                etRePut = viewPassword.findViewById(R.id.et_reput);
                tvIdPassword = viewPassword.findViewById(R.id.tv_reset_id);
                tvIdPassword.setText(examiner.getName());
                builderPassword.setView(viewPassword).setTitle("输入密码").setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(!etNewPassword.getText().toString().equals(etRePut.getText().toString())){
                            Toast.makeText(getActivity(), "密码不一致",Toast.LENGTH_SHORT).show();
                            return;
                        }else if(etNewPassword.getText().toString().length() <6 || etNewPassword.getText().toString().length() > 20){
                            Toast.makeText(getActivity(),"密码长度不合法",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        OkHttpClient httpClient = new OkHttpClient();
                        String json = "{\"" + "id\":\"" + examiner.getId() + "\",\"newPassword\":\"" +
                                etNewPassword.getText().toString() +"\",\"reNewPassword\":\"" + etRePut.getText().toString() + "\"}";
                        FormBody formBody = new FormBody.Builder()
                                .add("newPasswordJsonData",json)
                                .build();
                        Request request = new Request.Builder()
                                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/examiner/resetExaminerPassword.do")
                                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                                .post(formBody)
                                .build();
                        Call call = httpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                Toast.makeText(getActivity(),"请求修改失败",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                String responseString = response.body().string();
                                // 转json
                                Gson gson = new Gson();
                                Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                                final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                                // 判断是否登录成功
                                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(),responseResult.getMessage(),Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        });
                    }
                }).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        return root;
    }
}
package cn.hestyle.road_examination_examiner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.hestyle.road_examination_examiner.entity.Examiner;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    /** session id 服务器下发，用于记录是否登录 */
    public static String jSessionIdString = null;
    public static Examiner examiner = null;
    private EditText loginIdEditText = null;
    private EditText loginPasswordEditEditText = null;
    private Button loginButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginIdEditText = findViewById(R.id.loginIdEditText);
        loginPasswordEditEditText = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidForm()) {
                    return;
                }
                // 访问服务器，提交登录表单
                FormBody formBody = new FormBody.Builder()
                        .add("id", "362501888808088888")
                        .add("password", "123456")
                        .build();
                Request request = new Request.Builder()
                        .url("http://" + SettingFragment.serverIpAddressString + ":9090/road_examination_manager/examiner/login.do")
                        .post(formBody)
                        .build();
                OkHttpClient httpClient = new OkHttpClient();
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(LoginActivity.this, "登录失败！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // 转json
                        Gson gson = new Gson();
                        Type type =  new TypeToken<ResponseResult<Examiner>>(){}.getType();
                        final ResponseResult<Examiner> responseResult = gson.fromJson(responseString, type);
                        // 判断是否登录成功
                        if (responseResult.getCode() != 200) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        LoginActivity.examiner = responseResult.getData();
                        // 截取cookie中的JSESSIONID
                        String cookieString = response.headers("Set-Cookie").toString();
                        cookieString = cookieString.substring(cookieString.indexOf("JSESSIONID=") + 11);
                        cookieString = cookieString.substring(0, cookieString.indexOf(";"));
                        jSessionIdString = cookieString;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 回传参数、结果码，结束当前activity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                setResult(2, intent);
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 判断表单是否合法
     * @return      是否通过检测
     */
    private boolean isValidForm() {
        String id = loginIdEditText.getText().toString();
        String password = loginPasswordEditEditText.getText().toString();
        if (id.length() != 18) {
            Toast.makeText(LoginActivity.this, "请输入18位长的身份证号！", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() == 0) {
            Toast.makeText(LoginActivity.this, "请输入登录密码！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
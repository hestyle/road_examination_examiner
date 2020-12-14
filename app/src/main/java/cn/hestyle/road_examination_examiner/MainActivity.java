package cn.hestyle.road_examination_examiner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import cn.hestyle.road_examination_examiner.entity.Examiner;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;

public class MainActivity extends AppCompatActivity {
    /** 退出登录button */
    private Button logoutButton = null;

    private AppBarConfiguration mAppBarConfiguration;

    private ImageView ivMain;
    private TextView tvId;
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_my_invigilation, R.id.nav_my_account, R.id.nav_setting)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        View view = navigationView.getHeaderView(0);
        ivMain = view.findViewById(R.id.iv_main);
        tvId = view.findViewById(R.id.tv_id_main);
        tvName = view.findViewById(R.id.tv_name_main);

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清除session id，重新登录
                LoginActivity.jSessionIdString = null;
                LoginActivity.examiner = null;
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // 判断是否设置了服务器ip、tcp服务端口
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SettingFragment.serverIpAddressString = sharedPreferences.getString("serverIpAddress", null);
        SettingFragment.serverPortString = sharedPreferences.getString("serverPort", null);
        SettingFragment.tcpServerPortString = sharedPreferences.getString("tcpServerPort", null);
        if (SettingFragment.serverIpAddressString == null) {
            Toast.makeText(MainActivity.this, "请先设置服务器ip地址！", Toast.LENGTH_SHORT).show();
            // 跳转到setting界面
            navController.navigate(R.id.nav_setting);
        } else if (LoginActivity.jSessionIdString == null) {
            // 判断是否登录了
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // 关闭右上角三点菜单
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 判断是否登录了
        if (LoginActivity.jSessionIdString == null) {
            this.logoutButton.setText("去登录");
        } else {
            if (LoginActivity.examiner.getPhotoPath() != null) {
                Glide.with(this).load("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + LoginActivity.examiner.getPhotoPath()).into(ivMain);
            }
            tvName.setText(LoginActivity.examiner.getName());
            tvId.setText(LoginActivity.examiner.getId());

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2) {
            // 处理登录返回
            this.logoutButton.setText("退出登录");
            Toast.makeText(MainActivity.this, LoginActivity.examiner.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
package cn.hestyle.road_examination_examiner.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import cn.hestyle.road_examination_examiner.R;
import cn.hestyle.road_examination_examiner.util.NetworkHelp;

public class SettingFragment extends Fragment {
    public static String serverIpAddressString = null;
    public static String tcpServerPortString = null;
    public static String serverPortString = null;

    private EditText serverIpAddressEditText = null;
    private EditText serverPortEditText = null;
    private EditText tcpServerPortEditText = null;
    private Button saveSettingsButton = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, container, false);
        serverIpAddressEditText = root.findViewById(R.id.serverIpAddressEditText);
        serverPortEditText = root.findViewById(R.id.serverPortEditText);
        tcpServerPortEditText = root.findViewById(R.id.tcpServerPortEditText);
        saveSettingsButton = root.findViewById(R.id.saveSettingsButton);


        // 设置saveSettingsButton按钮点击事件
        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverIpAddressStringTmp = serverIpAddressEditText.getText().toString();
                String serverPortStringTmp = serverPortEditText.getText().toString();
                String tcpServerPortStringTmp = tcpServerPortEditText.getText().toString();
                // 检查ip、port的合法性
                if (!NetworkHelp.isValidIpv4Address(serverIpAddressStringTmp)) {
                    Toast.makeText(SettingFragment.this.getContext(), "服务器ip地址非法！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!NetworkHelp.isValidPort(serverPortStringTmp)) {
                    Toast.makeText(SettingFragment.this.getContext(), "服务器端口非法！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!NetworkHelp.isValidPort(tcpServerPortStringTmp)) {
                    Toast.makeText(SettingFragment.this.getContext(), "Tcp服务端口非法！", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 更新SharedPreferences
                SettingFragment.serverIpAddressString = serverIpAddressStringTmp;
                SettingFragment.serverPortString = serverPortStringTmp;
                SettingFragment.tcpServerPortString = tcpServerPortStringTmp;
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("serverIpAddress", serverIpAddressStringTmp);
                editor.putString("serverPort", serverPortStringTmp);
                editor.putString("tcpServerPort", tcpServerPortStringTmp);
                if (editor.commit()) {
                    Toast.makeText(SettingFragment.this.getActivity(), "保存成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingFragment.this.getActivity(), "保存失败！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 读取SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SettingFragment.serverIpAddressString = sharedPreferences.getString("serverIpAddress", null);
        SettingFragment.serverPortString = sharedPreferences.getString("serverPort", null);
        SettingFragment.tcpServerPortString = sharedPreferences.getString("tcpServerPort", null);
        if (SettingFragment.serverIpAddressString != null) {
            serverIpAddressEditText.setText(SettingFragment.serverIpAddressString);
        }
        if (SettingFragment.serverPortString != null) {
            serverPortEditText.setText(SettingFragment.serverPortString);
        }
        if (SettingFragment.tcpServerPortString != null) {
            tcpServerPortEditText.setText(SettingFragment.tcpServerPortString);
        }
    }
}
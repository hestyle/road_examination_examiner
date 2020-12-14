package cn.hestyle.road_examination_examiner.ui.my_account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import cn.hestyle.road_examination_examiner.LoginActivity;
import cn.hestyle.road_examination_examiner.R;
import cn.hestyle.road_examination_examiner.entity.Examiner;

public class MyAccountFragment extends Fragment {

    private Button btnEdit;
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
            Glide.with(this).load("http://192.168.31.219:9090" + examiner.getPhotoPath()).into(ivDetail);
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

        return root;
    }
}
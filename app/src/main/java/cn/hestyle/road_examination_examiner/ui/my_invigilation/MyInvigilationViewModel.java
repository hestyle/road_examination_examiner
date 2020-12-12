package cn.hestyle.road_examination_examiner.ui.my_invigilation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyInvigilationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MyInvigilationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is my_invigilation fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
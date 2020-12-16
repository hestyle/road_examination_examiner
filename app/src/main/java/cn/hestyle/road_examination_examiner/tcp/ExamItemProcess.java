package cn.hestyle.road_examination_examiner.tcp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.hestyle.road_examination_examiner.entity.Car;
import cn.hestyle.road_examination_examiner.entity.ExamItem;
import cn.hestyle.road_examination_examiner.entity.ExamOperation;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import cn.hestyle.tcp.TcpRequestMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ExamItem处理类
 */
public class ExamItemProcess {
    /** 单例对象 */
    private static ExamItemProcess examItemProcess = null;

    /** 记录考试相关的信息 */
    public static volatile Boolean isExamStarted = false;
    public static volatile Boolean isExaming = false;
    public static volatile Boolean isExamEnd = false;
    /** 正在考试的考试项，以及该考试项包括的操作项list */
    public static volatile ExamItem examingExamItem = null;
    public static volatile List<ExamOperation> examOperationList = new ArrayList<>();

    public static volatile Car car = null;

    /**
     * 私有构造函数
     */
    private ExamItemProcess() {

    }

    /**
     * 构造ExamItemProcess单例对象
     */
    private static void setSingleInstance() {
        synchronized (ExamItemProcess.class) {
            ExamItemProcess.examItemProcess = new ExamItemProcess();
        }
    }

    /**
     * 初始化考试相关的工作，启动tcp服务、tcp response消息处理线程
     */
    public static void initExam(Car car) {
        ExamItemProcess.isExamStarted = true;
        ExamItemProcess.isExaming = false;
        ExamItemProcess.isExamEnd = false;
        ExamItemProcess.car = car;
        // 初始化ExamItemProcess单例对象
        ExamItemProcess.setSingleInstance();
        // 启动tcp服务线程
        TcpNetWorkServiceThread tcpNetWorkServiceThread = TcpNetWorkServiceThread.getSingleInstance(car);
        tcpNetWorkServiceThread.start();
    }

    public static void startExam() {
        isExaming = true;
    }

    /**
     * 停止考试
     */
    public static void stopExam() {
        // 停止前发送TCP_CONNECT_CLOSE消息
        TcpRequestMessage tcpRequestMessage = new TcpRequestMessage();
        tcpRequestMessage.setTypeName(TcpRequestMessage.REQUEST_TCP_CONNECT_CLOSE);
        TcpNetWorkServiceThread.addTcpRequestMessage(tcpRequestMessage);
        // 停止tcpServiceThread、tcpResponseMessageHandler、TcpNetWorkServiceThread线程
        ExamItemProcess.restrictStopOtherThread();
    }

    /**
     * 强制停止三个线程
     */
    public static void restrictStopOtherThread() {
        ExamItemProcess.isExamStarted = false;
        ExamItemProcess.isExaming = false;
        ExamItemProcess.isExamEnd = true;
        // 丢弃examItemProcess对象
        ExamItemProcess.examItemProcess = null;
    }

    /**
     * 设置car信息
     */
    public static void checkCarInfo() {
        TcpRequestMessage tcpRequestMessage = new TcpRequestMessage();
        tcpRequestMessage.setTypeName(TcpRequestMessage.REQUEST_CHECK_IP_AND_MAC);
        tcpRequestMessage.setDescription("请返回你的（局域网）ip地址、mac地址！");
        TcpNetWorkServiceThread.addTcpRequestMessage(tcpRequestMessage);
    }

    /**
     * 开始一个examItem
     * @param examItem  examItem
     */
    public static void setExamingExamItem(ExamItem examItem) {
        if (examItem != null && examItem.getOperationIds() != null) {
            ExamItemProcess.examingExamItem = examItem;
            // 请求该examItem包含的examOperation
            getExamOperationByExamOperationIdsString(examItem.getOperationIds());
        } else {
            Log.e("ExamOperation", "考试项 id = " + examItem.getId() + " operationIds字段为空！");
        }
    }

    /**
     * 请求examItem包含的examOperation
     */
    private static void getExamOperationByExamOperationIdsString(String operationIdsString) {
        // 访问服务器，提交登录表单
        FormBody formBody = new FormBody.Builder()
                .add("idsString", operationIdsString)
                .build();
        Request request = new Request.Builder()
                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/examOperation/findByIdsString.do")
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ExamOperation", "考试项 id = " + examingExamItem.getId() + "操作项请求失败！");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new Gson();
                Type type =  new TypeToken<ResponseResult<List<ExamOperation>>>(){}.getType();
                ResponseResult<List<ExamOperation>> responseResult = gson.fromJson(responseString, type);
                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                    // 操作项获取失败
                    Log.e("ExamOperation", "考试项 id = " + examingExamItem.getId() + "操作项请求失败！");
                } else {
                    examOperationList.clear();
                    examOperationList.addAll(responseResult.getData());
                    Log.i("ExamOperation", "考试项 id = " + examingExamItem.getId() + "操作项请求成功！");
                }
            }
        });
    }
}

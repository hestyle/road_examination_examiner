package cn.hestyle.road_examination_examiner.tcp;

import android.media.MediaPlayer;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.hestyle.road_examination_examiner.ExamingActivity;
import cn.hestyle.road_examination_examiner.LoginActivity;
import cn.hestyle.road_examination_examiner.entity.Car;
import cn.hestyle.road_examination_examiner.entity.ExamItem;
import cn.hestyle.road_examination_examiner.entity.ExamOperation;
import cn.hestyle.road_examination_examiner.entity.ExamTemplate;
import cn.hestyle.road_examination_examiner.entity.ExamUpdateUiBroadcastMessage;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import cn.hestyle.road_examination_examiner.util.ExamUpdateUiBroadcastUtil;
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
    /** 是否正在灯光考试 */
    public static volatile Boolean isLightExaming = false;
    public static volatile Boolean isExaming = false;
    public static volatile Boolean isExamEnd = false;
    /** 正在考试的考试项，以及该考试项包括的操作项list */
    public static volatile ExamItem examingExamItem = null;

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

    /**
     * 开始考试，启动灯光考试线程
     * @param lightExamTemplate     灯光考试template
     */
    public static void startExam(ExamTemplate lightExamTemplate) {
        isExaming = true;
        isLightExaming = true;
        // 启动灯光考试音频播放线程
        LightExamThread lightExamThread = new LightExamThread(lightExamTemplate);
        lightExamThread.start();
        // 发送广播，更新ui,已开始考试
        ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
        examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_HAS_STARTED);
        examUpdateUiBroadcastMessage.setMessage("考试已经开始！");
        ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
    }

    /**
     * 停止考试
     */
    public static void stopExam() {
        // 停止前发送TCP_CONNECT_CLOSE消息
        TcpRequestMessage tcpRequestMessage = new TcpRequestMessage();
        tcpRequestMessage.setTypeName(TcpRequestMessage.REQUEST_TCP_CONNECT_CLOSE);
        TcpNetWorkServiceThread.addTcpRequestMessage(tcpRequestMessage);
        // 重置各个flag
        stopExamFlagReset();
    }

    /**
     * 强制停止三个线程
     */
    public static void immediateStopOtherThread(String message) {
        // 重置各个flag
        stopExamFlagReset();
        // 发送广播，更新ui
        ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
        examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_STOPPED_BY_EXCEPTION);
        examUpdateUiBroadcastMessage.setMessage(message);
        ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
    }

    /**
     * 停止考试时，重置各个flag
     */
    private static void stopExamFlagReset() {
        ExamItemProcess.isExamStarted = false;
        ExamItemProcess.isLightExaming = false;
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
    public static void startRoadExamItem(ExamItem examItem, Integer examItemPosition) {
        if (examItem != null && examItem.getOperationIds() != null) {
            ExamItemProcess.examingExamItem = examItem;
            RoadExamItemThread roadExamItemThread = new RoadExamItemThread(examItem, examItemPosition);
            roadExamItemThread.start();
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
                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
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
                    if (isLightExaming) {
                        TcpResponseMessageHandler.examOperationList.clear();
                        TcpResponseMessageHandler.examOperationList.addAll(responseResult.getData());
                    } else {
                        // 非灯光考试项
                        TcpResponseMessageHandler.startRoadExamItem(responseResult.getData());
                    }
                    Log.i("ExamOperation", "考试项 id = " + examingExamItem.getId() + "操作项请求成功！");
                }
            }
        });
    }

    /**
     * 夜间灯光考试线程
     */
    static class LightExamThread extends Thread {
        /** 夜间灯光模拟考试模板 */
        private ExamTemplate lightExamTemplate;

        public LightExamThread(ExamTemplate lightExamTemplate) {
            this.lightExamTemplate = lightExamTemplate;
        }

        @Override
        public void run() {
            super.run();
            Log.i("LightExamThread", "灯光模拟考试线程已启动！");
            // 灯光考试项先进行随机排序
            Collections.shuffle(lightExamTemplate.getExamItemList());
            Collections.shuffle(lightExamTemplate.getExamItemList());
            Collections.shuffle(lightExamTemplate.getExamItemList());
            // 遍历灯光考试模板中的考试项
            Iterator<ExamItem> iterator = lightExamTemplate.getExamItemList().iterator();
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                // 播放“科目三道路考试”
                mediaPlayer.setDataSource("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/upload/audio/160809159797428104571.mp3");
                mediaPlayer.prepare();
                mediaPlayer.start();
                while (mediaPlayer.isPlaying()) {
                    sleep(300);
                }
                // 播放完后，等待三秒，再播放灯光考试项
                sleep(3000);
                while (isLightExaming && iterator.hasNext()) {
                    ExamItem examItem = iterator.next();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + examItem.getVoicePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    // 音频播放期间，请求该考试项包含的操作项
                    examingExamItem = examItem;
                    getExamOperationByExamOperationIdsString(examItem.getOperationIds());
                    while (mediaPlayer.isPlaying()) {
                        if (!isLightExaming) {
                            throw new Exception("已退出灯光考试！");
                        }
                        sleep(300);
                    }
                    Log.i("LightExamThread", examItem.getVoicePath() + "已播放完毕");
                    // 给ui发送已开始新examItem的广播
                    ExamUpdateUiBroadcastMessage examItemStartMessage = new ExamUpdateUiBroadcastMessage();
                    examItemStartMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_ITEM_START);
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("examItem", examItem);
                    examItemStartMessage.setData(dataMap);
                    ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examItemStartMessage);
                    // 每个灯光考试项等待5秒
                    int count = 10;
                    while (count > 0) {
                        if (!isLightExaming) {
                            throw new Exception("已退出灯光考试！");
                        }
                        sleep(500);
                        count -= 1;
                    }
                    // 查看结果，并且将结果发送到ui线程
                    ExamUpdateUiBroadcastMessage examItemResultMessage = new ExamUpdateUiBroadcastMessage();
                    examItemResultMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_ITEM_OPERATE_RESULT);
                    examItemResultMessage.setData(TcpResponseMessageHandler.getExamItemResultMap());
                    ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examItemResultMessage);
                }
                if (!isLightExaming) {
                    throw new Exception("已退出灯光考试！");
                }
                // 播放“灯光考试结束”
                mediaPlayer.reset();
                // 播放“科目三道路考试”
                mediaPlayer.setDataSource("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/upload/audio/160809164492438619086.mp3");
                mediaPlayer.prepare();
                mediaPlayer.start();
                while (mediaPlayer.isPlaying()) {
                    if (!isLightExaming) {
                        throw new Exception("已退出灯光考试！");
                    }
                    sleep(300);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (isLightExaming && iterator.hasNext()) {
                    Log.e("LightExamThread", "灯光考试LightExamThread线程【非正常】停止！");
                    // 如果还在isLightExaming状态下，并且还有灯光考试项没处理，则说明上面出错了
                    ExamItemProcess.immediateStopOtherThread("考试终止，灯光考试LightExamThread线程异常停止！");
                }
                ExamItemProcess.isLightExaming = false;
                Log.i("LightExamThread", "灯光模拟考试LightExamThread线程已停止！");
            }
        }
    }

    /**
     * 道路考试项考试线程
     */
    static class RoadExamItemThread extends Thread {
        /** 夜间灯光模拟考试模板 */
        private ExamItem examItem;
        private Integer examItemPosition;

        public RoadExamItemThread(ExamItem examItem, Integer examItemPosition) {
            this.examItem = examItem;
            this.examItemPosition = examItemPosition;
        }

        @Override
        public void run() {
            super.run();
            Log.i("RoadExamItemThread", "RoadExamItemThread线程已启动！");
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + examItem.getVoicePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                // 音频播放期间，请求该考试项包含的操作项
                examingExamItem = examItem;
                getExamOperationByExamOperationIdsString(examItem.getOperationIds());
                while (mediaPlayer.isPlaying()) {
                    if (!isExaming) {
                        throw new Exception("已退出考试！");
                    }
                    sleep(300);
                }
                Log.i("RoadExamItemThread", examItem.getVoicePath() + "已播放完毕");
                // 给ui发送已开始新examItem的广播
                ExamUpdateUiBroadcastMessage examItemStartMessage = new ExamUpdateUiBroadcastMessage();
                examItemStartMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_ITEM_START);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("examItem", examItem);
                dataMap.put("examItemPosition", examItemPosition);
                examItemStartMessage.setData(dataMap);
                ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examItemStartMessage);
                // 每个道路考试项等待60秒
                int count = 120;
                while (count > 0) {
                    if (!isExaming) {
                        throw new Exception("已退出考试！");
                    }
                    // 检查是否完成了全部的考试项，检测到了，退出等待
                    synchronized (TcpResponseMessageHandler.class) {
                        if (TcpResponseMessageHandler.hadResult) {
                            count = -1;
                        }
                    }
                    sleep(500);
                    count -= 1;
                }
                // 20秒还没检测到结果，则直接返回超时
                ExamUpdateUiBroadcastMessage examItemResultMessage = new ExamUpdateUiBroadcastMessage();
                examItemResultMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_ITEM_OPERATE_RESULT);
                Map<String, Object> resultMap = TcpResponseMessageHandler.getRoadExamItemResult();
                resultMap.put("examItemPosition", examItemPosition);
                examItemResultMessage.setData(resultMap);
                ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examItemResultMessage);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Log.i("RoadExamItemThread", "RoadExamItemThread线程已停止！");
            }
        }
    }
}

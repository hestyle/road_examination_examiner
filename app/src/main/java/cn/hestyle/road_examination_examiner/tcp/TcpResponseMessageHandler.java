package cn.hestyle.road_examination_examiner.tcp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.hestyle.road_examination_examiner.ExamingActivity;
import cn.hestyle.road_examination_examiner.LoginActivity;
import cn.hestyle.road_examination_examiner.entity.Car;
import cn.hestyle.road_examination_examiner.entity.ExamOperation;
import cn.hestyle.road_examination_examiner.entity.ExamUpdateUiBroadcastMessage;
import cn.hestyle.road_examination_examiner.entity.Gear;
import cn.hestyle.road_examination_examiner.entity.ResponseResult;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import cn.hestyle.road_examination_examiner.util.ExamUpdateUiBroadcastUtil;
import cn.hestyle.tcp.TcpRequestMessage;
import cn.hestyle.tcp.TcpResponseMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * TcpResponseMessage处理线程
 */
public class TcpResponseMessageHandler extends Thread {
    /** 单例对象 */
    private static TcpResponseMessageHandler tcpResponseMessageHandler = null;
    /** 车辆端返回的消息 */
    public static LinkedList<TcpResponseMessage> tcpResponseMessageLinkedList = new LinkedList<>();
    /** 考试项是否合格 */
    public static volatile boolean isCorrect = false;
    /** 灯光考试时，上一次灯光操作 */
    public static volatile String beforeLightOperationName = null;
    /** 考试项考试时的操作描述 */
    public static final String defaultResultMessage = "未在规定时间内进行操作！";
    public static volatile String resultMessage = defaultResultMessage;
    /** (道路)考试项考试中，下一个操作项所在examOperationList的下标 */
    public static volatile boolean hadResult = false;
    private static volatile Integer nextExamOperationIndex = null;
    /** 考试项包含的操作项 */
    public static volatile List<ExamOperation> examOperationList = new ArrayList<>();
    /** 车门状态 */
    private static volatile boolean isDoorClosed = false;
    /** 安全带状态 */
    private static volatile boolean isSeatBeltFasten = false;
    /** 当前的档位 */
    private static volatile Gear nowGear = null;
    /** 档位配置 */
    private static List<Gear> gearList = null;
    /** 离合踏板是否踩住 */
    private static boolean isStepOnClutchPedal = false;

    private TcpResponseMessageHandler() {}

    /**
     * 获取TcpResponseMessageHandler单例对象
     * @return      单例对象
     */
    public static TcpResponseMessageHandler getSingleInstance() {
        synchronized (TcpResponseMessageHandler.class) {
            if (tcpResponseMessageHandler == null) {
                tcpResponseMessageHandler = new TcpResponseMessageHandler();
            }
        }
        // 获取档位信息
        nowGear = null;
        getAllGear();
        isStepOnClutchPedal = false;
        // 默认安全带未系上、车门未关闭
        isDoorClosed = false;
        isSeatBeltFasten = false;
        examOperationList.clear();
        beforeLightOperationName = null;
        tcpResponseMessageLinkedList.clear();
        return tcpResponseMessageHandler;
    }

    @Override
    public void run() {
        super.run();
        Log.i("TcpResponseMsgHandler", "TcpResponseMessageHandler线程已启动！");
        try {
            while (ExamItemProcess.isExamStarted) {
                // 每次从tcpResponseMessageLinkedList读取待处理的消息
                TcpResponseMessage tcpResponseMessage = null;
                synchronized (TcpResponseMessageHandler.class) {
                    if (tcpResponseMessageLinkedList.size() != 0) {
                        tcpResponseMessage = tcpResponseMessageLinkedList.removeFirst();
                    }
                }
                if (tcpResponseMessage != null) {
                    TcpResponseMessageHandler.handleTcpResponseMessage(tcpResponseMessage);
                }
                sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ExamItemProcess.isExamStarted) {
                Log.i("TcpResponseHandler", "TcpResponseMessageHandler线程【非正常】停止！");
                // 非正常情况下停止
                ExamItemProcess.immediateStopOtherThread("考试终止，TcpResponseMessageHandler线程【非正常】停止！");
            }
            tcpResponseMessageHandler = null;
            Log.i("TcpResponseHandler", "TcpResponseMessageHandler线程已停止！");
        }
    }

    /**
     * 获取所有的档位信息
     */
    private static void getAllGear() {
        FormBody formBody = new FormBody.Builder()
                .add("pageIndex", 1 + "")
                .add("pageSize", Integer.MAX_VALUE + "")
                .build();
        Request request = new Request.Builder()
                .url("http://" + SettingFragment.serverIpAddressString + ":" + SettingFragment.serverPortString + "/road_examination_manager/gear/findByPage.do")
                .addHeader("Cookie", "JSESSIONID=" + LoginActivity.jSessionIdString)
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("TcpResponseMsgHandler", "档位信息获取失败，网络访问失败！");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new Gson();
                Type type =  new TypeToken<ResponseResult<List<Gear>>>(){}.getType();
                ResponseResult<List<Gear>> responseResult = gson.fromJson(responseString, type);
                // 判断是否登录成功
                if (responseResult.getCode() == null || responseResult.getCode() != 200) {
                    Log.e("TcpResponseMsgHandler", "档位信息获取失败，" + responseResult.getMessage());
                    return;
                }
                gearList = responseResult.getData();
            }
        });
    }

    /**
     * 增加TcpResponseMessage
     * @param tcpResponseMessage    待处理的TcpResponseMessage
     */
    public static void addTcpResponseMessage(TcpResponseMessage tcpResponseMessage) {
        synchronized (TcpResponseMessageHandler.class) {
            tcpResponseMessageLinkedList.addLast(tcpResponseMessage);
        }
    }

    /**
     * 获取灯光考试项的结果
     * @return      当前灯光考试项的结果
     */
    public static Map<String, Object> getExamItemResultMap() {
        Map<String, Object> resultDataMap = new HashMap<>();
        synchronized (TcpResponseMessageHandler.class) {
            // 如果当前灯光考试项没有进行操作，则默认沿用上一次的灯光操作
            if (defaultResultMessage.equals(resultMessage) && beforeLightOperationName != null) {
                if (examOperationList.get(0).getName().equals(beforeLightOperationName)) {
                    // 并且是上一次灯光操作是当前灯光考试项的答案
                    resultDataMap.put("isCorrect", true);
                    resultDataMap.put("resultMessage", "正确" + examOperationList.get(0).getDescription());
                } else {
                    // 上一次灯光操作不适用与当前灯光考试项
                    resultDataMap.put("isCorrect", false);
                    resultDataMap.put("resultMessage", "错误" + ExamOperation.getOpMsgByOpName(beforeLightOperationName));
                }
            } else {
                resultDataMap.put("isCorrect", isCorrect);
                resultDataMap.put("resultMessage", resultMessage);
            }
            // 重置结果
            isCorrect = false;
            resultMessage = defaultResultMessage;
        }
        return resultDataMap;
    }

    /**
     * 开始一个道路考试项
     * @param examOperationList     该考试项包含的考试操作项
     */
    public static void startRoadExamItem(List<ExamOperation> examOperationList) {
        synchronized (TcpResponseMessageHandler.class) {
            isCorrect = false;
            hadResult = false;
            resultMessage = defaultResultMessage;
            TcpResponseMessageHandler.nextExamOperationIndex = 0;
            TcpResponseMessageHandler.examOperationList.clear();
            TcpResponseMessageHandler.examOperationList.addAll(examOperationList);
        }
    }

    /**
     * 获取RoadExamItem的结果
     */
    public static Map<String, Object> getRoadExamItemResult() {
        Map<String, Object> resultDataMap = new HashMap<>();
        synchronized (TcpResponseMessageHandler.class) {
            if (nextExamOperationIndex < examOperationList.size()) {
                // 操作不完整
                resultDataMap.put("isCorrect", false);
                resultDataMap.put("resultMessage", "未进行【" + examOperationList.get(nextExamOperationIndex).getDescription() + "】等操作");
            } else {
                resultDataMap.put("isCorrect", isCorrect);
                resultDataMap.put("resultMessage", resultMessage);
            }
            // 重置结果
            isCorrect = false;
            nextExamOperationIndex = null;
            resultMessage = defaultResultMessage;
            examOperationList.clear();
        }
        return resultDataMap;
    }

    /**
     * 处理远端返回的tcpResponseMessage
     * @param tcpResponseMessage    待处理的tcpResponseMessage
     */
    private static void handleTcpResponseMessage(TcpResponseMessage tcpResponseMessage) {
        if (TcpResponseMessage.RESPONSE_CHECK_IP_AND_MAC.equals(tcpResponseMessage.getTypeName())) {
            // 返回的mac、ip地址
            Map<String, String> dataMap = tcpResponseMessage.getDataMap();
            if (dataMap == null) {
                TcpRequestMessage tcpRequestMessage = new TcpRequestMessage();
                tcpRequestMessage.setTypeName("false");
                TcpNetWorkServiceThread.addTcpRequestMessage(tcpRequestMessage);
                // 发送车辆连接失败的通知
                ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.CAR_SETTING_RESULT);
                examUpdateUiBroadcastMessage.setMessage("连接车辆失败，对方未发送ip、mac地址进行验证！");
                Map<String, Object> messageDataMap = new HashMap<>();
                messageDataMap.put("isSuccess", false);
                examUpdateUiBroadcastMessage.setData(messageDataMap);
                ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
                return;
            }
            String ipAddress = dataMap.get("ipAddress");
            String macAddress = dataMap.get("macAddress");
            Car car = ExamItemProcess.car;
            TcpRequestMessage tcpRequestMessage = new TcpRequestMessage();
            if (car.getIpAddress().equals(ipAddress) && car.getMacAddress().equals(macAddress)) {
                tcpRequestMessage.setTypeName("true");
                TcpNetWorkServiceThread.addTcpRequestMessage(tcpRequestMessage);
                // 发送车辆连接成功的通知
                ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.CAR_SETTING_RESULT);
                examUpdateUiBroadcastMessage.setMessage("连接车辆成功，对方发送ip、mac地址通过验证！");
                Map<String, Object> messageDataMap = new HashMap<>();
                messageDataMap.put("isSuccess", true);
                examUpdateUiBroadcastMessage.setData(messageDataMap);
                ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
            } else {
                tcpRequestMessage.setTypeName("false");
                TcpNetWorkServiceThread.addTcpRequestMessage(tcpRequestMessage);
                // 发送车辆连接失败的通知
                ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.CAR_SETTING_RESULT);
                examUpdateUiBroadcastMessage.setMessage("连接车辆失败，对方发送ip、mac地址没有通过验证！");
                Map<String, Object> messageDataMap = new HashMap<>();
                messageDataMap.put("isSuccess", true);
                examUpdateUiBroadcastMessage.setData(messageDataMap);
                ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
            }
        } else if (TcpResponseMessage.RESPONSE_OPERATION_NAME.equals(tcpResponseMessage.getTypeName())) {
            String operationName = tcpResponseMessage.getExamItemOperationName().get(0);
            if (operationName == null) {
                return;
            }
            if (!isDoorClosed) {
                // 车门未关闭，就进行考试操作，零分
                ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_STOPPED_BY_DANGEROUS_OPERATION);
                examUpdateUiBroadcastMessage.setMessage("考试前未关上车门，考试成绩计零分！");
                ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
                return;
            } else if (!isSeatBeltFasten) {
                // 安全带未系，就进行考试操作，零分
                ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_STOPPED_BY_DANGEROUS_OPERATION);
                examUpdateUiBroadcastMessage.setMessage("考试前未系上安全带，考试成绩计零分！");
                ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
                return;
            }
            if (ExamOperation.isSetGearOperation(operationName)) {
                // 如果是换挡操作，更新当前档位
                if (!isStepOnClutchPedal) {
                    // 换挡没有踩住离合
                    ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                    examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_DEDUCT_POINT);
                    Map<String, Object> detailMap = new HashMap<>();
                    detailMap.put("score", 10);
                    examUpdateUiBroadcastMessage.setData(detailMap);
                    examUpdateUiBroadcastMessage.setMessage("没有踩住离合换挡，扣10分！");
                    ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
                    return;
                }
                nowGear = Gear.findGearByOperationName(gearList, operationName);
            } else if (ExamOperation.STEP_ON_CLUTCH_PEDAL.equals(operationName)) {
                // 踩住离合
                isStepOnClutchPedal = true;
            } else if (ExamOperation.STEP_OFF_CLUTCH_PEDAL.equals(operationName)) {
                // 松开离合
                isStepOnClutchPedal = false;
            }
            // 处理operation操作
            if (ExamItemProcess.isLightExaming) {
                // 正在灯光考试，只检测灯光操作，其它操作不做评判
                if (ExamOperation.isLightOperation(operationName)) {
                    if (operationName.equals(examOperationList.get(0).getName())) {
                        synchronized (TcpResponseMessageHandler.class) {
                            isCorrect = true;
                            // 保存当前的灯光操作
                            beforeLightOperationName = operationName;
                            resultMessage = "正确" + examOperationList.get(0).getDescription();
                        }
                    } else {
                        synchronized (TcpResponseMessageHandler.class) {
                            isCorrect = false;
                            // 保存当前的灯光操作
                            beforeLightOperationName = operationName;
                            resultMessage = "错误" + ExamOperation.getOpMsgByOpName(operationName);
                        }
                    }
                }
            } else if (ExamItemProcess.isExaming && nextExamOperationIndex != null) {
                // 道路考试状态下
                if (nextExamOperationIndex < examOperationList.size()) {
                    // 操作项列表未匹配完
                    if (operationName.equals(examOperationList.get(nextExamOperationIndex).getName())) {
                        synchronized (TcpResponseMessageHandler.class) {
                            // 成功匹配一个操作，下标后移
                            nextExamOperationIndex += 1;
                        }
                        if (nextExamOperationIndex == examOperationList.size()) {
                            // 刚好匹配完list，操作完美
                            synchronized (TcpResponseMessageHandler.class) {
                                hadResult = true;
                                nextExamOperationIndex = Integer.MAX_VALUE;
                                isCorrect = true;
                                resultMessage = "操作完美";
                            }
                        }
                    } else if (ExamOperation.SET_UP_GEAR.equals(examOperationList.get(nextExamOperationIndex).getName())) {
                        // 判断升档操作
                        if (nowGear != null && Gear.isSetUpGearOperationName(nowGear.getName(), operationName)) {
                            synchronized (TcpResponseMessageHandler.class) {
                                // 成功匹配一个操作，下标后移
                                nextExamOperationIndex += 1;
                            }
                        } else {
                            // 操作不匹配，直接结束
                            synchronized (TcpResponseMessageHandler.class) {
                                hadResult = true;
                                isCorrect = false;
                                if (nextExamOperationIndex == 0) {
                                    resultMessage = "未进行【" + examOperationList.get(0).getDescription() + "】操作";
                                } else {
                                    resultMessage = "【" + examOperationList.get(nextExamOperationIndex - 1).getDescription() + "】操作后未进行【" + examOperationList.get(nextExamOperationIndex).getDescription() + "】操作";
                                }
                                nextExamOperationIndex = Integer.MAX_VALUE;
                            }
                        }
                    } else if (ExamOperation.SET_DOWN_GEAR.equals(examOperationList.get(nextExamOperationIndex).getName())) {
                        // 判断降档操作
                        if (nowGear != null && Gear.isSeDownGearOperationName(nowGear.getName(), operationName)) {
                            synchronized (TcpResponseMessageHandler.class) {
                                // 成功匹配一个操作，下标后移
                                nextExamOperationIndex += 1;
                            }
                        } else {
                            // 操作不匹配，直接结束
                            synchronized (TcpResponseMessageHandler.class) {
                                hadResult = true;
                                isCorrect = false;
                                if (nextExamOperationIndex == 0) {
                                    resultMessage = "未进行【" + examOperationList.get(0).getDescription() + "】操作";
                                } else {
                                    resultMessage = "【" + examOperationList.get(nextExamOperationIndex - 1).getDescription() + "】操作后未进行【" + examOperationList.get(nextExamOperationIndex).getDescription() + "】操作";
                                }
                                nextExamOperationIndex = Integer.MAX_VALUE;
                            }
                        }
                    } else {
                        // 操作不匹配，直接结束
                        synchronized (TcpResponseMessageHandler.class) {
                            hadResult = true;
                            isCorrect = false;
                            if (nextExamOperationIndex == 0) {
                                resultMessage = "未进行【" + examOperationList.get(0).getDescription() + "】操作";
                            } else {
                                resultMessage = "【" + examOperationList.get(nextExamOperationIndex - 1).getDescription() + "】操作后未进行【" + examOperationList.get(nextExamOperationIndex).getDescription() + "】操作";
                            }
                            nextExamOperationIndex = Integer.MAX_VALUE;
                        }
                    }
                }
            }
        } else if (TcpResponseMessage.RESPONSE_BASE_STATE.equals(tcpResponseMessage.getTypeName())) {
            // 汇报车速、里程信息
            if (tcpResponseMessage.getExamItemOperationName() == null || tcpResponseMessage.getExamItemOperationName().size() == 0) {
                Map<String, String> dataMap = tcpResponseMessage.getDataMap();
                if (dataMap == null) {
                    return;
                }
                // 包含速度，则判断是否与档位匹配
                if (dataMap.containsKey("SPEED")) {
                    double speed = Double.parseDouble(dataMap.get("SPEED"));
                    if (nowGear != null && (speed < nowGear.getMinSpeed() || speed > nowGear.getMaxSpeed())) {
                        // 档位与速度不匹配
                        ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                        examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_DEDUCT_POINT);
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("score", 10);
                        examUpdateUiBroadcastMessage.setData(detailMap);
                        examUpdateUiBroadcastMessage.setMessage("【" + nowGear.getDescription() + "】档位与【" + speed + "km/h】速度不匹配，扣10分！");
                        ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
                    }
                }
                // 包含已行驶的路程
                if (dataMap.containsKey("DISTANCE_TRAVELED")) {

                }
                return;
            }
            // 处理汇报基本状态的tcpMessage
            String operationName = tcpResponseMessage.getExamItemOperationName().get(0);
            if (ExamOperation.CLOSE_DOOR.equals(operationName)) {
                // 关上了车门
                isDoorClosed = true;
            } else if (ExamOperation.FASTEN_SEAT_BELT.equals(operationName)) {
                // 系上了安全带
                isSeatBeltFasten = true;
            } else if (ExamOperation.OPEN_DOOR.equals(operationName)) {
                // 打开了车门
                if (ExamItemProcess.isExamStarted) {
                    // 发送停止考试更新ui的通知
                    ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                    examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_STOPPED_BY_DANGEROUS_OPERATION);
                    examUpdateUiBroadcastMessage.setMessage("考试过程中出现打开车门的危险操作，考试成绩计零分！");
                    ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
                } else {
                    isDoorClosed = false;
                }
            } else if (ExamOperation.UNFASTEN_SEAT_BELT.equals(operationName)) {
                // 解开了安全带
                if (ExamItemProcess.isExamStarted) {
                    // 发送停止考试更新ui的通知
                    ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage = new ExamUpdateUiBroadcastMessage();
                    examUpdateUiBroadcastMessage.setTypeName(ExamUpdateUiBroadcastMessage.EXAM_STOPPED_BY_DANGEROUS_OPERATION);
                    examUpdateUiBroadcastMessage.setMessage("考试过程中出现解安全带的危险操作，考试成绩计零分！");
                    ExamUpdateUiBroadcastUtil.sendBroadcast(ExamingActivity.examingActivity, examUpdateUiBroadcastMessage);
                } else {
                    isSeatBeltFasten = false;
                }
            }
        } else {
            System.err.println("未知类型的TcpResponseMessage");
        }
    }
}

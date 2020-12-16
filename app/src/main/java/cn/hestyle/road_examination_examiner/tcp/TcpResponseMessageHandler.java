package cn.hestyle.road_examination_examiner.tcp;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.hestyle.road_examination_examiner.ExamingActivity;
import cn.hestyle.road_examination_examiner.entity.Car;
import cn.hestyle.road_examination_examiner.entity.ExamUpdateUiBroadcastMessage;
import cn.hestyle.road_examination_examiner.util.ExamUpdateUiBroadcastUtil;
import cn.hestyle.tcp.TcpRequestMessage;
import cn.hestyle.tcp.TcpResponseMessage;

/**
 * TcpResponseMessage处理线程
 */
public class TcpResponseMessageHandler extends Thread {
    /** 单例对象 */
    private static TcpResponseMessageHandler tcpResponseMessageHandler = null;
    /** 车辆端返回的消息 */
    public static LinkedList<TcpResponseMessage> tcpResponseMessageLinkedList = new LinkedList<>();

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
                ExamItemProcess.restrictStopOtherThread();
            }
            tcpResponseMessageHandler = null;
            Log.i("TcpResponseHandler", "TcpResponseMessageHandler线程已停止！");
        }

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
        }
    }
}

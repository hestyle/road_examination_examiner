package cn.hestyle.road_examination_examiner.tcp;

import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

import cn.hestyle.road_examination_examiner.entity.Car;
import cn.hestyle.road_examination_examiner.ui.setting.SettingFragment;
import cn.hestyle.tcp.TcpRequestMessage;
import cn.hestyle.tcp.TcpResponseMessage;

/**
 * tcp网络服务线程
 */
public class TcpNetWorkServiceThread extends Thread {
    /** 单例对象 */
    private static TcpNetWorkServiceThread tcpNetWorkServiceThread = null;
    private static LinkedList<TcpRequestMessage> tcpRequestMessageLinkedList = new LinkedList<>();
    private static TcpObjectReadThread tcpObjectReadThread = null;

    private static Car car = null;

    private TcpNetWorkServiceThread() {}

    /**
     * 获取单例对象
     * @return  单例对象
     */
    public static TcpNetWorkServiceThread getSingleInstance(Car car) {
        synchronized (TcpNetWorkServiceThread.class) {
            if (tcpNetWorkServiceThread == null) {
                tcpNetWorkServiceThread = new TcpNetWorkServiceThread();
            }
        }
        TcpNetWorkServiceThread.car = car;
        tcpRequestMessageLinkedList.clear();
        return tcpNetWorkServiceThread;
    }

    @Override
    public void run() {
        super.run();
        Log.i("TcpServiceThread", "TcpServiceThread线程已启动！");
        try {
            Socket socket = new Socket(car.getIpAddress(), Integer.parseInt(SettingFragment.tcpServerPortString));
            socket.setKeepAlive(true);
            // 建立连接后，发送一个消息
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            final TcpRequestMessage tcpRequestMessage = new TcpRequestMessage();
            tcpRequestMessage.setDescription("第一个消息！");
            objectOutputStream.writeObject(tcpRequestMessage);
            // 等待对方，发一个消息
            final ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Log.i("TcpServiceThread", ((TcpResponseMessage)objectInputStream.readObject()).toString());
            // 启动tcp response消息处理线程
            TcpResponseMessageHandler responseMessageHandler = TcpResponseMessageHandler.getSingleInstance();
            responseMessageHandler.start();
            // objectInputStream会堵塞，所以单独放到一个线程
            tcpObjectReadThread = new TcpObjectReadThread(objectInputStream);
            tcpObjectReadThread.start();
            // 死循环，等待主线程关闭socket
            while (ExamItemProcess.isExamStarted || tcpRequestMessageLinkedList.size() != 0) {
                if (socket.isClosed()) {
                    throw new Exception("socket已关闭");
                }
                // 发message
                synchronized (TcpNetWorkServiceThread.class) {
                    // 发送队列中的第一个tcpMessage
                    if (tcpRequestMessageLinkedList.size() != 0) {
                        Log.i("TcpServiceThread", "发送object" + tcpRequestMessageLinkedList.getFirst().toString());
                        objectOutputStream.writeObject(tcpRequestMessageLinkedList.removeFirst());
                    }
                }
                sleep(200);
            }
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ExamItemProcess.isExamStarted) {
                Log.e("TcpServiceThread", "TcpServiceThread线程【非正常】停止！");
                // 非正常退出，tcp线程启动失败
                ExamItemProcess.immediateStopOtherThread("考试终止，TcpServiceThread线程与车辆的Tcp连接异常中断！");
            }
            tcpNetWorkServiceThread = null;
            Log.i("TcpServiceThread", "TcpServiceThread线程停止！");
        }
    }

    /**
     * 发送tcpRequestMessage
     * @param tcpRequestMessage    待发送的tcpRequestMessage
     */
    public static void addTcpRequestMessage(TcpRequestMessage tcpRequestMessage) {
        if (tcpRequestMessage == null) {
            return;
        }
        synchronized (TcpNetWorkServiceThread.class) {
            tcpRequestMessageLinkedList.addLast(tcpRequestMessage);
        }
    }

    /**
     * tcp网路中读取object
     */
    static class TcpObjectReadThread extends Thread {
        private ObjectInputStream objectInputStream;
        public TcpObjectReadThread(ObjectInputStream objectInputStream) {
            this.objectInputStream = objectInputStream;
        }
        @Override
        public void run() {
            Log.i("TcpObjectReadThread", "TcpObjectReadThread线程启动！");
            try {
                TcpResponseMessage tcpResponseMessage = null;
                while (ExamItemProcess.isExamStarted) {
                    tcpResponseMessage = (TcpResponseMessage) objectInputStream.readObject();
                    // 调用TcpResponseMessageHandler线程进行处理
                    if (tcpResponseMessage != null) {
                        System.out.println("收到object" + tcpResponseMessage.toString());
                        TcpResponseMessageHandler.addTcpResponseMessage(tcpResponseMessage);
                    }
                    sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ExamItemProcess.isExamStarted) {
                    Log.e("TcpObjectReadThread", "TcpObjectReadThread线程【非正常】停止！");
                    // 非正常退出，tcp线程启动失败
                    ExamItemProcess.immediateStopOtherThread("考试终止，TcpObjectReadThread线程与车辆的Tcp连接异常中断！");
                }
                tcpObjectReadThread = null;
                Log.i("TcpObjectReadThread", "TcpObjectReadThread线程停止！");
            }
        }
    }
}

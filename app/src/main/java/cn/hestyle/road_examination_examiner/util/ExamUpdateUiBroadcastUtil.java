package cn.hestyle.road_examination_examiner.util;

import android.content.Context;
import android.content.Intent;

import cn.hestyle.road_examination_examiner.entity.ExamUpdateUiBroadcastMessage;

/**
 * ExamUpdateUiBroadcast工具类
 */
public class ExamUpdateUiBroadcastUtil {
    /** intent action */
    public static final String EXAM_UPDATE_UI_THREAD_ACTION_TYPE = "cn.hestyle.road_examination_examiner.EXAM";

    /**
     * 发送ExamUpdateUiBroadcastMessage
     * @param context                       发送通知的上下文
     * @param examUpdateUiBroadcastMessage  消息
     */
    public static void sendBroadcast(Context context, ExamUpdateUiBroadcastMessage examUpdateUiBroadcastMessage) {
        Intent intent = new Intent();
        intent.setAction(EXAM_UPDATE_UI_THREAD_ACTION_TYPE);
        intent.putExtra(ExamUpdateUiBroadcastMessage.MESSAGE_NAME, examUpdateUiBroadcastMessage);
        context.sendBroadcast(intent);
    }
}

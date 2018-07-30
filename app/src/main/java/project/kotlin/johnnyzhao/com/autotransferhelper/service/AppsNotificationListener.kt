package project.kotlin.johnnyzhao.com.autotransferhelper.service

import android.annotation.SuppressLint
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.eightbitlab.rxbus.Bus
import project.kotlin.johnnyzhao.com.autotransferhelper.eventbus.Event
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.HelpUtils

@SuppressLint("OverrideAbstract")
class AppsNotificationListener : NotificationListenerService() {

    val TAG: String = "NotificationListener"

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        handlerNotificationPosted(sbn)
        //addStatusBarNotification(sbn)
    }

    fun handlerNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn != null) {
            val pack = sbn.packageName
            Log.d(TAG, "packageName: ${pack}")
            val extras = sbn.notification.extras
            val title = extras.getString("android.title")
            Log.d(TAG, "title: ${title}")
            val text = extras.getCharSequence("android.text").toString()
            Log.d(TAG, "text: ${text}")
            when (pack) {
                HelpUtils.ALIPAY_PACKAGE_NAME -> {
                    /////////////////////////转出钱后////////////////////////////
                    //title: 交易提醒
                    //text: 你有一笔0.01元的支出，点此查看详情。
                    if (title.equals("交易提醒") && text.indexOf("支出") != -1)
                        HelpUtils.uploadTransferOutRecord()

                    /////////////////////////收到钱后，开始做转出工作////////////////////////////
                    //title: 支付宝消息
                    //text: xxx已成功向你转了x笔钱
                    if (title.equals("支付宝消息") && text.indexOf("成功向你转了") != -1)
                        HelpUtils.receivedMoneyFromAlipay(this)
                }

                HelpUtils.WECHAT_PACKAGE_NAME -> {
                    ////////////////////转出钱并朋友确认收到钱后///////////////////
                    //title: 刘成朴
                    //text: [转账]朋友已确认收钱
//                    if (text.indexOf("朋友已确认收钱") != -1)不等好友确认才uploadTransferOutRecord，现在放在AutoTransferService.payNowByWechat()中
//                        HelpUtils.uploadTransferOutRecord()

                    /////////////////收到钱后，开始做转出工作////////////////
                    //以下好友间的转账
                    //title: 刘成朴
                    //text: [2条]刘成朴: [转账]请你确认收钱
                    //以下为别人扫码向我支付
                    //text: 收款到账通知
                    if (text.indexOf("请你确认收钱") != -1) {
                        try {
                            sbn.notification.contentIntent.send()
                            HelpUtils.threadSleep(1000L)
                            //用eventbus是因为通过contentIntent.send打开聊天列表,在onAccessibilityEvent里收不到com.tencent.mm.ui.chatting.ChattingUI类名
                            Bus.send(Event.ReceivedWechatMomey())
                        } catch (ex: Exception) {
                        }
                    } else if (text.indexOf("收款到账通知") != -1) {
                        //这里不用去确认收款，收款成功后，开始进入转账界面进行对好友的转出工作 (现在统一通过DoTaskRunnable处理)
                        HelpUtils.receivedMoneyFromWechat(this)
                        /*val transferOutAccount = HelpUtils.receivedMoneyFromWechat(this)
                        if (transferOutAccount.equals(""))
                            Toast.makeText(this, getString(R.string.none_transfer_out_wechat_account), Toast.LENGTH_SHORT).show()
                        else
                            HelpUtils.execShellCmd(InputTapUtils.ZTE_V0721_LAUNCH_WECHAT_PAY_CMD + transferOutAccount)
                            */
                    }
                }
            }
        }
    }
}
package project.kotlin.johnnyzhao.com.autotransferhelper.service

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.orhanobut.hawk.Hawk
import project.kotlin.johnnyzhao.com.autotransferhelper.MainActivity
import project.kotlin.johnnyzhao.com.autotransferhelper.MyApplication
import project.kotlin.johnnyzhao.com.autotransferhelper.R
import project.kotlin.johnnyzhao.com.autotransferhelper.eventbus.Event
import project.kotlin.johnnyzhao.com.autotransferhelper.task.TaskOperation
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.HelpUtils
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.InputTapUtils
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.NotificationUtils
import java.util.concurrent.atomic.AtomicInteger
import android.app.Service
import project.kotlin.johnnyzhao.com.autotransferhelper.database.DoingTaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.TimeUtils

class AutoTransferService : AccessibilityService() {
    val TAG: String = "AlipayTransferService"

    val handler = Handler()
    //val doTaskRunnable = DoTaskRunnable()
    var checkTaskRunnable: Runnable = object : Runnable {
        override fun run() {
            getTaskToDo()
            handler.postDelayed(this, 10000L)
        }
    }

    fun getTaskToDo() {
        try {
            Log.d(TAG, "查询是否有任务要做...")
            if (TaskOperation.instance.hasDoingTask() != null) {
                //如果在做的任务超过5分钟还没做完，说明出现问题，删除temp表数据，继续取任务做
                val doingTask = TaskOperation.instance.hasDoingTask()
                Log.d(TAG, "[有任务在做, task.id: ${doingTask?.doingTaskId}, insert time: ${doingTask?.insertTime}]")
                //946656000为2000-01-01的时间戳,当doingTask时就按这个时间去作对比
                if (TimeUtils.getTimeDiffInMins(doingTask?.insertTime ?: 946656000) > 5)
                    TaskOperation.instance.deleteDoingTask()
                return
            }
            //hasInputWechatPayMoney=false
            val taskDBO = TaskOperation.instance.getTopTask()
            if (taskDBO.id == -1L) return
            val doingTaskDBO = DoingTaskDBO()
            doingTaskDBO.doingTaskId = taskDBO.id
            doingTaskDBO.insertTime = System.currentTimeMillis() / 1000
            TaskOperation.instance.addDoingTask(doingTaskDBO)
            when (taskDBO.type) {
                TaskDBO.TaskTypeAlipay -> HelpUtils.gotoAlipay(MyApplication.getApplicationContext(this))
                TaskDBO.TaskTypeWechat -> {
                    HelpUtils.gotoWechatPay(this)
                    HelpUtils.threadSleep(200L)
                    //微信首页要先启动了，ZTE_V0721_LAUNCH_WECHAT_PAY_CMD才有效
                    HelpUtils.execShellCmd(InputTapUtils.ZTE_V0721_LAUNCH_WECHAT_PAY_CMD + taskDBO.account)
                }
            }
        } catch (ex: Exception) {
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AlipayTransferService onCreate")

        // foreground service
        val intent = Intent(this, FakeService::class.java)
        startService(intent)

        handler.post(checkTaskRunnable)

        //在AppsNotificationListener收到微信转账，通过Bus发送ReceivedWechatMomey到这里处理
        //执行 findAndClickTransToMeItemInWechat()
        Bus.observe<Event.ReceivedWechatMomey>().subscribe {

            findAndClickTransferToMeItemInWechat()
            Log.d(TAG, "Event.ReceivedWechatMomey")
            HelpUtils.threadSleep(1000L)
            //交易详情-待确认收款界面打开后
            HelpUtils.performClick(rootInActiveWindow, "com.tencent.mm:id/cwk")
            HelpUtils.threadSleep(1000L)
            //收款成功后，开始进入转账界面进行对好友的转出工作(现在统一通过 checkTaskRunnable 处理)
            HelpUtils.receivedMoneyFromWechat(this)
            /*val transferOutAccount = HelpUtils.receivedMoneyFromWechat(this)
            if (transferOutAccount.equals(""))
                Toast.makeText(this, getString(R.string.none_transfer_out_wechat_account), Toast.LENGTH_SHORT).show()
            else
                HelpUtils.execShellCmd(InputTapUtils.ZTE_V0721_LAUNCH_WECHAT_PAY_CMD + transferOutAccount)
                */
        }.registerInBus(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            handler.removeCallbacks(checkTaskRunnable)
            Bus.unregister(this)
        } catch (ex: Exception) {
        }
        //stopForeground(true)
//        if (mWakeLock != null) {
//            if (mWakeLock.isHeld())
//                mWakeLock.release()
//        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected")
//        if (mWakeLock != null)
//            mWakeLock.acquire()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        //val eventType = event.eventType
        val classNameChr = event.className
        val className = classNameChr.toString()

//        Log.d(TAG, "eventType:${eventType}")
        Log.d(TAG, "className:${className}")

        val taskDBO = TaskOperation.instance.getTopTask()
        Log.d(TAG, "taskObject taskId: ${taskDBO.id}, isDoneTransfer: ${taskDBO.isTransfered}, account: ${taskDBO.account}, money: ${taskDBO.money}")
        if (taskDBO.id == -1L) return //important!
        openTransfer(event)//for alipay
        //when (eventType) {
        //   AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
        when (className.trim()) {
            "com.alipay.mobile.transferapp.ui.TransferHomeActivity_" -> toAccount()
            "com.alipay.mobile.transferapp.ui.TFToAccountInputActivity_" -> enterAccount(taskDBO.account)
            "com.alipay.mobile.transferapp.ui.TFToAccountConfirmActivity_" -> confirmTransfer(taskDBO.money, "remark")
            "com.alipay.android.app.settings.widget.MiniProgressDialog" -> payNowByAlipay()
        ////////////////////////below for wechat////////////////////
        //在联系人界面上点击'发信息'按钮
        //    "com.tencent.mm.plugin.profile.ui.ContactInfoUI" -> HelpUtils.performClick(rootInActiveWindow, "com.tencent.mm:id/ana")
        //当微信收到转账号给我后，点击intent后会打后聊天界面
            //"com.tencent.mm.ui.chatting.ChattingUI" -> findAndClickTransferToMeItemInWechat()
        //在联系人转账界面输入金额
            "com.tencent.mm.plugin.remittance.ui.RemittanceUI" -> confirmTransferInWechat(taskDBO.money)
        //输入密码界面输入密码
            "com.tencent.mm.plugin.wallet_core.ui.n" -> payNowByWechat(Hawk.get(MyApplication.LOCAL_LOGIN_WECHAT_ACCOUNT_PAY_PASSWORD))
        //待对方确认收钱的界面
            "com.tencent.mm.plugin.remittance.ui.RemittanceResultNewUI" -> {
                //完成按钮的bounds[295,1614][784,1710]
            }
        //我的确认收款界面，点击"确认收款"
        /*"com.tencent.mm.ui.base.r","com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI" -> {
             HelpUtils.performClick(rootInActiveWindow, "com.tencent.mm:id/cwk")
             HelpUtils.threadSleep(1000L)
             //收款成功后，开始进入转账界面进行对好友的转出工作
             HelpUtils.execShellCmd(InputTapUtils.ZTE_V0721_LAUNCH_WECHAT_PAY_CMD + taskDBO.account)
         }*/
        }
        //}
        //}//end when (eventType)

    }

    override fun onInterrupt() {
    }

    //在联系人界面上click"转账给你"的item
    fun findAndClickTransferToMeItemInWechat() {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            //val listNode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/y")
            val listNode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ad8")
            if (listNode != null && listNode.size > 0) {
                //val msgNodes = listNode[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ad8")
                //if (msgNodes != null && msgNodes.size > 0) {
                for (rpNode in listNode) {
                    val rpStatusNode = rpNode.findAccessibilityNodeInfosByText("转账给你")
                    if (rpStatusNode != null && rpStatusNode.size > 0) {
                        rpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        break
                    }
                }
            }
//            }
        }
    }

    fun openTransfer(event: AccessibilityEvent) {
        //val rootNode = rootInActiveWindow
        val rootNode = event.source
        if (rootNode === null) return
        val listNode = rootNode.findAccessibilityNodeInfosByViewId("com.alipay.android.phone.openplatform:id/home_apps_grid")
        if (listNode != null && listNode.size > 0) {
            for (rpNode in listNode) {
                if (rpNode != null) {
                    //id为home_app_view的所有view的集合
                    val menus = rpNode.findAccessibilityNodeInfosByViewId("com.alipay.android.phone.openplatform:id/home_app_view")
                    if (menus != null) {
                        if (menus.size == 12) {
                            val menuTransfer = menus[0]
                            menuTransfer.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            break
                        }
                    }
                }
            }//end for
        }
    }

    //打开"转到支付宝账号"
    fun toAccount() {
        //com.alipay.mobile.transferapp:id/to_account_head
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            try {
                //com.alipay.mobile.transferapp:id/to_account
                /*val open = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.transferapp:id/to_account")[0]
                if (open != null)
                    open.performAction(AccessibilityNodeInfo.ACTION_CLICK)*/
                HelpUtils.performClick(nodeInfo, "com.alipay.mobile.transferapp:id/to_account")
            } catch (ex: Exception) {
            }
        }
    }


    //在"转到支付宝账号"界面输入账号
    fun enterAccount(account: String) {
        //editText id: com.alipay.mobile.ui:id/content
        //next btn id: com.alipay.mobile.transferapp:id/tf_toAccountNextBtn
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            try {
                val nodeInfoEditText = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.ui:id/content")[0]
                HelpUtils.performInputClean(this, nodeInfoEditText)
                HelpUtils.performInput(this, nodeInfoEditText, "account", account)
                /*val nodeInfoBtn = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.transferapp:id/tf_toAccountNextBtn")[0]
                if (nodeInfoBtn != null) {
                    HelpUtils.threadSleep100L()
                    nodeInfoBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }*/
                HelpUtils.performClick(nodeInfo, "com.alipay.mobile.transferapp:id/tf_toAccountNextBtn")
            } catch (ex: Exception) {
            }
        }
    }

    fun confirmTransferInWechat(money: String) {
        //com.tencent.mm:id/bt
        //btn:  com.tencent.mm:id/ak_
        //if (hasInputWechatPayMoney) return
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            try {
                //输入金额
                var nodeInfoEditText: List<AccessibilityNodeInfo>
                nodeInfoEditText = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bt")
                if (nodeInfoEditText != null && nodeInfoEditText.size == 0)
                    nodeInfoEditText = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bx")
                var nodeInfoEditTextMoney = nodeInfoEditText[0]
                HelpUtils.performInputClean(this, nodeInfoEditTextMoney)
                HelpUtils.performInput(this, nodeInfoEditTextMoney, "money", money)
                HelpUtils.threadSleep500L()
                //点击按钮
                var nodeInfoBtns: List<AccessibilityNodeInfo>
                nodeInfoBtns = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/akb")
                if (nodeInfoBtns != null && nodeInfoBtns.size == 0)
                    nodeInfoBtns = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ak_")
                var nodeInfoBtn = nodeInfoBtns[0]
                if (nodeInfoBtn != null) {
                    HelpUtils.threadSleep100L()
                    nodeInfoBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    //hasInputWechatPayMoney=true
                }
                //HelpUtils.performClick(nodeInfo, "com.tencent.mm:id/akb")
            } catch (ex: Exception) {
                //HelpUtils.performClick(nodeInfo, "com.tencent.mm:id/ak_")
                Log.e(TAG, "confirmTransfer exception: ${ex}")
            }
        }
    }

    //在支付宝输入转账金额和备注
    fun confirmTransfer(money: String, remark: String) {
        //edittext id: com.alipay.mobile.antui:id/amount_edit
        //confirm button id: com.alipay.mobile.transferapp:id/tf_nextBtn
        //com.alipay.mobile.transferapp:id/remarkEdit
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            try {
                //输入金额
                val nodeInfoEditTextMoney = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.antui:id/amount_edit")[0]
                HelpUtils.performInputClean(this, nodeInfoEditTextMoney)
                HelpUtils.performInput(this, nodeInfoEditTextMoney, "money", money)
                HelpUtils.threadSleep500L()
                //输入备注
                val nodeInfoEditTextRemark = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.transferapp:id/remarkEdit")[0]
                HelpUtils.performInputClean(this, nodeInfoEditTextRemark)
                HelpUtils.performInput(this, nodeInfoEditTextRemark, "remark", remark)
                //点击"确认转账"
                /*val nodeInfoBtn = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.transferapp:id/tf_nextBtn")[0]
                if (nodeInfoBtn != null) {
                    HelpUtils.threadSleep100L()
                    nodeInfoBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }*/
                HelpUtils.performClick(nodeInfo, "com.alipay.mobile.transferapp:id/tf_nextBtn")
            } catch (ex: Exception) {
                Log.e(TAG, "confirmTransfer exception: ${ex}")
            }
        }
    }

    //微信马上支付
    fun payNowByWechat(password: String) {
        //因为微信的密码输入框拿不了resId,所以用input tap的方式输入
        InputTapUtils.inputWechatPayPassword(this, password)
        HelpUtils.threadSleep500L()
        //当输完密码后，因为微信转账要等对方确认收钱，不知几时对方才确认，所以我们这里删除doingtask，不等对方确认再删除doingtask
        HelpUtils.uploadTransferOutRecord()
        Log.d(TAG, "payNowByWechat")
    }

    //支付宝马上支付
    fun payNowByAlipay() {
        HelpUtils.threadSleep500L()
        try {
            HelpUtils.execShellCmd(InputTapUtils.ZTE_V0721_CLICK_BOTTOM_FOR_ALIPAY_CMD)
        } catch (ex: Exception) {
            Log.e(TAG, "payNow exception: ${ex}")
        }
    }

    //click back button
    fun performBackClick() {
        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 300L)
    }

    //唤醒屏幕和解锁
    /*fun wakeAndUnlock(unLock: Boolean) {
        if (unLock) {
            //若为黑屏状态则唤醒屏幕
            if (!pm.isScreenOn()) {
                //获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
                wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright")
                //点亮屏幕
                wl?.acquire()
            }
            //若在锁屏界面则解锁直接跳过锁屏
            if (km.inKeyguardRestrictedInputMode()) {
                //设置解锁标志，以判断抢完红包能否锁屏
                enableKeyguard = false
                //解锁
                kl.disableKeyguard()
                Log.d(TAG, "解锁")
            }
        } else {
            //如果之前解过锁则加锁以恢复原样
            if (!enableKeyguard) {
                //锁屏
                kl.reenableKeyguard()
                Log.d(TAG, "加锁")
            }
            //若之前唤醒过屏幕则释放之使屏幕不保持常亮
            if (wl != null) {
                wl?.release()
                wl = null
                Log.d(TAG, "关灯")
            }
        }
    }*/
}
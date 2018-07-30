package project.kotlin.johnnyzhao.com.autotransferhelper.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import project.kotlin.johnnyzhao.com.autotransferhelper.MyApplication
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.orhanobut.hawk.Hawk
import project.kotlin.johnnyzhao.com.autotransferhelper.R
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.http.APIInterface
import project.kotlin.johnnyzhao.com.autotransferhelper.http.BaseCallBack
import project.kotlin.johnnyzhao.com.autotransferhelper.task.TaskOperation
import project.kotlin.johnnyzhao.com.autotransferhelper.task.TransferRecordOperation


class HelpUtils {

    companion object {//花括号里的相当于java的static方法

        const val ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone"
        const val WECHAT_PACKAGE_NAME = "com.tencent.mm"

        fun gotoAlipay(context: Context) {
            val packageName = ALIPAY_PACKAGE_NAME
            val intent = context.getPackageManager().getLaunchIntentForPackage(packageName)
            if (intent != null)
                context.startActivity(intent)
        }

        fun gotoWechatPay(context: Context) {
            val packageName = WECHAT_PACKAGE_NAME
            val intent = context.getPackageManager().getLaunchIntentForPackage(packageName)
            if (intent != null)
                context.startActivity(intent)
        }

        fun execShellCmd(cmd: String) {
            var su: Process? = null
            try {
                su = Runtime.getRuntime().exec("su")
                su.outputStream.write(cmd.toByteArray())
                su.outputStream.write("\n".toByteArray())
                su.outputStream.write("exit\n".toByteArray())
                su.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (su != null)
                    su.destroy()
            }
        }

        fun performInputClean(context: Context, nodeInfo: AccessibilityNodeInfo) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val clipboard = MyApplication.getApplicationContext(context).getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("clean", "")
                clipboard.primaryClip = clip
                threadSleep100L()
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                threadSleep100L()
            }
        }

        fun performInput(context: Context, nodeInfo: AccessibilityNodeInfo, label: String, text: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val clipboard = MyApplication.getApplicationContext(context).getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, text)
                clipboard.primaryClip = clip
                threadSleep100L()
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            }
        }

        fun performClick(nodeInfo: AccessibilityNodeInfo, resId: String) {
            if (nodeInfo != null) {
                val nodeInfoBtns = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
                if (nodeInfoBtns != null && nodeInfoBtns.size > 0) {
                    val nodeInfoBtn = nodeInfo.findAccessibilityNodeInfosByViewId(resId)[0]
                    if (nodeInfoBtn != null) {
                        HelpUtils.threadSleep100L()
                        nodeInfoBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
        }

        fun threadSleep(millis: Long) {
            try {
                Thread.sleep(millis)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        fun threadSleep100L() {
            threadSleep(100L);
        }

        fun threadSleep500L() {
            threadSleep(500L);
        }

        fun receivedMoneyFromAlipay(context: Context) {
            val transferOutAccount = TransferRecordOperation.instance.getAccountByType(TaskDBO.TaskTypeAlipay)
            if (transferOutAccount == null) {
                Toast.makeText(context,
                        context.getString(R.string.none_transfer_out_alipay_account), Toast.LENGTH_SHORT).show()
                return
            }
            val taskObject = TaskDBO()
            taskObject.account = transferOutAccount.account
            taskObject.money = transferOutAccount.money
            taskObject.type = transferOutAccount.type
            //添加要处理的任务
            val id = TaskOperation.instance.add(taskObject)
            //上报到账记录
            val uploadRecord = APIInterface.create().uploadTransferToMeRecord(
                    Hawk.get(MyApplication.LOCAL_LOGIN_ALIPAY_ACCOUNT_KEY, ""),
                    id.toString(),
                    taskObject.money,
                    taskObject.type,
                    System.currentTimeMillis()
            )
            uploadRecord.enqueue(object : BaseCallBack<Boolean>() {
                override fun onSuccess(response: Boolean?) {
                }

                override fun onFail(msg: String) {
                }
            })
            //打开支付宝客户端进行转出工作，通过DoTaskRunnable去处理现在
            /*try {
                HelpUtils.gotoAlipay(context)
            } catch (ex: Exception) {
            }*/
        }

        fun receivedMoneyFromWechat(context: Context) {
            val transferOutAccount = TransferRecordOperation.instance.getAccountByType(TaskDBO.TaskTypeWechat)
            //if (transferOutAccount == null) return
            if (transferOutAccount == null) {
                Toast.makeText(context,
                        context.getString(R.string.none_transfer_out_wechat_account), Toast.LENGTH_SHORT).show()
                return
            }
            val taskObject = TaskDBO()
            taskObject.account = transferOutAccount.account
            taskObject.money = transferOutAccount.money
            taskObject.type = transferOutAccount.type
            //添加要处理的任务
            val id = TaskOperation.instance.add(taskObject)
            //上报到账记录
            val uploadRecord = APIInterface.create().uploadTransferToMeRecord(
                    Hawk.get(MyApplication.LOCAL_LOGIN_WECHAT_ACCOUNT_KEY, ""),
                    id.toString(),
                    taskObject.money,
                    taskObject.type,
                    System.currentTimeMillis()
            )
            uploadRecord.enqueue(object : BaseCallBack<Boolean>() {
                override fun onSuccess(response: Boolean?) {
                }

                override fun onFail(msg: String) {
                }
            })
            //返回要转出的账号
            //return taskObject.account
        }

        //上报转出的记录
        fun uploadTransferOutRecord() {
            val doneTaskDBO = TaskOperation.instance.doneTopTask()
            var doneAccountType: Int = -1
            if (doneTaskDBO != null) {
                doneAccountType = doneTaskDBO.type
                try {
                    //上报转出记录
                    var localTransOutAccount = ""
                    when (doneAccountType) {
                        TaskDBO.TaskTypeAlipay -> localTransOutAccount = Hawk.get(MyApplication.LOCAL_LOGIN_ALIPAY_ACCOUNT_KEY, "")
                        TaskDBO.TaskTypeWechat -> localTransOutAccount = Hawk.get(MyApplication.LOCAL_LOGIN_WECHAT_ACCOUNT_KEY, "")
                    }
                    var uploadRecord = APIInterface.create().uploadTransferRecord(
                            localTransOutAccount,
                            doneTaskDBO.account,
                            doneTaskDBO.id.toString(),
                            doneTaskDBO.money,
                            doneTaskDBO.type,
                            1,
                            System.currentTimeMillis()
                    )
                    uploadRecord.enqueue(object : BaseCallBack<Boolean>() {
                        override fun onSuccess(response: Boolean?) {
                            //Log.d(TAG, getString(R.string.log_transfer_out_record_uploaded))
                        }

                        override fun onFail(msg: String) {
                        }
                    })
                } catch (ex: Exception) {
                }
            }

            HelpUtils.threadSleep(500L)

            when (doneAccountType) {
                TaskDBO.TaskTypeAlipay -> alipayTransferSuccessful()
                TaskDBO.TaskTypeWechat -> wechatTransferSuccessful()
            }
        }//end uploadTransferOutRecord

        //当输完密码后，因为微信转账要等对方确认收钱，不知几时对方才确认，所以我们这里删除doingtask，不等对方确认再删除doingtask
        fun wechatTransferSuccessful() {
            //按4次返回
            for (i in 0..3) {
                HelpUtils.execShellCmd("input keyevent 4")
                HelpUtils.threadSleep(1000L)
            }
            TaskOperation.instance.deleteDoingTask()
        }

        //支付宝转账给别人成功后
        fun alipayTransferSuccessful() {
            //[45,1186][98,1267],  这个是中兴的
            //在成功转账界面，按1次返回，回到与转账人的记录列表
            HelpUtils.execShellCmd("input keyevent 4")
            HelpUtils.threadSleep(1000L)
            //在转账人的记录列表，按1次返回，回到朋友tab
            HelpUtils.execShellCmd("input keyevent 4")
            HelpUtils.threadSleep(1000L)
            //点击"首页",返回主页的tab,因为这样做后，下次再打开时，才能在首页tab显示，因为每次操作的第一步是从首页tab开始
            HelpUtils.execShellCmd(InputTapUtils.ZTE_V0721_CLICK_BOTTOM_FOR_ALIPAY_CMD)
            //Log.d(TAG, "click home tab")
            //note !!这时回到首页再继续触发支付！！所以加了任务列表判断
            HelpUtils.threadSleep(1000L)
            //按1次返回
            HelpUtils.execShellCmd("input keyevent 4")
            HelpUtils.threadSleep500L()
            //按1次返回
            HelpUtils.execShellCmd("input keyevent 4")

            TaskOperation.instance.deleteDoingTask()
        }
    }
}
package project.kotlin.johnnyzhao.com.autotransferhelper

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife

import butterknife.OnClick
import com.eightbitlab.rxbus.Bus
import com.orhanobut.hawk.Hawk
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TransferOutAccountDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.eventbus.Event
import project.kotlin.johnnyzhao.com.autotransferhelper.http.APIInterface
import project.kotlin.johnnyzhao.com.autotransferhelper.task.TaskOperation
import project.kotlin.johnnyzhao.com.autotransferhelper.task.dataobject.TransferOutInfo
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.HelpUtils
import project.kotlin.johnnyzhao.com.autotransferhelper.http.BaseCallBack
import project.kotlin.johnnyzhao.com.autotransferhelper.http.BaseCallModel
import project.kotlin.johnnyzhao.com.autotransferhelper.task.TransferRecordOperation
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.InputTapUtils


class MainActivity : AppCompatActivity() {

    val TAG: String = "MainActivity"

    @BindView(R.id.btnOpenService)
    lateinit var btnOpenService: Button
    @BindView(R.id.btnOpenNotification)
    lateinit var btnOpenNotification: Button
    @BindView(R.id.btnPrint)
    lateinit var btnPrint: Button
    @BindView(R.id.btnTest)
    lateinit var btnTest: Button
    @BindView(R.id.btnTestWechat)
    lateinit var btnTestWechat: Button
    @BindView(R.id.tvNote)
    lateinit var tvNote: TextView
    @BindView(R.id.etTransferOutAlipayAccount)
    lateinit var etTransferOutAlipayAccount: EditText
    @BindView(R.id.etTransferOutWechatAccount)
    lateinit var etTransferOutWechatAccount: EditText
    @BindView(R.id.etTransferOutWechatPayPassword)
    lateinit var etTransferOutWechatPayPassword: EditText
    @BindView(R.id.btnOk)
    lateinit var btnOk: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this@MainActivity)

        val transferOutInfoRequest = APIInterface.create().requestTransferOutInfo(Hawk.get(MyApplication.LOCAL_LOGIN_ALIPAY_ACCOUNT_KEY,"").trim())
        transferOutInfoRequest.enqueue(object : BaseCallBack<List<TransferOutInfo>>() {
            override fun onSuccess(response: List<TransferOutInfo>?) {
                if (response == null) return
                for (transferOutInfo in response) {
                    if (TransferRecordOperation.instance.userExist(transferOutInfo.account)) continue
                    val transferOutAccountDBO = TransferOutAccountDBO()
                    transferOutAccountDBO.account = transferOutInfo.account
                    transferOutAccountDBO.money = transferOutInfo.price.toString()
                    transferOutAccountDBO.type = transferOutInfo.account_type
                    TransferRecordOperation.instance.addTransferOutUser(transferOutAccountDBO)
                }
            }
            override fun onFail(msg: String) {
                Log.e(TAG, "TransferOutInfoRequest onFail: ${msg}")
            }
        })

        val alipayAccount: String = Hawk.get(MyApplication.LOCAL_LOGIN_ALIPAY_ACCOUNT_KEY,"")
        etTransferOutAlipayAccount.setText(alipayAccount)
        val wechatAccount: String = Hawk.get(MyApplication.LOCAL_LOGIN_WECHAT_ACCOUNT_KEY,"")
        etTransferOutWechatAccount.setText(wechatAccount)//用于微信转出的账号
        val wechatPayPassword: String = Hawk.get(MyApplication.LOCAL_LOGIN_WECHAT_ACCOUNT_PAY_PASSWORD,"")//用于微信转出的支付密码
        etTransferOutWechatPayPassword.setText(wechatPayPassword)
    }

    override fun onResume() {
        super.onResume()
        val sb = StringBuilder()
        sb.append("服务器没有配置: ")
        if (TransferRecordOperation.instance.notSetTransferOutAlipayAccount())
            sb.append("支付宝转出账号")
        if (TransferRecordOperation.instance.notSetTransferOutAlipayAccount())
            sb.append(" 微信转出账号")
        tvNote.visibility = if (TransferRecordOperation.instance.notSetTransferOutAlipayAccount() ||
                TransferRecordOperation.instance.notSetTransferOutAlipayAccount()) View.VISIBLE else View.GONE
    }

    @OnClick(R.id.btnOpenService, R.id.btnPrint, R.id.btnOpenNotification, R.id.btnTest, R.id.btnTestWechat, R.id.btnOk)
    fun onClick(view: View) {
        when (view.id) {
            R.id.btnOk -> {
                if (etTransferOutAlipayAccount.text.toString().equals("") ||
                        etTransferOutWechatAccount.text.toString().equals("") ||
                        etTransferOutWechatPayPassword.text.toString().equals("")) {
                    Toast.makeText(this, "请输入全部所需信息", Toast.LENGTH_LONG).show()
                    return
                }
                Hawk.put(MyApplication.LOCAL_LOGIN_ALIPAY_ACCOUNT_KEY, etTransferOutAlipayAccount.text.toString())
                Hawk.put(MyApplication.LOCAL_LOGIN_WECHAT_ACCOUNT_KEY, etTransferOutWechatAccount.text.toString())
                Hawk.put(MyApplication.LOCAL_LOGIN_WECHAT_ACCOUNT_PAY_PASSWORD, etTransferOutWechatPayPassword.text.toString())
                Toast.makeText(this, "设置成功", Toast.LENGTH_LONG).show()
                finish()
            }
            R.id.btnOpenNotification -> openNotificationSettings()
            R.id.btnOpenService -> openAccessibilityServiceSettings()
            R.id.btnPrint -> TaskOperation.instance.printTaskList()
            R.id.btnTestWechat, R.id.btnTest -> {
                var transferOutAccount = TransferRecordOperation.instance.getAccountByType(if (view.id == R.id.btnTest) TaskDBO.TaskTypeAlipay else TaskDBO.TaskTypeWechat)
                var toastStr = if (view.id == R.id.btnTest)
                    getString(R.string.none_transfer_out_alipay_account)
                else
                    getString(R.string.none_transfer_out_wechat_account)
                if (transferOutAccount == null) {
                    Toast.makeText(this, toastStr, Toast.LENGTH_SHORT).show()
                    return
                }
                var taskObject = TaskDBO()
                taskObject.account = transferOutAccount.account
                taskObject.money = transferOutAccount.money
                taskObject.type = transferOutAccount.type
                val id = TaskOperation.instance.add(taskObject)
            }
        }
    }

    fun openNotificationSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 打开辅助服务的设置
    fun openAccessibilityServiceSettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

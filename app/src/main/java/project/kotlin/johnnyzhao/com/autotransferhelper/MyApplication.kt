package project.kotlin.johnnyzhao.com.autotransferhelper

import android.app.Application
import android.content.Context
import com.orhanobut.hawk.Hawk
import com.raizlabs.android.dbflow.config.FlowManager

class MyApplication : Application() {

    companion object {
        //use companion object to do static
        val LOCAL_NOTIFICATION_LIST_KEY = "local_notification_list_key"
        val LOCAL_LOGIN_ALIPAY_ACCOUNT_KEY = "local_alipay_account_key"
        val LOCAL_LOGIN_WECHAT_ACCOUNT_KEY = "local_wechat_account_key"
        val LOCAL_LOGIN_WECHAT_ACCOUNT_PAY_PASSWORD = "local_wechat_account_pay_password_key"
        //val LOCAL_WECHAT_TRANSFER_OUT_ACCOUNT_KEY = "wechat_transfer_out_account_key"

        fun getApplicationContext(context: Context): Context {
            return context.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (!Hawk.isBuilt()) Hawk.init(this).build()
//        Hawk.put(LOCAL_LOGIN_ALIPAY_ACCOUNT_KEY, "zmq13@163.com")//用于支付宝转出的账号
//        Hawk.put(LOCAL_LOGIN_WECHAT_ACCOUNT_KEY, "minandroid")//用于微信转出的账号
//        Hawk.put(LOCAL_LOGIN_WECHAT_ACCOUNT_PAY_PASSWORD, "090401")//用于微信转出的支付密码
        FlowManager.init(this)
    }
}
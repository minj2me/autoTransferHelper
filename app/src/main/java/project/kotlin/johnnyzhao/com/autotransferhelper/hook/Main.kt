package project.kotlin.johnnyzhao.com.autotransferhelper.hook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.orhanobut.hawk.Hawk
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.HelpUtils

@Deprecated("暂不使用Xposed")
class  Main : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("autotransferhelper Xposed load app: " + lpparam.packageName)
        when (lpparam.packageName) {
            HelpUtils.WECHAT_PACKAGE_NAME -> {
                XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI",
                        lpparam.classLoader, "onCreate", Bundle::class.java, object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        val activity = param!!.thisObject as Activity
                        if (activity != null) {
                            if (!Hawk.isBuilt()) Hawk.init(activity).build()
                            val intent = activity.intent
                            if (intent != null) {
                                val className = intent.component!!.className
                                if (!TextUtils.isEmpty(className) && className == "com.tencent.mm.ui.LauncherUI") {
                                    val jumpIntent = Intent()
                                    /*donateIntent.setClassName(activity, "com.tencent.mm.plugin.remittance.ui.RemittanceUI")
                                    donateIntent.putExtra("scene", 1)
                                    donateIntent.putExtra("pay_scene", 32)
                                    donateIntent.putExtra("fee", 0.01)
                                    donateIntent.putExtra("pay_channel", 13)
                                    donateIntent.putExtra("receiver_name", "x229706987")*/
                                    jumpIntent.setClassName(activity, "com.tencent.mm.plugin.profile.ui.ContactInfoUI")
                                    jumpIntent.putExtra("Contact_User", "")
//                                    var account:String = Hawk.get(MyApplication.LOCAL_WECHAT_TRANSFER_OUT_ACCOUNT_KEY)
//                                    jumpIntent.putExtra("Contact_User", account)
                                    activity.startActivity(jumpIntent)
                                    activity.finish()
                                }
                            }
                        }
                    }

                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        super.beforeHookedMethod(param)
                    }
                })
            }
        }
    }
}
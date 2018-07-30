package project.kotlin.johnnyzhao.com.autotransferhelper.utils

import android.content.Context
import android.widget.Toast
import project.kotlin.johnnyzhao.com.autotransferhelper.R

class InputTapUtils {
    companion object {
        //[30,1166][690,1250],  这个是中兴的 ZTE C880S
        //[45,1749][1035,1875], 这个是中兴的 ZTE V0721
        //const val ZX_CLICK_BOTTOM_FOR_ALIPAY_CMD = "input tap 50 1200"//这个是中兴的 ZTE C880S
        ///////////////////////////适配ZTE V0721的input tap///////////////////////////
        const val ZTE_V0721_CLICK_BOTTOM_FOR_ALIPAY_CMD = "input tap 65 1800"//支付宝底部按钮
        //[294,1502][540,1772]//<--微信，在聊天界面点+号弹出的框后，账号按钮的bounds
        const val ZTE_V0721_WECHAT_BEGIN_TRANSFER_CMD = "input tap 310 1550"
        //[360,1739][719,1919]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_0_CMD = "input tap 380 1800"//加上偏移量,模拟在按钮范围内点击
        //[1,1201][360,1380]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_1_CMD = "input tap 20 1250"
        //[360,1201][719,1380]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_2_CMD = "input tap 380 1250"
        //[719,1201][1079,1380]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_3_CMD = "input tap 800 1250"
        //[1,1380][360,1559]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_4_CMD = "input tap 20 1450"
        //[360,1380][719,1559]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_5_CMD = "input tap 380 1450"
        //[719,1380][1079,1559]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_6_CMD = "input tap 800 1450"
        //[1,1559][360,1739]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_7_CMD = "input tap 20 1650"
        //[360,1559][719,1739]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_8_CMD = "input tap 380 1650"
        //[719,1559][1079,1739]
        const val ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_9_CMD = "input tap 750 1650"

        const val ZTE_V0721_LAUNCH_WECHAT_PAY_CMD =
                "am start -n com.tencent.mm/com.tencent.mm.plugin.remittance.ui.RemittanceUI -e scene 1 -e pay_scene 32 -e pay_channel 13 -e receiver_name "
        const val ZTE_V0721_LAUNCH_WECHAT_CONTACT_INFO_CMD =
                "am start -n com.tencent.mm/com.tencent.mm.plugin.profile.ui.ContactInfoUI -e Contact_User "

        fun inputWechatPayPassword(context: Context, password: String) {
            try {
                var chars = password.toCharArray()
                if (chars.size != 6) {
                    Toast.makeText(context, context.getString(R.string.note_wrong_wechat_password), Toast.LENGTH_SHORT).show()
                    return
                }
                for (ch in chars)
                    HelpUtils.execShellCmd(getWechatPayPasswordCmd(ch))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        private fun getWechatPayPasswordCmd(num: Char): String {
            when (num) {
                '0' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_0_CMD
                '1' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_1_CMD
                '2' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_2_CMD
                '3' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_3_CMD
                '4' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_4_CMD
                '5' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_5_CMD
                '6' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_6_CMD
                '7' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_7_CMD
                '8' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_8_CMD
                '9' -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_9_CMD
                else -> return ZTE_V0721_WECHAT_PAY_PASSWORD_NUM_0_CMD
            }
        }
    }
}
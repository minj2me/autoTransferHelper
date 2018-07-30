package project.kotlin.johnnyzhao.com.autotransferhelper.task

import android.util.Log
import com.raizlabs.android.dbflow.sql.language.SQLite
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TransferOutAccountDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TransferOutAccountDBO_Table

class TransferRecordOperation : ITransferRecordOperation {

    val TAG: String = "TransferRecordOperation"

    init {
    }

    private object Holder {
        val INSTANCE = TransferRecordOperation()
    }

    companion object {
        val instance: TransferRecordOperation by lazy { Holder.INSTANCE }
    }

    override fun addTransferOutUser(transferOutAccountDBO: TransferOutAccountDBO) {
        try {
            transferOutAccountDBO.insert()
            Log.d(TAG, "add transfer out user: ${transferOutAccountDBO.account}")
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    override fun userExist(account: String): Boolean {
        var transferOutAccountDBO = SQLite.select().from(TransferOutAccountDBO::class.java).where(TransferOutAccountDBO_Table.account.eq(account)).querySingle()
        return transferOutAccountDBO != null
    }

    override fun getAccountByType(type: Int): TransferOutAccountDBO? {
        try {
            return SQLite.select().from(TransferOutAccountDBO::class.java).where(TransferOutAccountDBO_Table.type.eq(type)).querySingle()
        } catch (ex: Exception) {
            return null
        }
    }

    override fun notSetTransferOutAlipayAccount(): Boolean {
        return getAccountByType(TaskDBO.TaskTypeAlipay)==null
    }

    override fun notSetTransferOutWechatAccount(): Boolean {
        return getAccountByType(TaskDBO.TaskTypeWechat)==null
    }
}
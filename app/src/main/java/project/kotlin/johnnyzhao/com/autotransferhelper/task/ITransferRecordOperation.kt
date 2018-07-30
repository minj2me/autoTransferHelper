package project.kotlin.johnnyzhao.com.autotransferhelper.task

import project.kotlin.johnnyzhao.com.autotransferhelper.database.TransferOutAccountDBO

interface ITransferRecordOperation {
    fun addTransferOutUser(transferOutAccountDBO: TransferOutAccountDBO)
    fun userExist(account: String): Boolean
    fun getAccountByType(type: Int): TransferOutAccountDBO?
    fun notSetTransferOutAlipayAccount():Boolean
    fun notSetTransferOutWechatAccount():Boolean
}
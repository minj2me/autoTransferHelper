package project.kotlin.johnnyzhao.com.autotransferhelper.database

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * 保存转出任务的流水记录
 * */
@Table(database = DBFlowDatabase::class)
class TaskDBO : BaseModel() {
    companion object {
        val TaskTypeAlipay: Int = 1
        val TaskTypeWechat: Int = 2
    }

    //at least one primary key required
    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0
    @Column
    var type: Int = TaskTypeAlipay
    @Column
    var account: String = ""
    @Column
    var money: String = "'"
    @Column
    var isTransfered: Boolean = false
}
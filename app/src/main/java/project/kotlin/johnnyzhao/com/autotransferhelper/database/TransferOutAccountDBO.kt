package project.kotlin.johnnyzhao.com.autotransferhelper.database

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * 保存转出账号记录
 * */
@Table(database = DBFlowDatabase::class)
class TransferOutAccountDBO : BaseModel() {
    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0
    @Column
    var type: Int = TaskDBO.TaskTypeAlipay
    @Column
    var account: String = ""
    @Column
    var money: String = ""
}
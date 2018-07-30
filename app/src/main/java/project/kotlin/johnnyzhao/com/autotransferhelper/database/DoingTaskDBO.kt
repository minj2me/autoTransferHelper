package project.kotlin.johnnyzhao.com.autotransferhelper.database

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * 当有记录在操作中，数据放在这个临时表，完成后删除，用于检测是否有数据在处理中
 * */
@Table(database = DBFlowDatabase::class)
class DoingTaskDBO : BaseModel() {
    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0
    @Column
    var doingTaskId: Long = 0
    @Column
    var insertTime: Long = 0
}
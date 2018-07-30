package project.kotlin.johnnyzhao.com.autotransferhelper.database

import com.raizlabs.android.dbflow.annotation.Database

@Database(name = DBFlowDatabase.NAME, version = DBFlowDatabase.VERSION)
class DBFlowDatabase {
    companion object {
        //数据库名称
        const  val NAME = "MobaseDataBase"
        //数据库版本号
        const val VERSION = 3
    }
}
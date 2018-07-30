package project.kotlin.johnnyzhao.com.autotransferhelper.task

import android.util.Log
import android.widget.Toast
import com.orhanobut.hawk.Hawk
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import project.kotlin.johnnyzhao.com.autotransferhelper.MyApplication
import project.kotlin.johnnyzhao.com.autotransferhelper.R
import project.kotlin.johnnyzhao.com.autotransferhelper.database.DoingTaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TaskDBO_Table
import java.util.*

class TaskOperation private constructor() : ITaskOperation {

    val TAG: String = "TaskOperation"

    init {
        //println("")
    }

    private object Holder {
        val INSTANCE = TaskOperation()
    }

    companion object {
        val instance: TaskOperation by lazy { Holder.INSTANCE }
    }

    @Synchronized
    override fun isEmpayTask(): Boolean {
        try {
            val taskDBOList = SQLite.select().from(TaskDBO::class.java).queryList()
            return taskDBOList.isEmpty()
        } catch (ex: Exception) {
            return true
        }
    }

    @Synchronized
    override fun add(taskDBO: TaskDBO): Long {
        try {
            taskDBO.insert()
            Log.d(TAG, "add taskId: ${taskDBO.id}")
            return taskDBO.id
            /*var taskEventBus = TaskEventBus()
            taskEventBus.toDoTask = taskDBO
            EventBus.getDefault().post(taskEventBus)
            Log.d(TAG, "add taskId: ${taskDBO.id}, and send taskEventBus")*/
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
            return 0
        }
    }

    @Synchronized
    override fun getTopTask(): TaskDBO {
        val taskDBONullData = TaskDBO()
        taskDBONullData.id = -1L
        try {
            val ascending = true
            //取出最先insert的一条没做转账处理的数据
            val taskDBO = SQLite.select().from(TaskDBO::class.java).where(TaskDBO_Table.isTransfered.eq(false)).orderBy(TaskDBO_Table.id, ascending).querySingle()
            return taskDBO ?: taskDBONullData//如果taskDBO为空，就返回taskDBONullData
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
            return taskDBONullData
        }
    }

    override fun doneTopTask(): TaskDBO? {
        try {
            val taskDBO = getTopTask()
            taskDBO.isTransfered = true
            taskDBO.update()
            Log.d(TAG, "doneTopTask taskId: ${taskDBO.id}")
            return taskDBO
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
            return null
        }
    }

    override fun printTaskList() {
        try {
            val list = SQLite.select().from(TaskDBO::class.java).orderBy(TaskDBO_Table.id, false).queryList()
            for (taskDBO in list) {
                Log.d(TAG, "taskList taskId: ${taskDBO.id}, isDoneTransfer: ${taskDBO.isTransfered}")
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    override fun hasDoingTask(): DoingTaskDBO? {
        try {
            val doingTaskDBO = SQLite.select().from(DoingTaskDBO::class.java).querySingle()
            return doingTaskDBO
        } catch (ex: Exception) {
            return null
        }
    }

    override fun deleteDoingTask(): Boolean {
        try {
            Delete.table(DoingTaskDBO::class.java)
            Log.d(TAG, "deleted doing task")
            return true
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
            return false
        }
    }

    override fun addDoingTask(doingTaskDBO: DoingTaskDBO): Long {
        try {
            doingTaskDBO.insert()
            Log.d(TAG, "add doing taskId: ${doingTaskDBO.doingTaskId}")
            return doingTaskDBO.doingTaskId
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
            return 0
        }
    }

    override fun ifExistSameTask(checkTaskDBO: TaskDBO): Boolean {
        val account = checkTaskDBO.account
        val type = checkTaskDBO.type
        val money = checkTaskDBO.money
        val taskDBO = SQLite.select().from(TaskDBO::class.java).where(TaskDBO_Table.type.eq(type)).and(TaskDBO_Table.account.eq(account)).and(TaskDBO_Table.money.eq(money)).and(TaskDBO_Table.isTransfered.eq(false)).querySingle()
        return taskDBO != null
    }
}
package project.kotlin.johnnyzhao.com.autotransferhelper.task

import project.kotlin.johnnyzhao.com.autotransferhelper.database.DoingTaskDBO
import project.kotlin.johnnyzhao.com.autotransferhelper.database.TaskDBO

interface ITaskOperation {
    fun add(taskDBO: TaskDBO): Long
    fun isEmpayTask(): Boolean
    fun getTopTask(): TaskDBO
    fun doneTopTask(): TaskDBO?
    fun printTaskList()
    fun hasDoingTask(): DoingTaskDBO?
    fun addDoingTask(doingTask: DoingTaskDBO): Long
    fun deleteDoingTask(): Boolean
    fun ifExistSameTask(taskDBO: TaskDBO):Boolean
}
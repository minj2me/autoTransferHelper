package project.kotlin.johnnyzhao.com.autotransferhelper.http

class BaseCallModel<T> {
    var code: Int = 0
    //var msg: String? = null
    var msg: T? = null
}
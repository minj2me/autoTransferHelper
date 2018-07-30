package project.kotlin.johnnyzhao.com.autotransferhelper.http

import retrofit2.Call
import retrofit2.Response

abstract class BaseCallBack<T> : retrofit2.Callback<BaseCallModel<T>> {
    val SERVER_SUCCESSFUL_CODE = 10000
    override fun onResponse(call: Call<BaseCallModel<T>>?, response: Response<BaseCallModel<T>>?) {
        if (response == null)
            onFail("system error")
        else {
            if (response.code() == 200) {
                val baseCallModel = response.body()
                if (baseCallModel == null)
                    onFail("system error")
                else {
                    val returnCode = baseCallModel.code
                    when (returnCode) {
                        SERVER_SUCCESSFUL_CODE ->
                            onSuccess(baseCallModel.msg)
                        else -> onFail(baseCallModel.msg.toString())
                    }
                }
            } else
                onFail(response.message())
        }
    }

    override fun onFailure(call: Call<BaseCallModel<T>>?, t: Throwable?) {
        /*if (t is SocketTimeoutException) {
     } else if (t is ConnectException) {
     } else if (t is RuntimeException) {
     }*/
        onFail(t?.message ?: "system error")//<-如果t不为空就返回message,否则返回error
    }

    abstract fun onSuccess(response: T?)
    abstract fun onFail(msg: String)
}
package project.kotlin.johnnyzhao.com.autotransferhelper.http

import com.google.gson.GsonBuilder
import okhttp3.Credentials
import project.kotlin.johnnyzhao.com.autotransferhelper.task.dataobject.TransferOutInfo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient

interface APIInterface {

    companion object {
        fun create(): APIInterface {
            val gsonBuilder = GsonBuilder()

//            gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//            gsonBuilder.registerTypeAdapter(Model.User::class.java, UserDeserializer)

            val authToken = Credentials.basic("admin", "s^&R@Gl9")
            val interceptor = AuthenticationInterceptor(authToken)
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(interceptor)
            val restAdapter = Retrofit.Builder()
                    .baseUrl("http://192.168.1.115:5000/api/")
                    .client(httpClient.build())
                    //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build()

            return restAdapter.create(APIInterface::class.java)
        }
    }

    //获取转出账号
    @GET("transfer/out/{uid}")
    fun requestTransferOutInfo(@Path("uid") uid: String):
            retrofit2.Call<BaseCallModel<List<TransferOutInfo>>>

    //收到转账后，上报记录
    //http://192.168.1.115:5000/api/transfer_record/in
    // post方法 参数:transfer_in_account order_no price account_type transfer_time
    @POST("transfer_record/in")
    @FormUrlEncoded
    fun uploadTransferToMeRecord(
            @Field("transfer_in_account") account: String,
            @Field("order_no") orderNo: String,
            @Field("price") price: String,
            @Field("account_type") accountType: Int,
            @Field("transfer_time") transferTime: Long):
            retrofit2.Call<BaseCallModel<Boolean>>

    //转出记录上传
    //http://192.168.1.115:5000/api/transfer_record/out
    // post方法 参数:
    // transfer_in_account transfer_out_account order_no price account_type transfer_time, state:0失败 1成功
    @POST("transfer_record/out")
    @FormUrlEncoded
    fun uploadTransferRecord(
            @Field("transfer_in_account") inAccount: String,
            @Field("transfer_out_account") outAccount: String,
            @Field("order_no") orderNo: String,
            @Field("price") price: String,
            @Field("account_type") accountType: Int,
            @Field("state") state: Int,
            @Field("transfer_time") transferTime: Long):
            retrofit2.Call<BaseCallModel<Boolean>>
}
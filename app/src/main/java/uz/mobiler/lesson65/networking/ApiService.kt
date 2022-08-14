package uz.mobiler.lesson65.networking

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import uz.mobiler.lesson65.model.NotificationData
import uz.mobiler.lesson65.model.NotificationResponse

interface ApiService {

    @Headers("Authorization: key=AAAA_rmzEfk:APA91bEh0xBx5yjfu5k0qFQ798ch4qfQJN0SjQSfGoYi7_29_LGE3T-31EKN25RuLuMJOPOHoHxMq5R7LjrQCyjnEXnC6UsuzlniW2eNqsIn-2mN42-sSHwMvQFZpcybFyZbxNphfwep")
    @POST("fcm/send")
    fun sendMessage(@Body notificationData: NotificationData): Call<NotificationResponse>

}
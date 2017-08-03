package cn.com.lcase.app.net;

import java.util.List;
import java.util.Map;

import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.model.SysToken;
import cn.com.lcase.app.model.Us;
import cn.com.lcase.app.model.UserInfo;
import cn.com.lcase.app.model.Version;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by admin on 2016/10/24.
 */

public interface LCaseApi {

    @POST(LCaseConstants.API_LOGIN)
    Call<ReturnVo<SysToken>> login(@Query("username") String username, @Query("password") String password);

    @POST(LCaseConstants.API_LOGOUT)
    Call<ReturnVo<Object>> logout(@Header("cookie") String cookie, @Header("token") String token);


    @POST(LCaseConstants.API_GET_VERIFY_CODE)
    Call<ReturnVo<Object>> getVerifyCode(@Body Map<String, String> phone);

    @POST(LCaseConstants.API_SAVE_USER)
    Call<ReturnVo<Object>> saveUser(@Header("cookie") String cookie, @Body UserInfo user);

    @POST(LCaseConstants.API_SAVE_SUB_USER)
    Call<ReturnVo<Object>> saveSubUser(@Header("cookie") String cookie, @Header("token") String token, @Body UserInfo user);


    @POST(LCaseConstants.API_RESET_PASS)
    Call<ReturnVo<Object>> resetPassword(@Header("cookie") String cookie, @Body UserInfo user);

    @POST(LCaseConstants.API_RESET_PASS_EMAIL)
    Call<ReturnVo<Object>> resetPasswordEmail(@Header("cookie") String cookie, @Body UserInfo user);

    @POST(LCaseConstants.API_SEND_EMAIL)
    Call<ReturnVo<Object>> getEmail(@Body Map<String, String> email);

    @POST(LCaseConstants.API_DEVICE_LIST)
    Call<ReturnVo<List<Device>>> deviceList(@Header("cookie") String cookie, @Header("token") String token, @Body Device device);


    @POST(LCaseConstants.API_ADD_DEVICE)
    Call<ReturnVo<Object>> addDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Device device);

    @POST(LCaseConstants.API_QUERY_EXIST_DEVICE)
    Call<ReturnVo<Object>> queryExistDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, List<Device>> device);

    @POST(LCaseConstants.API_RE_PASSWORD)
    Call<ReturnVo<Object>> rePassword(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_CHECK_PASSWORD)
    Call<ReturnVo<Object>> checkPassword(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_QUERY_GROUP_INFO)
    Call<ReturnVo<List<Group>>> queryGroupInfo(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_QUERY_SUB_USER_DEVICE)
    Call<ReturnVo<List<Group>>> querySubUserDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_SAVE_GROUP)
    Call<ReturnVo<Group>> saveGroup(@Header("cookie") String cookie, @Header("token") String token, @Body Group group);

    @POST(LCaseConstants.API_UPDATE_GROUP)
    Call<ReturnVo<Object>> updateGroup(@Header("cookie") String cookie, @Header("token") String token, @Body Group group);

    @POST(LCaseConstants.API_DELETE_GROUP)
    Call<ReturnVo<Object>> deleteGroup(@Header("cookie") String cookie, @Header("token") String token, @Body Group group);

    @POST(LCaseConstants.API_SCENE_LIST)
    Call<ReturnVo<List<Scene>>> sceneList(@Header("cookie") String cookie, @Header("token") String token);

    @POST(LCaseConstants.API_SAVE_SCENE)
    Call<ReturnVo<Scene>> addScene(@Header("cookie") String cookie, @Header("token") String token, @Body Scene Scene);

    @POST(LCaseConstants.API_DELETE_SCENE)
    Call<ReturnVo<Object>> deleteScene(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, Integer> map);

   @POST(LCaseConstants.API_DELETE_SCENE_DEVICE)
    Call<ReturnVo<Object>> deleteSceneDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, Integer> map);

   @POST(LCaseConstants.API_OPEN_SCENE)
    Call<ReturnVo<Object>> openScene(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

   @POST(LCaseConstants.API_ABOUT_US)
    Call<ReturnVo<Us>> aboutUs();

   @POST(LCaseConstants.API_GROUP_LIST)
    Call<ReturnVo<List<Group>>> groupList(@Header("cookie") String cookie, @Header("token") String token);

   @POST(LCaseConstants.API_QUERY_PRIVATE_DEVICE)
    Call<ReturnVo<List<Group>>> queryPrivateDevice(@Header("cookie") String cookie, @Header("token") String token);

   @POST(LCaseConstants.API_UPDATE_DEVICE_GROUP)
    Call<ReturnVo<Object>> updateDeviceGroup(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_DEVICE_DELETE)
    Call<ReturnVo<Object>> deviceDelete(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_OPEN_DEVICE)
    Call<ReturnVo<Object>> openDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_CLOSE_DEVICE)
    Call<ReturnVo<Object>> closeDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_UPDATE_DEVICE)
    Call<ReturnVo<Object>> updateDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Device device);

    @POST(LCaseConstants.API_FEED_BACK_SAVE)
    Call<ReturnVo<Object>> feedbackSave(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_QUERY_SUB_USER)
    Call<ReturnVo<List<UserInfo>>> querySubUser(@Header("cookie") String cookie, @Header("token") String token);

    @POST(LCaseConstants.API_SCENE_ADD_DEVICE)
    Call<ReturnVo<Object>> sceneAddDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_QUERY_SCENE_DETAIL)
    Call<ReturnVo<List<SceneDetail>>> querySceneDetail(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_UPDATE_DEVICE_STATUS)
    Call<ReturnVo<Object>> updateDeviceStatus(@Header("cookie") String cookie, @Header("token") String token, @Body List<SceneDetail> list);

    @POST(LCaseConstants.API_OPEN_ALL)
    Call<ReturnVo<Object>> openAll(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_CLOSE_ALL)
    Call<ReturnVo<Object>> closeAll(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_SET_POWER)
    Call<ReturnVo<Object>> setPower(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, Object> map);

    @POST(LCaseConstants.API_SET_PRIVATE)
    Call<ReturnVo<Object>> setPrivate(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_SET_PUBLIC)
    Call<ReturnVo<Object>> setPublic(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_QUERY_APP_VERSION)
    Call<ReturnVo<Version>> queryAppVersion();

    @POST(LCaseConstants.API_SAVE_PRIVATE_DEVICE)
    Call<ReturnVo<Object>> savePrivateDevice(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

    @POST(LCaseConstants.API_TRANS_FOR_MAIN_USER)
    Call<ReturnVo<Object>> transformMainUser(@Header("cookie") String cookie, @Header("token") String token, @Body Map<String, String> map);

}

package cn.com.lcase.app.net;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.model.SysToken;
import cn.com.lcase.app.model.Us;
import cn.com.lcase.app.model.UserInfo;
import cn.com.lcase.app.model.Version;
import cn.com.lcase.app.utils.CacheUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by admin on 2016/10/24.
 */

public class LCaseApiClient {
    private static final String API_URL = LCaseConstants.SERVER_URL + LCaseConstants.CONTEXT_PATH;
    private LCaseApi getEndpoint(){
        Retrofit restAdapter = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(getCustomHttpClient())
                .baseUrl(API_URL).build();
        LCaseApi api = restAdapter.create(LCaseApi.class);
        return api;
    }

    private OkHttpClient getCustomHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(30, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(30, TimeUnit.SECONDS);//设置写入超时时间
        return builder.build();
    }

    public Call<ReturnVo<SysToken>> login(String username , String password){
        LCaseApi api = getEndpoint();
        return api.login(username,password);
    }
    public Call<ReturnVo<Object>> logout(){
        LCaseApi api = getEndpoint();
        return api.logout(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken());
    }

    public Call<ReturnVo<Object>> getVerifyCode(Map<String,String> phone){
        LCaseApi api = getEndpoint();
        return api.getVerifyCode(phone);
    }
    public Call<ReturnVo<Object>> saveUser(UserInfo userInfo){
        LCaseApi api = getEndpoint();
        return api.saveUser(CacheUtils.getInstants().getCookie(),userInfo);
    }
    public Call<ReturnVo<Object>> saveSubUser(String token, UserInfo userInfo){
        LCaseApi api = getEndpoint();
        return api.saveSubUser(CacheUtils.getInstants().getCookie(),token,userInfo);
    }
    public Call<ReturnVo<Object>> resetPassword(UserInfo userInfo){
        LCaseApi api = getEndpoint();
        return api.resetPassword(CacheUtils.getInstants().getCookie(),userInfo);
    }
    public Call<ReturnVo<Object>> resetPasswordEmail(UserInfo userInfo){
        LCaseApi api = getEndpoint();
        return api.resetPasswordEmail(CacheUtils.getInstants().getCookie(),userInfo);
    }
    public Call<ReturnVo<Object>> getEmail(Map<String,String> email){
        LCaseApi api = getEndpoint();
        return api.getEmail(email);
    }

    public Call<ReturnVo<List<Device>>> deviceList(Device device){
        LCaseApi api = getEndpoint();
        return api.deviceList(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),device);
    }
    public Call<ReturnVo<Object>> queryExistDevice(Map<String,List<Device>> devices){
        LCaseApi api = getEndpoint();
        return api.queryExistDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),devices);
    }
    public Call<ReturnVo<Object>> addDevice(Device device){
        LCaseApi api = getEndpoint();
        return api.addDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),device);
    }
    public Call<ReturnVo<Object>> rePassword(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.rePassword(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> checkPassword(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.checkPassword(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<List<Group>>> queryGroupInfo(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.queryGroupInfo(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<List<Group>>> querySubUserDevice(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.querySubUserDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Group>> saveGroup(Group group){
        LCaseApi api = getEndpoint();
        return api.saveGroup(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),group);
    }
    public Call<ReturnVo<Object>> updateGroup(Group group){
        LCaseApi api = getEndpoint();
        return api.updateGroup(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),group);
    }
    public Call<ReturnVo<Object>> deleteGroup(Group group){
        LCaseApi api = getEndpoint();
        return api.deleteGroup(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),group);
    }
    public Call<ReturnVo<List<Scene>>> sceneList(){
        LCaseApi api = getEndpoint();
        return api.sceneList(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken());
    }
    public Call<ReturnVo<Scene>> addScene(Scene scene){
        LCaseApi api = getEndpoint();
        return api.addScene(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),scene);
    }
    public Call<ReturnVo<Object>> deleteScene(Map<String,Integer> map){
        LCaseApi api = getEndpoint();
        return api.deleteScene(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> deleteSceneDevice(Map<String,Integer> map){
        LCaseApi api = getEndpoint();
        return api.deleteSceneDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> openScene(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.openScene(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Us>> aboutUs(){
        LCaseApi api = getEndpoint();
        return api.aboutUs();
    }
    public Call<ReturnVo<List<Group>>> groupList(){
        LCaseApi api = getEndpoint();
        return api.groupList(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken());
    }
    public Call<ReturnVo<List<Group>>> queryPrivateDevice(){
        LCaseApi api = getEndpoint();
        return api.queryPrivateDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken());
    }
    public Call<ReturnVo<Object>> updateDeviceGroup(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.updateDeviceGroup(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> deviceDelete(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.deviceDelete(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> openDevice(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.openDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> closeDevice(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.closeDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> updateDevice(Device device){
        LCaseApi api = getEndpoint();
        return api.updateDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),device);
    }
    public Call<ReturnVo<Object>> feedbackSave(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.feedbackSave(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<List<UserInfo>>> querySubUser(){
        LCaseApi api = getEndpoint();
        return api.querySubUser(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken());
    }
    public Call<ReturnVo<Object>> sceneAddDevice(Map<String,String> map){
        LCaseApi api = getEndpoint();
        return api.sceneAddDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<List<SceneDetail>>> querySceneDetail(Map<String,String> map ){
        LCaseApi api = getEndpoint();
        return api.querySceneDetail(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> updateDeviceStatus(List<SceneDetail> list ){
        LCaseApi api = getEndpoint();
        return api.updateDeviceStatus(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),list);
    }
    public Call<ReturnVo<Object>> openAll(Map<String,String> map ) {
        LCaseApi api = getEndpoint();
        return api.openAll(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(), map);
    }
    public Call<ReturnVo<Object>> closeAll(Map<String,String> map ){
        LCaseApi api = getEndpoint();
        return api.closeAll(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> setPower(Map<String,Object> map ){
        LCaseApi api = getEndpoint();
        return api.setPower(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> setPrivate(Map<String,String> map ){
        LCaseApi api = getEndpoint();
        return api.setPrivate(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Object>> setPublic(Map<String,String> map ){
        LCaseApi api = getEndpoint();
        return api.setPublic(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
    public Call<ReturnVo<Version>> queryAppVersion(){
        LCaseApi api = getEndpoint();
        return api.queryAppVersion();
    }
    public Call<ReturnVo<Object>> savePrivateDevice(Map<String,String> map ){
        LCaseApi api = getEndpoint();
        return api.savePrivateDevice(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
	public Call<ReturnVo<Object>> transformMainUser(Map<String,String> map ){
        LCaseApi api = getEndpoint();
        return api.transformMainUser(CacheUtils.getInstants().getCookie(), CacheUtils.getInstants().getToken(),map);
    }
}

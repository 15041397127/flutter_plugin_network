package zhangxu.com.flutter_plugin_network;

import android.app.Activity;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** FlutterPluginNetworkPlugin */
public class FlutterPluginNetworkPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

   public Activity activity = new Activity();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_plugin_network");
    channel.setMethodCallHandler(new FlutterPluginNetworkPlugin());
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_plugin_network");
    channel.setMethodCallHandler(new FlutterPluginNetworkPlugin());
  }



  //方法回调
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    //响应doRequest方法调用
    if (call.method.equals("doRequest")){

      //取出query参数和URL
      HashMap param = call.argument("param");
      String url = call.argument("url");

      doRequest(url,param,result);


    }else {

      result.notImplemented();
    }

  }



  //处理网络调用
  void  doRequest(String url ,HashMap<String,String>param,final  Result result){

    //初始化网络调用实例
    OkHttpClient client = new OkHttpClient();

    //加工URL及query参数
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    for(String key : param.keySet()){

      String value = param.get(key);
      urlBuilder.addQueryParameter(key,value);

    }

    //加入自定义通用参数
    urlBuilder.addQueryParameter("ppp","yyyy");
    String requestUrl = urlBuilder.build().toString();


    //发起网络调用
    final Request request = new Request.Builder().url(requestUrl).build();
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NotNull Call call, @NotNull final IOException e) {

       //切换至主线程 通知dart 调用失败
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            result.error("Error",e.toString(),null);
          }
        });
      }

      @Override
      public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {


        if (!response.isSuccessful()){

          final  String content = "Unexpected code " + response;

          activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              result.error("Error",content,null);
            }
          });

        }else {

          //取出响应数据
          final  String content = response.body().string();

          //切换至主线程 响应Dart调用
          activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              result.success(content);
            }
          });

        }



      }
    });

  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {

    activity =  binding.getActivity();

  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {



  }



  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {

  }
}

package com.yikolemon;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.PropertiesUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author yikolemon
 */
public class PortRunnable implements Runnable{

    private Integer count = 0;

    private static final Properties P;

    static {
        P = loadOutterProperties();
        assert P != null;
        P.forEach((a, b)->{
            System.out.println("key:" + a +" ,value:" + b);
        });
    }

    @Override
    public void run() {
        try{
            login();
            Map<String, Object> first = getFirst();
            updatePort(first);
        }
        catch (Exception e){
            if (count <= 3){
                count++;
                this.run();
            }else {
                throw e;
            }
        }
    }

    public static void main(String[] args) {
        new PortRunnable().run();
    }

    private void login(){
        String username = P.getProperty("name");
        String password = P.getProperty("password");
        String url = P.getProperty("url");
        HttpRequest post = HttpUtil.createPost(url + "login");
        // 设置 Content-Type 为 form-data
        post.contentType(ContentType.FORM_URLENCODED.getValue());
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        post.body(HttpUtil.toParams(data)).execute();
    }

//    private HttpCookie getCookie(HttpResponse resp){
//        List<HttpCookie> cookies = resp.getCookies();
//        return cookies.get(0);
//    }

    private Map<String, Object> getFirst(){
        String url = P.getProperty("url");
        HttpRequest post = HttpUtil.createPost(url + "xui/inbound/list");
        HttpResponse execute = post.execute();
        String body = execute.body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        // 获取 "obj" 数组
        JSONArray objArray = jsonObject.getJSONArray("obj");

        // 处理 obj 数组中的每个元素
        for (int i = 0; i < objArray.size(); i++) {
            JSONObject objItem = objArray.getJSONObject(i);

            // 获取并解析嵌套的 JSON 字符串
            String settingsStr = objItem.getString("settings");
            String streamSettingsStr = objItem.getString("streamSettings");
            String sniffingStr = objItem.getString("sniffing");

            // 将嵌套 JSON 字符串转换为 JSONObject
            JSONObject settingsJson = JSON.parseObject(settingsStr);
            JSONObject streamSettingsJson = JSON.parseObject(streamSettingsStr);
            JSONObject sniffingJson = JSON.parseObject(sniffingStr);

            // 将解析后的 JSON 放回原来的 objItem 中
            objItem.put("settings", settingsJson);
            objItem.put("streamSettings", streamSettingsJson);
            objItem.put("sniffing", sniffingJson);
        }
        Object obj = jsonObject.getJSONArray("obj").get(0);
        if (obj instanceof JSONObject){
            JSONObject resObj = (JSONObject) obj;
            return resObj.toJavaObject(Map.class);
        }
        return null;
    }

    private void updatePort(Map<String, Object> map){
        String url = P.getProperty("url");
        HttpRequest post = HttpUtil.createPost(url + "xui/inbound/update/1");
        // 设置 Content-Type 为 form-data
        post.contentType(ContentType.FORM_URLENCODED.getValue());
        String port = getPort();
        map.put("port", port);
        HttpResponse execute = post.body(HttpUtil.toParams(map)).execute();
        System.out.println(execute);
    }

    private static String getPort() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 获取年份的最后一位
        int yearLastDigit = now.getYear() % 10;
        // 保持两位格式
        String month = String.format("%02d", now.getMonthValue());
        // 保持两位格式
        String day = String.format("%02d", now.getDayOfMonth());
        // 拼接结果
        return String.format("%d%s%s", yearLastDigit, month, day);
    }

    /**
     * 加载同级的配置文件
     * @return 配置类
     */
    private static Properties loadOutterProperties(){
        // 获取当前 JAR 文件的路径
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File jarDir = new File(jarPath).getParentFile();

        if (jarDir != null) {
            // 构造 application.properties 的文件路径
            File propertiesFile = new File(jarDir, "application.properties");

            if (propertiesFile.exists()) {
                System.out.println("配置文件路径: " + propertiesFile.getAbsolutePath());

                // 使用 Hutool 的 PropertiesUtil 读取配置文件
                return new Props(propertiesFile);
            } else {
                System.out.println("配置文件不存在");
            }
        } else {
            System.out.println("无法获取 JAR 所在的目录");
        }
        return null;
    }

}

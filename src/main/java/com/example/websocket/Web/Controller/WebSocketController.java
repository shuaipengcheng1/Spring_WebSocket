package com.example.websocket.Web.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.util.buf.StringUtils;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/websocket/{userid}")
@RestController
@Component
public class WebSocketController {

    //    在线人数
    private int Count = 0;
    //   在线 用户列表 Stinng = userid 第二个参数是控制器对象本省
    private static ConcurrentHashMap<String, WebSocketController> concurrentHashMap = new ConcurrentHashMap<>();
    //    传入的userid
    private String userid;
    //    用于发送数据的session
    private Session session;

    private void add() {
        this.Count++;
    }

    private void sub() {
        this.Count--;
    }

    //    前端链接到websocket的服务器 会调用的函数
    @OnOpen
    public void OnOpen(Session session, @PathParam("userid") String id) {
        System.out.println("登录的id"+id);
        this.userid = id;
        this.session = session;
// 判断已经登录的数组中是否有该用户
        Set<Map.Entry<String, WebSocketController>> entrySet = concurrentHashMap.entrySet();
//   循环
        entrySet.forEach(item -> {
//        如果有值
            if (item.getKey().equals(userid)) {
                concurrentHashMap.remove(userid);
                concurrentHashMap.put(userid, this);
                System.out.println("le"+concurrentHashMap.size());
            }
        });
//    没有登录信息 则直接添加
        concurrentHashMap.put(userid, this);
        System.out.println("le"+concurrentHashMap.size());

//    登陆的人数加一
        add();
        try {
            sendMessage("成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("当前的用户" + userid);


    }



    /*
   @param message 客户端发送的信息
    */
    //        获取信息
//    前端必须要传入一个json字符粗 并且包含用户要发送的信息和发送对象
    @OnMessage
    public void OnMessage(String message, Session session) {
//        不为空
        if (!message.equals("")) {
            //        将传入的json字符粗 解析为json对象
            System.out.println(message);
            JSONObject jsonpObject = JSON.parseObject(message);
            String messag = jsonpObject.getString("message");
            System.out.println("发送人" + userid + "发送的消息" + messag);
//        获取发送的对象
            String ToUserId = jsonpObject.getString("ToUserId");
//            添加一个发送者的信息到报文中
            jsonpObject.put("StartUser", this.userid);
//        遍历 判断是否在线
            if (concurrentHashMap.containsKey(ToUserId)) {
//          在线
                try {
                    //                获取id对应值
                    //
                    concurrentHashMap.get(ToUserId).sendMessage(jsonpObject.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
//          不在线
                System.out.println("不在线");
            }
        }

    }


    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    //    错误
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("发生错误");
        throwable.printStackTrace();
    }

    @RequestMapping(value = "/Online")
    public Map Online(HttpServletResponse response, HttpServletRequest request) {
        response.setHeader("Access-Control-Allow-Credentials", "true");

        response.setHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
//        返回在线人数
        Map map = new HashMap();
        map.put("message", concurrentHashMap);
        System.out.println(concurrentHashMap.size());
        return map;
    }
    //关闭
    @OnClose
    public void OnClose() {
        if (concurrentHashMap.containsKey(userid)) {
            concurrentHashMap.remove(userid);
            System.out.println("删除"+userid);
            sub();
        }


    }
    /**
     * 发送自定义消息
     * */
    public static void sendInfo(String message, String userId) throws IOException {

        if(concurrentHashMap.containsKey(userId)){
            concurrentHashMap.get(userId).sendMessage(message);
        }else{
           System.out.println("用户"+userId+",不在线！");
        }
    }


}

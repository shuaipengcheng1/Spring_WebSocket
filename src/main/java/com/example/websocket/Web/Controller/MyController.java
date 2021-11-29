package com.example.websocket.Web.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.misaka.Domain.User;
import com.misaka.Servvicer.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MyController {
    //    获取Dubbo共享对象
    @Reference(interfaceClass = UserService.class, version = "1.0.0", timeout = 30000)
    UserService userService;

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Map<String, User> SelectUser(int id) {
//
        Map<String, User> map = new HashMap<>();
        map.put("message", userService.SelectById(id));
        return map;

    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Map Login(HttpServletRequest request, HttpServletResponse response, String username, String password) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        Map map = new HashMap();
        Boolean state = userService.Login(username, password);
        if (state) {

            map.put("message", "登录成功");
            map.put("state", true);

//         存入session
            User user = new User();
            user.setPassword(password);
            user.setUser(username);
            request.getSession().setAttribute("user", user);
//            System.out.println(request.getSession().isNew());
            return map;
        } else {
            map.put("message", "失败");
            map.put("state", false);

            return map;
        }

    }

    //    判断是否登录
    @RequestMapping(value = "/isLogin", method = RequestMethod.POST)
    public Map isLogin(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        Map map = new HashMap();

        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
//        System.out.println(request.getSession().getAttribute("user"));
        if (request.getSession().getAttribute("user") == null) {
            map.put("message", "失败");
            map.put("state", false);
            return map;
        } else {
            map.put("message", "成功");
            map.put("state", true);
            map.put("data", request.getSession().getAttribute("user"));

            return map;
        }

    }

    @RequestMapping("/push")
    public void pushToWeb(String message, String toUserId) throws IOException {
        WebSocketController webSocketController = new WebSocketController();

        webSocketController.sendInfo(message, toUserId);

    }


}

package com.csd.qq.controller;

import com.csd.qq.util.HttpUtil;
import com.csd.qq.util.QQConfig;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * Created by sdc on 2018/3/22.
 * csdc
 */
@Controller
public class QQLoginController {
    Logger logger = Logger.getLogger(QQLoginController.class);

    @RequestMapping("/calback")
    @ResponseBody
    public String calback(HttpServletRequest request, HttpServletResponse response) {
        try {
            //拼接获取access_token的请求
            String url = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code" +
                    "&client_id=" + QQConfig.APPID + "&client_secret=" + QQConfig.APPKEY +
                    "&code=" + request.getParameter("code") + "" +
                    "&redirect_uri=" + URLEncoder.encode("https://www.chensidan.com/calback1");
            String result = HttpUtil.doGetString(url);
            //拆分返回的结果获取到access_token
            String accessToken = result.split("&")[0].split("=")[1];
            //获取用户openid
            url = "https://graph.qq.com/oauth2.0/me?access_token=" + accessToken;
            String jsonObject = HttpUtil.doGetString(url);

            String scriptStr = "<script>" +
                    "function callback(obj){" +
                    "window.opener.location.href='https://www.chensidan.com/get_user_info?openid='+obj.openid+'&access_token=" + accessToken + "';" +
                    "window.close();" +
                    "}" +
                    jsonObject +
                    "</script>";
            return scriptStr;
        }catch (Exception ex){
            logger.error(ex.toString());
            return "<h1>QQ第三方登录失败</h1>";
        }

//        response.getWriter().println("<script>" +
//                "window.opener.location.href='https://www.chensidan.com/get_user_info?access_token=" + accessToken + "';" +
//                "window.close();" +
//                "</script>");
        //response.getWriter().println("<script>alert('1')</script>");
    }

    @RequestMapping("/get_user_info")
    @ResponseBody
    public String getUserInfo(HttpServletRequest request, HttpServletResponse response) {
        try {
            String openId = request.getParameter("openid");
            String accessToken = request.getParameter("access_token");

            //获取用户信息
            String url = "https://graph.qq.com/user/get_user_info?access_token=" + accessToken + "&oauth_consumer_key=" + QQConfig.APPID + "&openid=" + openId;
            JSONObject jsonObject = HttpUtil.doGetJson(url);
            String html = "头像：<img src='" + jsonObject.getString("figureurl") + "'></br>" +
                    "姓名：" + jsonObject.getString("nickname") + "</br>" +
                    "性别：" + jsonObject.getString("gender") + "</br>" +
                    "省份：" + jsonObject.getString("province") + "</br>" +
                    "城市：" + jsonObject.getString("city") + "</br>" +
                    "年份：" + jsonObject.getString("year");
            return html;
        }catch (Exception ex){
            logger.error(ex.toString());
            ex.printStackTrace();
            return "<h1>QQ第三方登录失败</h1>";
        }
    }
}
package cn.itrip.auth.controller;

import cn.itrip.common.UrlUtils;
import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RequestMapping("/wx/login")
@Controller
public class WxLoginController {

    private Logger logger = Logger.getLogger(WxLoginController.class);

    @RequestMapping("/callBack")
    public void callBack(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws IOException {
        //1. 编写请求Code
        //1.1 请求地址
        String accessUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=wx860bf23c66d93e33" +
                "&secret=9c92026ab4faa4a4f7ac4cf10b2a8a3c" +
                "&code=" + code +
                "&grant_type=authorization_code";
        //1.2 通过发送accessURL请求地址,并得到返回参数,返回参数是一个json字符串，
        String jsonStr = UrlUtils.loadURL(accessUrl);
        //将json字符串转换成Map集合
        Map<String ,String> accessMap = JSON.parseObject(jsonStr, Map.class);
        //2. 通过code——access_token，获取json字符串/accessMap集合中的accessToken
        String accessToken = accessMap.get("access_token");
        String openId = accessMap.get("openid");
        logger.info("accessToken的值为：" + accessToken + ",openId的值为：" + openId);

        //3、通过access_token获取用户信息，参考：资源中心 网站应用微信登录功能授权后接口调用获取用户个人信息（UnionID机制）
        //3.1 请求URL地址
        String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId;
        //3.2 发送请求，返回JSON字符串
        String userInfoStr = UrlUtils.loadURL(userInfoUrl);
        Map<String,String> userInfoMap = JSON.parseObject(userInfoStr, Map.class);
        //3.3 获取用户个人信息
        String city = userInfoMap.get("city"); //获取用户信息中的城市
        String nickname = userInfoMap.get("nickname");
        logger.info("city的值为：" + city + "，nickname的值为：" + nickname);

        response.sendRedirect("http://www.baidu.com");
    }

}


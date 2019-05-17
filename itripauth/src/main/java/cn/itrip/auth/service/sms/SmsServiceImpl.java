package cn.itrip.auth.service.sms;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service("smsService")
public class SmsServiceImpl implements SmsService {

    /**
     * 用于发送短信。
     * @param to 将短信发送给谁
     * @param templateId 短信模板的ID
     * @param datas 需要发送的内容
     */
    @Override
    public void sendMsg(String to, String templateId, String[] datas) throws Exception{
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        //1、初始【开发者账号】信息中的URL。
        sdk.init("app.cloopen.com","8883");
        //ACCOUNT SID和AUTH TOKEN，注：下面蓝色代码是需要各自开发者账号进行修改
        sdk.setAccount("8aaf070869dc0b88016a0621b73f10e2","831d45c7412d414e80366bcb701102cb");
        // APPID
        sdk.setAppId("8aaf070869dc0b88016a0621b79910e9");
        HashMap result = sdk.sendTemplateSMS(to,templateId, datas);
        //查看短信是否发送成功。就通过查看statusCoe的值是否为000000
        if ("000000".equals(result.get("statusCode"))) {
            System.out.println("短信发送成功");
        } else {
            throw new Exception(result.get("statusCode").toString() + ":" + result.get("statusMsg").toString());
        }
    }
}


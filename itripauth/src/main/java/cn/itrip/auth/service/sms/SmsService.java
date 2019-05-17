package cn.itrip.auth.service.sms;

public interface SmsService {


    /**
     * 用于发送短信。
     * @param to 将短信发送给谁
     * @param templateId 短信模板的ID
     * @param datas 需要发送的内容
     */
    public void sendMsg(String to,String templateId, String [] datas) throws Exception;

}

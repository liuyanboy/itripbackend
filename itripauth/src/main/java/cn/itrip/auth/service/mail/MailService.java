package cn.itrip.auth.service.mail;

public interface MailService {

    /**
     * 发送包括激活码的邮件，用于激活用户账号
     * @param mailTo 收件人的邮箱地址
     * @param activationCode 验证码
     */
    public void sendActivationMail(String mailTo,String activationCode);

}

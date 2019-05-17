package cn.itrip.auth.service.mail;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("mailService")
public class MailServiceImpl implements MailService {

    //允许用户快速设置邮件内容的各种属性信息
// SimpleMailMessage实现了MailMessage接口。
    @Resource
    private SimpleMailMessage activationMailMessage;

    @Resource
    private MailSender mailSender;

    //https://mail.aliyun.com  进行个人邮箱注册。
    @Override
    public void sendActivationMail(String mailTo, String activationCode) {
        activationMailMessage.setTo(mailTo);
        activationMailMessage.setText("您的激活码是：" + activationCode);
        mailSender.send(activationMailMessage);
    }
}


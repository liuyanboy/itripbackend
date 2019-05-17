package cn.itrip.auth.service.user;

import cn.itrip.beans.pojo.ItripUser;

public interface UserService {
    public ItripUser login(String userCode, String userPassword) throws Exception;

    public void insertUser(ItripUser user) throws Exception;

    public ItripUser findByUsername(String username);

    /**
     * 邮箱激活
     * @param mail 邮箱地址
     * @param code 为存入到Redis的激活码所对应的key。格式是：activation: + 验证码
     * @return true/false，true表示验证成功
     */
    public boolean activate(String mail,String code) throws Exception ;


    /**
     * 通过手机注册完成用户的新增操作
     * @param user 用户对象
     */
    public void insertUserByPhone(ItripUser user) throws Exception;


    /**
     * 短信验证
     * @param phoneNum 手机号码
     * @param code 验证码
     * @return true表示验证成功，false表示验证失败。
     */
    public boolean validatePhone(String phoneNum,String code) throws Exception;

}

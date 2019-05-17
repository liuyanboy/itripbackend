package cn.itrip.dao.user;

import cn.itrip.beans.pojo.ItripUser;

import java.util.List;
import java.util.Map;

public interface ItripUserMapper {
    public List<ItripUser> getItripUserListByMap(Map<String,Object> param);

    public Integer insertItripUser(ItripUser itripUser)throws Exception;

    public Integer updateItripUser(ItripUser itripUser)throws Exception;
}

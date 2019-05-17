package cn.itrip.dao.orderlinkuser;

import cn.itrip.beans.pojo.ItripOrderLinkUser;
import cn.itrip.beans.vo.order.ItripOrderLinkUserVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItripOrderLinkUserMapper {
    public Integer deleteItripOrderLinkUserByOrderId(@Param(value = "orderId") Long orderId)throws Exception;

    public Integer insertItripOrderLinkUser(ItripOrderLinkUser itripOrderLinkUser)throws Exception;

    public List<ItripOrderLinkUserVo> getItripOrderLinkUserListByMap(Map<String, Object> param)throws Exception;

    public List<Long> getItripOrderLinkUserIdsByOrder() throws Exception;
}

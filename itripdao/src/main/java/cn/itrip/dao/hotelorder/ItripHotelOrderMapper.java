package cn.itrip.dao.hotelorder;

import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.beans.vo.order.ItripListHotelOrderVO;
import cn.itrip.beans.vo.order.ItripPersonalOrderRoomVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItripHotelOrderMapper {
    //通过条件查询【爱旅行项目的酒店订单列表】
    public List<ItripHotelOrder> getItripHotelOrderListByMap(Map<String, Object> param)throws Exception;
    //更新爱旅行项目的酒店订单
    public Integer updateItripHotelOrder(ItripHotelOrder itripHotelOrder)throws Exception;

    public Integer updateHotelOrderStatus(@Param(value = "id")Long id,@Param(value = "modifiedBy")Long modifiedBy) throws Exception;

    public Integer insertItripHotelOrder(ItripHotelOrder itripHotelOrder)throws Exception;

    public ItripHotelOrder getItripHotelOrderById(@Param(value = "id") Long id)throws Exception;

    public ItripPersonalOrderRoomVO getItripHotelOrderRoomInfoById(@Param(value = "id") Long id)throws Exception;

    /***
     * 获取订单数量
     */
    public Integer getOrderCountByMap(Map<String,Object> param)throws Exception;

    /**
     * 获取订单列表 add by hanlu
     */
    public List<ItripListHotelOrderVO> getOrderListByMap(Map<String,Object> param)throws Exception;

}

package cn.itrip.dao.hotelroom;

import cn.itrip.beans.pojo.ItripHotelRoom;
import cn.itrip.beans.vo.hotelroom.ItripHotelRoomVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItripHotelRoomMapper {
    public List<ItripHotelRoomVO> getItripHotelRoomListByMap(Map<String, Object> param)throws Exception;

    public ItripHotelRoom getItripHotelRoomById(@Param(value = "id") Long id)throws Exception;
}

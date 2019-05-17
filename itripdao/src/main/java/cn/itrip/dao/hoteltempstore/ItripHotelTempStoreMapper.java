package cn.itrip.dao.hoteltempstore;

import cn.itrip.beans.vo.store.StoreVO;

import java.util.List;
import java.util.Map;

public interface ItripHotelTempStoreMapper {
    public List<StoreVO> queryRoomStore(Map<String, Object> param) throws Exception;
    public void flushStore(Map<String, Object> param)throws Exception;

}

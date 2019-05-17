package cn.itrip.dao.hotel;

import cn.itrip.beans.pojo.ItripAreaDic;
import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.hotel.ItripSearchFacilitiesHotelVO;
import cn.itrip.beans.vo.hotel.ItripSearchPolicyHotelVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItripHotelMapper {

    public ItripSearchFacilitiesHotelVO getItripHotelFacilitiesById(@Param(value = "id") Long id) throws Exception;

    public ItripSearchPolicyHotelVO queryHotelPolicy(@Param(value = "id") Long id) throws Exception;

    public ItripHotel getItripHotelById(@Param(value = "id") Long id)throws Exception;

    /**
     *  根据酒店ID获取特色
     * @param id 酒店ID
     */
    public List<ItripLabelDic> getHotelFeatureByHotelId(@Param(value = "id") Long id)throws Exception;

    /**
     *  根据酒店ID获取商圈
     * @param id 酒店ID
     */
    public List<ItripAreaDic> getHotelAreaByHotelId(@Param(value = "id") Long id)throws Exception;

}

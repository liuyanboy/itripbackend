package cn.itrip.dao.areadic;

import cn.itrip.beans.pojo.ItripAreaDic;

import java.util.List;
import java.util.Map;

public interface ItripAreaDicMapper {

    public List<ItripAreaDic> getItripAreaDicListByMap(Map<String,Object> param)throws  Exception;

}

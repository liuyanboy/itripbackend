package cn.itrip.service.areadic;

import cn.itrip.beans.pojo.ItripAreaDic;

import java.util.List;
import java.util.Map;

public interface ItripAreaDicService {

    public List<ItripAreaDic> getItripAreaDicListByMap(Map<String,Object> param)throws  Exception;

}

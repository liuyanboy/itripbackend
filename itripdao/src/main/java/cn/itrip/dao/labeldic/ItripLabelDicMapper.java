package cn.itrip.dao.labeldic;

import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.ItripLabelDicVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItripLabelDicMapper {
    public List<ItripLabelDic> getItripLabelDicListByMap(Map<String, Object> param)throws Exception;

    /**
     * 根据parentId查询数据字典
     */
    public List<ItripLabelDicVO> getItripLabelDicByParentId(@Param(value = "parentId") Long parentId)throws Exception;

}

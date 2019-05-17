package cn.itrip.dao.comment;

import cn.itrip.beans.pojo.ItripComment;
import cn.itrip.beans.vo.comment.ItripListCommentVO;
import cn.itrip.beans.vo.comment.ItripScoreCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItripCommentMapper {

    public List<ItripListCommentVO> getItripCommentListByMap(Map<String, Object> param)throws Exception;

    public Integer getItripCommentCountByMap(Map<String, Object> param)throws Exception;

    public ItripScoreCommentVO getCommentAvgScore(@Param(value = "hotelId") Long hotelId)      throws Exception;

    public Long insertItripComment(ItripComment itripComment)throws Exception;
}

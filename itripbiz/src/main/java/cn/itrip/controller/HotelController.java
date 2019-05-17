package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripAreaDic;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.ItripAreaDicVO;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.beans.vo.hotel.HotelVideoDescVO;
import cn.itrip.beans.vo.hotel.ItripSearchDetailsHotelVO;
import cn.itrip.beans.vo.hotel.ItripSearchFacilitiesHotelVO;
import cn.itrip.beans.vo.hotel.ItripSearchPolicyHotelVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.service.areadic.ItripAreaDicService;
import cn.itrip.service.hotel.ItripHotelService;
import cn.itrip.service.image.ItripImageService;
import cn.itrip.service.labeldic.ItripLabelDicService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Api(value = "API",basePath = "/hhtp://api.itrap.com/api")
@RequestMapping(value = "/api/hotel")
public class HotelController {
    private Logger logger = Logger.getLogger(HotelController.class);

    @Resource
    private ItripAreaDicService itripAreaDicService;

    @Resource
    private ItripLabelDicService itripLabelDicService;

    @Resource
    private ItripImageService itripImageService;

    @Resource
    private ItripHotelService itripHotelService;
    /****
     * 查询热门城市的接口
     * @return返回热门城市id与热门城市名称，我们将这两个值封装到了ItripAreaDicVO类中。
     */

    @ApiOperation(value = "查询热门城市", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "查询国内、国外的热门城市(1:国内 2:国外)" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>10201 : hotelId不能为空 </p>" +
            "<p>10202 : 系统异常,获取失败</p>")
    @RequestMapping(value = "/queryhotcity/{type}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripAreaDicVO> queryHotCity(@PathVariable Integer type) {
        List<ItripAreaDic> itripAreaDics = null;
        List<ItripAreaDicVO> itripAreaDicVOs = null;
        try {
            if (EmptyUtils.isNotEmpty(type)) {
                Map param = new HashMap();
                param.put("isHot", 1);
                param.put("isChina", type);
                itripAreaDics = itripAreaDicService.getItripAreaDicListByMap(param);
                if (EmptyUtils.isNotEmpty(itripAreaDics)) {
                    itripAreaDicVOs = new ArrayList();
                    for (ItripAreaDic dic : itripAreaDics) {
                        ItripAreaDicVO vo = new ItripAreaDicVO();
                        //将ItripAreaDic中的城市id与城市名称复制到ItripAreaDicVO对象中。
                        BeanUtils.copyProperties(dic, vo);
                        itripAreaDicVOs.add(vo);
                    }
                }
            } else {
                DtoUtil.returnFail("type不能为空", "10201");
            }
        } catch (Exception e) {
            DtoUtil.returnFail("系统异常", "10202");
            e.printStackTrace();
        }
        return DtoUtil.returnDataSuccess(itripAreaDicVOs);
    }

    @ApiOperation(value = "根据酒店id查询酒店特色、商圈、酒店名称", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据酒店id查询酒店特色、商圈、酒店名称（视频文字描述）" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100214 : 获取酒店视频文字描述失败 </p>" +
            "<p>100215 : 酒店id不能为空</p>")
    @RequestMapping(value = "/getvideodesc/{hotelId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dto<Object> getVideoDescByHotelId(@ApiParam(required = true, name = "hotelId", value = "酒店ID")
                                             @PathVariable String hotelId) {
        Dto<Object> dto = new Dto<Object>();
        logger.debug("getVideoDescByHotelId hotelId : " + hotelId);
        if (null != hotelId && !"".equals(hotelId)) {
            HotelVideoDescVO hotelVideoDescVO = null;
            try {
                hotelVideoDescVO = itripHotelService.getVideoDescByHotelId(Long.valueOf(hotelId));
                dto = DtoUtil.returnSuccess("获取酒店视频文字描述成功", hotelVideoDescVO);
            } catch (Exception e) {
                e.printStackTrace();
                dto = DtoUtil.returnFail("获取酒店视频文字描述失败", "100214");
            }

        } else {
            dto = DtoUtil.returnFail("酒店id不能为空", "100215");
        }
        return dto;
    }


    /***
     * 查询商圈的接口
     * @param cityId 根据城市编号查询商圈
     */
    @ApiOperation(value = "查询商圈", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据城市查询商圈" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>10203 : cityId不能为空 </p>" +
            "<p>10204 : 系统异常,获取失败</p>")
    @RequestMapping(value = "/querytradearea/{cityId}" +
            "", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripAreaDicVO> queryTradeArea(@PathVariable Long cityId) {
        List<ItripAreaDic> itripAreaDics = null;
        List<ItripAreaDicVO> itripAreaDicVOs = null;
        try {
            if (EmptyUtils.isNotEmpty(cityId)) {
                Map param = new HashMap();
                //isTradingArea 是否是商圈，1表示是。
                param.put("isTradingArea", 1);
                param.put("parent", cityId);
                itripAreaDics = itripAreaDicService.getItripAreaDicListByMap(param);
                if (EmptyUtils.isNotEmpty(itripAreaDics)) {
                    itripAreaDicVOs = new ArrayList();
                    for (ItripAreaDic dic : itripAreaDics) {
                        ItripAreaDicVO vo = new ItripAreaDicVO();
                        BeanUtils.copyProperties(dic, vo);
                        itripAreaDicVOs.add(vo);
                    }
                }
            } else {
                DtoUtil.returnFail("cityId不能为空", "10203");
            }
        } catch (Exception e) {
            DtoUtil.returnFail("系统异常", "10204");
            e.printStackTrace();
        }
        return DtoUtil.returnDataSuccess(itripAreaDicVOs);
    }



    @ApiOperation(value = "查询特色酒店",httpMethod = "GET",
            protocols = "HTTP",produces = "application/json",
    response = Dto.class ,notes = "获取酒店特色（用于查询列表）" +
    "<p>成功：success = 'true' | 失败： success = 'false' 返回错误代码，如下：</p>" +
    "<p>错误码：</p>" +
    "<p>10205：系统异常，获取失败</p>")
    @RequestMapping(value = "/queryhotelfeature",produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripLabelDicVO> queryHotelFeature(){
        List<ItripLabelDic> itripLabelDics = null;
        List<ItripLabelDicVO> itripLabelDicVOS = null;
        try {
            Map param = new HashMap();
            param.put("parentId",16);
            itripLabelDics = itripLabelDicService.getItripLabelDicListByMap(param);
            if(EmptyUtils.isNotEmpty(itripLabelDics)){
                itripLabelDicVOS = new ArrayList();
                for (ItripLabelDic dic : itripLabelDics){
                    ItripLabelDicVO vo = new ItripLabelDicVO();
                    BeanUtils.copyProperties(dic,vo);
                    itripLabelDicVOS.add(vo);
                }
            }
        } catch (Exception e) {
            DtoUtil.returnFail("系统异常","10205");
            e.printStackTrace();
        }
        return DtoUtil.returnDataSuccess(itripLabelDicVOS);
    }


    @ApiOperation(value = "根据酒店id查询酒店设施",httpMethod = "GET",
    protocols = "HTTP" , produces = "aplication/json",
    response = Dto.class ,notes = "根据酒店id查询酒店设施" +
    "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误代码，如下：</p>" +
    "<p>10206：酒店id不能为空</p>" +
    "<p>10207:系统异常，获取失败</p>")
    @RequestMapping(value = "/queryhotelfacilities/{id}",produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripSearchFacilitiesHotelVO> queryHotelFacilities(
            @ApiParam(required = true,name = "id",value = "酒店Id")@PathVariable Long id){
        ItripSearchFacilitiesHotelVO itripSearchFacilitiesHotelVO = null;
        try {
            if(EmptyUtils.isNotEmpty(id)){
                itripSearchFacilitiesHotelVO = itripHotelService.getItripHotelFacilitiesById(id);
                return DtoUtil.returnDataSuccess(itripSearchFacilitiesHotelVO.getFacilities());
            }else{
                return DtoUtil.returnFail("酒店id不能为空","10206");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，获取失败","10207");
        }
    }

    /***
     * 根据酒店id查询酒店政策 -add by donghai
     */
    @ApiOperation(value = "根据酒店id查询酒店政策", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据酒店id查询酒店政策" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>10208: 酒店id不能为空</p>" +
            "<p>10209: 系统异常,获取失败</p>")
    @RequestMapping(value = "/queryhotelpolicy/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripSearchPolicyHotelVO> queryHotelPolicy(
            @ApiParam(required = true, name = "id", value = "酒店ID")
            @PathVariable Long id) {
        ItripSearchPolicyHotelVO itripSearchPolicyHotelVO = null;
        try {
            if (EmptyUtils.isNotEmpty(id)) {
                itripSearchPolicyHotelVO = itripHotelService.queryHotelPolicy(id);
                return DtoUtil.returnDataSuccess(itripSearchPolicyHotelVO.getHotelPolicy());
            } else {
                return DtoUtil.returnFail("酒店id不能为空", "10208");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常,获取失败", "10209");
        }
    }


    /***
     * 根据酒店id查询酒店特色和介绍 -add by donghai
     */
    @ApiOperation(value = "根据酒店id查询酒店特色和介绍", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据酒店id查询酒店特色和介绍" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>10210: 酒店id不能为空</p>" +
            "<p>10211: 系统异常,获取失败</p>")
    @RequestMapping(value = "/queryhoteldetails/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripSearchDetailsHotelVO> queryHotelDetails(
            @ApiParam(required = true, name = "id", value = "酒店ID")
            @PathVariable Long id) {
        List<ItripSearchDetailsHotelVO> itripSearchDetailsHotelVOList = null;
        try {
            if (EmptyUtils.isNotEmpty(id)) {
                itripSearchDetailsHotelVOList = itripHotelService.queryHotelDetails(id);
                return DtoUtil.returnDataSuccess(itripSearchDetailsHotelVOList);
            } else {
                return DtoUtil.returnFail("酒店id不能为空", "10210");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常,获取失败", "10211");
        }
    }

    @ApiOperation(value = "根据targetId查询酒店图片(type=0)", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据酒店ID查询酒店图片" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100212 : 获取酒店图片失败 </p>" +
            "<p>100213 : 酒店id不能为空</p>")
    @RequestMapping(value = "/getimg/{targetId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dto<Object> getImgByTargetId(@ApiParam(required = true, name = "targetId", value = "酒店ID")
                                        @PathVariable String targetId) {
        Dto<Object> dto = new Dto<Object>();
        logger.debug("getImgBytargetId targetId : " + targetId);
        if (null != targetId && !"".equals(targetId)) {
            List<ItripImageVO> itripImageVOList = null;
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("type", "0"); //0表示酒店图片
            param.put("targetId", targetId);  //targetId为酒店id。
            try {
                itripImageVOList = itripImageService.getItripImageListByMap(param);
                dto = DtoUtil.returnSuccess("获取酒店图片成功", itripImageVOList);
            } catch (Exception e) {
                e.printStackTrace();
                dto = DtoUtil.returnFail("获取酒店图片失败", "100212");
            }

        } else {
            dto = DtoUtil.returnFail("酒店id不能为空", "100213");
        }
        return dto;
    }

}

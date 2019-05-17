package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.*;
import cn.itrip.beans.vo.order.*;
import cn.itrip.beans.vo.store.StoreVO;
import cn.itrip.common.*;
import cn.itrip.service.hotel.ItripHotelService;
import cn.itrip.service.hotelorder.ItripHotelOrderService;
import cn.itrip.service.hotelroom.ItripHotelRoomService;
import cn.itrip.service.hoteltempstore.ItripHotelTempStoreService;
import cn.itrip.service.orderlinkuser.ItripOrderLinkUserService;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Api(value = "API", basePath = "/http://api.itrap.com/api")
@RequestMapping(value = "/api/hotelorder")
public class HotelOrderController {

    private Logger logger = Logger.getLogger(HotelOrderController.class);

    @Resource
    private ValidationToken validationToken;

    @Resource
    private ItripHotelService hotelService;

    @Resource
    private ItripHotelRoomService roomService;

    @Resource
    private ItripHotelTempStoreService itripHotelTempStoreService;

    @Resource
    private ItripHotelOrderService itripHotelOrderService;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private ItripOrderLinkUserService itripOrderLinkUserService;


    @ApiOperation(value = "生成订单", httpMethod = "POST",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "生成订单" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100505 : 生成订单失败 </p>" +
            "<p>100506 : 不能提交空，请填写订单信息 </p>" +
            "<p>100507 : 库存不足 </p>" +
            "<p>100000 : token失效，请重登录</p>")
    @RequestMapping(value = "/addhotelorder", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Dto<Object> addHotelOrder(@RequestBody ItripAddHotelOrderVO itripAddHotelOrderVO, HttpServletRequest request) {
        Dto<Object> dto = new Dto<Object>();
        String tokenString = request.getHeader("token");
        logger.debug("token name is from header : " + tokenString);
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        Map<String, Object> validateStoreMap = new HashMap<String, Object>();
        validateStoreMap.put("startTime", itripAddHotelOrderVO.getCheckInDate());
        validateStoreMap.put("endTime", itripAddHotelOrderVO.getCheckOutDate());
        validateStoreMap.put("hotelId", itripAddHotelOrderVO.getHotelId());
        validateStoreMap.put("roomId", itripAddHotelOrderVO.getRoomId());
        validateStoreMap.put("count", itripAddHotelOrderVO.getCount());
        List<ItripUserLinkUser> linkUserList = itripAddHotelOrderVO.getLinkUser();
        if(EmptyUtils.isEmpty(currentUser)){
            return DtoUtil.returnFail("token失效，请重登录", "100000");
        }
        try {
            //判断库存是否充足
            Boolean flag = itripHotelTempStoreService.validateRoomStore(validateStoreMap);
            if (flag && null != itripAddHotelOrderVO) {
                //计算订单的预定天数
                Integer days = DateUtil.getBetweenDates(
                        itripAddHotelOrderVO.getCheckInDate(), itripAddHotelOrderVO.getCheckOutDate()
                ).size()-1;
                if(days<=0){
                    return DtoUtil.returnFail("退房日期必须大于入住日期", "100505");
                }
                ItripHotelOrder itripHotelOrder = new ItripHotelOrder();
                itripHotelOrder.setId(itripAddHotelOrderVO.getId());
                itripHotelOrder.setUserId(currentUser.getId());
                itripHotelOrder.setOrderType(itripAddHotelOrderVO.getOrderType());
                itripHotelOrder.setHotelId(itripAddHotelOrderVO.getHotelId());
                itripHotelOrder.setHotelName(itripAddHotelOrderVO.getHotelName());
                itripHotelOrder.setRoomId(itripAddHotelOrderVO.getRoomId());
                itripHotelOrder.setCount(itripAddHotelOrderVO.getCount());
                itripHotelOrder.setCheckInDate(itripAddHotelOrderVO.getCheckInDate());
                itripHotelOrder.setCheckOutDate(itripAddHotelOrderVO.getCheckOutDate());
                itripHotelOrder.setNoticePhone(itripAddHotelOrderVO.getNoticePhone());
                itripHotelOrder.setNoticeEmail(itripAddHotelOrderVO.getNoticeEmail());
                itripHotelOrder.setSpecialRequirement(itripAddHotelOrderVO.getSpecialRequirement());
                itripHotelOrder.setIsNeedInvoice(itripAddHotelOrderVO.getIsNeedInvoice());
                itripHotelOrder.setInvoiceHead(itripAddHotelOrderVO.getInvoiceHead());
                itripHotelOrder.setInvoiceType(itripAddHotelOrderVO.getInvoiceType());
                itripHotelOrder.setCreatedBy(currentUser.getId());
                StringBuilder linkUserName = new StringBuilder();
                int size = linkUserList.size();
                for (int i = 0; i < size; i++) {
                    if (i != size - 1) {
                        linkUserName.append(linkUserList.get(i).getLinkUserName() + ",");
                    } else {
                        linkUserName.append(linkUserList.get(i).getLinkUserName());
                    }
                }
                itripHotelOrder.setLinkUserName(linkUserName.toString());
                itripHotelOrder.setBookingDays(days);
                if (tokenString.startsWith("token:PC")) {
                    itripHotelOrder.setBookType(0);
                } else if (tokenString.startsWith("token:MOBILE")) {
                    itripHotelOrder.setBookType(1);
                } else {
                    itripHotelOrder.setBookType(2);
                }
                //支付之前生成的订单的初始状态为未支付
                itripHotelOrder.setOrderStatus(0);
                try {
                    //生成订单号：机器码 +日期+（MD5）（商品IDs+毫秒数+1000000的随机数）
                    StringBuilder md5String = new StringBuilder();
                    md5String.append(itripHotelOrder.getHotelId());
                    md5String.append(itripHotelOrder.getRoomId());
                    md5String.append(System.currentTimeMillis());
                    md5String.append(Math.random() * 1000000);
                    String md5 = MD5.getMd5(md5String.toString(), 6);

                    //生成订单编号
                    StringBuilder orderNo = new StringBuilder();
                    orderNo.append(systemConfig.getMachineCode());
                    orderNo.append(DateUtil.format(new Date(), "yyyyMMddHHmmss"));
                    orderNo.append(md5);
                    itripHotelOrder.setOrderNo(orderNo.toString());
                    //计算订单的总金额
                    itripHotelOrder.setPayAmount(itripHotelOrderService.getOrderPayAmount(days * itripAddHotelOrderVO.getCount(), itripAddHotelOrderVO.getRoomId()));

                    Map<String, String> map = itripHotelOrderService.itriptxAddItripHotelOrder(itripHotelOrder, linkUserList);
                    DtoUtil.returnSuccess();
                    dto = DtoUtil.returnSuccess("生成订单成功", map);
                } catch (Exception e) {
                    e.printStackTrace();
                    dto = DtoUtil.returnFail("生成订单失败", "100505");
                }
            } else if (flag && null == itripAddHotelOrderVO) {
                dto = DtoUtil.returnFail("不能提交空，请填写订单信息", "100506");
            } else {
                dto = DtoUtil.returnFail("库存不足", "100507");
            }
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100508");
        }
    }


    @ApiOperation(value = "生成订单前,获取预订信息", httpMethod = "POST",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "生成订单前,获取预订信息" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100000 : token失效，请重登录 </p>" +
            "<p>100510 : hotelId不能为空</p>" +
            "<p>100511 : roomId不能为空</p>" +
            "<p>100512 : 暂时无房</p>" +
            "<p>100513 : 系统异常</p>")
    @RequestMapping(value = "/getpreorderinfo", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Dto<RoomStoreVO> getPreOrderInfo(@RequestBody ValidateRoomStoreVO validateRoomStoreVO, HttpServletRequest request) {
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        ItripHotel hotel = null;
        ItripHotelRoom room = null;
        RoomStoreVO roomStoreVO = null;
        try {
            if (EmptyUtils.isEmpty(currentUser)) {
                return DtoUtil.returnFail("token失效，请重登录", "100000");
            }
            if (EmptyUtils.isEmpty(validateRoomStoreVO.getHotelId())) {
                return DtoUtil.returnFail("hotelId不能为空", "100510");
            } else if (EmptyUtils.isEmpty(validateRoomStoreVO.getRoomId())) {
                return DtoUtil.returnFail("roomId不能为空", "100511");
            } else {
                roomStoreVO = new RoomStoreVO();
                hotel = hotelService.getItripHotelById(validateRoomStoreVO.getHotelId());
                room = roomService.getItripHotelRoomById(validateRoomStoreVO.getRoomId());
                Map param = new HashMap();
                param.put("startTime", validateRoomStoreVO.getCheckInDate());
                param.put("endTime", validateRoomStoreVO.getCheckOutDate());
                param.put("roomId", validateRoomStoreVO.getRoomId());
                param.put("hotelId", validateRoomStoreVO.getHotelId());
                roomStoreVO.setCheckInDate(validateRoomStoreVO.getCheckInDate());
                roomStoreVO.setCheckOutDate(validateRoomStoreVO.getCheckOutDate());
                roomStoreVO.setHotelName(hotel.getHotelName());
                roomStoreVO.setRoomId(room.getId());
                roomStoreVO.setPrice(room.getRoomPrice());
                roomStoreVO.setHotelId(validateRoomStoreVO.getHotelId());
                List<StoreVO> storeVOList = itripHotelTempStoreService.queryRoomStore(param);
                roomStoreVO.setCount(1);
                if (EmptyUtils.isNotEmpty(storeVOList)) {
                    roomStoreVO.setStore(storeVOList.get(0).getStore());
                } else {
                    return DtoUtil.returnFail("暂时无房", "100512");
                }
                return DtoUtil.returnSuccess("获取成功", roomStoreVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100513");
        }
    }

    @ApiOperation(value = "根据订单ID查看个人订单详情", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据订单ID查看个人订单详情" +
            "<p>订单状态(orderStatus)（0：待支付 1:已取消 2:支付成功 3:已消费 4：已点评）：</p>" +
            "<p>订单流程：</p>" +
            "<p>订单状态(0：待支付 2:支付成功 3:已消费 4:已点评)的流程：{\"1\":\"订单提交\",\"2\":\"订单支付\",\"3\":\"支付成功\",\"4\":\"入住\",\"5\":\"订单点评\",\"6\":\"订单完成\"}</p>" +
            "<p>订单状态(1:已取消)的流程：{\"1\":\"订单提交\",\"2\":\"订单支付\",\"3\":\"订单取消\"}</p>" +
            "<p>支持支付类型(roomPayType)：{\"1\":\"在线付\",\"2\":\"线下付\",\"3\":\"不限\"}</p>" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100525 : 请传递参数：orderId </p>" +
            "<p>100526 : 没有相关订单信息 </p>" +
            "<p>100527 : 获取个人订单信息错误 </p>" +
            "<p>100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/getpersonalorderinfo/{orderId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dto<Object> getPersonalOrderInfo(@ApiParam(required = true, name = "orderId", value = "订单ID")
                                            @PathVariable String orderId,
                                            HttpServletRequest request) {
        logger.debug("orderId : " + orderId);
        Dto<Object> dto = null;
        String tokenString = request.getHeader("token");
        logger.debug("token name is from header : " + tokenString);
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if (null != currentUser) {
            if (null == orderId || "".equals(orderId)) {
                return DtoUtil.returnFail("请传递参数：orderId", "100525");
            }
            try {
                ItripHotelOrder hotelOrder = itripHotelOrderService.getItripHotelOrderById(Long.valueOf(orderId));
                if (null != hotelOrder) {
                    ItripPersonalHotelOrderVO itripPersonalHotelOrderVO = new ItripPersonalHotelOrderVO();
                    itripPersonalHotelOrderVO.setId(hotelOrder.getId());
                    itripPersonalHotelOrderVO.setBookType(hotelOrder.getBookType());
                    itripPersonalHotelOrderVO.setCreationDate(hotelOrder.getCreationDate());
                    itripPersonalHotelOrderVO.setOrderNo(hotelOrder.getOrderNo());
                    //查询预订房间的信息
                    ItripHotelRoom room = roomService.getItripHotelRoomById(hotelOrder.getRoomId());
                    if (EmptyUtils.isNotEmpty(room)) {
                        itripPersonalHotelOrderVO.setRoomPayType(room.getPayType());
                    }
                    Integer orderStatus = hotelOrder.getOrderStatus();
                    itripPersonalHotelOrderVO.setOrderStatus(orderStatus);
                    //订单状态（0：待支付 1:已取消 2:支付成功 3:已消费 4:已点评）
                    //{"1":"订单提交","2":"订单支付","3":"支付成功","4":"入住","5":"订单点评","6":"订单完成"}
                    //{"1":"订单提交","2":"订单支付","3":"订单取消"}
                    if (orderStatus == 1) {
                        itripPersonalHotelOrderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessCancel()));
                        itripPersonalHotelOrderVO.setProcessNode("3");
                    } else if (orderStatus == 0) {
                        itripPersonalHotelOrderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                        itripPersonalHotelOrderVO.setProcessNode("2");//订单支付
                    } else if (orderStatus == 2) {
                        itripPersonalHotelOrderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                        itripPersonalHotelOrderVO.setProcessNode("3");//支付成功（未出行）
                    } else if (orderStatus == 3) {
                        itripPersonalHotelOrderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                        itripPersonalHotelOrderVO.setProcessNode("5");//订单点评
                    } else if (orderStatus == 4) {
                        itripPersonalHotelOrderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                        itripPersonalHotelOrderVO.setProcessNode("6");//订单完成
                    } else {
                        itripPersonalHotelOrderVO.setOrderProcess(null);
                        itripPersonalHotelOrderVO.setProcessNode(null);
                    }
                    itripPersonalHotelOrderVO.setPayAmount(hotelOrder.getPayAmount());
                    itripPersonalHotelOrderVO.setPayType(hotelOrder.getPayType());
                    itripPersonalHotelOrderVO.setNoticePhone(hotelOrder.getNoticePhone());
                    dto = DtoUtil.returnSuccess("获取个人订单信息成功", itripPersonalHotelOrderVO);
                } else {
                    dto = DtoUtil.returnFail("没有相关订单信息", "100526");
                }
            } catch (Exception e) {
                e.printStackTrace();
                dto = DtoUtil.returnFail("获取个人订单信息错误", "100527");
            }
        } else {
            dto = DtoUtil.returnFail("token失效，请重登录", "100000");
        }
        return dto;
    }

    @ApiOperation(value = "根据订单ID查看个人订单详情-房型相关信息", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据订单ID查看个人订单详情-房型相关信息" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100529 : 请传递参数：orderId </p>" +
            "<p>100530 : 没有相关订单房型信息 </p>" +
            "<p>100531 : 获取个人订单房型信息错误 </p>" +
            "<p>支持支付类型(roomPayType)：{\"1\":\"在线付\",\"2\":\"线下付\",\"3\":\"不限\"}</p>" +
            "<p>100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/getpersonalorderroominfo/{orderId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dto<Object> getPersonalOrderRoomInfo(@ApiParam(required = true, name = "orderId", value = "订单ID")
                                                @PathVariable String orderId,
                                                HttpServletRequest request) {
        logger.debug("orderId : " + orderId);
        Dto<Object> dto = null;
        String tokenString = request.getHeader("token");
        logger.debug("token name is from header : " + tokenString);
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if (null != currentUser) {
            if (null == orderId || "".equals(orderId)) {
                return DtoUtil.returnFail("请传递参数：orderId", "100529");
            }
            try {
                ItripPersonalOrderRoomVO vo = itripHotelOrderService.getItripHotelOrderRoomInfoById(Long.valueOf(orderId));
                if (null != vo) {
                    dto = DtoUtil.returnSuccess("获取个人订单房型信息成功", vo);
                } else {
                    dto = DtoUtil.returnFail("没有相关订单房型信息", "100530");
                }
            } catch (Exception e) {
                e.printStackTrace();
                dto = DtoUtil.returnFail("获取个人订单房型信息错误", "100531");
            }
        } else {
            dto = DtoUtil.returnFail("token失效，请重登录", "100000");
        }
        return dto;
    }

    @ApiOperation(value = "分页查询个人订单列表，并分页显示", httpMethod = "POST",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据条件查询个人订单列表，并分页显示" +
            "<p>订单类型(orderType)（-1：全部订单 0:旅游订单 1:酒店订单 2：机票订单）：</p>" +
            "<p>订单状态(orderStatus)（0：待支付 1:已取消 2:支付成功 3:已消费 4：已点评）：</p>" +
            "<p>对于页面tab条件：</p>" +
            "<p>全部订单（orderStatus：-1）</p>" +
            "<p>未出行（orderStatus：2）</p>" +
            "<p>待付款（orderStatus：0）</p>" +
            "<p>待评论（orderStatus：3）</p>" +
            "<p>已取消（orderStatus：1）</p>" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100501 : 请传递参数：orderType </p>" +
            "<p>100502 : 请传递参数：orderStatus </p>" +
            "<p>100503 : 获取个人订单列表错误 </p>" +
            "<p>100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/getpersonalorderlist", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Dto<Object> getPersonalOrderList(@RequestBody ItripSearchOrderVO itripSearchOrderVO,
                                            HttpServletRequest request) {
        logger.debug("orderType : " + itripSearchOrderVO.getOrderType());
        logger.debug("orderStatus : " + itripSearchOrderVO.getOrderStatus());
        Integer orderType = itripSearchOrderVO.getOrderType();
        Integer orderStatus = itripSearchOrderVO.getOrderStatus();
        Dto<Object> dto = null;
        String tokenString = request.getHeader("token");
        logger.debug("token name is from header : " + tokenString);
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if (null != currentUser) {
            if (orderType == null) {
                return DtoUtil.returnFail("请传递参数：orderType", "100501");
            }
            if (orderStatus == null) {
                return DtoUtil.returnFail("请传递参数：orderStatus", "100502");
            }

            Map<String, Object> param = new HashMap<>();
            param.put("orderType", orderType == -1 ? null : orderType);
            param.put("orderStatus", orderStatus == -1 ? null : orderStatus);
            param.put("userId", currentUser.getId());
            param.put("orderNo", itripSearchOrderVO.getOrderNo());
            param.put("linkUserName", itripSearchOrderVO.getLinkUserName());
            param.put("startDate", itripSearchOrderVO.getStartDate());
            param.put("endDate", itripSearchOrderVO.getEndDate());
            try {
                Page page = itripHotelOrderService.queryOrderPageByMap(param,
                        itripSearchOrderVO.getPageNo(),
                        itripSearchOrderVO.getPageSize());
                dto = DtoUtil.returnSuccess("获取个人订单列表成功", page);
            } catch (Exception e) {
                e.printStackTrace();
                dto = DtoUtil.returnFail("获取个人订单列表错误", "100503");
            }

        } else {
            dto = DtoUtil.returnFail("token失效，请重登录", "100000");
        }
        return dto;
    }

    @ApiOperation(value = "根据订单ID获取订单信息", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据订单ID获取订单信息" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100533 : 没有查询到相应订单 </p>" +
            "<p>100534 : 系统异常 </p>")
    @RequestMapping(value = "/queryOrderById/{orderId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dto<Object> queryOrderById(@ApiParam(required = true, name = "orderId", value = "订单ID") @PathVariable Long orderId,HttpServletRequest request) {
        logger.debug("orderId : " + orderId);
        ItripModifyHotelOrderVO itripModifyHotelOrderVO = null;
        try {
            String tokenString = request.getHeader("token");
            logger.debug("token name is from header : " + tokenString);
            ItripUser currentUser = validationToken.getCurrentUser(tokenString);
            if(EmptyUtils.isEmpty(currentUser)){
                return DtoUtil.returnFail("token失效，请重登录", "100000");
            }
            ItripHotelOrder order = itripHotelOrderService.getItripHotelOrderById(orderId);
            if (EmptyUtils.isEmpty(order)) {
                return DtoUtil.returnFail("100533", "没有查询到相应订单");
            }
            itripModifyHotelOrderVO = new ItripModifyHotelOrderVO();
            BeanUtils.copyProperties(order, itripModifyHotelOrderVO);
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("orderId", order.getId());
            List<ItripOrderLinkUserVo> itripOrderLinkUserList = itripOrderLinkUserService.getItripOrderLinkUserListByMap(param);
            itripModifyHotelOrderVO.setItripOrderLinkUserList(itripOrderLinkUserList);
            return DtoUtil.returnSuccess("获取订单成功", itripModifyHotelOrderVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100534");
        }
    }

}


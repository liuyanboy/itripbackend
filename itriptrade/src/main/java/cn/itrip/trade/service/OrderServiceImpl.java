package cn.itrip.trade.service;

import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.common.EmptyUtils;
import cn.itrip.dao.hotelorder.ItripHotelOrderMapper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单支付处理实现
 */
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    private Logger logger = Logger.getLogger(OrderServiceImpl.class);

    @Resource
    private ItripHotelOrderMapper itripHotelOrderMapper;

    //加载酒店订单
    @Override
    public ItripHotelOrder loadItripHotelOrder(String orderNo) throws Exception {
        logger.debug("加载订单：" + orderNo);
        Map<String, Object> param = new HashMap();
        param.put("orderNo", orderNo);
        List<ItripHotelOrder> orders = itripHotelOrderMapper.getItripHotelOrderListByMap(param);
        if (orders.size() == 1) {
            return orders.get(0);
        } else {
            return null;
        }
    }

    @Override
    public boolean processed(String orderNo) throws Exception {
        ItripHotelOrder itripHotelOrder=this.loadItripHotelOrder(orderNo);
        return itripHotelOrder.getOrderStatus().equals(2)&&
                !EmptyUtils.isEmpty(itripHotelOrder.getTradeNo());
    }

    @Override
    public void paySuccess(String orderNo, int payType,String tradeNo) throws Exception {
        //更新订单状态、支付宝交易号
        logger.debug("订单支付成功："+orderNo);
        ItripHotelOrder itripHotelOrder=this.loadItripHotelOrder(orderNo);
        itripHotelOrder.setOrderStatus(2);//支付成功
        itripHotelOrder.setPayType(payType);
        itripHotelOrder.setTradeNo(tradeNo);//交易号（如支付宝交易号）
        itripHotelOrderMapper.updateItripHotelOrder(itripHotelOrder);

        //支付成功后，能减库存，由业务提供API，在这里进行调用(后续代码不写)
        //订单状态0: 表示未支付  1: 表示订单已取消  2: 表示支付成功3: 表示支付失败   //只有0和3的订单可以再进行支付。
    }

    @Override
    public void payFailed(String orderNo, int payType,String tradeNo) throws Exception {
        logger.debug("订单支付失败："+orderNo);
        ItripHotelOrder itripHotelOrder=this.loadItripHotelOrder(orderNo);
        itripHotelOrder.setOrderStatus(1);//支付状态：已取消
        itripHotelOrder.setPayType(payType);
        itripHotelOrder.setTradeNo(tradeNo);//交易号（如支付宝交易号）
        itripHotelOrderMapper.updateItripHotelOrder(itripHotelOrder);
    }

}

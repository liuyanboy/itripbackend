package cn.itrip.trade.service;

import cn.itrip.beans.pojo.ItripHotelOrder;

public interface OrderService {

    /**
     * 加载酒店订单
     * @param orderNo 订单编号
     * @throws Exception
     */
    public ItripHotelOrder loadItripHotelOrder(String orderNo) throws Exception;


    /**
     * 判断该订单是否已被处理过（被更新为已支付状态）
     * @param orderNo
     * @throws Exception
     */
    public boolean processed(String orderNo) throws Exception;
    /**
     * 支付成功
     * @param orderNo 订单编号
     * @param payType 支付方式:1:支付宝 2:微信 3:到店付
     * @param tradeNo 支付平台返回的交易码
     * @throws Exception
     */
    public void paySuccess(String orderNo, int payType, String tradeNo) throws Exception;
    /**
     * 支付失败
     * @param orderNo 订单编号
     * @param payType 支付方式:1:支付宝 2:微信 3:到店付
     * @param tradeNo 支付平台返回的交易码
     * @throws Exception
     */
    public void payFailed(String orderNo, int payType, String tradeNo) throws Exception;

}

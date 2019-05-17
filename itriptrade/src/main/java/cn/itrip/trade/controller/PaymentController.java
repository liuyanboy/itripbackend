package cn.itrip.trade.controller;

import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.common.EmptyUtils;
import cn.itrip.trade.config.AlipayConfig;
import cn.itrip.trade.service.OrderService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付处理控制器
 * @author wenjie
 */
@Controller
@RequestMapping("/api")

public class PaymentController {


    private Logger logger=Logger.getLogger(PaymentController.class);

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private OrderService orderService;

    /**
     * 确认订单信息
     * @param orderNo 订单号
     */
    @RequestMapping(value = "/prepay/{orderNo}", method = RequestMethod.GET)
    public String prePay(@PathVariable String orderNo, ModelMap model) {
        try {
            //通过订单号查询爱旅行酒店订单对象详情。
            ItripHotelOrder order = orderService.loadItripHotelOrder(orderNo);

            if (!EmptyUtils.isEmpty(order)) {
                model.addAttribute("orderNo", order.getOrderNo());
                model.addAttribute("hotelName", order.getHotelName());
                model.addAttribute("roomId", order.getRoomId());
                model.addAttribute("count", order.getCount());
                model.addAttribute("payAmount", order.getPayAmount());
                return "pay";  //跳转到支付确认页面，将当前订单信息在确认页面 中进行显示
            }else {
                return "notfound";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * 向支付宝提交支付请求
     * @param WIDout_trade_no
     *            商户订单号，商户网站订单系统中唯一订单号，必填
     * @param WIDsubject
     *            订单名称，必填
     * @param WIDtotal_amount
     *            付款金额，必填
     * 【代码参考alipay.trade.wap.pay-java-utf-8\WebContent\wappay\pay.jsp页面中小脚本的java代码】
     */
    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public void pay(
            @RequestParam String WIDout_trade_no,
            @RequestParam String WIDsubject,
            @RequestParam String WIDtotal_amount, HttpServletResponse response) {
        // 超时时间 可空
        String timeout_express = "2m";
        // 销售产品码 必填
        String product_code = "QUICK_WAP_PAY";
        /**********************/
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        // 调用RSA签名方式
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(),
                alipayConfig.getAppID(), alipayConfig.getRsaPrivateKey(),
                alipayConfig.getFormat(), alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(), alipayConfig.getSignType());
        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();

        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(WIDout_trade_no);
        model.setSubject(WIDsubject);
        model.setTotalAmount(WIDtotal_amount);
        model.setTimeoutExpress(timeout_express);
        model.setProductCode(product_code);
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotifyUrl());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturnUrl());
        // form表单生产
        String form = "";
        try {
            // 调用SDK生成表单
            form = client.pageExecute(alipay_request).getBody();
            System.out.println(form);
            response.setContentType("text/html;charset="
                    + alipayConfig.getCharset());
            response.getWriter().write(form);// 直接将完整的表单html输出到页面
            response.getWriter().flush();
            response.getWriter().close();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 异步通知，跟踪支付状态变更
     * 【代码参考alipay.trade.wap.pay-java-utf-8\WebContent\notify_url.jsp页面中小脚本的java代码】
     */
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public void trackPaymentStatus(HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = new HashMap<String, String>();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
                params.put(name, valueStr);
            }
            // 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no")
                    .getBytes("ISO-8859-1"), "UTF-8");
            // 支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes(
                    "ISO-8859-1"), "UTF-8");
            // 交易状态
            String trade_status = new String(request.getParameter("trade_status")
                    .getBytes("ISO-8859-1"), "UTF-8");

            // 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
            // 计算得出通知验证结果
            // boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String
            // publicKey, String charset, String sign_type)
            boolean verify_result = AlipaySignature.rsaCheckV1(params,
                    alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), "RSA2");

            if (verify_result) {// 验证成功
                // ////////////////////////////////////////////////////////////////////////////////////////
                // 请在这里加上商户的业务逻辑程序代码

                //即时到账普通版，那么这时的交易状态值为：  TRADE_FINISHED；如果是即时到账高级版，此时的交易状态值就为：TRADE_SUCCESS
                //收到TRADE_FINISHED请求后，这笔订单就结束了，支付宝不会再主动请求商户网站了；收到TRADE_SUCCESS请求后，后续一定还有至少一条通知记录，即TRADE_FINISHED。
                if (trade_status.equals("TRADE_FINISHED")) {
                    // 判断该笔订单是否在商户网站中已经做过处理
                    // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    // 如果有做过处理，不执行商户的业务程序
                    if(!orderService.processed(out_trade_no))
                    {
                        orderService.paySuccess(out_trade_no, 2,trade_no);
                    }
                    logger.info("订单："+out_trade_no+" 交易完成");
                    // 注意：
                    // 如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                    // 如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                } else if (trade_status.equals("TRADE_SUCCESS")) {
                    // 判断该笔订单是否在商户网站中已经做过处理
                    // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    // 如果有做过处理，不执行商户的业务程序
                    if(!orderService.processed(out_trade_no))
                    {
                        orderService.paySuccess(out_trade_no, 2,trade_no);
                    }
                    logger.info("订单："+out_trade_no+" 交易成功");

                    // 注意：
                    // 如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                }

                response.getWriter().println("success"); // 请不要修改或删除

                // ////////////////////////////////////////////////////////////////////////////////////////
            } else {// 验证失败
                orderService.payFailed(out_trade_no, 1,trade_no);
                response.getWriter().println("fail");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (AlipayApiException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

    }

    /**
     * 支付宝页面跳转同步通知页面
     * 【代码参考alipay.trade.wap.pay-java-utf-8\WebContent\return_url.jsp页面中小脚本的java代码】
     */
    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public void callback(HttpServletRequest request,
                         HttpServletResponse response) {
        try {
            //获取支付宝GET过来反馈信息
            Map<String,String> params = new HashMap<String,String>();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }

            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
            //计算得出通知验证结果
            //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
            boolean verify_result = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), "RSA2");

            if(verify_result){//验证成功
                //提示支付成功，跳转到支付成功页面
                response.sendRedirect(alipayConfig.getPaymentSuccessUrl());
            }else{
                //提示支付失败，跳转到支付失败页面
                response.sendRedirect(alipayConfig.getPaymentFailureUrl());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (AlipayApiException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }



    //支付成功跳转页面
    @RequestMapping("/paymentSucc")
    public String paymentSucc(){
        return "success";
    }

    //支付失败跳转页面
    @RequestMapping("/paymentFail")
    public String paymentFail(){
        return "fail";
    }

}

package com.androidcat.biz.consts;

/**
 * 错误码类
 */
public class SDKConsts {
    public static final String DESC_SUCCESS = "成功";

    /**
     * 订单类型
     */
    public static final String ORDER_TYPE_PERSONALIZE = "1";
    public static final String ORDER_TYPE_QC = "2";
    public static final String ORDER_TYPE_DOWLNLOAD = "3";

    /**
     * hce业务回调类型
     */
    //卡禁用
    public static final int MSG_DISABLE = 1;
    //卡删除
    public static final int MSG_DELETE = 2;
    //消费
    public static final int HCE_MSG_CONSUME = 8054;

    public static final int MSG_SESSION_CLOSED = 3;

    /**
     * 业务类型
     */
    public static final int TYPE_LOGIN=1001;//登录
    public static final int TYPE_REGISTER=1002;//注册
    public static final int TYPE_IMPLICIT=1003;//隐式登录
    public static final int TYPE_GET_VERIFY=1004;//获取验证码
    public static final int TYPE_REALNAME=1005;//实名认证
    public static final int TYPE_RESET_PWD=1006;//重置密码
    public static final int TYPE_MODIFY_PWD=1007;//修改密码
    public static final int TYPE_LOGOUT=1008;//退出登录
    public static final int TYPE_CHECK_AUTH=1009;//检验实名认证
    public static final int TYPE_QUERY_USER_INFO=1010;//查询用户信息
    public static final int TYPE_SEND_FEEDBACK=1011;//意见反馈
    public static final int TYPE_QUERY_APPLETS=2001;//查询应用列表
    public static final int TYPE_WHITE_LIST=2003;//更新白名单
    public static final int TYPE_CHECK_UPDATE=2004;//查询应用支付渠道
    public static final int TYPE_CREATE_ORDER=3001;//创建订单
    public static final int TYPE_QUERY_ORDER_DETAIL=3002;//查询订单详情
    public static final int TYPE_QUERY_ORDERS=3003;//查询订单列表
    public static final int TYPE_GET_ADLIST=3004;//订单支付
    public static final int TYPE_REFUND=3005;//退款申请
    public static final int TYPE_GET_SERVER_TIME=3006;//查询失败的充值订单
    public static final int TYPE_GET_SHORTCUT_INFO=3007;//查询消费订单
    public static final int TYPE_PERSONAL=4001;//开卡
    public static final int TYPE_RECHARGE=4002;//充值
    public static final int TYPE_SET_CANTEEN_SITE=4003;//卡片迁出
    public static final int TYPE_QUERY_DEVICEID=4004;//卡片迁入
    public static final int TYPE_BIND_DEVICE=4011;//查询实名认证要素
    public static final int TYPE_BIND_JPUSH=5005;//卡片恢复
    public static final int TYPE_UPLOAD_CRASH=6001;//上传crash
    public static final int TYPE_UPLOAD_CONSUME=5004;//上传消费记录


    /**
     * 接口返回值
     */
    public static final int SUCCESS=0;//业务正常
    public static final int CALL_BACK_NULL=1;//callBack不能为空
    public static final int LOGIN_TYPE_ERROR=2;//登录类型type不正确
    public static final int PHONE_ERROR=3;//手机号为空或格式不正确
    public static final int VERIFY_ERROR=4;//验证码为空或格式不正确
    public static final int PWD_NULL=5;//密码为空
    public static final int PWD_ERROR=6;//密码为8-20位数字和字母
    public static final int VERIFY_TYPE_NULL=7;//验证码类型为空
    public static final int ID_NULL=8;//应用id为空
    public static final int ORDER_TYPE_NULL=9;//创建订单id为空
    public static final int RECHARGE_MONEY_NULL=10;//充值金额为空
    public static final int CARDNO_NULL=11;//卡号为空
    public static final int BALANCE_ERROR=12;//余额获取失败
    public static final int APPLET_NULL=13;//应用为空
    public static final int EXPIRE_VALIDITY=14;//有效期过期
    public static final int WAIT_CALLBACK=15;//进入异步流程，等待回调

    /**
     * 回调错误码
     */

    public static final String CODE_SUCCESS = "0000";//成功
    public static final String UPLOAD_CONSUME_FAIL="1001";//上传交易记录失败
    public static final String FAIL_RECHARGE="1002";//有未完成的充值订单
    public static final String CARD_OUT_CHANGE_DEFCARD="431";//需设置默认卡
    public static final String REAL_NAME_FAIL="1003";//实名认证失败
    public static final String KK_SUCCESS_QC_FAIL="1004";//开卡成功，圈存失败
}

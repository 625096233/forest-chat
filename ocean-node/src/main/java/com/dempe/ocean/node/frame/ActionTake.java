package com.dempe.ocean.node.frame;


import com.alibaba.fastjson.JSONObject;
import com.dempe.ocean.common.protocol.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 业务处理快照类
 * User: Dempe
 * Date: 2015/11/4
 * Time: 10:17
 * To change this template use File | Settings | File Templates.
 */
public class ActionTake implements Take<Message, Message> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActionTake.class);

    private ServerContext context;

    public ActionTake(ServerContext context) {
        this.context = context;
    }

    /**
     * 一个request获取一个response
     *
     * @param request 请求消息
     * @return Response 返回消息
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    public Message act(Message request) throws InvocationTargetException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        String uri = request.getUri();
        if (StringUtils.isBlank(uri)) {
            LOGGER.warn("[dispatcher]:jsonURI is blank");
            return null;
        }
        // 通过Request uri找到对应的ActionMethod
        ActionMethod actionMethod = context.tackAction(uri);
        if (actionMethod == null) {
            LOGGER.warn("[dispatcher]:not find jsonURI {}", uri);
            return null;
        }
        Method method = actionMethod.getMethod();
        // 获取方法参数
        String[] parameterNames = MethodParam.getParameterNames(method);
        Map<String, String> paramMap = request.getParamMap();
        // 获取方法执行参数值
        Object[] parameterValues = MethodParam.getParameterValues(parameterNames, method, paramMap);
        Object result = MethodInvoker.interceptorInvoker(actionMethod, parameterValues);
        if (result == null) {
            // 当action method 返回是void的时候，不返回任何消息
            LOGGER.debug("actionMethod:{} return void.", actionMethod);
            return null;
        }
        return buildResp(result, request);
    }

    /**
     * 封装返回消息
     *
     * @param result
     * @return
     */
    public Message buildResp(Object result, Message request) throws IllegalAccessException, InstantiationException,
            ClassNotFoundException {
        Message resp = new Message();
        // set请求消息id标识，用于client将Response&Request对应
        resp.setExtendData(JSONObject.toJSONBytes(result));
        resp.setUid(request.getUid());
        resp.setMessageID(request.getMessageID());
        return resp;
    }


}
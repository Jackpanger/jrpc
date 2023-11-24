package com.jackpang;

import com.jackpang.annotation.TryTimes;

/**
 * description: com.jackpang.HelloJrpc
 * date: 11/3/23 9:55 PM
 * author: jinhao_pang
 * version: 1.0
 */
public interface HelloJrpc {
    /**
     * General interface, service and client provider implements this interface
     * @param msg
     * @return
     */
    @TryTimes()
    String sayHi(String msg);
}

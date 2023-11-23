package com.jackpang.loadBalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface Selector {

    InetSocketAddress getNext();
}

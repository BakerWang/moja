package com.song.moja.zk;

public interface ZkConstant {

//    String ZK_CONNECTION_STRING = "localhost:2181";
	//zk地址
	String ZK_CONNECTION_STRING = "10.1.1.131:2181";
    int ZK_SESSION_TIMEOUT = 5000;
    String ZK_REGISTRY_PATH = "/registry";
    String ZK_PROVIDER_PATH = ZK_REGISTRY_PATH + "/provider";
}
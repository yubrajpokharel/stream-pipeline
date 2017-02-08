package com.ingestion.api.endpoints;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;

/**
 * Created by prayagupd
 * on 2/8/17.
 */

public class EventStream {

    public List<JSONObject> ping() throws IOException, KeeperException, InterruptedException {

        ZooKeeper serviceState = new ZooKeeper("localhost:2181", 1000, watchedEvent ->
                out.println(watchedEvent.toString()));

        List<String> brokerIds = serviceState.getChildren("/brokers/ids", false);

        return brokerIds.stream().map(brokerIdentifier -> broker(serviceState, brokerIdentifier)).collect(toList());
    }

    private JSONObject broker(ZooKeeper serviceState, String broker) {
        try {
            return new JSONObject(new String(serviceState.getData("/brokers/ids/" + broker, false, null)));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

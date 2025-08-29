package com.system24seven.ignition.eventstream.source.gateway;

import static com.system24seven.ignition.eventstream.source.SnmpSourceModule.MODULE_ID;

import com.inductiveautomation.eventstream.SourceDescriptor;
import com.inductiveautomation.eventstream.gateway.api.EventStreamContext;
import com.inductiveautomation.eventstream.gateway.api.EventStreamSource;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.system24seven.ignition.eventstream.source.SnmpSourceConfig;
import com.system24seven.ignition.eventstream.source.SnmpSourceModule;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Starts a listener for SNMP Traps and converts them to events
 */
public class SnmpSource implements EventStreamSource {

    public static Factory createFactory() {
        return new Factory() {
            @Override
            public SourceDescriptor getDescriptor() {
                return new SourceDescriptor(
                        SnmpSourceModule.MODULE_ID,
                        SnmpSourceModule.MODULE_NAME,
                    "Listens for SNMP traps and emits them as events."
                );
            }

            @Override
            public EventStreamSource create(EventStreamContext context, JsonObject jsonConfig) {
                return new SnmpSource(context, SnmpSourceConfig.fromJson(jsonConfig));
            }
        };
    }


    private final EventStreamContext context;
    private final AtomicReference<Subscriber> subscriber = new AtomicReference<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    private final String listenAddress,listenUrl;
    private final String port;
    private SNMPListener listener;

    public SnmpSource(EventStreamContext context, SnmpSourceConfig config) {
        this.context = context;
        this.listenAddress = config.listenIp();
        this.port = config.listenPort();
        this.listenUrl = "udp:" + this.listenAddress + "/" + this.port;
    }

    @Override
    public void onStartup(Subscriber subscriber) {
        context.logger().infof("Starting %s", MODULE_ID);
        //String listenAddress = "udp:0.0.0.0/162";
        this.listener = new SNMPListener(listenUrl,subscriber);
        context.logger().infof("Starting Listener");
        try {
            this.listener.listen();
            context.logger().infof("Started Listener");
        } catch (IOException e) {
            context.logger().errorf( "Error running trap listener", e);
        }
    }

    @Override
    public void onShutdown() {
        context.logger().infof("Shutting down %s", MODULE_ID);
        try {
            listener.stop();
        } catch (IOException e) {
            context.logger().errorf( "Error stopping trap listener", e);
        }
        subscriber.set(null);

    }
}
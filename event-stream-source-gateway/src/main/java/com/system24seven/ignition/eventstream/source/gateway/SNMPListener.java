package com.system24seven.ignition.eventstream.source.gateway;

import com.inductiveautomation.eventstream.EventPayload;
import com.inductiveautomation.eventstream.gateway.api.EventStreamSource;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import java.io.IOException;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.snmp4j.*;
import org.snmp4j.mp.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

/**
 * SNMP4J Trap Listener Dependencies: snmp4j-3.7.7.jar (or latest version) Maven:
 * <groupId>org.snmp4j</groupId><artifactId>snmp4j</artifactId><version>3.7.7</version>
 */
public class SNMPListener implements CommandResponder {
  private final LoggerEx logger = LoggerEx.newBuilder().build(SnmpSourceGatewayHook.class);

  private MultiThreadedMessageDispatcher dispatcher;
  private Snmp snmp = null;
  private final Address listenAddress;
  private ThreadPool threadPool;
  private final AtomicReference<EventStreamSource.Subscriber> subscriber = new AtomicReference<>();

  public SNMPListener(String listenAddress, EventStreamSource.Subscriber subscriber) {
    this.listenAddress = GenericAddress.parse(listenAddress);
    this.subscriber.set(subscriber);
  }

  /** Initialize and start the trap listener */
  public void listen() throws IOException {
    // Create thread pool for handling traps
    threadPool = ThreadPool.create("TrapListener", 10);
    dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

    // Create UDP transport
    DefaultUdpTransportMapping transport =
        new DefaultUdpTransportMapping(new UdpAddress(listenAddress.toString()));

    // Create SNMP instance
    snmp = new Snmp(dispatcher, transport);
    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());

    // Add USM for SNMPv3 support
    USM usm =
        new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
    SecurityModels.getInstance().addSecurityModel(usm);

    // Add command responder (this class handles the traps)
    snmp.addCommandResponder(this);

    // Start listening
    transport.listen();
    snmp.listen();

    logger.info("SNMP Trap Listener started on " + listenAddress);
  }

  /** Handle incoming SNMP messages (traps) */
  @Override
  public synchronized void processPdu(CommandResponderEvent event) {
    try {
      PDU pdu = event.getPDU();

      if (pdu == null) {
        logger.warn("Received null PDU");
        return;
      }

      // Get source address
      String sourceAddress = event.getPeerAddress().toString();
      logger.info("Trap received from: " + sourceAddress);
      List<? extends VariableBinding> variables = pdu.getVariableBindings();
      Dictionary<String, String> params = new java.util.Hashtable<>();
      params.put("sourceAddress", sourceAddress);
      params.put("requestID", pdu.getRequestID().toString());
      if (!variables.isEmpty()) {
        for (VariableBinding vb : variables) {
          OID oid = vb.getOid();
          if (oid.equals(SnmpConstants.sysUpTime)) {
            params.put("sysUpTime", vb.getVariable().toString());
          } else if (oid.equals(SnmpConstants.snmpTrapOID)) {
            params.put("trapOID", vb.getVariable().toString());
            // Catch All
          } else {
            params.put(vb.getOid().toString(), vb.getVariable().toString());
          }
        }
      }
      this.subscriber.get().submitEvent(EventPayload.builder(params).build());
    } catch (Exception e) {
      logger.error("Error processing trap", e);
    }
  }

  /** Stop the trap listener */
  public void stop() throws IOException {
    if (snmp != null) {
      snmp.close();
    }
    if (threadPool != null) {
      threadPool.stop();
    }
    logger.info("SNMP Trap Listener stopped");
  }
}

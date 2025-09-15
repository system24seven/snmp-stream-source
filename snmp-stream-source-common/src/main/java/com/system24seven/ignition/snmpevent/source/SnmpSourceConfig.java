package com.system24seven.ignition.snmpevent.source;

import com.inductiveautomation.ignition.common.gson.JsonObject;

public record SnmpSourceConfig(String listenIp, String listenPort) {

  public static final String LISTEN_IP = "listenIp";
  public static final String LISTEN_PORT = "listenPort";

  public JsonObject toJson() {
    var json = new JsonObject();
    json.addProperty(LISTEN_IP, listenIp);
    json.addProperty(LISTEN_PORT, listenPort);
    return json;
  }

  public static SnmpSourceConfig fromJson(JsonObject config) {
    if (config == null || config.isEmpty()) {
      return defaultConfig();
    }
    return new SnmpSourceConfig(
        config.get(LISTEN_IP).getAsString(), config.get(LISTEN_PORT).getAsString());
  }

  public static SnmpSourceConfig defaultConfig() {
    return new SnmpSourceConfig("0.0.0.0", "162");
  }
}

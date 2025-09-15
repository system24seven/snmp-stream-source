package com.system24seven.ignition.snmpevent.source.designer;

import com.inductiveautomation.eventstream.designer.api.EventStreamContext;
import com.inductiveautomation.eventstream.designer.api.source.SourceEditor;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.system24seven.ignition.snmpevent.source.SnmpSourceConfig;
import java.text.NumberFormat;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class SnmpSourceEditor extends SourceEditor {

    private final JTextField listenIp = new JTextField();
    private final JTextField listenPort = new JFormattedTextField(NumberFormat.getNumberInstance());

    public SnmpSourceEditor() {
        super();
        setLayout(new MigLayout(
            "ins 0, fillx, gapy 4, wrap 1",
            "[fill, grow]", "")
        );
        add(new JLabel("Listen IP:"));
        add(listenIp, "width 20:400:400, wrap 16");
        add(new JLabel("Listen Port:"));
        add(listenPort, "width 20:400:400, wrap 16");
    }

    /**
     * This method is executed on the Event Dispatcher Thread (EDT).
     */
    @Override
    public void initialize(EventStreamContext context, JsonObject json) {
        SnmpSourceConfig config = SnmpSourceConfig.fromJson(json);
        listenIp.setText(config.listenIp());
        listenPort.setText(config.listenPort());
    }

    /**
     * This method is executed on the Event Dispatcher Thread (EDT).
     */
    @Override
    public JsonObject getConfig() {
        return new SnmpSourceConfig(listenIp.getText(),listenPort.getText()).toJson();
    }
}


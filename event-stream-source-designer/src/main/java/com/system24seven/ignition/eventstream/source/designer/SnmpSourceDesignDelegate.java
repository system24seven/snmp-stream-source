package com.system24seven.ignition.eventstream.source.designer;

import com.inductiveautomation.eventstream.designer.api.EventStreamContext;
import com.inductiveautomation.eventstream.designer.api.source.EventStreamSourceDesignDelegate;
import com.inductiveautomation.eventstream.designer.api.source.SourceEditor;
import com.system24seven.ignition.eventstream.source.SnmpSourceModule;

public class SnmpSourceDesignDelegate implements EventStreamSourceDesignDelegate {

    @Override
    public SourceEditor getEditor(EventStreamContext context) {
        return new SnmpSourceEditor();
    }

    @Override
    public String getType() {
        return SnmpSourceModule.MODULE_ID;
    }

}

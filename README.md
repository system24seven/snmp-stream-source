# SNMP Trap Event Stream Source for Ignition 8.3

This is a working implementation of SNMP4J implementing an SNMP Traps listener that converts SNMP Traps into Ignition
Events for the Event Streams Module.

Installation and Configuration:
Install as a normal module on the gateway.
Open up incoming port UDP:162 (or custom if you change the trap port)
Configure the Event Listener in the Ignition Event Streams UI in Designer. 
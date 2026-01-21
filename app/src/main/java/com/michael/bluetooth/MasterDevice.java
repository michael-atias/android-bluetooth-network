package com.michael.bluetooth;


public class MasterDevice extends Device
{
    public MasterDevice(String id, String name, boolean isMaster)
    {
        super(id, name, isMaster);
    }

    public MasterDevice(MasterDevice m)
    {
        super(m);
    }
}

package com.michael.bluetooth;

public abstract class Device
{
    private String id;
    private String name;
    private boolean isMaster;

    public Device(String id, String name, boolean isMaster)
    {
        this.id = id;
        this.name = name;
        this.isMaster = isMaster;
    }

    public Device(Device d)
    {
        this.id = d.id;
        this.name = d.name;
        this.isMaster = d.isMaster;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    @Override
    public String toString() {
//        return "Device{" +
//    //            "id='" + id + '\'' +
//                ", name='" + name + '\'' +
//    //            ", isMaster=" + isMaster +
//                '}';
        return name;
    }
}

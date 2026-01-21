package com.michael.bluetooth;

public class ClientDevice extends Device
{
    private ClientConnection connection;
    private boolean isSetName;

    public ClientDevice(String id, String name, boolean isMaster, ClientConnection connection)
    {
        super(id, name, isMaster);
        this.connection = connection;
        isSetName = false;
    }

    public ClientDevice(ClientDevice c)
    {
        super(c.getId(), c.getName(), c.isMaster());
        this.connection = c.getConnection();
    }

    public boolean getIsSetName()
    {
        return isSetName;
    }

    public void setIsSetName(boolean isSetName)
    {
        this.isSetName = isSetName;
    }

    public ClientConnection getConnection()
    {
        return this.connection;
    }
}

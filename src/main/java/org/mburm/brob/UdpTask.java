/* receive packets from the Balancing Robot and
 * process them so that we can set commands back
 * to the robot
 */

package org.mburm.brob;

import android.os.HandlerThread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * Created by mbmis006 on 3/6/2017.
 */

public class UdpTask extends HandlerThread implements Runnable {

    BROBActivity u;
    private int Port;
    private DatagramPacket UDP;
    private String Host;
    private byte[] data = new byte[256];
    private byte[] buffer = new byte[256];
    private int head, tail;
    private DatagramSocket udp;
    private boolean snd;
    private boolean rcv;
    private boolean Connected;
    private InetAddress Client;

    public UdpTask(String name, String host) {
        super(name);
        init(host);
    }

    public UdpTask(String name, int priority, String host) {
        super(name, priority);
        init(host);
    }

    private void init(String host)
    {
        Port = 0x2616;
        Host = host;
        UDP = new DatagramPacket(data, data.length);
        Connected = false;
        for (head=0;head<256;head++)
            buffer[head] = 0;
        head = 0;
        tail = 0;
        u = null;
    }
    @Override
    public void run() {
//        super.run();
        Connected = true;
        try {
            if (Host == null)
                doReceive();
            else
                doSend(Host);
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        Connected = false;
    }

    private void doReceive() throws Exception
    {
        int i;
        udp = new DatagramSocket(Port);
        udp.setBroadcast(true);
        rcv = false;
        while (true) {
            udp.receive(UDP);
            Client = UDP.getAddress();
            for (i=0;i<data.length;i++)
            {
                if (data[i] == 0)
                    break;
                buffer[tail++] = data[i];
                if (tail >= 256)
                    tail = 0;
                data[i] = 0;
            }
            rcv = true;
            if (u != null)
                doUpdate();
        }
    }

    private void doSend(String host) throws Exception
    {
        InetAddress adr = InetAddress.getByName(host);
        UDP.setAddress(adr);
        UDP.setPort(Port);
        udp = new DatagramSocket();
        while (true) {
            if (snd) {
                udp.send(UDP);
                snd = false;
            }
        }
    }

    public void Send(String s)
    {
        byte[] b = s.getBytes();
        UDP.setData(b);
        if (Client != null)
            UDP.setAddress(Client);
        snd = true;
    }

    public boolean Connected()
    {
        return Connected;
    }

    public void setPort(int port)
    {
        Port = port;
    }

    public int getPort() {return Port;}

    public boolean isRcv() { return rcv;}

    public boolean isSnd() { return snd;}

    public InetAddress getClient() {
        return Client;
    }

    public void setClient(InetAddress c) {
        Client = c;
    }

    public String Receive()
    {
        int i;
        String s = "";
        if (tail < head)
        {
            s = new String(buffer, head, 256-head);
            head = 0;
        }
        s = s + new String(buffer, head, tail-head);
        head = tail;
        rcv = false;
        return s;
    }

    public void doUpdate()
    {
        String s = Receive();
        u.update(s);
    }
}

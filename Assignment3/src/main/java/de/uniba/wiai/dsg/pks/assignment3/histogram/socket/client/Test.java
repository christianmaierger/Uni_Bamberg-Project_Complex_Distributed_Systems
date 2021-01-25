package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import java.io.Serializable;

class Test implements Serializable {
    public int i;

    public Test(int i, String hello) {
        this.i = i;
        this.hello = hello;
    }

    public String hello;


}

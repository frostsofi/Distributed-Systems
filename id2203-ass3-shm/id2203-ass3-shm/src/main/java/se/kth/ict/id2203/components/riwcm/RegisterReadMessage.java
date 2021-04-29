package se.kth.ict.id2203.components.riwcm;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.sics.kompics.address.Address;

public class RegisterReadMessage extends BebDeliver
{
    private int curSeq;

    public RegisterReadMessage(Address source, int curSeq) {
        super(source);
        this.curSeq = curSeq;
    }

    public int getCurSeq() {
        return curSeq;
    }
}

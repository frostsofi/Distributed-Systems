package se.kth.ict.id2203.components.riwcm;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.sics.kompics.address.Address;

public class RegisterWriteMessage extends BebDeliver {

    int curSeq = 0;
    Tuple curTuple;

    public RegisterWriteMessage(Address source, int curSeq, Tuple tuple) {
        super(source);
        this.curSeq = curSeq;
        this.curTuple = tuple;
    }

    public int getCurSeq() {
        return curSeq;
    }

    public Tuple getCurTuple() {
        return curTuple;
    }
}

package se.kth.ict.id2203.components.rb;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

public class RbDataMessage extends BebDeliver {

    private static final long serialVersionUID = 5491596109178800519L;

    private RbDeliver message;
    private Integer seqNumber;

    public RbDataMessage(Address source, RbDeliver mes, Integer seqNumber) {
        super(source);
        this.message = mes;
        this.seqNumber = seqNumber;
    }

    public RbDeliver getMessage() {
        return message;
    }

    public  Integer getSeqNumber()
    {
        return seqNumber;
    }
}

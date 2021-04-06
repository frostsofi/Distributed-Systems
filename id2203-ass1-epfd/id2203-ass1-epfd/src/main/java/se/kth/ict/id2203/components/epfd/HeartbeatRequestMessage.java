package se.kth.ict.id2203.components.epfd;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class HeartbeatRequestMessage extends Pp2pDeliver {

    private long seqNumber;

    public long getSeqNumber()
    {
        return seqNumber;
    }

    protected HeartbeatRequestMessage(Address source, long seqNumber)
    {
        super(source);
        this.seqNumber = seqNumber;
    }
}

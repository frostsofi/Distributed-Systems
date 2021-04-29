package se.kth.ict.id2203.components.riwcm;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class RegisterAckMessage extends Pp2pDeliver
{
    private int curSeq;
    protected RegisterAckMessage(Address source, int curSeq) {
        super(source);
        this.curSeq = curSeq;
    }

    public int getCurSeq() {
        return curSeq;
    }
}

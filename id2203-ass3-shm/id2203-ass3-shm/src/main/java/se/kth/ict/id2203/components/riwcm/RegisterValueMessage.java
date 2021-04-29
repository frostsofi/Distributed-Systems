package se.kth.ict.id2203.components.riwcm;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class RegisterValueMessage extends Pp2pDeliver
{
    private Tuple curTuple;
    private int curSeq;
    private int tCurSeq;
    private int tCurId;
    private int tCurVal;


    protected RegisterValueMessage(Address source, Tuple curTuple, int curSeq) {
        super(source);
        this.tCurSeq = curTuple.curSeq;
        this.tCurId = curTuple.curId;
        this.tCurVal = curTuple.curVal;
        this.curSeq = curSeq;
    }

    public int getCurSeq() {
        return curSeq;
    }

    public Tuple getCurTuple() {
        return new Tuple(tCurSeq, tCurId, tCurVal);
    }
}

package se.kth.ict.id2203.components.riwcm;

import java.io.Serializable;

class Tuple implements Comparable<Tuple>, Serializable
{
    public int curSeq = 0;
    public int curId = 0;
    public int curVal = 0;

    Tuple(int curSeq, int curId, int curVal) {
        this.curSeq = curSeq;
        this.curId = curId;
        this.curVal = curVal;
    }

    @Override
    public int compareTo(Tuple t) {
        if (t.curSeq > curSeq || (t.curSeq == curSeq && t.curId > curId))
            return -1;
        return 1;
    }

    @Override
    public Tuple clone() throws CloneNotSupportedException {
        return new Tuple(curSeq, curId, curVal);
    }
}

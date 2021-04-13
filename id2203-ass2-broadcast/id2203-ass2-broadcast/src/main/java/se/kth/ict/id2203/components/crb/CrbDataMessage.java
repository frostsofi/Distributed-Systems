package se.kth.ict.id2203.components.crb;

import se.kth.ict.id2203.pa.broadcast.CrbMessage;
import se.kth.ict.id2203.ports.crb.CrbDeliver;
import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.sics.kompics.address.Address;

import java.util.HashMap;
import java.util.Map;

public class CrbDataMessage extends RbDeliver implements Comparable<CrbDataMessage> {

    private static final long serialVersionUID = -1855724247802103843L;
    private HashMap<Address, Integer> clock;
    private CrbDeliver message;

    public CrbDataMessage(Address source, HashMap<Address, Integer> clock, CrbDeliver mes)
    {
        super(source);
        this.clock = clock;
        this.message = mes;
    }

    public HashMap<Address, Integer> getClock() {
        return clock;
    }

    @Override
    public int compareTo(CrbDataMessage oMes) {
        if (message != null) {
            if (oMes.getMessage().getSource() == message.getSource()
                    &&
                    ((CrbMessage) oMes.getMessage()).getMessage()
                            .equals(((CrbMessage) message).getMessage()))
                return 0;
        }

        int count = 0;

        for (Map.Entry<Address, Integer> pair : oMes.getClock().entrySet())
        {
            if (pair.getValue() <= clock.get(pair.getKey()))
                count++;
        }
        if (count == clock.size())
            return 1;
        else
            return -1;
    }

    public CrbDeliver getMessage() {
        return message;
    }
}

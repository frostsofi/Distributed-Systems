package se.kth.ict.id2203.components.beb;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class BebDataMessage  extends Pp2pDeliver {

    private static final long serialVersionUID = 2193713942080123560L;

    private final BebDeliver message;

    public BebDeliver getMessage() {
        return message;
    }

    public BebDataMessage(Address source, BebDeliver mes)
    {
        super(source);
        this.message = mes;
    }
}

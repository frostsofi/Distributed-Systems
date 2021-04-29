package se.kth.ict.id2203.components.rb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.components.beb.BebDataMessage;
import se.kth.ict.id2203.pa.broadcast.BebMessage;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.kth.ict.id2203.ports.rb.RbBroadcast;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

import java.util.*;

public class EagerRb extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(EagerRb.class);

	private Address selfAddress;
	private HashMap<Address, ArrayList<Integer>> allAddresses;
	private Integer seqNumber;

	private Negative<ReliableBroadcast> rb = provides(ReliableBroadcast.class);
	private Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

	public EagerRb(EagerRbInit init)
	{
		allAddresses = new HashMap<>();
		selfAddress = init.getSelfAddress();

		for (Address adr : init.getAllAddresses())
		{
			allAddresses.put(adr, new ArrayList<>());
		}
		seqNumber = 0;

		subscribe(handleRbBroadcast, rb);
		subscribe(handleRbDataMessage, beb);
	}

	private Handler<RbBroadcast> handleRbBroadcast = new Handler<RbBroadcast>()
	{
		@Override
		public void handle(RbBroadcast event)
		{
			seqNumber++;
			trigger(new BebBroadcast(new RbDataMessage(selfAddress, event.getDeliverEvent(), seqNumber)),
					beb);
		}
	};

	private Handler<RbDataMessage> handleRbDataMessage = new Handler<RbDataMessage>()
	{
		@Override
		public void handle(RbDataMessage event)
		{
			Address adr = event.getSource();
			Integer number = event.getSeqNumber();
			ArrayList<Integer> numbers = allAddresses.get(adr);

			if (!numbers.contains(number))
			{
				numbers.add(number);
				trigger(event.getMessage(), rb);
				trigger(new BebBroadcast(event), beb);
			}
		}
	};
}

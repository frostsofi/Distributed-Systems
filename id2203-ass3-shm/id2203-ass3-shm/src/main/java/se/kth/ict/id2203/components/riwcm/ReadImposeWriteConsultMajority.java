package se.kth.ict.id2203.components.riwcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ports.ar.*;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public class ReadImposeWriteConsultMajority extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(ReadImposeWriteConsultMajority.class);

	private Negative<AtomicRegister> ar = provides(AtomicRegister.class);
	private Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
	private Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

	private Tuple curTuple = new Tuple(0,0,0);
	private int acks = 0;
	private int seq = 0;

	private int writeVal = -1;
	private int readVal = -1;

	private TreeSet<Tuple> readSet = new TreeSet<>();
	boolean isReading = false;

	private Address selfAddress;
	private Set<Address> allAddresses;

	public ReadImposeWriteConsultMajority(ReadImposeWriteConsultMajorityInit event)
	{
		this.selfAddress = event.getSelfAddress();
		this.allAddresses = event.getAllAddresses();

		subscribe(handleArReadRequest, ar);
		subscribe(handleRegisterReadMessage, beb);
		subscribe(handleRegisterValueMessage, pp2p);
		subscribe(handleArWriteRequest, ar);
		subscribe(handleRegisterWriteMessage, beb);
		subscribe(handleRegisterAckMessage, pp2p);

	}

	private Handler<ArReadRequest> handleArReadRequest = new Handler<ArReadRequest>() {
		@Override
		public void handle(ArReadRequest event) {
			//logger.info("Reading is start");
			seq++;
			acks = 0;
			readSet.clear();
			isReading = true;
			trigger(new BebBroadcast(new RegisterReadMessage(selfAddress, seq)), beb);

		}
	};

	private Handler<RegisterReadMessage> handleRegisterReadMessage = new Handler<RegisterReadMessage>() {
		@Override
		public void handle(RegisterReadMessage event) {
			//logger.info("Send to reading curTuple ");
			RegisterValueMessage valueMessage = new RegisterValueMessage(selfAddress, curTuple, event.getCurSeq());
			Pp2pSend sendEvent = new Pp2pSend(event.getSource(), valueMessage);
			trigger(sendEvent, pp2p);
		}
	};

	private Handler<RegisterValueMessage> handleRegisterValueMessage = new Handler<RegisterValueMessage>() {
		@Override
		public void handle(RegisterValueMessage event) {
			//logger.info("Get value message "+event.getCurSeq());
			if (event.getCurSeq() == seq) {
				//logger.info("Added tuple to readset");
				readSet.add(event.getCurTuple());

				if (readSet.size() > allAddresses.size()/2) {
					//logger.info("Get tuple with the biggest seq");

					Tuple maxTuple = readSet.last();
					readVal = maxTuple.curVal;
					readSet.clear();

					if (isReading) {
						//logger.info("Start write impose to complete read");
						trigger(new BebBroadcast(new RegisterWriteMessage(selfAddress, seq, maxTuple)), beb);
					} else {
						//logger.info("Get highest seq and write message");
						Tuple writeTuple = new Tuple(maxTuple.curSeq+1, selfAddress.getId(), writeVal);
						trigger(new BebBroadcast(new RegisterWriteMessage(selfAddress, seq, writeTuple)), beb);
					}
				}
			}
		}
	};

	private Handler<ArWriteRequest> handleArWriteRequest = new Handler<ArWriteRequest>() {
		@Override
		public void handle(ArWriteRequest event) {
			//logger.info("Writing is start");
			seq++;
			acks = 0;
			readSet.clear();
			writeVal = event.getValue();

			trigger(new BebBroadcast(new RegisterReadMessage(selfAddress, seq)), beb);
		}
	};

	private Handler<RegisterWriteMessage> handleRegisterWriteMessage = new Handler<RegisterWriteMessage>() {
		@Override
		public void handle(RegisterWriteMessage event) {
			//logger.info("Get request to write value and send ack");
			if (event.getCurTuple().compareTo(curTuple) > 0) {
				try {
					curTuple = event.getCurTuple().clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}

			RegisterAckMessage ackMessage = new RegisterAckMessage(selfAddress, event.getCurSeq());
			Pp2pSend sendEvent = new Pp2pSend(event.getSource(), ackMessage);
			trigger(sendEvent, pp2p);
		}
	};

	private Handler<RegisterAckMessage> handleRegisterAckMessage = new Handler<RegisterAckMessage>() {
		@Override
		public void handle(RegisterAckMessage event) {
			if (event.getCurSeq() == seq) {
				//logger.info("Get ack");
				acks++;
				if (acks > allAddresses.size() / 2) {
					acks = 0;
					if (isReading) {
						//logger.info("Get acks and read complete");
						isReading = false;
						trigger(new ArReadResponse(readVal), ar);
					} else {
						//logger.info("Get acks and write complete");
						trigger(new ArWriteResponse(), ar);
					}
				}
			}
		}
	};
}

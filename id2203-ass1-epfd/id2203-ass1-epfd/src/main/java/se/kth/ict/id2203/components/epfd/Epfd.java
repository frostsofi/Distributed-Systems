package se.kth.ict.id2203.components.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ports.epfd.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.ports.epfd.Restore;
import se.kth.ict.id2203.ports.epfd.Suspect;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.*;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;
import java.util.Set;

public class Epfd extends ComponentDefinition
{

	private static final Logger logger = LoggerFactory.getLogger(Epfd.class);

	private Positive<Timer> timer = requires(Timer.class);
	private Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
	private Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);

    private int seqNumber;
	private Set<Address> suspected;
	private  Set<Address> alive;
	private Set<Address> allAddresses;
	private long initialDelay;
	private long deltaDelay;
	private Address selfAddress;

	public Epfd(EpfdInit init)
    {
        subscribe(handleStart, control);
		subscribe(handleCheckTimeOut, timer);
		subscribe(handleHBRequestMessage, pp2p);
		subscribe(handleHBReplyMessage, pp2p);

		seqNumber = 0;
		allAddresses = new HashSet<>(init.getAllAddresses());
		alive = new HashSet<>(init.getAllAddresses());
        suspected = new HashSet<Address>();
        initialDelay = init.getInitialDelay();
        deltaDelay = init.getDeltaDelay();
        selfAddress = init.getSelfAddress();
	}

    private Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            ScheduleTimeout st = new ScheduleTimeout(initialDelay);
            st.setTimeoutEvent(new CheckTime(st));
            trigger(st, timer);
        }
    };

	private Handler<CheckTime> handleCheckTimeOut = new Handler<CheckTime>()
    {
		@Override
		public void handle(CheckTime event)
        {
            HashSet<Address> intersection = new HashSet<>(suspected);
            intersection.retainAll(alive);
            if (intersection.size() != 0)
            {
                initialDelay += deltaDelay;
                logger.info("Intersection doesn't empty, delta increase, DELAY: "
                        .concat(String.valueOf(initialDelay)));
            }

            seqNumber += 1;

            for (Address adr : allAddresses)
            {
                if (adr.equals(selfAddress))
                    continue;

                if (!alive.contains(adr) && !suspected.contains(adr))
                {
                    suspected.add(adr);
                    trigger(new Suspect(adr), epfd);
                }
                else if (alive.contains(adr) && suspected.contains(adr))
                {
                    suspected.remove(adr);
                    trigger(new Restore(adr), epfd);
                }

                Pp2pSend sendEvent = new Pp2pSend(adr, new HeartbeatRequestMessage(selfAddress, seqNumber));
                trigger(sendEvent, pp2p);
            }
            alive.clear();

            ScheduleTimeout st = new ScheduleTimeout(initialDelay);
            st.setTimeoutEvent(new CheckTime(st));
            trigger(st, timer);
		}
	};

	private Handler<HeartbeatRequestMessage> handleHBRequestMessage = new Handler<HeartbeatRequestMessage>()
    {
		@Override
		public void handle(HeartbeatRequestMessage event)
        {
            logger.info("Get Request ".concat(event.getSource().toString()));
            Pp2pSend sendEvent = new Pp2pSend(event.getSource(), new HeartbeatReplyMessage(selfAddress, event.getSeqNumber()));
            trigger(sendEvent, pp2p);
		}
	};

	private Handler<HeartbeatReplyMessage> handleHBReplyMessage = new Handler<HeartbeatReplyMessage>()
    {
		@Override
		public void handle(HeartbeatReplyMessage event)
        {
            logger.info("Get reply".concat(event.getSource().toString()));
            logger.info(String.valueOf(event.getSeqNumber()));
            logger.info(String.valueOf(seqNumber));

            if (event.getSeqNumber() == seqNumber || suspected.contains(event.getSource()))
            {
                alive.add(event.getSource());
            }
		}
	};
}
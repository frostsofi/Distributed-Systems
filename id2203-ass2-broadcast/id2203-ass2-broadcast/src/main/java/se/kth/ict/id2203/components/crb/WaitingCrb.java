/**
 * This file is part of the ID2203 course assignments kit.
 * 
 * Copyright (C) 2009-2013 KTH Royal Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ict.id2203.components.crb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.components.beb.BebDataMessage;
import se.kth.ict.id2203.components.rb.RbDataMessage;
import se.kth.ict.id2203.pa.broadcast.CrbMessage;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.crb.CausalOrderReliableBroadcast;
import se.kth.ict.id2203.ports.crb.CrbBroadcast;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.kth.ict.id2203.ports.rb.RbBroadcast;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

import java.util.*;

public class WaitingCrb extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(WaitingCrb.class);

	private Address selfAddress;

	//clockVector
	private HashMap<Address, Integer> allAddresses;
	private int curClock;
	private Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
	private Negative<CausalOrderReliableBroadcast> crb = provides(CausalOrderReliableBroadcast.class);
	private TreeSet<CrbDataMessage> messages;

	public WaitingCrb(WaitingCrbInit init)
	{
		this.selfAddress = init.getSelfAddress();
		this.allAddresses = new HashMap<>();

		for (Address adr : init.getAllAddresses())
		{
			allAddresses.put(adr, 0);
		}

		curClock = 0;
		messages = new TreeSet<>();

		subscribe(handleCrbBroadcast, crb);
		subscribe(handleCrbDataMessage, rb);
	}

	private Handler<CrbBroadcast> handleCrbBroadcast = new Handler<CrbBroadcast>()
	{
		@Override
		public void handle(CrbBroadcast event)
		{
			HashMap<Address, Integer> copy = new HashMap<>(allAddresses);
			copy.replace(selfAddress, curClock);
			curClock++;
			trigger(new RbBroadcast(new CrbDataMessage(selfAddress, copy, event.getDeliverEvent())), rb);
		}
	};

	private Handler<CrbDataMessage> handleCrbDataMessage = new Handler<CrbDataMessage>()
	{
		@Override
		public void handle(CrbDataMessage event)
		{
			messages.add(event);

			CrbDataMessage curEvent = new CrbDataMessage(selfAddress, allAddresses, null);

			TreeSet<CrbDataMessage> copy = new TreeSet<CrbDataMessage>(messages);
            for (CrbDataMessage mes : copy)
			{
				if (curEvent.compareTo(mes) > 0)
				{
                    int curClock = allAddresses.get(mes.getSource());
                    curClock++;
                    allAddresses.replace(mes.getSource(), curClock);
                    trigger(mes.getMessage(), crb);
					messages.remove(mes);
				}
			}
		}
	};
}

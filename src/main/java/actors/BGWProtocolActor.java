package actors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import math.IntegersUtils;
import messages.Messages;
import messages.Messages.BGWNPoint;
import messages.Messages.Participants;
import protocol.BGWParameters.BGWPrivateParameters;
import protocol.BGWParameters.BGWPublicParameters;
import protocol.ProtocolParameters;
import actordata.BGWData;
import actors.BGWProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;


/**
 * Encodes the BGW protocol used as a subprotocol to allow n parties to compute N=pq
 * without revealing p and q to any party. Based on the paper <i>Completeness theorem for 
 * non-cryptographic fault-tolerant distributed computation </i> by Ben-Or M.,
 * Goldwasser S., Wigderson A.
 * 
 * @author Christian Mouchet
 */
public class BGWProtocolActor extends AbstractLoggingFSM<States, BGWData>{
	
	public enum States {INITILIZATION,
						BGW_COLLECTING_PjQj,
						BGW_COLLECTING_Nj};
	
	private final SecureRandom sr = new SecureRandom();
	private final ProtocolParameters protocolParameters;
	private final ActorRef master;
	
	/** Standalone actor constructor, when this actor is has no master actor
	 * @param protocolParam the pre-agreed public parameters of the protocol
	 */
	public BGWProtocolActor(ProtocolParameters protocolParam) {
		this(protocolParam, null);
	}
	
	
	/** Subordinate constructor, when this actor is executed as a part of a bigger FSM. When
	 *  a master is given, all messages sent by this actor have <code>sender=master</code>.
	 * @param protocolParam the pre-agreed public parameters of the protocol
	 * @param master the ActorRef of the master that executes this actor as a sub-protocol
	 */
	public BGWProtocolActor(ProtocolParameters protocolParam, ActorRef master) {
		this.protocolParameters = protocolParam;
		this.master = master != null ? master : self();
		
		startWith(States.INITILIZATION, BGWData.init());
		
		when(States.INITILIZATION, matchEvent(Participants.class,
				(participants,data) -> {
					
					// Generates new p, q and all necessary sharings
					Map<ActorRef,Integer> actors = participants.getParticipants();
					BGWPrivateParameters bgwPrivateParameters = BGWPrivateParameters.genFor(actors.get(this.master),
																							protocolParameters,
																							sr);
					BGWPublicParameters bgwSelfShare = BGWPublicParameters.genFor(actors.get(this.master),
																					bgwPrivateParameters);
					
					BGWData nextStateData = data.withPrivateParameters(bgwPrivateParameters)
										.withNewShare(bgwSelfShare, actors.get(this.master))
										.withParticipants(actors);
					
					return goTo(States.BGW_COLLECTING_PjQj).using(nextStateData);
				}
				));
		
		onTransition(matchState(States.INITILIZATION,States.BGW_COLLECTING_PjQj, () -> {
			
			// Send each party j its share of pi pij.
			Map<ActorRef, Integer> actors = nextStateData().getParticipants();
			actors.entrySet().stream()
			.filter(e -> !e.getKey().equals(this.master))
			.forEach(e -> e.getKey().tell(BGWPublicParameters.genFor(e.getValue(), nextStateData().bgwPrivateParameters), this.master));
		}));
		
		
		when(States.BGW_COLLECTING_PjQj, matchEvent(BGWPublicParameters.class, 
				(newShare, data) -> {
					
					// Collect the pji and qji shares and compute its own Ni share
					Map<ActorRef,Integer> actors = data.getParticipants();
					BGWData dataWithNewShare = data.withNewShare(newShare, actors.get(sender()));
					if(!dataWithNewShare.hasShareOf(actors.values()))
						return stay().using(dataWithNewShare);
					else {
						
						Stream<Integer> badActors = dataWithNewShare.shares()
								.filter(e -> !e.getValue().isCorrect(protocolParameters))
								.map(e -> (Integer) e.getKey()); // Check the shares (not implemented yet)
						
						if (badActors.count() > 0) { 
							badActors.forEach(id -> broadCast(new Messages.Complaint(id),actors.keySet()));
							return stop().withStopReason(new Failure("A BGW share was invalid."));
						} else {
							BigInteger sumPj = dataWithNewShare.shares().map(e -> e.getValue().pij).reduce(BigInteger.ZERO, (p1,p2) -> p1.add(p2));
							BigInteger sumQj = dataWithNewShare.shares().map(e -> e.getValue().qij).reduce(BigInteger.ZERO, (q1,q2) -> q1.add(q2));
							BigInteger sumHj = dataWithNewShare.shares().map(e -> e.getValue().hij).reduce(BigInteger.ZERO, (h1,h2) -> h1.add(h2));
							BigInteger Ni = (sumPj.multiply(sumQj)).add(sumHj).mod(protocolParameters.P);
							
							return goTo(States.BGW_COLLECTING_Nj).using(dataWithNewShare.withNewNi(Ni, actors.get(this.master)));
						}
					}
				}));
		
		onTransition(matchState(States.BGW_COLLECTING_PjQj,States.BGW_COLLECTING_Nj, () -> {
			
			// Publish its share of N
			Map<ActorRef, Integer> actors = nextStateData().getParticipants();
			broadCast(new BGWNPoint(nextStateData().Ns.get(actors.get(this.master))), actors.keySet());
		}));
		
		
		when(States.BGW_COLLECTING_Nj, matchEvent(BGWNPoint.class,
				(newNi,data) -> {
					
					// Collect the Nj shares and compute N using Lagrangian interpolation
					Map<ActorRef,Integer> actors = data.getParticipants();
					BGWData dataWithNewNi = data.withNewNi(newNi.point, actors.get(sender()));
					if (!dataWithNewNi.hasNiOf(actors.values())){
						return stay().using(dataWithNewNi);
					}
					else {
						List<BigInteger> Nis = dataWithNewNi.nis()
								.map(e -> e.getValue())
								.collect(Collectors.toList());
						BigInteger N = IntegersUtils.getIntercept(Nis, protocolParameters.P);
						
						if(this.master != self())
							this.master.tell(new Messages.CandidateN(N, data.bgwPrivateParameters),  self());
					}
					
					return goTo(States.INITILIZATION).using(BGWData.init().withParticipants(data.getParticipants()));
				}));
		
		// A message that cannot be handled goes back to the message queue
		whenUnhandled(matchAnyEvent((evt,data) -> {
			self().tell(evt, sender());
			return stay();
		}));
	}


	private void broadCast(Object o, Set<ActorRef> targets) {
		targets.stream().forEach(actor -> {if (!actor.equals(this.master)) actor.tell(o, this.master);});
	}
	

	
}

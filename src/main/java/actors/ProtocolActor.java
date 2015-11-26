package actors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.stream.Stream;

import messages.Messages;
import protocol.BGWParameters;
import protocol.BGWParameters.BGWPrivateParameters;
import protocol.BGWParameters.BGWPublicParameters;
import protocol.ProtocolParameters;
import actors.ProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

public class ProtocolActor extends AbstractLoggingFSM<States, Data>{
	
	public enum States {PARAM_AGREEMENT,BGW_AWAITING_PjQj, BGW_AWAITING_Ni};
	
	private final SecureRandom sr = new SecureRandom();
	public static Map<ActorRef, Integer> indexMap;
	
	{
		startWith(States.PARAM_AGREEMENT, Data.init());
		
		when(States.PARAM_AGREEMENT, matchEvent(ProtocolParameters.class, 
				(protocolParameters, data) -> {
					BGWPrivateParameters bgwPrivateParameters = BGWPrivateParameters.genFor(indexMap.get(self()),
																							protocolParameters,
																							sr);
					BGWPublicParameters bgwSelfShare = BGWPublicParameters.genFor(indexMap.get(self()),
																					bgwPrivateParameters,
																					sr);
					Data localData = data.with(protocolParameters)
											.with(bgwPrivateParameters)
											.withNewShare(bgwSelfShare, indexMap.get(self()));
					
					indexMap.entrySet().stream().filter(e -> !e.getKey().equals(self()))
												.forEach(e -> e.getKey().tell(BGWPublicParameters.genFor(e.getValue(), bgwPrivateParameters, sr), self()));
					return goTo(States.BGW_AWAITING_PjQj).using(localData);
				}));
		
		when(States.BGW_AWAITING_PjQj, matchEvent(BGWPublicParameters.class, 
				(newShare, data) -> {
					Data dataWithNewShare = data.withNewShare(newShare, indexMap.get(sender()));
					if(!dataWithNewShare.hasShareOf(indexMap.values()))
						return stay().using(dataWithNewShare);
					else {
						Stream<Integer> badActors = dataWithNewShare.shares()
								.filter(e -> !e.getValue().isCorrect(dataWithNewShare.protocolParameters, indexMap.get(self()), e.getKey()))
								.map(e -> (Integer) e.getKey());
						if (badActors.count() > 0) {
							badActors.forEach(id -> broadCast(new Messages.Complaint(id)));
							return stop().withStopReason(new Failure("A BGW share was invalid."));
						} else {
							BigInteger sumPj = dataWithNewShare.shares().map(e -> e.getValue().pj).reduce(BigInteger.ZERO, (p1,p2) -> p1.add(p2));
							BigInteger sumQj = dataWithNewShare.shares().map(e -> e.getValue().qj).reduce(BigInteger.ZERO, (q1,q2) -> q1.add(q2));
							BigInteger sumHj = dataWithNewShare.shares().map(e -> e.getValue().hj).reduce(BigInteger.ZERO, (h1,h2) -> h1.add(h2));
							BigInteger Ni = (sumPj.multiply(sumQj)).add(sumHj).mod(dataWithNewShare.protocolParameters.Pp);
							broadCast(Ni);
							return goTo(States.BGW_AWAITING_Ni).using(dataWithNewShare.withNewNi(Ni, indexMap.get(self())));
						}
					}
				}));
		
		
		when(States.BGW_AWAITING_Ni, matchEvent(BigInteger.class,
				(newNi,data) -> {
					Data dataWithNewNi = data.withNewNi(newNi, indexMap.get(sender()));
					if (!dataWithNewNi.hasNiOf(indexMap.values())){
						return stay().using(dataWithNewNi);
					}
					else {
						System.out.println(self().path().name()+" computes N.");
						return stop();
					}
				}));
		
		onTransition((from,to) -> {
			System.out.println(String.format("%s transition %s -> %s", self().path().name(), from, to ));
		});
	}
	
	private void broadCast(Object o) {
		indexMap.keySet().stream().forEach(actor -> {if (!actor.equals(self())) actor.tell(o, self());});
	}
	
}

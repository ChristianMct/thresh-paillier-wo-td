package actors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions;

import math.LagrangianInterpolation;
import messages.Messages;
import messages.Messages.BGWNPoint;
import messages.Messages.BGWResult;
import messages.Messages.Participants;
import protocol.BGWParameters.BGWPrivateParameters;
import protocol.BGWParameters.BGWPublicParameters;
import protocol.ProtocolParameters;
import scala.concurrent.CanAwait;
import actordata.BGWData;
import actors.BGWProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

public class BGWProtocolActor extends AbstractLoggingFSM<States, BGWData>{
	
	public enum States {INITILIZATION,
						BGW_AWAITING_PjQj, BGW_AWAITING_Ni,
						BGW_BIPRIMAL_TEST};
	
	private final SecureRandom sr = new SecureRandom();
	private final ProtocolParameters protocolParameters;
	private final ActorRef master;
	private int iter = 0;
	
	public BGWProtocolActor(ProtocolParameters protocolParam) {
		this(protocolParam, null);
	}
	
	public BGWProtocolActor(ProtocolParameters protocolParam, ActorRef master) {
		this.protocolParameters = protocolParam;
		this.master = master != null ? master : self();
		
		startWith(States.INITILIZATION, BGWData.init());
		
		when(States.INITILIZATION, matchEvent(Participants.class,
				(participants,data) -> {
					iter++;
					Map<ActorRef,Integer> actors = participants.getParticipants();
					BGWPrivateParameters bgwPrivateParameters = BGWPrivateParameters.genFor(actors.get(this.master),
																							protocolParameters,
																							sr);
					BGWPublicParameters bgwSelfShare = BGWPublicParameters.genFor(actors.get(this.master),
																					bgwPrivateParameters,
																					sr);
					
					BGWData nextStateData = data.withPrivateParameters(bgwPrivateParameters)
										.withNewShare(bgwSelfShare, actors.get(this.master))
										.withParticipants(actors);

					actors.entrySet().stream()
						.filter(e -> !e.getKey().equals(this.master))
						.forEach(e -> e.getKey().tell(BGWPublicParameters.genFor(e.getValue(), bgwPrivateParameters, sr), this.master));
					
					return goTo(States.BGW_AWAITING_PjQj).using(nextStateData);
				}
				));
		
		
		when(States.BGW_AWAITING_PjQj, matchEvent(BGWPublicParameters.class, 
				(newShare, data) -> {
					Map<ActorRef,Integer> actors = data.getParticipants();
					BGWData dataWithNewShare = data.withNewShare(newShare, actors.get(sender()));
					if(!dataWithNewShare.hasShareOf(actors.values()))
						return stay().using(dataWithNewShare);
					else {
						Stream<Integer> badActors = dataWithNewShare.shares()
								.filter(e -> !e.getValue().isCorrect(protocolParameters, actors.get(this.master), e.getKey()))
								.map(e -> (Integer) e.getKey());
						if (badActors.count() > 0) {
							badActors.forEach(id -> broadCast(new Messages.Complaint(id),actors.keySet()));
							return stop().withStopReason(new Failure("A BGW share was invalid."));
						} else {
							BigInteger sumPj = dataWithNewShare.shares().map(e -> e.getValue().pj).reduce(BigInteger.ZERO, (p1,p2) -> p1.add(p2));
							BigInteger sumQj = dataWithNewShare.shares().map(e -> e.getValue().qj).reduce(BigInteger.ZERO, (q1,q2) -> q1.add(q2));
							BigInteger sumHj = dataWithNewShare.shares().map(e -> e.getValue().hj).reduce(BigInteger.ZERO, (h1,h2) -> h1.add(h2));
							BigInteger Ni = (sumPj.multiply(sumQj)).add(sumHj).mod(protocolParameters.Pp);
							
							broadCast(new BGWNPoint(Ni), actors.keySet());
							return goTo(States.BGW_AWAITING_Ni).using(dataWithNewShare.withNewNi(Ni, actors.get(this.master)));
						}
					}
				}));
		
		
		when(States.BGW_AWAITING_Ni, matchEvent(BGWNPoint.class,
				(newNi,data) -> {
					Map<ActorRef,Integer> actors = data.getParticipants();
					BGWData dataWithNewNi = data.withNewNi(newNi.point, actors.get(sender()));
					if (!dataWithNewNi.hasNiOf(actors.values())){
						return stay().using(dataWithNewNi);
					}
					else {
						List<BigInteger> Nis = dataWithNewNi.nis()
								.map(e -> e.getValue())
								.collect(Collectors.toList());
						BigInteger N = LagrangianInterpolation.getIntercept(Nis, protocolParameters.Pp);
						
						BigInteger gp = getGp(N);
						BigInteger Qi = getQi(gp,N, data.bgwPrivateParameters.p, data.bgwPrivateParameters.q, actors.get(this.master));
						
						broadCast(Qi, actors.keySet());
						return goTo(States.BGW_BIPRIMAL_TEST).using(data.withCandidateN(N)
																			.withNewQi(Qi, actors.get(this.master)));
					}
				}));
		
		when(States.BGW_BIPRIMAL_TEST, matchEvent(BigInteger.class,
				(newQi, data) -> {
					Map<ActorRef,Integer> actors = data.getParticipants();
					BGWData newData = data.withNewQi(newQi, actors.get(sender()));
					if(!newData.hasQiOf(actors.values())) {
						return stay().using(newData);
					} else {
						
						BigInteger check1 = newData.qis().map(qi -> {if (qi.getKey() == 1) return qi.getValue(); 
																	else return qi.getValue().modInverse(newData.candidateN);})
									.reduce(BigInteger.ONE, (qi,qj) -> qi.multiply(qj)).mod(newData.candidateN);
						
						BigInteger q1 = newData.qiss().get(1);
						BigInteger prod = BigInteger.ONE;
						for(Entry<Integer, BigInteger> e : newData.qiss().entrySet()) {
							if(e.getKey() != 1)
								prod = prod.multiply(e.getValue());
						}
						
						BigInteger check = q1.divide(prod.mod(newData.candidateN)).mod(newData.candidateN);
						
						BigInteger minusOne = BigInteger.ONE.negate().mod(newData.candidateN);
						
						
						if (check.equals(minusOne) || check.equals(BigInteger.ONE)) {
							System.out.println("OK");
							if(!this.master.equals(self()))
								this.master.tell(new BGWResult(newData.candidateN), self());
							else 
								System.out.println("Found N = "+newData.candidateN);
							return stop();
						} else {
							System.out.println(self().path().toStringWithoutAddress()+"-"+check);
							iter++;
							BGWPrivateParameters bgwPrivateParameters = BGWPrivateParameters.genFor(actors.get(this.master),
																									protocolParameters,
																									sr);
							BGWPublicParameters bgwSelfShare = BGWPublicParameters.genFor(actors.get(this.master),
																							bgwPrivateParameters,
																							sr);
							
							BGWData nextStateData = BGWData.init().withPrivateParameters(bgwPrivateParameters)
												.withNewShare(bgwSelfShare, actors.get(this.master))
												.withParticipants(actors);

							actors.entrySet().stream()
								.filter(e -> !e.getKey().equals(this.master))
								.forEach(e -> e.getKey().tell(BGWPublicParameters.genFor(e.getValue(), bgwPrivateParameters, sr), this.master));
							
							
							return goTo(States.BGW_AWAITING_PjQj).using(nextStateData);
						}
						
					}
				}));
		
		whenUnhandled(matchAnyEvent((evt,data) -> {
			
			log().warning(String.format(" got Unhandled event %s on state %s%d", evt, this.stateName(),iter));
			//self().tell(evt, sender());
			
			return stay();
		}));
		
		onTransition((from,to) -> {
			System.out.println(String.format("%s transition %s -> %s %d", ActorUtils.nameOf(self()), from, to , iter));
			if(from.equals(States.BGW_BIPRIMAL_TEST) && to.equals(States.BGW_AWAITING_PjQj))
				Thread.sleep(10);
			//else 
			//	Thread.sleep(new Random().nextInt(1000));
		});
	}


	private void broadCast(Object o, Set<ActorRef> targets) {
		targets.stream().forEach(actor -> {if (!actor.equals(this.master)) actor.tell(o, this.master);});
	}
	
	private BigInteger getGp(BigInteger N) {
		BigInteger candidateGp = BigInteger.TEN;
		
		while(IntegerFunctions.jacobi(candidateGp, N) != 1)
			candidateGp = candidateGp.add(BigInteger.ONE);
		return candidateGp;
	}
	
	private BigInteger getQi(BigInteger gp, BigInteger N, BigInteger pi, BigInteger qi, int i) {
		BigInteger four = BigInteger.valueOf(4);
		BigInteger exp;
		if (i == 1) {
			exp = N.add(BigInteger.ONE).subtract(pi).subtract(qi).divide(four);
		} else {
			exp = pi.add(qi).divide(four);
		}
		return gp.modPow(exp, N);
	}
	
}

package actors;

import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import messages.Messages.CandidateN;
import messages.Messages.Participants;
import messages.Messages.QiTestForRound;

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions;

import actordata.BiprimalityTestData;
import actors.BiprimalityTestActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

public class BiprimalityTestActor extends AbstractLoggingFSM<States, BiprimalityTestData> {
	
	public enum States {INITIALIZATION, AWAITING_N, COLLECT_Qis}
	
	private final ActorRef master;
	
	public BiprimalityTestActor(ActorRef master) {
		
		this.master = master;
		
		startWith(States.INITIALIZATION, BiprimalityTestData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class, 
				(participants, data) -> {				
					return goTo(States.AWAITING_N).using(data.withParticipants(participants.getParticipants()));
		}));
		
		when(States.AWAITING_N, matchEvent(CandidateN.class, (candidateN, data) -> {
			
			Map<ActorRef, Integer> actors = data.getParticipants();
			BigInteger gp = getGp(candidateN.N);
			BigInteger Qi = getQi(gp,candidateN.N, candidateN.bgwPrivateParameters.p, candidateN.bgwPrivateParameters.q, actors.get(this.master));
			
			broadCast(new QiTestForRound(Qi, data.round), actors.keySet());
			
			return goTo(States.COLLECT_Qis).using(data.withNewCandidateN(candidateN.N)
														.withNewQi(Qi, actors.get(this.master), data.round));
		}));
		
		when(States.COLLECT_Qis, matchEvent(QiTestForRound.class,
				(newQi, data) -> {
					Map<ActorRef,Integer> actors = data.getParticipants();
					BiprimalityTestData newData = data.withNewQi(newQi.Qi, actors.get(sender()), newQi.round);
					if(!newData.hasQiOf(actors.values(), data.round)) {
						return stay().using(newData);
					} else {
						
						BigInteger check1 = newData.qis(data.round).map(qi -> {if (qi.getKey() == 1) return qi.getValue(); 
																	else return qi.getValue().modInverse(newData.N);})
									.reduce(BigInteger.ONE, (qi,qj) -> qi.multiply(qj)).mod(newData.N);
						
						BigInteger q1 = newData.qiss(data.round).get(1);
						BigInteger prod = BigInteger.ONE;
						for(Entry<Integer, BigInteger> e : newData.qiss(data.round).entrySet()) {
							if(e.getKey() != 1)
								prod = prod.multiply(e.getValue());
						}
						
						BigInteger check = q1.divide(prod.mod(newData.N)).mod(newData.N);
						
						BigInteger minusOne = BigInteger.ONE.negate().mod(newData.N);
						
						
						if (check.equals(minusOne) || check.equals(BigInteger.ONE)) {
							System.out.println("OK");
							if(!this.master.equals(self()))
								System.out.println("PKoKOK");
								//this.master.tell(new BGWResult(newData.N), self());
							else 
								System.out.println("Found N = "+newData.N);
							return stop();
						} else {
							System.out.println(self().path().toStringWithoutAddress()+"-"+check);
							
							return goTo(States.COLLECT_Qis).using(data.forNextRound());
						}
						
					}
				}));
		
	
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
	
	private void broadCast(Object o, Set<ActorRef> targets) {
		targets.stream().forEach(actor -> {if (!actor.equals(this.master)) actor.tell(o, this.master);});
	}
}

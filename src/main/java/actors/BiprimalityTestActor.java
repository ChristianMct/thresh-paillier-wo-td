package actors;

import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import messages.Messages;
import messages.Messages.BiprimalityTestResult;
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

	private static final int NUMBER_OF_ROUNDS = 10;
	
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
			BigInteger gp = getGp(candidateN.N, 0);
			BigInteger Qi = getQi(gp,candidateN.N, candidateN.bgwPrivateParameters.p, candidateN.bgwPrivateParameters.q, actors.get(this.master));
			
			//System.out.println("TEST "+data.round+" gp="+gp+" Qi="+Qi);
			
			return goTo(States.COLLECT_Qis).using(data.withNewCandidateN(candidateN.N, candidateN.bgwPrivateParameters)
														.withNewQi(Qi, actors.get(this.master), data.round));
		}));
		
		when(States.COLLECT_Qis, matchEvent(QiTestForRound.class,
				(newQi, data) -> {
					Map<ActorRef,Integer> actors = data.getParticipants();
					BiprimalityTestData newData = data.withNewQi(newQi.Qi, actors.get(sender()), newQi.round);
					if(!newData.hasQiOf(actors.values(), data.round)) {
						return stay().using(newData);
					} else {
						
						BigInteger check = newData.qis(data.round).map(qi -> {if (qi.getKey() == 1) return qi.getValue(); 
																	else return qi.getValue().modInverse(newData.N);})
									.reduce(BigInteger.ONE, (qi,qj) -> qi.multiply(qj)).mod(newData.N);
						
						//BigInteger q1 = newData.qiss(data.round).get(1);
						//BigInteger prod = BigInteger.ONE;
						//for(Entry<Integer, BigInteger> e : newData.qiss(data.round).entrySet()) {
						//	if(e.getKey() != 1)
						//		prod = prod.multiply(e.getValue());
						//}
						
						//BigInteger check1 = q1.divide(prod.mod(newData.N)).mod(newData.N);
						
						BigInteger minusOne = BigInteger.ONE.negate().mod(newData.N);
						
						
						if (check.equals(minusOne) || check.equals(BigInteger.ONE)) {
							
							if(actors.get(this.master)==1)
								System.out.println("PASSED TEST "+data.round);
							
							if (data.round == NUMBER_OF_ROUNDS) {
								master.tell(new BiprimalityTestResult(data.N, data.bgwPrivateParameters, true), self());
								return stop();
							}
							
							BiprimalityTestData nextData = data.forNextRound();
							
							BigInteger nextgp = getGp(nextData.N, nextData.round);
							BigInteger nextQi = getQi(nextgp, nextData.N, nextData.bgwPrivateParameters.p, nextData.bgwPrivateParameters.q, actors.get(this.master));
							//System.out.println("TEST "+nextData.round+" gp="+nextgp+" Qi="+nextQi);
							
							return goTo(States.COLLECT_Qis).using(nextData.withNewQi(nextQi, actors.get(this.master), nextData.round));
						} else {
							
							master.tell(new BiprimalityTestResult(data.N, data.bgwPrivateParameters, false), self());

							return goTo(States.AWAITING_N).using(data.forNextCandidate());
						}
						
					}
				}));
		
		whenUnhandled(matchAnyEvent((evt,data) -> {
			
			//log().warning(String.format(" got Unhandled event %s on state %s", evt, this.stateName()));
			//master.tell(evt, sender());
			self().tell(evt, sender());
			//Thread.sleep(1000);
			return stay();
		}));
		
		onTransition((from,to) -> {
			//System.out.println(String.format("%s transition %s -> %s", ActorUtils.nameOf(self()), from, to));
			//if(from.equals(States.BGW_BIPRIMAL_TEST) && to.equals(States.BGW_AWAITING_PjQj))
				//Thread.sleep(1000);
			//else 
			//	Thread.sleep(new Random().nextInt(1000));
			
			if (to == States.COLLECT_Qis) {
				broadCast(new QiTestForRound(nextStateData().qiss(nextStateData().round).get(nextStateData().getParticipants().get(this.master)), nextStateData().round), nextStateData().getParticipants().keySet());
			}
		});
		
	
	}
	
	private BigInteger getGp(BigInteger N,int round) {
		
		int hash = N.hashCode()*(round+1);
		
		BigInteger candidateGp = BigInteger.valueOf(hash).abs();
		
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

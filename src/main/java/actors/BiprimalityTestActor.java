package actors;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

import messages.Messages.BiprimalityTestResult;
import messages.Messages.CandidateN;
import messages.Messages.Participants;
import messages.Messages.QiTestForRound;

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions;

import actordata.BiprimalityTestData;
import actors.BiprimalityTestActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

/**
 * Encodes a distributed biprimality test for N based on <i>Efficient generation of shared RSA keys</i>
 * by Boneh D., Franklin M. 
 * <p> It allows the parties to check whether some integer N is the product of two
 * primes without leaking the knowledge of p or q to any of the party.
 * @author Christian Mouchet
 */
public class BiprimalityTestActor extends AbstractLoggingFSM<States, BiprimalityTestData> {
	
	public enum States {INITIALIZATION, AWAITING_N, COLLECT_Qjs}

	
	/**
	 * The number of tests that a candidate N needs to pass in order to be considered as prime.
	 */
	public static final int NUMBER_OF_ROUNDS = 10;
	
	private final ActorRef master;
	
	/**
	 * Standalone actor constructor, when this actor is has no master actor.
	 */
	public BiprimalityTestActor() {
		this(null);
	}
	
	/**Subordinate constructor, when this actor is executed as a part of a bigger FSM. When
	 *  a master is given, all messages sent by this actor have <code>sender=master</code>.
	 * @param master the ActorRef of the master that executes this actor as a sub-protocol
	 */
	public BiprimalityTestActor(ActorRef master) {
		
		this.master = master != null ? master : self();
		
		startWith(States.INITIALIZATION, BiprimalityTestData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class, 
				(participants, data) -> {				
					return goTo(States.AWAITING_N).using(data.withParticipants(participants.getParticipants()));
		}));
		
		when(States.AWAITING_N, matchEvent(CandidateN.class, (candidateN, data) -> {
			
			// Generates gprime = |H(N)*round| and its Qi 
			Map<ActorRef, Integer> actors = data.getParticipants();
			BigInteger gp = getGp(candidateN.N, 0);
			BigInteger Qi = getQi(gp,candidateN.N, candidateN.bgwPrivateParameters.pi, candidateN.bgwPrivateParameters.qi, actors.get(this.master));
						
			return goTo(States.COLLECT_Qjs).using(data.withNewCandidateN(candidateN.N, candidateN.bgwPrivateParameters)
														.withNewQi(Qi, actors.get(this.master), data.round));
		}));
		
		onTransition((from,to) -> {
			if (to == States.COLLECT_Qjs) {
				// Publish its Qi 
				broadCast(new QiTestForRound(nextStateData().qiss(nextStateData().round).get(nextStateData().getParticipants().get(this.master)),
											nextStateData().round), nextStateData().getParticipants().keySet());
			}
		});
		
		when(States.COLLECT_Qjs, matchEvent(QiTestForRound.class,
				(newQi, data) -> {
					
					// Collect the Qj and perform the test
					Map<ActorRef,Integer> actors = data.getParticipants();
					BiprimalityTestData newData = data.withNewQi(newQi.Qi, actors.get(sender()), newQi.round);
					
					
					if(!newData.hasQiOf(actors.values(), data.round)) {
						return stay().using(newData);
					} else {
						
						BigInteger check = newData.qis(data.round).map(qi -> {if (qi.getKey() == 1) return qi.getValue(); 
																	else return qi.getValue().modInverse(newData.N);})
									.reduce(BigInteger.ONE, (qi,qj) -> qi.multiply(qj)).mod(newData.N);
						
						BigInteger minusOne = BigInteger.ONE.negate().mod(newData.N);
						
						
						if (check.equals(minusOne) || check.equals(BigInteger.ONE)) {
							if(actors.get(this.master)==1)
								System.out.println("PASSED TEST "+data.round);
							
							if (data.round == NUMBER_OF_ROUNDS) {
								if(this.master != self())
									this.master.tell(new BiprimalityTestResult(data.N, data.bgwPrivateParameters, true), self());
								return stop();
							}
							
							// Resets N, bgwPrivateParameters, gprime, Qi and increments round counter for next round
							BiprimalityTestData nextData = data.forNextRound();
							
							BigInteger nextgp = getGp(nextData.N, nextData.round);
							BigInteger nextQi = getQi(nextgp, nextData.N, nextData.bgwPrivateParameters.pi, nextData.bgwPrivateParameters.qi, actors.get(this.master));
							
							// Loop back to the collection of Qj for next test round
							return goTo(States.COLLECT_Qjs).using(nextData.withNewQi(nextQi, actors.get(this.master), nextData.round));
						} else {
							
							if (this.master != self())
								this.master.tell(new BiprimalityTestResult(data.N, data.bgwPrivateParameters, false), self()); //TODO: put this in a onTransition handler

							// Loop back to the candidate awaiting
							return goTo(States.AWAITING_N).using(data.forNextCandidate());
						}
						
					}
				}));
		
		whenUnhandled(matchAnyEvent((evt,data) -> {			
			self().tell(evt, sender()); // An unhandled message goes back in the message queue
			return stay();
		}));
		
	
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

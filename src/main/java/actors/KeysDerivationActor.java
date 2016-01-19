package actors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import math.IntegersUtils;
import messages.Messages.BiprimalityTestResult;
import messages.Messages.Participants;
import messages.Messages.ThetaPoint;
import messages.Messages.VerificationKey;
import paillierp.key.PaillierPrivateThresholdKey;
import protocol.KeysDerivationParameters.KeysDerivationPrivateParameters;
import protocol.KeysDerivationParameters.KeysDerivationPublicParameters;
import protocol.ProtocolParameters;
import actordata.KeysDerivationData;
import actors.KeysDerivationActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

/**
 * This encodes the distributed key derivation from an accept N candidate as described by Takashi Nishide 
 * and Kouichi Sakurai in <i> Distributed Paillier Cryptosystem without Trusted Dealer </i>.
 * <p>
 * It waits for a valid biprimal N and its associated private parameters and generate all elements needed to construct a 
 * private Threshold Paillier key.
 * <p>
 * The output of this protocol is a PaillierThresholdPrivateKey object from the UTD Paillier Threshold Encryption Toolbox
 * ({@link http://cs.utdallas.edu/dspl/cgi-bin/pailliertoolbox/} )
 * @author Christian Mouchet
 */
public class KeysDerivationActor extends AbstractLoggingFSM<States, KeysDerivationData> {
	
	public static enum States {INITIALIZATION,
								AWAITING_N,
								COLLECTING_BETAj_DRj_PHIj, 
								COLLECTING_THETAj, 
								COLLECTING_VERIF_KEYS }

	private SecureRandom rand = new SecureRandom();
	private ActorRef master;
	
	/** Standalone actor constructor, when this actor is has no master actor
	 * @param protocolParam the pre-agreed public parameters of the protocol
	 */
	public KeysDerivationActor(ProtocolParameters protocolParameters) {
		this(protocolParameters, null);
	}
	
	/** Subordinate constructor, when this actor is executed as a part of a bigger FSM. When
	 *  a master is given, all messages sent by this actor have <code>sender=master</code>.
	 * @param protocolParam the pre-agreed public parameters of the protocol
	 * @param master the ActorRef of the master that executes this actor as a sub-protocol
	 */
	public KeysDerivationActor(ProtocolParameters protocolParameters, ActorRef master) {
		
		this.master = master != null ? master : self();
		
		startWith(States.INITIALIZATION, KeysDerivationData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class, (participants, data) -> {
			return goTo(States.AWAITING_N).using(data.withParticipants(participants.getParticipants()));
		}));
		
		when(States.AWAITING_N, matchEvent(BiprimalityTestResult.class, (acceptedN, data) -> {
			
			// Generates beta, delta*R and phi_i and their polynomial sharings
			int self = data.getParticipants().get(this.master);
			
			BigInteger N = acceptedN.N;
			BigInteger pi = acceptedN.bgwPrivateParameters.pi;
			BigInteger qi = acceptedN.bgwPrivateParameters.qi;
			
			BigInteger Phii = self == 1 ? N.subtract(pi).subtract(qi).add(BigInteger.ONE):
											pi.negate().subtract(qi);

			KeysDerivationPrivateParameters keysDerivationPrivateParameters = KeysDerivationPrivateParameters.gen(protocolParameters, self, N, Phii, rand);
			
			KeysDerivationPublicParameters keysDerivationPublicParameters = KeysDerivationPublicParameters.genFor(self, keysDerivationPrivateParameters);
			
			KeysDerivationData nextData = data.withN(N)
												.withPrivateParameters(keysDerivationPrivateParameters)
												.withNewPublicParametersFor(self, keysDerivationPublicParameters);
			
			return goTo(States.COLLECTING_BETAj_DRj_PHIj).using(nextData);
		}));
		
		onTransition(matchState(States.AWAITING_N, States.COLLECTING_BETAj_DRj_PHIj, () -> {
			
			// Distribute the beta_ij, delta*R_ij and phi_ij shares to the parties
			Map<ActorRef, Integer> actors = nextStateData().getParticipants();
			KeysDerivationPrivateParameters privateParameters = nextStateData().keysDerivationPrivateParameters;
			actors.entrySet().stream().forEach(e -> {
				if(!e.getKey().equals(this.master)){
					e.getKey().tell(KeysDerivationPublicParameters.genFor(e.getValue(), privateParameters), this.master);
				}
			});
		}));
		
		when(States.COLLECTING_BETAj_DRj_PHIj, matchEvent(KeysDerivationPublicParameters.class, (newShare, data) -> {
			
			// Collect the beta_ji, delta*R_ji and phi_ji shares and compute its own share of theta, theta_i
			Map<ActorRef, Integer> actors = data.getParticipants();
			int sender = actors.get(sender());
			int self = actors.get(this.master);
			
			KeysDerivationData nextData = data.withNewPublicParametersFor(sender, newShare);
			
			if(!nextData.hasBetaiRiOf(actors.values())) {
				return stay().using(nextData);
			} else {				
				Stream<Entry<Integer, KeysDerivationPublicParameters>> publicParameters = nextData.publicParameters();
				
				BigInteger betaPointi = publicParameters.map(e -> e.getValue().betaij)
													.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				publicParameters = nextData.publicParameters();
				BigInteger DRPointi = publicParameters.map(e -> e.getValue().DRij)
													.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				publicParameters = nextData.publicParameters();
				BigInteger PhiPointi = publicParameters.map(e -> e.getValue().Phiij)
													.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				publicParameters = nextData.publicParameters();
				
				BigInteger hij = publicParameters.map(e -> e.getValue().hij)
						.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				
				
				BigInteger delta = IntegersUtils.factorial(BigInteger.valueOf(protocolParameters.n));
				
				
				BigInteger thetai =  (delta.multiply(PhiPointi).multiply(betaPointi).mod(protocolParameters.P))
											.add(nextData.N.multiply(DRPointi).mod(protocolParameters.P))
											.add(hij).mod(protocolParameters.P);
				
				return goTo(States.COLLECTING_THETAj).using(nextData.withNewThetaFor(self, thetai)
																	.withRPoint(DRPointi));
			}
			
		}));
		
		onTransition(matchState(States.COLLECTING_BETAj_DRj_PHIj, States.COLLECTING_THETAj, () -> {
			
			// Publish its own theta share theta_i
			int self = nextStateData().getParticipants().get(this.master);
			BigInteger thetai = nextStateData().thetas.get(self);
			broadCast(new ThetaPoint(thetai), nextStateData().getParticipants().keySet());
		}));
		
		when(States.COLLECTING_THETAj, matchEvent(ThetaPoint.class, (newTheta, data) -> {
			
			// Collect the theta shares theta_j and reconstruct theta' using Lagrangian interpolation
			// Also compute its own verification keys VK_i
			Map<ActorRef, Integer> actors = data.getParticipants();
			int sender = actors.get(sender());
			int self = actors.get(this.master);
			KeysDerivationData newData = data.withNewThetaFor(sender, newTheta.thetai);
			if (!newData.hasThetaiOf(actors.values())) {
				return stay().using(newData);
			} else {
				List<BigInteger> thetas = newData.thetas.entrySet().stream()
						.map(e -> e.getValue())
						.collect(Collectors.toList());
				BigInteger thetap = IntegersUtils.getIntercept(thetas, protocolParameters.P);
				BigInteger theta = thetap.mod(newData.N);
								
				// Parties should have the same v, using theta to seed the random generator
				BigInteger v = IntegersUtils.pickProbableGeneratorOfZNSquare(newData.N,
																				2*protocolParameters.k,
																				new SecureRandom(theta.toByteArray())); //TODO ok ?
				
				BigInteger secreti = thetap.subtract(newData.N.multiply(newData.DRpoint));
				BigInteger delta = IntegersUtils.factorial(BigInteger.valueOf(protocolParameters.n));
				
				BigInteger verificationKeyi = v.modPow(delta.multiply(secreti), newData.N.multiply(newData.N));
				
				return goTo(States.COLLECTING_VERIF_KEYS).using(newData.withNewVerificationKeyFor(self, verificationKeyi)
																		.withNewV(v)
																		.withFi(secreti)
																		.withThetaprime(thetap));
			}
		}));
		
		onTransition(matchState(States.COLLECTING_THETAj, States.COLLECTING_VERIF_KEYS, () -> {
			
			// Publish its verification key
			int self = nextStateData().getParticipants().get(this.master);
			BigInteger verifKeyi = nextStateData().verificationKeys.get(self);
			broadCast(new VerificationKey(verifKeyi), nextStateData().getParticipants().keySet());
		}));
		
		when(States.COLLECTING_VERIF_KEYS, matchEvent(VerificationKey.class, (newVerifKey, data) -> {
			
			// Collect all verification keys VK_j and build the PaillierPrivateThresholdKey object
			Map<ActorRef, Integer> actors = data.getParticipants();
			int sender = actors.get(sender());
			int self = actors.get(this.master);
			KeysDerivationData newData = data.withNewVerificationKeyFor(sender, newVerifKey.verificationKey);
			if (!newData.hasVerifKeyOf(actors.values())) {
				return stay().using(newData);
			} else {
				
				BigInteger[] verificationKeys = new BigInteger[protocolParameters.n];
				for (Entry<Integer, BigInteger> entry : newData.verificationKeys.entrySet()) {
					verificationKeys[entry.getKey()-1] = entry.getValue();
				}
				
				PaillierPrivateThresholdKey privateKey = new PaillierPrivateThresholdKey(newData.N,
																							newData.thetaprime,
																							protocolParameters.n, 
																							protocolParameters.t+1,
																							newData.v, 
																							verificationKeys, 
																							newData.fi, 
																							self, 
																							rand.nextLong());
				if(this.master != self())
					this.master.tell(privateKey, self());
				
				return stop();
			}
		}));
		
		whenUnhandled(matchAnyEvent((evt, data) -> {
			self().tell(evt, sender()); // An unhandled message goes back to the message queue
			return stay();
		}));
		
	}
	
	private void broadCast(Object o, Set<ActorRef> targets) {
		targets.stream().forEach(actor -> {if (!actor.equals(this.master)) actor.tell(o, this.master);});
	}
}
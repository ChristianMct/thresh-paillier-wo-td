package actors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map.Entry;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bouncycastle.pqc.math.linearalgebra.BigIntUtils;

import math.IntegersUtils;
import math.LagrangianInterpolation;
import math.Polynomial;
import math.PolynomialMod;
import messages.Messages.BiprimalityTestResult;
import messages.Messages.Participants;
import messages.Messages.Thetai;
import messages.Messages.VerificationKey;
import paillierp.key.PaillierPrivateThresholdKey;
import protocol.KeysDerivationParameters.KeysDerivationPrivateParameters;
import protocol.KeysDerivationParameters.KeysDerivationPublicParameters;
import protocol.ProtocolParameters;
import actordata.KeysDerivationData;
import actors.KeysDerivationActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

public class KeysDerivationActor extends AbstractLoggingFSM<States, KeysDerivationData> {
	
	public static enum States {INITIALIZATION, AWAITING_N, AWAITING_BETAi_Ri_SHARES, AWAITING_THETAiS, AWAITING_VERIF_KEYS}

	private SecureRandom rand = new SecureRandom();
	private ActorRef master;
	
	public KeysDerivationActor(ActorRef master, ProtocolParameters protocolParameters) {
		
		this.master = master;
		
		startWith(States.INITIALIZATION, KeysDerivationData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class, (participants, data) -> {
			return goTo(States.AWAITING_N).using(data.withParticipants(participants.getParticipants()));
		}));
		
		when(States.AWAITING_N, matchEvent(BiprimalityTestResult.class, (acceptedN, data) -> {
			int self = data.getParticipants().get(this.master);
			
			BigInteger N = acceptedN.N;
			BigInteger pi = acceptedN.bgwPrivateParameters.p;
			BigInteger qi = acceptedN.bgwPrivateParameters.q;
			
			BigInteger Phii = self == 1 ? N.subtract(pi).subtract(qi).add(BigInteger.ONE):
											pi.negate().subtract(qi);

			KeysDerivationPrivateParameters keysDerivationPrivateParameters = KeysDerivationPrivateParameters.gen(protocolParameters, self, N, Phii, rand);
			
			KeysDerivationPublicParameters keysDerivationPublicParameters = KeysDerivationPublicParameters.genFor(self, keysDerivationPrivateParameters);
			
			KeysDerivationData nextData = data.withN(N)
												.withPrivateParameters(keysDerivationPrivateParameters)
												.withNewPublicParametersFor(self, keysDerivationPublicParameters);
			
			return goTo(States.AWAITING_BETAi_Ri_SHARES).using(nextData);
		}));
		
		onTransition(matchState(States.AWAITING_N, States.AWAITING_BETAi_Ri_SHARES, () -> {
			Map<ActorRef, Integer> actors = nextStateData().getParticipants();
			KeysDerivationPrivateParameters privateParameters = nextStateData().keysDerivationPrivateParameters;
			actors.entrySet().stream().forEach(e -> {
				if(!e.getKey().equals(this.master)){
					e.getKey().tell(KeysDerivationPublicParameters.genFor(e.getValue(), privateParameters), this.master);
				}
			});
		}));
		
		when(States.AWAITING_BETAi_Ri_SHARES, matchEvent(KeysDerivationPublicParameters.class, (newShare, data) -> {
			
			Map<ActorRef, Integer> actors = data.getParticipants();
			int sender = actors.get(sender());
			int self = actors.get(this.master);
			
			KeysDerivationData nextData = data.withNewPublicParametersFor(sender, newShare);
			
			if(!nextData.hasBetaiRiOf(actors.values())) {
				return stay().using(nextData);
			} else {
				System.out.println("HAS ALL");
				
				Stream<Entry<Integer, KeysDerivationPublicParameters>> publicParameters = nextData.publicParameters();
				
				BigInteger betaPointi = publicParameters.map(e -> e.getValue().betaij.fij)
													.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				publicParameters = nextData.publicParameters();
				BigInteger DRPointi = publicParameters.map(e -> e.getValue().DRij.fij)
													.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				publicParameters = nextData.publicParameters();
				BigInteger PhiPointi = publicParameters.map(e -> e.getValue().Phiij.fij)
													.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				publicParameters = nextData.publicParameters();
				
				BigInteger hij = publicParameters.map(e -> e.getValue().Phiij.hij)
						.reduce(BigInteger.ZERO, (b1, b2) -> b1.add(b2));
				
						//new PolynomialMod(2*protocolParameters.t, protocolParameters.Pp, BigInteger.ZERO, protocolParameters.k, rand);
				
				BigInteger delta = IntegersUtils.factorial(BigInteger.valueOf(protocolParameters.n));
				
				
				BigInteger thetai =  (delta.multiply(PhiPointi).multiply(betaPointi).mod(protocolParameters.Pp))
											.add(nextData.N.multiply(DRPointi).mod(protocolParameters.Pp))
											.add(hij).mod(protocolParameters.Pp);
				
				return goTo(States.AWAITING_THETAiS).using(nextData.withNewThetaFor(self, thetai)
																	.withRPoint(DRPointi));
			}
			
		}));
		
		onTransition(matchState(States.AWAITING_BETAi_Ri_SHARES, States.AWAITING_THETAiS, () -> {
			int self = nextStateData().getParticipants().get(this.master);
			BigInteger thetai = nextStateData().thetas.get(self);
			broadCast(new Thetai(thetai), nextStateData().getParticipants().keySet());
		}));
		
		when(States.AWAITING_THETAiS, matchEvent(Thetai.class, (newTheta, data) -> {
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
				BigInteger thetap = LagrangianInterpolation.getIntercept(thetas, protocolParameters.Pp);
				BigInteger theta = thetap.mod(newData.N);
				
				System.out.println("THETAP="+thetap);
				
				BigInteger v = IntegersUtils.pickProbableGeneratorOfZNSquare(newData.N,
																				2*protocolParameters.k,
																				new SecureRandom(theta.toByteArray())); //TODO ok ?
				
				BigInteger secreti = thetap.subtract(newData.N.multiply(newData.Rpoint));
				BigInteger delta = IntegersUtils.factorial(BigInteger.valueOf(protocolParameters.n));
				
				BigInteger verificationKeyi = v.modPow(delta.multiply(secreti), newData.N.multiply(newData.N));
				
				return goTo(States.AWAITING_VERIF_KEYS).using(newData.withNewVerificationKeyFor(self, verificationKeyi)
																		.withNewV(v)
																		.withFi(secreti)
																		.withThetaprime(thetap));
			}
		}));
		
		onTransition(matchState(States.AWAITING_THETAiS, States.AWAITING_VERIF_KEYS, () -> {
			int self = nextStateData().getParticipants().get(this.master);
			BigInteger verifKeyi = nextStateData().verificationKeys.get(self);
			broadCast(new VerificationKey(verifKeyi), nextStateData().getParticipants().keySet());
		}));
		
		when(States.AWAITING_VERIF_KEYS, matchEvent(VerificationKey.class, (newVerifKey, data) -> {
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
																							rand.nextLong()); // WOOT ?!
				this.master.tell(privateKey, self());
				//Thread.sleep(1000);
				return stop();
			}
		}));
		
		whenUnhandled(matchAnyEvent((evt, data) -> {
			self().tell(evt, sender());
			return stay();
		}));
		
	}
	
	private void broadCast(Object o, Set<ActorRef> targets) {
		targets.stream().forEach(actor -> {if (!actor.equals(this.master)) actor.tell(o, this.master);});
	}
}
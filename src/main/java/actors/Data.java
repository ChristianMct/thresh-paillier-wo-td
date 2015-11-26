package actors;

import java.math.BigInteger;
import java.security.KeyStore.Entry;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import protocol.BGWParameters.BGWPrivateParameters;
import protocol.BGWParameters.BGWPublicParameters;
import protocol.ProtocolParameters;

public class Data {
	public final ProtocolParameters protocolParameters;
	public final BGWPrivateParameters bgwPrivateParameters;
	private final Map<Integer,BGWPublicParameters> bgwPublicParameters;
	public final Map<Integer,BigInteger> Ns;
	
	private Data(ProtocolParameters protocolParameters,
					BGWPrivateParameters bgwParameters,
					Map<Integer,BGWPublicParameters> bgwPublicParameters,
					Map<Integer,BigInteger> Ns) {
		this.protocolParameters = protocolParameters;
		this.bgwPrivateParameters = bgwParameters;
		this.bgwPublicParameters = new HashMap<Integer,BGWPublicParameters>(bgwPublicParameters);
		this.Ns = new HashMap<Integer,BigInteger>(Ns);
	}
	
	public static Data init() {
		return new Data(null, null,new HashMap<Integer,BGWPublicParameters>(), new HashMap<Integer,BigInteger>());
	}
	
	public boolean hasShareOf(Collection<Integer> is) {
		return is.stream().allMatch(i->bgwPublicParameters.containsKey(i));
	}
	
	public Stream<Map.Entry<Integer,BGWPublicParameters>> shares() {
		return bgwPublicParameters.entrySet().stream();
	}
	
	public boolean hasNiOf(Collection<Integer> is) {
		return is.stream().allMatch(i->Ns.containsKey(i));
	}
	
	public Stream<Map.Entry<Integer, BigInteger>> nis() {
		return Ns.entrySet().stream();
	}
	
	public Data with(ProtocolParameters protocolParameters) {
		return new Data(protocolParameters, bgwPrivateParameters, bgwPublicParameters,Ns);
	}
	
	public Data with(BGWPrivateParameters bgwPrivateParameters) {
		return new Data(protocolParameters, bgwPrivateParameters, bgwPublicParameters,Ns);
	}
	
	public Data withNewShare(BGWPublicParameters share, int fromId) {
		if (bgwPublicParameters.containsKey(fromId))
			return this;
		
		Map<Integer, BGWPublicParameters> newMap = new HashMap<Integer,BGWPublicParameters>(bgwPublicParameters);
		newMap.put(fromId, share);
		return new Data(protocolParameters, bgwPrivateParameters, newMap,Ns);
	}
	
	public Data withNewNi(BigInteger Ni, int fromId) {
		if(Ns.containsKey(fromId))
			return this;
		
		Map<Integer,BigInteger> newNs = new HashMap<Integer, BigInteger>(Ns);
		newNs.put(fromId, Ni);
		return new Data(protocolParameters,bgwPrivateParameters, bgwPublicParameters , newNs);
	}
	
}
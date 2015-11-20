package actors;

import java.security.SecureRandom;
import java.util.Map;

import protocol.BGWParameters;
import protocol.BGWParameters.BGWPrivateParameters;
import protocol.BGWParameters.BGWPublicParameters;
import protocol.ProtocolParameters;
import actors.ProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

public class ProtocolActor extends AbstractLoggingFSM<States, Data>{
	
	public enum States {PARAM_AGREEMENT,BGW_AWAITING};
	
	private final SecureRandom sr = new SecureRandom();
	public static Map<ActorRef, Integer> indexMap;
	
	{
		startWith(States.PARAM_AGREEMENT, Data.init());
		
		when(States.PARAM_AGREEMENT, matchEvent(ProtocolParameters.class, 
				(protocolParameters, data) -> {
					Data newData = Data.from(data).set(protocolParameters);
					BGWPrivateParameters bgwParameters = BGWParameters.BGWPrivateParameters.genFor(indexMap.get(self()),
																									protocolParameters,
																									sr);
					return goTo(States.BGW_AWAITING).using(Data.from(newData).set(bgwParameters));
				}));
		
		when(States.BGW_AWAITING, matchEvent(BGWPublicParameters.class, 
				(bgwPublicParameters, data) -> {
					return stay();
				}));
		
		
		
		
		onTransition((from,to) -> {
			System.out.println(String.format("%s transition %s -> %s", self(), from, to ));
		});
	}
	
}

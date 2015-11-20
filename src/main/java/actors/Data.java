package actors;

import protocol.BGWParameters.BGWPrivateParameters;
import protocol.ProtocolParameters;

public class Data {
	public final ProtocolParameters protocolParameters;
	public final BGWPrivateParameters bgwParameters;
	
	private Data(ProtocolParameters protocolParameters, BGWPrivateParameters bgwParameters) {
		this.protocolParameters = protocolParameters;
		this.bgwParameters = bgwParameters;
	}
	
	public static Data init() {
		return new Data(null, null);
	}
	
	public static DataBuilder from(Data data) {
		return new DataBuilder(data);
	}
	
	static class DataBuilder {
		
		private ProtocolParameters protocolParameters;
		private BGWPrivateParameters bgwParameters;
		
		DataBuilder(Data data) {
			protocolParameters = data.protocolParameters;
			bgwParameters = data.bgwParameters;
		}
		
		public Data set(ProtocolParameters protocolParameters) {
			return new Data(protocolParameters, bgwParameters);
		}
		
		public Data set(BGWPrivateParameters bgwParameters) {
			return new Data(protocolParameters, bgwParameters);
		}
	}
	
}
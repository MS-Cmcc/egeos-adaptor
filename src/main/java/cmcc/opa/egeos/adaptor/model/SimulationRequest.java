package cmcc.opa.egeos.adaptor.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SimulationRequest {
	public SimulationRequest() {     }

	public SimulationRequest( int processing_id) {
		this.processing_id = processing_id;
	}

	private int processing_id;

	public int getProcessing_id() {
		return processing_id;
	}

	public void setProcessing_id(int processing_id) {
		this.processing_id = processing_id;
	}
}
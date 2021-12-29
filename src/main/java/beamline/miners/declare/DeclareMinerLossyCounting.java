package beamline.miners.declare;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;

import beamline.miners.declare.events.lossycounting.LCReplayer;
import beamline.miners.declare.model.DeclareModel;
import beamline.miners.declare.model.SimplifiedDeclareModel;
import beamline.miners.declare.view.DeclareModelView;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class DeclareMinerLossyCounting extends StreamMiningAlgorithm<XTrace, DeclareModelView> {

	// configuration variables
	private int eventsReceived = 0;
	private double maximalError = 0.005;
	private LCReplayer replayer = new LCReplayer();
	private int bucketWidth;
	
	private int constraintsToShow = 10;
	
	public DeclareMinerLossyCounting(double maximalError, int constraintsToShow) {
		setMaximalError(maximalError);
		setConstraintsToShow(constraintsToShow);
	}

	public void setMaximalError(double maximalError) {
		this.maximalError = maximalError;
		this.bucketWidth = (int)(1.0 / this.maximalError);
	}
	
	public void setConstraintsToShow(int constraintsToShow) {
		this.constraintsToShow = constraintsToShow;
	}

	@Override
	public DeclareModelView ingest(XTrace event) {
		String caseID = XConceptExtension.instance().extractName(event);
		String activityName = XConceptExtension.instance().extractName(event.get(0));
		
		// statistics update
		eventsReceived++;
		int currentBucket = (int)((double)eventsReceived / (double)bucketWidth);

		// data structure update
		replayer.addObservation(caseID, currentBucket);
		replayer.process(activityName, caseID);

		// events cleanup
		if (eventsReceived % bucketWidth == 0) {
			replayer.cleanup(currentBucket);
		}

		// incrementally update the model
		return getModel();
	}

	public DeclareModelView getModel() {
		DeclareModel model = replayer.getModel();
		DeclareModel filteredModel;
		if (model.hasTraces()) {
			filteredModel = DeclareModel.filterOnTraceSupport(model, 1.0);
		} else {
			filteredModel = DeclareModel.filterOnFulfillmentRatio(model, 1.0);
		}
		DeclareModel filteredSmallModel = DeclareModel.getTopConstraints(filteredModel, constraintsToShow);

		SimplifiedDeclareModel simplifiedModel = new SimplifiedDeclareModel();
		simplifiedModel.addConstraintsFromModel(filteredSmallModel);

		return new DeclareModelView(simplifiedModel);
	}
}

package beamline.miners.declare;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;

import beamline.miners.declare.events.budgetlossycounting.BudgetLCReplayer;
import beamline.miners.declare.model.DeclareModel;
import beamline.miners.declare.model.SimplifiedDeclareModel;
import beamline.miners.declare.view.DeclareModelView;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class DeclareMinerBudgetLossyCounting extends StreamMiningAlgorithm<XTrace, DeclareModelView> {

	// configuration variables
	private static int CONSTRAINT_NUMBER = 11;
	private BudgetLCReplayer replayer;
	
	private int constraintsToShow = 10;
	
	public DeclareMinerBudgetLossyCounting(int budgetSize, int constraintsToShow) {
		setBudgetSize(budgetSize);
		setConstraintsToShow(constraintsToShow);
	}

	public void setBudgetSize(int budgetSize) {
		replayer = new BudgetLCReplayer((int) (budgetSize / CONSTRAINT_NUMBER));
	}
	
	public void setConstraintsToShow(int constraintsToShow) {
		this.constraintsToShow = constraintsToShow;
	}

	@Override
	public DeclareModelView ingest(XTrace event) {
		String caseID = XConceptExtension.instance().extractName(event);
		String activityName = XConceptExtension.instance().extractName(event.get(0));
		
		// data structure update
		replayer.process(activityName, caseID);

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

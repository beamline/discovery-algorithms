package beamline.miners.trivial;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import beamline.graphviz.Dot;
import beamline.miners.trivial.graph.PMDotModel;
import beamline.models.responses.GraphvizResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessMap extends GraphvizResponse {

	private static final long serialVersionUID = 6248452599496125805L;
	private Map<String, Pair<Double, Double>> activities;
	private Map<Pair<String, String>, Pair<Double, Double>> relations;
	private Set<String> startingActivities;
	private Set<String> endingActivities;

	@Override
	public Dot generateDot() {
		return new PMDotModel(this, beamline.miners.trivial.graph.ColorPalette.Colors.BLUE);
	}

	public ProcessMap() {
		this.activities = new HashMap<>();
		this.relations = new HashMap<>();
		this.startingActivities = new HashSet<>();
		this.endingActivities = new HashSet<>();
	}

	public void addActivity(String activityName, Double relativeFrequency, Double absoluteFrequency) {
		this.activities.put(activityName, Pair.of(relativeFrequency, absoluteFrequency));
	}

	public void removeActivity(String activityName) {
		this.activities.remove(activityName);
	}

	public void addRelation(String activitySource, String activityTarget, Double relativeFrequency, Double absoluteFrequency) {
		relations.put(Pair.of(activitySource, activityTarget), Pair.of(relativeFrequency, absoluteFrequency));
	}

	public void removeRelation(String activitySource, String activityTarget) {
		relations.remove(new ImmutablePair<String, String>(activitySource, activityTarget));
	}

	public Set<String> getActivities() {
		return activities.keySet();
	}

	public Set<Pair<String, String>> getRelations() {
		return relations.keySet();
	}

	public Double getActivityRelativeFrequency(String activity) {
		return this.activities.get(activity).getLeft();
	}
	
	public Double getActivityAbsoluteFrequency(String activity) {
		return this.activities.get(activity).getRight();
	}

	public Double getRelationRelativeValue(Pair<String, String> relation) {
		return this.relations.get(relation).getLeft();
	}
	
	public Double getRelationAbsoluteValue(Pair<String, String> relation) {
		return this.relations.get(relation).getRight();
	}

	public Set<String> getIncomingActivities(String candidate) {
		Set<String> result = new HashSet<>();
		for (Pair<String, String> relation : getRelations()) {
			if (relation.getRight().equals(candidate)) {
				result.add(relation.getLeft());
			}
		}
		return result;
	}

	public Set<String> getOutgoingActivities(String candidate) {
		Set<String> result = new HashSet<>();
		for (Pair<String, String> relation : getRelations()) {
			if (relation.getLeft().equals(candidate)) {
				result.add(relation.getRight());
			}
		}
		return result;
	}
	
	public void addStartingActivity(String activity) {
		startingActivities.add(activity);
	}
	
	public void addEndActivity(String activity) {
		endingActivities.add(activity);
	}

	public boolean isStartActivity(String candidate) {
		return getIncomingActivities(candidate).isEmpty() || startingActivities.contains(candidate);
	}

	public boolean isEndActivity(String candidate) {
		return getOutgoingActivities(candidate).isEmpty() || endingActivities.contains(candidate);
	}

	public boolean isIsolatedNode(String candidate) {
		return getOutgoingActivities(candidate).equals(getIncomingActivities(candidate));
	}
}
package beamline.miners.trivial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import beamline.events.BEvent;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class DirectlyFollowsDependencyDiscoveryMiner extends StreamMiningAlgorithm<ProcessMap> {

	private static final long serialVersionUID = 6047581630499346814L;
	private Map<String, String> latestActivityInCase = new HashMap<>();
	private Map<Pair<String, String>, Double> relations = new HashMap<>();
	private Map<String, Double> activities = new HashMap<>();
	private Map<String, Double> startingActivities = new HashMap<>();
	private Double maxActivityFreq = 1d;
	private Double maxRelationsFreq = 1d;
	private double minDependency = 0.8;
	private int modelRefreshRate = 10;
	
	public DirectlyFollowsDependencyDiscoveryMiner() {
		// nothing to see here
	}
	
	public DirectlyFollowsDependencyDiscoveryMiner setMinDependency(double minDependency) {
		this.minDependency = minDependency;
		return this;
	}

	public DirectlyFollowsDependencyDiscoveryMiner setModelRefreshRate(int modelRefreshRate) {
		this.modelRefreshRate = modelRefreshRate;
		return this;
	}
	
	@Override
	public ProcessMap ingest(BEvent event) throws Exception {
		String caseID = event.getTraceName();
		String activityName = event.getEventName();
		
		Double activityFreq = 1d;
		if (activities.containsKey(activityName)) {
			activityFreq += activities.get(activityName);
			maxActivityFreq = Math.max(maxActivityFreq, activityFreq);
		}
		activities.put(activityName, activityFreq);

		if (latestActivityInCase.containsKey(caseID)) {
			Pair<String, String> relation = new ImmutablePair<>(latestActivityInCase.get(caseID), activityName);
			Double relationFreq = 1d;
			if (relations.containsKey(relation)) {
				relationFreq += relations.get(relation);
				maxRelationsFreq = Math.max(maxRelationsFreq, relationFreq);
			}
			relations.put(relation, relationFreq);
		} else {
			Double count = 1d;
			if (startingActivities.containsKey(activityName)) {
				count += startingActivities.get(activityName);
			}
			startingActivities.put(activityName, count);
		}
		latestActivityInCase.put(caseID, activityName);

		if (getProcessedEvents() % modelRefreshRate == 0) {
			return mine(minDependency);
		}
		
		return null;
	}
	
	public ProcessMap mine(double threshold) {
		ProcessMap process = new ProcessMap();
		for (Entry<String, Double> entry : activities.entrySet()) {
			process.addActivity(entry.getKey(), entry.getValue() / maxActivityFreq, entry.getValue());
		}
		for (Entry<Pair<String, String>, Double> entry : relations.entrySet()) {
			double dependency = entry.getValue() / maxRelationsFreq;
			if (dependency >= threshold) {
				process.addRelation(entry.getKey().getLeft(), entry.getKey().getRight(), dependency, entry.getValue());
			}
		}
		Set<String> toRemove = new HashSet<>();
		Set<String> selfLoopsToRemove = new HashSet<>();
		for (String activity : activities.keySet()) {
			if (process.isStartActivity(activity) && process.isEndActivity(activity)) {
				toRemove.add(activity);
			}
			if (process.isIsolatedNode(activity)) {
				selfLoopsToRemove.add(activity);
			}
		}
		for (String activity : toRemove) {
			process.removeActivity(activity);
		}
		for (Entry<String, Double> entry : startingActivities.entrySet()) {
			Double freq = entry.getValue();
			double dependency = freq / maxRelationsFreq;
			if (dependency >= threshold) {
				process.addStartingActivity(entry.getKey());
			}
		}

		return process;
	}
}

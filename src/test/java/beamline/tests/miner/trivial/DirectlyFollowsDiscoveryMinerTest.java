package beamline.tests.miner.trivial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

import beamline.events.BEvent;
import beamline.miners.trivial.DirectlyFollowsDependencyDiscoveryMiner;
import beamline.miners.trivial.ProcessMap;
import beamline.sources.StringTestSource;

class DirectlyFollowsDiscoveryMinerTest {

	
	@Test
	void test_miner() throws Exception {
		final List<ProcessMap> results = new LinkedList<>();
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env
			.fromElements(
					BEvent.create("p", "c1", "A"),
					BEvent.create("p", "c1", "B"),
					BEvent.create("p", "c1", "C"),
					BEvent.create("p", "c2", "A"),
					BEvent.create("p", "c2", "B"),
					BEvent.create("p", "c2", "C"))
			.keyBy(BEvent::getProcessName)
			.flatMap(new DirectlyFollowsDependencyDiscoveryMiner().setModelRefreshRate(1))
			.executeAndCollect().forEachRemaining((ProcessMap e) -> {
				results.add(e);
			});
		
		ProcessMap p = results.get(results.size() - 1);
		assertTrue(p.getActivities().containsAll(Arrays.asList("A", "B", "C")));
		assertTrue(p.getRelations().containsAll(Arrays.asList(Pair.of("A", "B"), Pair.of("B", "C"))));
	}

	@Test
	void test_miner_threshold() throws Exception {
		
		final List<ProcessMap> results = new LinkedList<>();
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env
			.addSource(new StringTestSource("ABC", "ABC", "ABC", "ABC", "ABC"))
			.keyBy(BEvent::getProcessName)
			.flatMap(new DirectlyFollowsDependencyDiscoveryMiner()
					.setModelRefreshRate(1)
					.setMinDependency(0.8))
			.executeAndCollect().forEachRemaining((ProcessMap e) -> {
				results.add(e);
			});
		ProcessMap p = results.get(results.size() - 1);
		
		assertTrue(p.getActivities().containsAll(Arrays.asList("A", "B", "C")));
		assertEquals(p.getActivities().size(), 3);
		assertTrue(p.getRelations().containsAll(Arrays.asList(Pair.of("A", "B"), Pair.of("B", "C"))));
		assertEquals(p.getRelations().size(), 2);
	}
}

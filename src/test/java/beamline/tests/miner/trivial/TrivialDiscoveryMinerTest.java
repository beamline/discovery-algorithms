package beamline.tests.miner.trivial;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.junit.jupiter.api.Test;

import beamline.miners.trivial.ProcessMap;
import beamline.miners.trivial.TrivialDiscoveryMiner;

class TrivialDiscoveryMinerTest {

	private static XFactory factory = new XFactoryNaiveImpl();
	
	@Test
	void test_miner() {
		TrivialDiscoveryMiner m = new TrivialDiscoveryMiner();
		m.ingest(prepare("A", "c1"));
		m.ingest(prepare("B", "c1"));
		m.ingest(prepare("C", "c1"));
		ProcessMap p = m.getLatestResponse();
		assertTrue(p.getActivities().containsAll(Arrays.asList("A", "B", "C")));
		assertEquals(p.getActivities().size(), 3);
		assertTrue(p.getRelations().containsAll(Arrays.asList(Pair.of("A", "B"), Pair.of("B", "C"))));
		assertEquals(p.getRelations().size(), 2);
	}

	@Test
	void test_miner_threshold() {
		TrivialDiscoveryMiner m = new TrivialDiscoveryMiner();
		m.setModelRefreshRate(1);
		m.setMinDependency(0.8);
		m.ingest(prepare("A", "c1")); m.ingest(prepare("D", "c1")); m.ingest(prepare("C", "c1"));
		m.ingest(prepare("A", "c2")); m.ingest(prepare("B", "c2")); m.ingest(prepare("C", "c2"));
		m.ingest(prepare("A", "c3")); m.ingest(prepare("B", "c3")); m.ingest(prepare("C", "c3"));
		m.ingest(prepare("A", "c4")); m.ingest(prepare("B", "c4")); m.ingest(prepare("C", "c4"));
		m.ingest(prepare("A", "c5")); m.ingest(prepare("B", "c5")); m.ingest(prepare("C", "c5"));
		ProcessMap p = m.getLatestResponse();
		assertTrue(p.getActivities().containsAll(Arrays.asList("A", "B", "C")));
		assertEquals(p.getActivities().size(), 3);
		assertTrue(p.getRelations().containsAll(Arrays.asList(Pair.of("A", "B"), Pair.of("B", "C"))));
		assertEquals(p.getRelations().size(), 2);
	}
	
	
	private static XTrace prepare(String activityName, String caseId) {
		XTrace wrapper = factory.createTrace();
		XEvent event = factory.createEvent();
		
		XConceptExtension.instance().assignName(wrapper, caseId);
		XConceptExtension.instance().assignName(event, activityName);
		
		wrapper.add(event);
		return wrapper;
	}

}

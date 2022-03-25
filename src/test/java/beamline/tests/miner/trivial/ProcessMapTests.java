package beamline.tests.miner.trivial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import beamline.miners.trivial.ProcessMap;

public class ProcessMapTests {

	@Test
	void test_add_activity() {
		ProcessMap m = new ProcessMap();
		m.addActivity("A", 1d, 1d);
		m.addActivity("B", 1d, 1d);
		m.addActivity("C", 1d, 1d);
		assertTrue(m.getActivities().containsAll(Arrays.asList("A", "B", "C")));
	}
	
	@Test
	void test_remove_activity() {
		ProcessMap m = new ProcessMap();
		m.addActivity("A", 1d, 1d);
		m.addActivity("B", 1d, 1d);
		m.addActivity("C", 1d, 1d);
		m.addActivity("D", 1d, 1d);
		m.removeActivity("D");
		assertTrue(m.getActivities().containsAll(Arrays.asList("A", "B", "C")));
	}
	
	@Test
	void test_activity_value() {
		ProcessMap m = new ProcessMap();
		m.addActivity("A", 3.14d, 1d);
		m.addActivity("B", 42d, 1d);
		assertEquals(3.14d, m.getActivityRelativeFrequency("A"));
		assertEquals(42d, m.getActivityRelativeFrequency("B"));
	}
	
	@Test
	void test_add_relation() {
		ProcessMap m = new ProcessMap();
		m.addRelation("A", "B", 1d, 1d);
		m.addRelation("B", "C", 1d, 1d);
		assertTrue(m.getRelations().containsAll(Arrays.asList(Pair.of("A", "B"), Pair.of("B", "C"))));
		assertEquals(2, m.getRelations().size());
	}
	
	@Test
	void test_remove_relation() {
		ProcessMap m = new ProcessMap();
		m.addRelation("A", "B", 1d, 1d);
		m.addRelation("B", "C", 1d, 1d);
		m.addRelation("B", "D", 1d, 1d);
		m.removeRelation("B", "C");
		assertTrue(m.getRelations().containsAll(Arrays.asList(Pair.of("A", "B"), Pair.of("B", "D"))));
		assertEquals(2, m.getRelations().size());
	}
	
	@Test
	void test_relation_value() {
		ProcessMap m = new ProcessMap();
		m.addRelation("A", "B", 3.14d, 1d);
		m.addRelation("B", "C", 42d, 1d);
		assertEquals(3.14d, m.getRelationRelativeValue(Pair.of("A", "B")));
		assertEquals(42d, m.getRelationRelativeValue(Pair.of("B", "C")));
	}
	
	@Test
	void test_start() {
		ProcessMap m = new ProcessMap();
		m.addRelation("A", "B", 1d, 1d);
		m.addRelation("B", "C", 1d, 1d);
		assertTrue(m.isStartActivity("A"));
		assertFalse(m.isStartActivity("B"));
		assertFalse(m.isStartActivity("C"));
	}
	
	@Test
	void test_end() {
		ProcessMap m = new ProcessMap();
		m.addRelation("A", "B", 1d, 1d);
		m.addRelation("B", "C", 1d, 1d);
		assertFalse(m.isEndActivity("A"));
		assertFalse(m.isEndActivity("B"));
		assertTrue(m.isEndActivity("C"));
	}
	
	@Test
	void test_isolated() {
		ProcessMap m = new ProcessMap();
		m.addRelation("A", "B", 1d, 1d);
		m.addRelation("B", "C", 1d, 1d);
		m.addActivity("D", 1d, 1d);
		assertFalse(m.isIsolatedNode("A"));
		assertFalse(m.isIsolatedNode("B"));
		assertFalse(m.isIsolatedNode("C"));
		assertTrue(m.isIsolatedNode("D"));
	}
}

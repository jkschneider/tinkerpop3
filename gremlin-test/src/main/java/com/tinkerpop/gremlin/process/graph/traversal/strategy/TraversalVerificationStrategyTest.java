package com.tinkerpop.gremlin.process.graph.traversal.strategy;

import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import org.junit.Test;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static com.tinkerpop.gremlin.process.graph.__.*;
import static org.junit.Assert.fail;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TraversalVerificationStrategyTest extends AbstractGremlinProcessTest {

    public static class StandardTest extends TraversalVerificationStrategyTest {
        @Test
        @LoadGraphWith(MODERN)
        public void shouldAllowNestedGlobalTraversalToHaveBarriers() {
            try {
                g.V().values("age").union(max(), min(), sum()).iterate();
            } catch (IllegalStateException e) {
                fail("Nested global traversals can have barrier steps on STANDARD:" + e.getMessage());
            }
        }

        @Test
        @LoadGraphWith(MODERN)
        public void shouldAllowMidTraversalBarriersOnComputer() {
            try {
                g.V().count().sum().iterate();
            } catch (IllegalStateException e) {
                fail("Mid-traversal barrier steps are OK on STANDARD: " + e.getMessage());
            }
        }

        @Test
        @LoadGraphWith(MODERN)
        public void shouldAllowLocalTraversalsToLeaveTheStarGraphOnComputer() {
            try {
                g.V().local(out().out()).iterate();
            } catch (IllegalStateException e) {
                fail("Local traversals leaving the star-graph are OK on STANDARD: " + e.getMessage());
            }
        }
    }

    public static class ComputerTest extends TraversalVerificationStrategyTest {
        @Test
        @LoadGraphWith(MODERN)
        public void shouldNotAllowNestedGlobalTraversalToHaveBarriers() {
            try {
                final GraphTraversal t = g.V().values("age").union(max(), min(), sum()).submit(g.compute()).iterate();
                fail("Nested global traversals should not be allowed to contain barriers (COMPUTER): " + t);
            } catch (IllegalStateException e) {

            }
        }

        @Test
        @LoadGraphWith(MODERN)
        public void shouldNotAllowMidTraversalBarriersOnComputer() {
            try {
                final GraphTraversal t = g.V().count().sum().submit(g.compute()).iterate();
                fail("Mid-traversal barrier steps are not allowed (COMPUTER): " + t);
            } catch (IllegalStateException e) {

            }

            try {
                final GraphTraversal t = g.V().count().sum().map(x -> x.get() * 19).submit(g.compute()).iterate();
                fail("Mid-traversal barrier steps are not allowed (COMPUTER): " + t);
            } catch (IllegalStateException e) {

            }
        }

        @Test
        @LoadGraphWith(MODERN)
        public void shouldNotAllowLocalTraversalsToLeaveTheStarGraphOnComputer() {
            try {
                g.V().local(outE().values("weight")).submit(g.compute()).iterate();
                g.V().local(out().id()).submit(g.compute()).iterate();
                g.V().local(outE().inV()).submit(g.compute()).iterate();
                g.V().local(inE().as("a").values("weight").back("a").outV()).submit(g.compute()).iterate();
            } catch (IllegalStateException e) {
                fail("Local traversals on the star-graph are OK on COMPUTER: " + e.getMessage());
            }

            try {
                final GraphTraversal t = g.V().local(out().out()).submit(g.compute()).iterate();
                fail("Local traversals should not be allowed to leave the star-graph (COMPUTER): " + t);
            } catch (IllegalStateException e) {

            }

            try {
                final GraphTraversal t = g.V().local(out().values("name")).submit(g.compute()).iterate();
                fail("Local traversals should not be allowed to leave the star-graph (COMPUTER): " + t);
            } catch (IllegalStateException e) {

            }
        }
    }
}

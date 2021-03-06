package com.tinkerpop.gremlin.process.graph.traversal.strategy;

import com.tinkerpop.gremlin.process.Step;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.TraversalEngine;
import com.tinkerpop.gremlin.process.graph.traversal.step.filter.DedupStep;
import com.tinkerpop.gremlin.process.graph.traversal.step.map.OrderGlobalStep;
import com.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.IdentityStep;
import com.tinkerpop.gremlin.process.traversal.lambda.IdentityTraversal;
import com.tinkerpop.gremlin.process.traversal.util.TraversalHelper;

import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DedupOptimizerStrategy extends AbstractTraversalStrategy {

    private static final DedupOptimizerStrategy INSTANCE = new DedupOptimizerStrategy();

    private DedupOptimizerStrategy() {
    }

    private static final List<Class<? extends Step>> BIJECTIVE_PIPES = Arrays.asList(IdentityStep.class, OrderGlobalStep.class);

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal, final TraversalEngine engine) {
        if (!TraversalHelper.hasStepOfClass(DedupStep.class, traversal))
            return;

        boolean done = false;
        while (!done) {
            done = true;
            for (int i = 0; i < traversal.getSteps().size(); i++) {
                final Step step1 = traversal.getSteps().get(i);
                if (step1 instanceof DedupStep && !(((DedupStep) step1).getLocalTraversals().get(0) instanceof IdentityTraversal)) {
                    for (int j = i; j >= 0; j--) {
                        final Step step2 = traversal.getSteps().get(j);
                        if (BIJECTIVE_PIPES.stream().filter(c -> c.isAssignableFrom(step2.getClass())).findAny().isPresent()) {
                            traversal.removeStep(step1);
                            traversal.addStep(j, step1);
                            done = false;
                            break;
                        }
                    }
                }
                if (!done)
                    break;
            }
        }
    }

    public static DedupOptimizerStrategy instance() {
        return INSTANCE;
    }
}

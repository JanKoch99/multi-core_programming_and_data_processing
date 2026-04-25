import java.util.concurrent.atomic.AtomicReference;

public class LockFreeConsensus implements Consensus {

    private AtomicReference<Object> decision = new AtomicReference<>(null);

    @Override
    public Object decide(Object v) {
        decision.compareAndSet(null, v);
        return decision.get();
    }
}

package org.unipop.common.valueSuppliers;








import java.util.function.Supplier;

public abstract class TimeBasedSupplierFactory implements Supplier<Supplier<Integer>> {
    //region Constructors
    public TimeBasedSupplierFactory() {
        this.clock = Clock.System.instance;
    }

    public TimeBasedSupplierFactory(Clock clock) {
        this.clock = clock;
    }
    //endregion

    //region Properties
    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
    //endregion

    //region Fields
    protected Clock clock;
    //endregion
}

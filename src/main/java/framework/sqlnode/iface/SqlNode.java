package framework.sqlnode.iface;

import framework.sqlnode.DynamicContext;

public interface SqlNode {
    void apply(DynamicContext context);
}

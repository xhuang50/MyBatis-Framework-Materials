package framework.sqlnode;

import framework.sqlnode.iface.SqlNode;
import framework.utils.OgnlUtils;

public class IfSqlNode implements SqlNode {
    private String test;

    private SqlNode rootSqlNode;

    public IfSqlNode(String test, SqlNode rootSqlNode) {
        this.test = test;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public void apply(DynamicContext context) {
        Object parameter = context.getBindings().get("_parameter");
        boolean evaluateBoolean = OgnlUtils.evaluateBoolean(test, parameter);
        if (evaluateBoolean){
            // Resolve the children nodes recursively
            rootSqlNode.apply(context);
        }
    }
}

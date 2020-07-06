package framework.sqlsource;

import framework.sqlnode.DynamicContext;
import framework.sqlnode.iface.SqlNode;
import framework.sqlsource.iface.SqlSource;
import framework.sqlsource.model.BoundSql;
import framework.utils.GenericTokenParser;
import framework.utils.ParameterMappingTokenHandler;

public class DynamicSqlSource implements SqlSource {
    private SqlNode rootSqlNode;

    public DynamicSqlSource(SqlNode rootSqlNode) {
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object param) {
        DynamicContext context = new DynamicContext(param);
        rootSqlNode.apply(context);
        String sqlText = context.getSql();
        System.out.println("#{}未处理的SQL语句："+sqlText);


        ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser tokenParser = new GenericTokenParser("#{","}",tokenHandler);

        String sql = tokenParser.parse(sqlText);
        System.out.println("#{}处理之后的SQL语句："+sql);


        return new BoundSql(sql, tokenHandler.getParameterMappings());

    }
}

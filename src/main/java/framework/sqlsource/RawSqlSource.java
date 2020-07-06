package framework.sqlsource;

import framework.sqlnode.DynamicContext;
import framework.sqlnode.iface.SqlNode;
import framework.sqlsource.iface.SqlSource;
import framework.sqlsource.model.BoundSql;
import framework.utils.GenericTokenParser;
import framework.utils.ParameterMappingTokenHandler;

/**
 *  Encapsulate statements that contains no ${} or dynamic tags.
 *  Resolve in the constructor
 */
public class RawSqlSource implements SqlSource {
    private SqlSource sqlSource;

    public RawSqlSource(SqlNode rootSqlNode) {

        DynamicContext context = new DynamicContext(null);
        rootSqlNode.apply(context);

        // Get the sql statement that has already been parsed.
        // This sqlText might contains segment eg. #{}. These tags will be resolved in later section.
        String sqlText = context.getSql();

        ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser tokenParser = new GenericTokenParser("#{", "}", tokenHandler);

        String sql = tokenParser.parse(sqlText);
        sqlSource = new StaticSqlSource(sql, tokenHandler.getParameterMappings());

    }

    @Override
    public BoundSql getBoundSql(Object param) {
        return sqlSource.getBoundSql(param);
    }
}

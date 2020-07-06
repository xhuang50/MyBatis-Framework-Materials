package framework.sqlsource;

import framework.sqlsource.iface.SqlSource;
import framework.sqlsource.model.BoundSql;
import framework.sqlsource.model.ParameterMapping;

import java.util.List;

public class StaticSqlSource implements SqlSource {
    private String sql;
    private List<ParameterMapping> parameterMappings;

    public StaticSqlSource(String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
    }

    @Override
    public BoundSql getBoundSql(Object param) {
        return new BoundSql(sql, parameterMappings);
    }
}

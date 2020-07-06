package framework.sqlsource.iface;


import framework.sqlsource.model.BoundSql;

public interface SqlSource {
    BoundSql getBoundSql(Object param);
}

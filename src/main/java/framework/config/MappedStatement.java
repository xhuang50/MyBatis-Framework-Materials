package framework.config;

import framework.sqlsource.iface.SqlSource;

public class MappedStatement {
    private String statementId;
    private String resultType;
    private String statementType;
    private Class resultClass;
    private SqlSource sqlSource;

    public MappedStatement(String statementId, String statementType, Class resultClass, SqlSource sqlSource) {
        this.statementId = statementId;
        this.statementType = statementType;
        this.resultClass = resultClass;
        this.sqlSource = sqlSource;
    }

    public String getStatementId() {
        return statementId;
    }

    public void setStatementId(String statementId) {
        this.statementId = statementId;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getStatementType() {
        return statementType;
    }

    public void setStatementType(String statementType) {
        this.statementType = statementType;
    }

    public Class getResultClass() {
        return resultClass;
    }

    public void setResultClass(Class resultClass) {
        this.resultClass = resultClass;
    }

    public SqlSource  getSqlSource() {
        return sqlSource;
    }

    public void setSqlSource(SqlSource sqlSource) {
        this.sqlSource = sqlSource;
    }
}

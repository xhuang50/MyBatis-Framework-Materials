package assignment;

import framework.config.Configuration;
import framework.config.MappedStatement;
import framework.sqlnode.IfSqlNode;
import framework.sqlnode.MixedSqlNode;
import framework.sqlnode.StaticTextSqlNode;
import framework.sqlnode.TextSqlNode;
import framework.sqlnode.iface.SqlNode;
import framework.sqlsource.DynamicSqlSource;
import framework.sqlsource.RawSqlSource;
import framework.sqlsource.iface.SqlSource;
import framework.sqlsource.model.BoundSql;
import framework.sqlsource.model.ParameterMapping;
import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import pojo.User;

import javax.sql.DataSource;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class MyBatisV2 {

    private Configuration configuration = new Configuration();
    private String namespace;
    private boolean isDynamic;

    @Test
    public void test() {
        loadXML("mybatis-config.xml");

        Map params = new HashMap();
        params.put("username","王五");
        params.put("sex","男");

        List<User> users = selectList("test.queryUserByParams", params);
        System.out.println(users);

    }

    private void loadXML(String location) {
        InputStream inputStream = getResourceAsStream(location);
        Document document = getDocument(inputStream);
        parseConfiguration(document.getRootElement());
    }

    private void parseConfiguration(Element rootElement) {
        Element environments = rootElement.element("environments");
        parseEnvironments(environments);

        Element mappers = rootElement.element("mappers");
        parseMappers(mappers);
    }

    private void parseMappers(Element mappers) {
        List<Element> list = mappers.elements("mapper");
        for (Element element : list) {
            String resource = element.attributeValue("resource");
            InputStream inputStream = getResourceAsStream(resource);
            Document document = getDocument(inputStream);
            parseMapper(document.getRootElement());
        }

    }

    private void parseMapper(Element rootElement) {
        namespace = rootElement.attributeValue("namespace");
        List<Element> selectElements = rootElement.elements("select");
        for (Element selectElement : selectElements) {
            parseStatementElement(selectElement);
        }
    }

    private void parseStatementElement(Element selectElement) {
        String statementId = selectElement.attributeValue("id");
        if (statementId == null || statementId.equals("")){
            return;
        }
        // Make sure the in-used statementId in the select method includes the namespace and the dot.
        statementId = namespace + '.' + statementId;
        // parameterType unresolved
//        String parameterType = selectElement.attributeValue("parameterType");
//        Class<?> parameterClass = resolveType(parameterType);

        String resultType = selectElement.attributeValue("resultType");
        Class<?> resultClass = resolveType(resultType);

        String statementType = selectElement.attributeValue("statementType");
        statementType = statementType == null || statementType == "" ? "prepared" : statementType;

        // TODO Sql encapsulation
        SqlSource sqlSource = createSqlSource(selectElement);
        // TODO SqlSource
        MappedStatement mappedStatement = new MappedStatement(statementId, statementType, resultClass, sqlSource);
        configuration.addMappedStatement(statementId, mappedStatement);

    }

    private SqlSource createSqlSource(Element selectElement) {
        SqlSource sqlSource = parseScriptNode(selectElement);
        return sqlSource;
    }

    private SqlSource parseScriptNode(Element selectElement) {
        SqlNode mixedSqlnode = parseDynamicTags(selectElement);
        SqlSource sqlSource;
        if(isDynamic){
            sqlSource = new DynamicSqlSource(mixedSqlnode);
        }else {
            sqlSource = new RawSqlSource(mixedSqlnode);
        }
        return sqlSource;
    }

    private SqlNode parseDynamicTags(Element selectElement) {
        List<SqlNode> sqlNodes = new ArrayList<>();

        int nodeCount = selectElement.nodeCount();
        for (int i = 0; i < nodeCount; i++) {
            Node node = selectElement.node(i);
            if (node instanceof Text){
                String text = node.getText();
                if (text==null || "".equals(text.trim())){
                    continue;
                }
                TextSqlNode textSqlNode = new TextSqlNode(text.trim());
                if (textSqlNode.isDynamic()){
                    sqlNodes.add(textSqlNode);
                    isDynamic = true;
                } else {
                    sqlNodes.add(new StaticTextSqlNode(text.trim()));
                }
            } else if (node instanceof Element){
                Element element = (Element) node;
                String name = element.getName();
                if ("if".equals(name)){
                    String test = element.attributeValue("test");
                    SqlNode sqlNode = parseDynamicTags(element);
                    IfSqlNode ifSqlNode = new IfSqlNode(test, sqlNode);
                    sqlNodes.add(ifSqlNode);

                }
            } else {
                //TODO
            }
        }
        return new MixedSqlNode(sqlNodes);
    }

    private Class<?> resolveType(String parameterType) {
        try {
            Class<?> clazz = Class.forName(parameterType);
            return clazz;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseEnvironments(Element environments) {
        String defaultValue = environments.attributeValue("default");
        List<Element> environmentList = environments.elements("environment");
        for (Element element : environmentList) {
            String id = element.attributeValue("id");
            if (defaultValue.equals(id)){
                Element dataSource = element.element("dataSource");
                parseDataSource(dataSource);
            }
        }
    }

    private void parseDataSource(Element element) {
        String type = element.attributeValue("type");
        if (type.equals("DBCP")){
            BasicDataSource dataSource = new BasicDataSource();
            Properties properties = parseProperties(element);
            dataSource.setDriverClassName(properties.getProperty("db.driver"));
            dataSource.setUrl(properties.getProperty("db.url"));
            dataSource.setUsername(properties.getProperty("db.username"));
            dataSource.setPassword(properties.getProperty("db.password"));
            configuration.setDataSource(dataSource);
        }
    }

    private Properties parseProperties(Element element) {
        Properties properties = new Properties();
        List<Element> propertyList = element.elements("property");
        for (Element propertyElement : propertyList) {
            String name = propertyElement.attributeValue("name");
            String value = propertyElement.attributeValue("value");
            properties.put(name,value);
        }
        return properties;
    }

    private Document getDocument(InputStream inputStream) {
        SAXReader saxReader = new SAXReader();
        try {
            return saxReader.read(inputStream);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getResourceAsStream(String location) {
        return this.getClass().getClassLoader().getResourceAsStream(location);
    }

    private void loadProperties(String location) {
    }

    // use SQL statement that identified by stmtId; parse the param for the dynamic tags.
    // param can be anything, as far as it aligns with the SQL statement.
    private <T> List<T> selectList(String stmtId, Object param) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<T> results = new ArrayList<>();
        try {
            // mappedStatement: store all info of a sql statement. Identify by stmtId
            MappedStatement mappedStatement = configuration.getMappedStatementMapById(stmtId);
            // 1. connection
            connection = getConnection();
            // 2. sql
            BoundSql boundSql = getSql(mappedStatement, param);
            String sql = boundSql.getSql();
            // 3. statement object
            statement = createStatement(connection, sql, mappedStatement);
            // 4. set parameters
            setParameter(statement, param, boundSql);
            // 5. execute statement
            resultSet = handleStatement(statement);
            // 6. parse resultSet
            handleResultSet(resultSet, results, mappedStatement);
            return results;
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if (resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private <T> void handleResultSet(ResultSet resultSet, List<T> results, MappedStatement mappedStatement) throws Exception {
        Class clazz= mappedStatement.getResultClass();
        Object result = null;
        while(resultSet.next()){
            result = clazz.newInstance();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Field field = clazz.getDeclaredField(columnName);
                field.setAccessible(true);
                field.set(result, resultSet.getObject(i));
            }
            results.add((T) result);
        }
    }

    private ResultSet handleStatement(Statement statement) throws SQLException {
        if (statement instanceof PreparedStatement){
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            return preparedStatement.executeQuery();
        }
        return null;
    }

    private void setParameter(Statement statement, Object param, BoundSql boundSql) throws SQLException {
        if (statement instanceof PreparedStatement){
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            if (param instanceof String || param instanceof Integer){
                preparedStatement.setObject(1, param);
            } else if (param instanceof Map){
                Map paramMap = (Map) param;
                List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
                for (int i = 0; i < parameterMappings.size(); i++) {
                    // ParameterMapping stores all parameters that need to added in to the sql statement in key value pairs
                    ParameterMapping parameterMapping = parameterMappings.get(i);
                    String name = parameterMapping.getName();
                    Object value = paramMap.get(name);
                    Class type = parameterMapping.getType();
                    if (type != null){
                        // TODO simply type eg. Integer
                    } else {
                        preparedStatement.setObject(i+1, value);
                    }

                }
            } else {
                //TODO throw exceptions.
            }
        }

    }

    private Statement createStatement(Connection connection, String sql, MappedStatement mappedStatement) throws SQLException {
        String statementType = mappedStatement.getStatementType();
        if (statementType.equals("prepared")){
            return connection.prepareStatement(sql);
        } else if (statementType.equals("statement")){
            return connection.createStatement();
        } else if (statementType.equals("callable")){
            // TODO
        } else {
            //TODO
        }
        return null;
    }

    private BoundSql getSql(MappedStatement mappedStatement, Object param) {
        // SqlSource stores info of a Sql statement, eg. entire line of a select etc.
        SqlSource sqlSource = mappedStatement.getSqlSource();
        BoundSql boundSql = sqlSource.getBoundSql(param);
        return boundSql;
    }

    /**
     *
     * @return
     */
    private Connection getConnection() {
        DataSource dataSource = configuration.getDataSource();

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

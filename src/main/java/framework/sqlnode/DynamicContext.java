package framework.sqlnode;

import java.util.HashMap;
import java.util.Map;

public class DynamicContext {
    private StringBuffer sb = new StringBuffer();
    private Map<String, Object> bindings = new HashMap<>();

    public DynamicContext(Object param) {
        bindings.put("_parameter", param);
    }

    public String getSql(){
        return sb.toString();
    }

    public void appendSql(String sql){
        sb.append(sql);
        sb.append(" ");
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void addBindings(String key, Object value) {
        this.bindings.put(key, value);
    }
}

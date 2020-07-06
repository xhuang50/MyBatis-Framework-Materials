package framework.sqlnode;

import framework.sqlnode.iface.SqlNode;
import framework.utils.GenericTokenParser;
import framework.utils.OgnlUtils;
import framework.utils.SimpleTypeRegistry;
import framework.utils.TokenHandler;

public class TextSqlNode implements SqlNode {
    private String sqlText;

    public TextSqlNode(String sqlText) {
        this.sqlText = sqlText;
    }

    @Override
    public void apply(DynamicContext context) {
        // The TestSqlNode contains the ${} tags. Resolve them here.
        GenericTokenParser tokenParser = new GenericTokenParser("${", "}", new BindingTokenHandler(context));

        String sql = tokenParser.parse(sqlText);
        context.appendSql(sql);
    }

    public boolean isDynamic(){
        return (sqlText.contains("${"));
    }

    private class BindingTokenHandler implements TokenHandler {
        private DynamicContext context;

        public BindingTokenHandler(DynamicContext context) {
            this.context = context;
        }

        /**
         * get parameter values and then replace with the ${} tag
         * @param content
         * @return
         */
        @Override
        public String handleToken(String content) {
            Object parameter = context.getBindings().get("_parameter");
            if (parameter == null){
                return "";
            } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())){
                return parameter.toString();
            }
            Object value = OgnlUtils.getValue(content, parameter);
            return value == null ? "" : value.toString();
        }
    }
}

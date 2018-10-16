package com.sndj.financial;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.InsertDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.List;

/**
 * @author yuechao 2018/7/30
 */
public class ConcatenateStrategy implements ShardTableStrategy {


    private String SHARDING_MD = "_SHARDING_";

    @Override
    public String convert(String originalSQL, Object parameterObject, ShardingTable shardingTable) {


        String updateTableName = getTableName(shardingTable, parameterObject);
        String expectTableName = shardingTable.tableName();

        try {

            Statement statement = CCJSqlParserUtil.parse(originalSQL);
            if (Select.class.isInstance(statement)) {
                Select select = (Select) statement;

                //Start of value modification
                StringBuilder buffer = new StringBuilder();
                ExpressionDeParser expressionDeParser = new ExpressionDeParser();
                SelectDeParser deparser = new SelectDeParser(expressionDeParser,buffer ) {
                    @Override
                    public void visit(Table tableName) {
                        compareAndSet(tableName, expectTableName, updateTableName);
                        super.visit(tableName);
                    };
                };
                expressionDeParser.setSelectVisitor(deparser);
                expressionDeParser.setBuffer(buffer);
                select.getSelectBody().accept(deparser);

                return buffer.toString();
            }
            if (Update.class.isInstance(statement)) {
                UpdateDeParser updateDeParser = new UpdateDeParser();
                Update update = (Update) statement;
                compareAndSet(update.getTables(), expectTableName, updateTableName);
                updateDeParser.deParse(update);
                return updateDeParser.getBuffer().toString();
            }

            if (Insert.class.isInstance(statement)) {
                InsertDeParser insertDeParser = new InsertDeParser();
                Insert insert = (Insert) statement;
                compareAndSet(insert.getTable(), expectTableName, updateTableName);
                insertDeParser.deParse(insert);
                return insertDeParser.getBuffer().toString();
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void compareAndSet(List<Table> tables, String expectTableName, String updateTableName) {
        for (Table table : tables) {
            compareAndSet(table, expectTableName, updateTableName);
        }
    }

    public void compareAndSet(Table table, String expectTableName, String updateTableName) {
        if (table.getName().equalsIgnoreCase(expectTableName)) {
            table.setName(updateTableName);
        }
    }

    private String getTableName(ShardingTable shardingTable, Object parameterObject) {
        MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
        StringBuilder sb = new StringBuilder(shardingTable.tableName());
        int length = shardingTable.fieldNames().length;
        for (int i = 0; i < length; i++) {
            String fieldName = shardingTable.fieldNames()[i];
            String fieldValue = String.valueOf(metaObject.getValue(fieldName));

            sb.append("_").append(fieldValue);
        }
        return sb.toString();
    }

    @Override
    public String getNewMsId(String sourceMsID, ShardingTable shardingTable, Object parameterObject) {
        return sourceMsID + SHARDING_MD + getTableName(shardingTable, parameterObject);
    }
}

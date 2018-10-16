package com.sndj.financial;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.deparser.SelectDeParser;


/**
 * @author yuechao 2018/7/30
 */
public class FinancialShardingTableSelectDeParser extends SelectDeParser {

    private String destTableName;

    private String sourceTableName;

    public FinancialShardingTableSelectDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer, String sourceTableName, String destTableName) {
        super(expressionVisitor, buffer);
        this.sourceTableName = sourceTableName;
        this.destTableName = destTableName;
    }

    @Override
    public void visit(Table tableName) {
        if (tableName.getName().equals(sourceTableName)) {
            tableName.setName(this.destTableName);
        }
        super.visit(tableName);

    }
}

package com.sndj.financial;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;

import java.util.List;


/**
 * @author yuechao 2018/7/30
 */
public class FinancialShardingTableUpdateDeParser extends UpdateDeParser {

    private String destTableName;

    private String sourceTableName;

    public FinancialShardingTableUpdateDeParser(String sourceTableName, String destTableName) {
        super();
        this.sourceTableName = destTableName;
    }

    @Override
    public void deParse(Update update) {
        List<Table> tables = update.getTables();
        for (Table table : tables) {
            if (table.getName().equals(sourceTableName)) {
                table.setName(destTableName);
            }
        }
        super.deParse(update);
    }
}

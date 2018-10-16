package com.sndj;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.InsertDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import org.junit.Test;

import java.util.List;

/**
 * @author yuechao 2018/7/30
 */
public class AppTest {

    @Test
    public void test() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements("SELECT * FROM MY_TABLE1; update t2 set name = 'aa';");

//        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        for (Statement statement : statements.getStatements()) {
            List<String> tableList = tablesNamesFinder.getTableList(statement);
            tableList.forEach(x -> {
                System.out.println(x);
            });
        }

    }

    @Test
    public void test1() throws JSQLParserException {
        String sql ="select * from presto.books as t1 left join default.hbooks as t2 on t1.id=t2.id";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        //Start of value modification
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        SelectDeParser deparser = new MySelectDeParser(expressionDeParser,buffer );
        expressionDeParser.setSelectVisitor(deparser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);
        //End of value modification


        System.out.println(buffer.toString());
    }


    @Test
    public void test2() throws JSQLParserException {
        String sql ="SELECT NAME, ADDRESS, COL1 FROM USER WHERE SSN IN ('11111111111111', '22222222222222');";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

//Start of value modification
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {

            @Override
            public void visit(StringValue stringValue) {
                this.getBuffer().append("XXXX");
            }

        };
        SelectDeParser deparser = new SelectDeParser(expressionDeParser,buffer );
        expressionDeParser.setSelectVisitor(deparser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);
//End of value modification


        System.out.println(buffer.toString());
    }


    @Test
    public void select() throws JSQLParserException {
        String sql ="SELECT NAME, ADDRESS, COL1 FROM USER WHERE SSN IN ('11111111111111', '22222222222222');";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

//Start of value modification
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        SelectDeParser deparser = new SelectDeParser(expressionDeParser,buffer ) {
            @Override
            public void visit(Table tableName) {
                tableName.setName(tableName.getName() + "AAA");
                super.visit(tableName);
            };
        };
        expressionDeParser.setSelectVisitor(deparser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);
//End of value modification
        System.out.println(buffer.toString());
    }

    @Test
    public  void insert() throws JSQLParserException {
        String sql ="insert into t1(name, addr) values ('11111111111111', '22222222222222');";
        Insert insert = (Insert) CCJSqlParserUtil.parse(sql);

//Start of value modification
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        SelectDeParser sdeparser = new SelectDeParser(expressionDeParser,buffer ) {
            @Override
            public void visit(Table tableName) {
                tableName.setName(tableName.getName() + "AAA");
                super.visit(tableName);
            };
        };

        InsertDeParser deparser = new InsertDeParser(expressionDeParser, sdeparser, buffer) {

        };
        expressionDeParser.setSelectVisitor(sdeparser);
        expressionDeParser.setBuffer(buffer);
        insert.accept(new StatementVisitorAdapter() {

        });
//End of value modification
        System.out.println(buffer.toString());
    }
}

class MySelectDeParser extends SelectDeParser {


    public MySelectDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(expressionVisitor, buffer);
    }

    @Override
    public void visit(Table tableName) {
        tableName.setName("xx");
        StringBuilder buffer = getBuffer();
        buffer.append(tableName.getFullyQualifiedName());
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(alias);
        }

        Pivot pivot = tableName.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }

        MySQLIndexHint indexHint = tableName.getIndexHint();
        if (indexHint != null) {
            buffer.append(indexHint);
        }
    }
}
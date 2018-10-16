package com.sndj.financial;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;

import java.util.Properties;


@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
)})
public class ActionInterceptor implements Interceptor {

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
        Object param = invocation.getArgs()[1];
        if(ms == null) {
            return invocation.proceed();
        } else {
            long now;

            if(ms.getSqlCommandType().equals(SqlCommandType.INSERT) && param != null) {
//                now = this.getFormattedTimeStamp(DateTime.now());
//                e = (BaseItem)param;
//                if(e.getCreateTime() == null) {
//                    e.setCreateTime(Long.valueOf(now));
//                }
//
//                if(e.getActionTime() == null) {
//                    e.setActionTime(Long.valueOf(now));
//                }
//
//                if(e.getAction() == null) {
//                    e.setAction(BaseItem.Action.INSERT.getValue());
//                }
            }

            if(ms.getSqlCommandType().equals(SqlCommandType.UPDATE) && param != null) {
//                if (param instanceof BaseItem) {
//                    now = this.getFormattedTimeStamp(DateTime.now());
//                    e = (BaseItem) param;
//                    if (e.getActionTime() == null) {
//                        e.setActionTime(Long.valueOf(now));
//                    }
//
//                    if (e.getAction() == null) {
//                        e.setAction(BaseItem.Action.UPDATE.getValue());
//                    }
//                } else if (param instanceof Map) {
//                    now = this.getFormattedTimeStamp(DateTime.now());
//                    Map m = (Map)param;
//                    if (!m.containsKey("action")) {
//                        m.put("action", BaseItem.Action.UPDATE.getValue());
//                    }
//                    if (!m.containsKey("actionTime")) {
//                        m.put("actionTime", now);
//                    }
//                }
            }

            return invocation.proceed();
        }
    }

    public Object plugin(Object target) {
        return target instanceof Executor ? Plugin.wrap(target, this):target;
    }

    public void setProperties(Properties properties) {
    }


}

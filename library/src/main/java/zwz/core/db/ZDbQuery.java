package zwz.core.db;

public class ZDbQuery{
    private ZDbWhere zDbWhere;
    private String orderNames;
    private Integer pageIndex;
    private Integer pageCount;

    public ZDbQuery where(ZDbWhere where){
        zDbWhere=where;
        return this;
    }

    public ZDbQuery orderBy(String names){
        orderNames=names;
        return this;
    }

    /**从1开始*/
    public ZDbQuery index(int index){
        index--;
        if(index>=0){
            pageIndex=index;
        }
        return this;
    }

    public ZDbQuery count(int count){
        if(count>0){
            pageCount=count;
        }
        return this;
    }

    public Integer getPageIndex() {
        if(pageIndex==null){
            return 1;
        }
        return pageIndex+1;
    }

    public Integer getPageCount(){
        return pageCount;
    }

    public String toSqlString(){
        StringBuffer sql=new StringBuffer();
        if(zDbWhere!=null&&zDbWhere.hasSql()){
            sql.append(" WHERE "+zDbWhere.toSqlString());
        }
        if(orderNames!=null&&!"".equals(orderNames)){
            sql.append(" ORDER BY "+orderNames);
        }
        if(pageCount!=null){
            sql.append(" LIMIT "+pageCount);
            if(pageIndex!=null){
                sql.append(" OFFSET "+(pageCount*pageIndex));
            }
        }
        return sql.toString();
    }
}

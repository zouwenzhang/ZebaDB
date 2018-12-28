package zwz.core.db;

public class ZDbWhere {
    private String tempName;
    private StringBuffer whereSql=new StringBuffer();
    private int spaceType=0;

    public ZDbWhere(String name){
        if(notEmpty(name)){
            tempName=name;
            spaceType=1;
        }
    }

    public ZDbWhere and(String name){
        if(notEmpty(name)){
            tempName=name;
            spaceType=2;
        }
        return this;
    }

    public ZDbWhere or(String name){
        if(notEmpty(name)){
            tempName=name;
            spaceType=3;
        }
        return this;
    }

    private boolean notEmpty(String v){
        if(v!=null&&!"".equals(v)){
            return true;
        }
        return false;
    }

    public boolean hasSql(){
        if(whereSql.length()==0){
            return false;
        }
        return true;
    }

    private void append(Object value,String type){
        if(value==null){
            return;
        }
        String vs=value.toString();
        if(value instanceof String){
            if(!notEmpty(vs)){
                return;
            }
            vs="'"+vs+"'";
        }
        if(notEmpty(tempName)){
            if(spaceType==1||whereSql.length()==0){
                whereSql.append(tempName+" "+type+" "+vs);
            }else if(spaceType==2){
                whereSql.append(" AND "+tempName+" "+type+" "+vs);
            }else if(spaceType==3){
                whereSql.append(" OR "+tempName+" "+type+" "+vs);
            }
        }
    }

    /**等于=*/
    public ZDbWhere equ(Object value){
        append(value,"=");
        return this;
    }

    /**大于>*/
    public ZDbWhere gt(Object value){
        append(value.toString(),">");
        return this;
    }

    /**小于<*/
    public ZDbWhere lt(Object value){
        append(value.toString(),"<");
        return this;
    }

    /**大于等于>=*/
    public ZDbWhere gte(Object value){
        append(value.toString(),">=");
        return this;
    }

    /**小于等于<=*/
    public ZDbWhere lte(Object value){
        append(value.toString(),"<=");
        return this;
    }

    /**不等于!=*/
    public ZDbWhere nte(Object value){
        append(value.toString(),"!=");
        return this;
    }

    public String toSqlString(){
        return whereSql.toString();
    }

}

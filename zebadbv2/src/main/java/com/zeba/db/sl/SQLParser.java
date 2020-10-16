package com.zeba.db.sl;

import android.content.ContentValues;

import java.util.Map.Entry;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.zwz.zeba.orm.base.ColumnEntity;
import com.zwz.zeba.orm.base.curd.Delete;
import com.zwz.zeba.orm.base.curd.Insert;
import com.zwz.zeba.orm.base.curd.Query;
import com.zwz.zeba.orm.base.curd.QueryJoin;
import com.zwz.zeba.orm.base.curd.QueryOn;
import com.zwz.zeba.orm.base.curd.Update;
import com.zwz.zeba.orm.base.curd.Where;

public class SQLParser {
	
	public static Object[] update(Update update) throws SQLException{
		Object[] vs=new Object[4];
		vs[0]=update.getTableName();
		ContentValues cv=new ContentValues();
		Set<Entry<String,Object>> set= update.getValues().entrySet();
		for(Entry<String,Object> en:set){
			Object va=en.getValue();
			putContentValue(cv,en.getKey(),va);
		}
		vs[1]=cv;
		Object[] ws=where(update.getWhere());
		vs[2]=ws[0];
		vs[3]=ws[1];
		return vs;
	}

	private static void putContentValue(ContentValues cv,String key,Object value){
		if(value instanceof Integer){
			cv.put(key,(Integer)value);
		}else if(value instanceof Long){
			cv.put(key,(Long)value);
		}else if(value instanceof Float){
			cv.put(key,(Float)value);
		}else if(value instanceof Double){
			cv.put(key,(Double)value);
		}else if(value instanceof String){
			cv.put(key,(String)value);
		}else if(value instanceof BigDecimal){
			cv.put(key,((BigDecimal)value).toPlainString());
		}else if(value instanceof byte[]){
			cv.put(key,(byte[])value);
		}
	}
	
	public static Object[] delete(Delete delete) throws SQLException{
		Object[] vs=new Object[3];
		vs[0]=delete.getTableName();
		Object[] ws=where(delete.getWhere());
		vs[1]=ws[0];
		vs[2]=ws[1];
		return vs;
	}
	
	public static Object[] insert(Insert insert) throws SQLException{
		ContentValues cv=new ContentValues();
		List<ColumnEntity> enList= insert.getColumnList();
		for(ColumnEntity en:enList){
			try {
				Object v=en.getField().get(insert.getData());
				if(v==null){
					continue;
				}
				putContentValue(cv,en.getName(),v);
			} catch (Exception e) {
				e.printStackTrace();
				throw new SQLException("add value fail",e.getCause());
			}
		}
		return new Object[]{cv};
	}
	
	public static String query(Query query) throws SQLException{
		StringBuilder sql=new StringBuilder();
		sql.append("select ");
		if(query.getJoins().isEmpty()){
			List<String> clist= query.getColumns();
			for(String c:clist){
				sql.append(c+",");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(" from ");
			sql.append(query.getTableName());
		}else{
			List<QueryJoin> joinList=query.getJoins();
			for(QueryJoin j:joinList){
				for(String c:j.getColumns()){
					sql.append(c+",");
				}
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(" from ");
			StringBuilder sbj=new StringBuilder();
			int index=0;
			for(QueryJoin j:joinList){
				if(index==0){
					sbj.append(j.getTable());
					sbj.append(" as ");
					sbj.append(j.getAs());
				}else{
					sbj.insert(0, "(");
					switch(j.getType()){
					case QueryJoin.INNER:
						sbj.append(" INNER JOIN ");
						break;
					case QueryJoin.LEFT:
						sbj.append(" LEFT JOIN ");
						break;
					case QueryJoin.RIGHT:
						sbj.append(" RIGHT JOIN ");
						break;
					case QueryJoin.FULL:
						sbj.append(" union ");
						break;
					}
					sbj.append(j.getTable());
					sbj.append(" as ");
					sbj.append(j.getAs());
					QueryOn jo= query.getJoinOns().get(index-1);
					sbj.append(" on ")
						.append(jo.getLeftColumn())
						.append("=")
						.append(jo.getRightColumn())
						.append(")");
				}
				index++;
			}
			sbj.deleteCharAt(sbj.length()-1);
			sbj.deleteCharAt(0);
			sql.append(sbj);
		}
		if(query.getWhere()!=null){
			sql.append(whereString(query.getWhere()));
		}
		if(query.getPageSize()!=null){
			sql.append(" limit "+query.getPageSize());
			if(query.getPageIndex()!=null){
				sql.append(" offset "+(query.getPageNo()-1)*query.getPageSize());
			}
		}
		if(!query.getOrderBy().isEmpty()){
			sql.append(" order by ");
			for(String ob:query.getOrderBy()){
				sql.append(ob);
				if("asc".equals(ob)||"desc".equals(ob)){
					sql.append(",");
				}
			}
			if(sql.charAt(sql.length()-1)==','){
				sql.deleteCharAt(sql.length()-1);
			}
		}
		return sql.toString();
	}

	public static Object[] where(Where where) throws SQLException{
		Object[] ws=new Object[2];
		StringBuilder sql=new StringBuilder();
		Object[] wo=where.getWhere();
		List<String> values=new LinkedList<>();
		boolean nextIsValue=false;
		for(Object o:wo){
			if(nextIsValue){
				values.add(o.toString());
				nextIsValue=false;
				continue;
			}
			if(o instanceof String){
				if("and".equals(o)){
					sql.append(" and ");
					continue;
				}
				if("or".equals(o)){
					sql.append(" or ");
					continue;
				}
				String os=o.toString();
				sql.append(os);
				char c=os.charAt(os.length()-1);
				if(c=='='||c=='>'||c=='<'){
					sql.append(" ? ");
					nextIsValue=true;
					continue;
				}
				if(os.contains("?")){
					nextIsValue=true;
					continue;
				}
			}
		}
		ws[0]=sql.toString();
		String[] ss=new String[values.size()];
		for(int i=0;i<values.size();i++){
			ss[i]=values.get(i);
		}
		ws[1]=ss;
		return ws;
	}

	public static String whereString(Where where) throws SQLException{
		StringBuilder sql=new StringBuilder();
		sql.append(" where ");
		Object[] wo=where.getWhere();
		boolean nextIsValue=false;
		for(Object o:wo){
			if(nextIsValue){
				if(o instanceof String){
					sql.append("'").append(o.toString()).append("' ");
				}else{
					sql.append(o.toString());
				}
				nextIsValue=false;
				continue;
			}
			if(o instanceof String){
				if("and".equals(o)){
					sql.append(" and ");
					continue;
				}
				if("or".equals(o)){
					sql.append(" or ");
					continue;
				}
				String os=o.toString();
				sql.append(os);
				char c=os.charAt(os.length()-1);
				if(c=='='||c=='>'||c=='<'){
					nextIsValue=true;
					continue;
				}
				if(os.contains("?")){
					nextIsValue=true;
					continue;
				}
			}
		}
		return sql.toString();
	}
	
	public static List<Object> where(StringBuilder sql,Where where) throws SQLException{
		sql.append(" where ");
		Object[] wo=where.getWhere();
		List<Object> values=new LinkedList<>();
		boolean nextIsValue=false;
		for(Object o:wo){
			if(nextIsValue){
				values.add(o);
				nextIsValue=false;
				continue;
			}
			if(o instanceof String){
				if("and".equals(o)){
					sql.append(" and ");
					continue;
				}
				if("or".equals(o)){
					sql.append(" or ");
					continue;
				}
				String os=o.toString();
				sql.append(os);
				char c=os.charAt(os.length()-1);
				if(c=='='||c=='>'||c=='<'){
					sql.append(" ? ");
					nextIsValue=true;
					continue;
				}
				if(os.contains("?")){
					nextIsValue=true;
					continue;
				}
			}
		}
		return values;
	}
	
	public static void setValues(PreparedStatement ps,Object values) throws SQLException{
		List<Object> vs=(List<Object>)values;
		int i=1;
		for(Object v:vs){
			if(v.getClass()==Integer.class||v.getClass()==int.class){
				ps.setInt(i,(int)v);
			}else if(v.getClass()==Long.class||v.getClass()==long.class){
				ps.setLong(i, (long)v);
			}else if(v.getClass()==Float.class||v.getClass()==float.class){
				ps.setFloat(i, (float)v);
			}else if(v.getClass()==String.class){
				ps.setString(i, (String)v);
			}else if(v.getClass()==BigDecimal.class){
				ps.setBigDecimal(i, (BigDecimal)v);
			}else if(v.getClass()==Double.class||v.getClass()==double.class){
				ps.setDouble(i, (double)v);
			}else if(v.getClass()==Date.class){
				ps.setDate(i, (Date)v);
			}
			i++;
		}
	}
}

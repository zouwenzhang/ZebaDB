package zwz.core.test;

import zwz.core.db.BaseRecord;
import zwz.core.db.annotation.DbColumn;
import zwz.core.db.annotation.DbColumnType;
import zwz.core.db.annotation.DbTable;

@DbTable("book")
public class BookRecord extends BaseRecord{
    @DbColumn(name = "bookName",type = DbColumnType.TEXT)
    private String name;
    @DbColumn(name = "type",type = DbColumnType.INTEGER)
    private Integer bookType;
    @DbColumn(name = "time",type = DbColumnType.TEXT)
    private String bookTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBookType() {
        return bookType;
    }

    public void setBookType(Integer bookType) {
        this.bookType = bookType;
    }

    public String getBookTime() {
        return bookTime;
    }

    public void setBookTime(String bookTime) {
        this.bookTime = bookTime;
    }
}

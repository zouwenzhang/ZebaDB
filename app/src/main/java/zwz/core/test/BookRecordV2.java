package zwz.core.test;

import com.zwz.zeba.orm.base.annotation.Column;
import com.zwz.zeba.orm.base.annotation.ColumnType;
import com.zwz.zeba.orm.base.annotation.Entity;

@Entity("book")
public class BookRecordV2{
    @Column(name = "bookName",type = ColumnType.TEXT)
    private String name;
    @Column(name = "type",type = ColumnType.INT)
    private Integer bookType;
    @Column(name = "time",type = ColumnType.TEXT)
    private String bookTime;

    @Column(name = "bookData",type = ColumnType.TEXT)
    private String bookData;

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

    public String getBookData() {
        return bookData;
    }

    public void setBookData(String bookData) {
        this.bookData = bookData;
    }

}
